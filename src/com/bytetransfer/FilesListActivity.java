package com.bytetransfer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.StringTokenizer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class FilesListActivity extends ListActivity implements
		OnItemLongClickListener {

	private File file, byteTransferFile;;
	private List<String> myList;
	static String requestFileString;
	static String root_sd;
	static long fileClickedSize;
	static String sharedIp;
	static String fileClickedName, fileClickedPath;
	static File fileClickedFile;
	static Socket connectionServ;
	static ServerSocket serverSocket;
	static ObjectInputStream requestFileInServer, requestFileInClient;
	static ObjectOutputStream requestFileOutServer, requestFileOutClient;
	static Context fileList_context;
	static String generalAlertDialog;
	static String toSendOrNot;
	static String clientDeviceName;
	static String okString;
	static Boolean okToSend = null;
	static Handler handler;
	static Dialog db_dialog;
	static String error;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ListView lv = (ListView) this.getListView();
		lv.setBackgroundResource(R.drawable.cyan_background);
		fileList_context = FilesListActivity.this;
		okString = getString(android.R.string.ok);
		handler = new Handler();

		String receivedSharedData = getIntent().getExtras().getString("share");
		StringTokenizer stok = new StringTokenizer(receivedSharedData, "__");
		clientDeviceName = stok.nextToken();
		sharedIp = stok.nextToken();

		CommunicationScreenActivity.fa.finish();

		root_sd = Environment.getExternalStorageDirectory().toString();
		byteTransferFile = new File(root_sd + "/+ByteTransfer+");

		if (!byteTransferFile.exists()) {
			if (byteTransferFile.mkdir()) {
				// directory is created;
			}
		}

		myList = new ArrayList<String>();

		file = new File(root_sd);
		File list[] = file.listFiles();

		for (int i = 0; i < list.length; i++) {
			myList.add(list[i].getName());
		}

		// Array Adapter AND On Long Click Listener

		ArrayAdapter aa = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, myList);

		lv.setLongClickable(true);
		lv.setOnItemLongClickListener(this);
		setListAdapter(aa);

		ReceiveFileSendRequestThread ReceiveFileSendRequestThread = new ReceiveFileSendRequestThread();
		ReceiveFileSendRequestThread.start();

	}

	public boolean onItemLongClick(AdapterView<?> parentView, View childView,
			int position, long id) {
		// TODO Auto-generated method stub

		fileClickedName = (((TextView) childView).getText()).toString();
		fileClickedPath = file.toString() + "/" + fileClickedName;
		fileClickedFile = new File(fileClickedPath);
		fileClickedSize = fileClickedFile.length();

		SendFileThread SendFileThread = new SendFileThread();
		SendFileThread.start();

		return true;
	}

	static public class SendFileThread extends Thread {
		BufferedInputStream bis = null;
		FileInputStream fis = null;

		public void run() {
			Socket socket = new Socket();
			try {
				socket.connect(new InetSocketAddress(sharedIp, 6789), 200);
				requestFileOutClient = new ObjectOutputStream(
						socket.getOutputStream());
				requestFileOutClient.writeObject(fileClickedName);
				requestFileOutClient.flush();

				requestFileInClient = new ObjectInputStream(
						socket.getInputStream());
				toSendOrNot = (String) requestFileInClient.readObject();

				if (toSendOrNot.equals("oktosend")) {

					// /// send File
					byte[] mybytearray = new byte[(int) fileClickedFile
							.length()];
					fis = new FileInputStream(fileClickedFile);
					bis = new BufferedInputStream(fis);
					bis.read(mybytearray, 0, mybytearray.length);
					OutputStream os = socket.getOutputStream();
					int start = (int) System.currentTimeMillis();
					os.write(mybytearray, 0, mybytearray.length);
					os.flush();
					// bis.close(); // //
					socket.close();
					int end = (int) System.currentTimeMillis();
					generalAlertDialog = "File received in: "
							+ String.valueOf((end - start) / 1000) + " seconds";
					handler.post(new GeneralAlertDialogRunnable());

					// save sent file log to database
					boolean didItWork = true;
					try {

						String file_name, destination, file_type, date_sent, completed, size;
						file_name = fileClickedName;
						destination = clientDeviceName;
						file_type = getExtension(fileClickedFile);
						date_sent = getDate();
						completed = "Yes";

						size = String
								.valueOf(fileClickedFile.length() / 1024 / 1024)
								+ " MB";
						// handling in kbs file size case
						if (size.equals("0 MB")) {
							size = String
									.valueOf(fileClickedFile.length() / 1024)
									+ " KB";
						}

						HistoryLog entry = new HistoryLog(fileList_context);
						entry.open();
						entry.sfCreateEntry(file_name, destination, file_type,
								date_sent, completed, size);
						entry.close();
					} catch (Exception e) {
						didItWork = false;
						error = e.toString();
						handler.post(new DatabaseDialogNoRunnable());
					} finally {
						if (didItWork) {
							handler.post(new DatabaseDialogYesRunnable());
						}
					}

					ReceiveFileSendRequestThread ReceiveFileSendRequestThread = new ReceiveFileSendRequestThread();
					ReceiveFileSendRequestThread.start();

				} else if (toSendOrNot.equals("notosend")) {
					// don't send anything
					socket.close();
					ReceiveFileSendRequestThread ReceiveFileSendRequestThread = new ReceiveFileSendRequestThread();
					ReceiveFileSendRequestThread.start();
					handler.post(new ClientFileSendAlertDialogRunnable());
				}

				// //

			} catch (IOException e) {
				Log.d("Connection Error:",
						"Other device terminated the serverSocket");
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	static public class ReceiveFileSendRequestThread extends Thread {
		public void run() {
			FileOutputStream fos = null;
			BufferedOutputStream bos = null;
			try {
				serverSocket = new ServerSocket(6789, 200);
				connectionServ = serverSocket.accept();
				requestFileInServer = new ObjectInputStream(
						connectionServ.getInputStream());
				requestFileString = (String) requestFileInServer.readObject();

				handler.post(new AcceptFileSendAlertDialogRunnable());

				okToSend = null;
				while (okToSend == null) {

				}
				
				if (okToSend == true) {
					requestFileOutServer = new ObjectOutputStream(
							connectionServ.getOutputStream());
					requestFileOutServer.writeObject("oktosend");
					requestFileOutServer.flush();

					// Receive File
					int bytesRead;
					int current = 0;

					int start = (int) System.currentTimeMillis();
					byte[] aByte = new byte[1024];
					InputStream is = connectionServ.getInputStream();
					File f = new File(root_sd + "/+ByteTransfer+" + "/"
							+ requestFileString);
					fos = new FileOutputStream(f);
					bos = new BufferedOutputStream(fos);

					while ((bytesRead = is.read(aByte)) != -1) {
						bos.write(aByte, 0, bytesRead);
					}

					bos.write(aByte, 0, current);
					bos.flush();
					bos.close();
					serverSocket.close();
					int end = (int) System.currentTimeMillis();

					// save received file log to database
					boolean didItWork = true;
					try {

						String file_name, source, file_type, date_received, completed, size;
						file_name = requestFileString;
						source = clientDeviceName;
						file_type = getExtension(f);
						date_received = getDate();
						completed = "Yes";

						size = String.valueOf(f.length() / 1024 / 1024) + " MB";
						// handling in kbs file size case
						if (size.equals("0 MB")) {
							size = String.valueOf(f.length() / 1024) + " KB";
						}

						HistoryLog entry = new HistoryLog(fileList_context);
						entry.open();
						entry.rfCreateEntry(file_name, source, file_type,
								date_received, completed, size);
						entry.close();
					} catch (Exception e) {
						didItWork = false;
						error = e.toString();
						handler.post(new DatabaseDialogNoRunnable());
					} finally {
						if (didItWork) {
							handler.post(new DatabaseDialogYesRunnable());
						}
					}

					generalAlertDialog = "File received in: "
							+ String.valueOf((end - start) / 1000) + " seconds";
					handler.post(new GeneralAlertDialogRunnable());

					ReceiveFileSendRequestThread ReceiveFileSendRequestThread = new ReceiveFileSendRequestThread();
					ReceiveFileSendRequestThread.start();

				} else if (okToSend == false) {
					requestFileOutServer = new ObjectOutputStream(
							connectionServ.getOutputStream());
					requestFileOutServer.reset();
					requestFileOutServer.writeObject("notosend");
					requestFileOutServer.flush();

					serverSocket.close();
					ReceiveFileSendRequestThread ReceiveFileSendRequestThread = new ReceiveFileSendRequestThread();
					ReceiveFileSendRequestThread.start();
				}

			} catch (IOException e) {
				Log.d("Connection Error:", "Error binding port!");
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	static public class DatabaseDialogYesRunnable implements Runnable {
		public void run() {
			db_dialog = new Dialog(fileList_context);
			db_dialog.setTitle("Heck Yea! Log saved successfully to db!");
			TextView tv = new TextView(fileList_context);
			tv.setText("Success");
			db_dialog.setContentView(tv);
			db_dialog.show();
		}
	}

	static public class DatabaseDialogNoRunnable implements Runnable {
		public void run() {
			db_dialog = new Dialog(fileList_context);
			db_dialog.setTitle("Dang it!");
			TextView tv = new TextView(fileList_context);
			tv.setText(error);
			db_dialog.setContentView(tv);
		}
	}

	static public class ClientFileSendAlertDialogRunnable implements Runnable {
		public void run() {
			AlertDialog.Builder b = new AlertDialog.Builder(fileList_context);
			b.setMessage(clientDeviceName + " refused to receive: "
					+ fileClickedName);
			b.setPositiveButton(okString,
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dlg, int arg1) {
							dlg.dismiss();
						}
					});
			b.create().show();
		}
	}

	static public class AcceptFileSendAlertDialogRunnable implements Runnable {
		public void run() {
			AlertDialog.Builder b = new AlertDialog.Builder(fileList_context);
			b.setMessage(clientDeviceName + " wants to send: "
					+ requestFileString);
			b.setPositiveButton(okString,
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dlg, int arg1) {
							dlg.dismiss();
							okToSend = true;
						}
					});
			b.setNegativeButton(android.R.string.no,
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dlg, int which) {
							dlg.dismiss();
							okToSend = false;
						}
					});
			b.create().show();
		}
	}

	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		File temp_file = new File(file, myList.get(position));

		if (!temp_file.isFile()) {
			file = new File(file, myList.get(position));
			File list[] = file.listFiles();

			myList.clear();

			for (int i = 0; i < list.length; i++) {
				myList.add(list[i].getName());
			}

			Toast.makeText(getApplicationContext(), file.toString(),
					Toast.LENGTH_LONG).show();
			setListAdapter(new ArrayAdapter<String>(this,
					android.R.layout.simple_list_item_1, myList));

		}

	}

	@Override
	public void onBackPressed() {
		String parent = file.getParent().toString();
		if (parent.equals("/storage") || parent.equals("/mnt")) {

			try {
				serverSocket.close();
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				Log.d("Exception:", "SocketException");
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.d("Exception:", "IOException");
				e.printStackTrace();
			}

			finish();
			Intent intent = new Intent(fileList_context,
					MainByteTransferActivity.class);
			fileList_context.startActivity(intent);
		} else {
			file = new File(parent);
			File list[] = file.listFiles();

			myList.clear();

			for (int i = 0; i < list.length; i++) {
				myList.add(list[i].getName());
			}
			Toast.makeText(getApplicationContext(), parent, Toast.LENGTH_LONG)
					.show();
			setListAdapter(new ArrayAdapter<String>(this,
					android.R.layout.simple_list_item_1, myList));
		}

	}

	static public class GeneralAlertDialogRunnable implements Runnable {
		public void run() {
			AlertDialog.Builder b = new AlertDialog.Builder(fileList_context);
			b.setMessage(generalAlertDialog);
			b.setPositiveButton(okString,
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dlg, int arg1) {
							dlg.dismiss();
							generalAlertDialog = null;
						}
					});
			b.create().show();
		}
	}

	// Get the extension of a file.
	public static String getExtension(File f) {
		String ext = null;
		String s = f.getName();
		int i = s.lastIndexOf('.');

		if (i > 0 && i < s.length() - 1) {
			ext = s.substring(i + 1).toLowerCase();
		}
		return ext;
	}

	// Get the current date
	public static String getDate() {
		Calendar javaCalendar = null;
		String currentDate = "";

		javaCalendar = Calendar.getInstance();

		currentDate = javaCalendar.get(Calendar.DATE) + "/"
				+ (javaCalendar.get(Calendar.MONTH) + 1) + "/"
				+ javaCalendar.get(Calendar.YEAR);

		return currentDate;
	}

}
