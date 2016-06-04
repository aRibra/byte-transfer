package com.bytetransfer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.StringTokenizer;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class CommunicationScreenActivity extends Activity implements
		OnClickListener {

	TextView commScreenTv;
	Button fileTransferButton, instantMsgButton;
	static Context commScreen_context;

	static String requestString;
	static String receivedSharedData;
	static String choice;
	static StringTokenizer receivedSharedDataStok;
	static String deviceName, ip, context, serverDeviceName, serverIp,
			serverContext;
	static Handler handler;
	static Socket connectionServ;
	static ObjectOutputStream requestOut;
	static ObjectInputStream requestIn;
	static String okString;
	static boolean okToContinue = false;
	static ServerSocket serverSocket;
	static AlertDialog.Builder b;
	public static Activity fa;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_communication_screen);

		reset();
		fa = this;
		commScreen_context = CommunicationScreenActivity.this;
		okString = getString(android.R.string.ok);
		commScreenTv = (TextView) findViewById(R.id.comm_screen_tv);
		fileTransferButton = (Button) findViewById(R.id.file_transfer_button);
		instantMsgButton = (Button) findViewById(R.id.instant_msg_button);

		fileTransferButton.setOnClickListener(this);
		instantMsgButton.setOnClickListener(this);
		handler = new Handler();

		receivedSharedData = getIntent().getExtras().getString("share");
		commScreenTv.setText(receivedSharedData);
		MainByteTransferActivity.fa.finish();

		receivedSharedDataStok = new StringTokenizer(receivedSharedData, "__");
		deviceName = receivedSharedDataStok.nextToken();
		ip = receivedSharedDataStok.nextToken();
		context = receivedSharedDataStok.nextToken();

		serverDeviceName = deviceName;
		serverIp = ip;
		serverContext = context;

		AcceptTransferConnectionThread AcceptTransferConnectionThread = new AcceptTransferConnectionThread();
		AcceptTransferConnectionThread.start();

		if (context.equals("Server")) {

		} else if (context.equals("Client")) {

		}
	}

	private void reset() {
		// TODO Auto-generated method stub
		commScreenTv = null;
		fileTransferButton = null;
		instantMsgButton = null;
		commScreen_context = null;
		requestString = null;
		receivedSharedData = null;
		choice = null;
		receivedSharedDataStok = null;
		deviceName = null;
		ip = null;
		context = null;
		serverDeviceName = null;
		serverIp = null;
		serverContext = null;
		handler = null;
		connectionServ = null;
		requestOut = null;
		requestIn = null;
		okString = null;
		okToContinue = false;
		serverSocket = null;
		b = null;
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
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

		Intent intent = new Intent(commScreen_context,
				MainByteTransferActivity.class);
		commScreen_context.startActivity(intent);

	}

	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();

	}

	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.file_transfer_button:
			choice = " -[File Transfer]- ";
			RequestTrasnferConnectionThread RequestTrasnferConnectionThread1 = new RequestTrasnferConnectionThread();
			RequestTrasnferConnectionThread1.start();
			break;

		case R.id.instant_msg_button:
			choice = " -[Instant Messaging]- ";
			RequestTrasnferConnectionThread RequestTrasnferConnectionThread2 = new RequestTrasnferConnectionThread();
			RequestTrasnferConnectionThread2.start();
			break;
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_communication_screen, menu);
		return true;
	}

	static public class RequestTrasnferConnectionThread extends Thread {
		public void run() {
			Socket socket = new Socket();
			try {
				socket.connect(new InetSocketAddress(ip, 6789), 200);
				requestOut = new ObjectOutputStream(socket.getOutputStream());
				requestOut.writeObject(choice);
				requestOut.flush();
				socket.close();
				serverSocket.close();

				Bundle bundle = new Bundle();
				Intent intent = null;
				if (choice.equals(" -[File Transfer]- ")) {
					intent = new Intent(commScreen_context,
							FilesListActivity.class);
				} else if (choice.equals(" -[Instant Messaging]- ")) {
					intent = new Intent(commScreen_context,
							InstantMessagingActivity.class);
				}

				bundle.putString("share", receivedSharedData);
				intent.putExtras(bundle);
				commScreen_context.startActivity(intent);

			} catch (IOException e) {
				Log.d("Error", "Connection Error!");
				e.printStackTrace();
			}

		}
	}

	static public class OptionSelectedAlertDialogRunnable implements Runnable {
		public void run() {
			b = new AlertDialog.Builder(commScreen_context);
			b.setMessage(serverDeviceName + " has chosen: " + requestString);
			b.setPositiveButton(okString,
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dlg, int arg1) {
							dlg.dismiss();
							okToContinue = true;
						}
					});
			b.create().show();
		}
	}

	static public class AcceptTransferConnectionThread extends Thread {
		public void run() {
			try {
				serverSocket = new ServerSocket(6789, 200);
				connectionServ = serverSocket.accept();
				Log.d("TEXT", "TEXT");
				requestIn = new ObjectInputStream(
						connectionServ.getInputStream());
				requestString = (String) requestIn.readObject();
				serverSocket.close();
				handler.post(new OptionSelectedAlertDialogRunnable());

				while (!okToContinue) {

				}

				if (requestString.equals(" -[File Transfer]- ")) {
					Bundle bundle = new Bundle();
					Intent intent = new Intent(commScreen_context,
							FilesListActivity.class);
					bundle.putString("share", receivedSharedData);
					intent.putExtras(bundle);
					commScreen_context.startActivity(intent);
				} else if (requestString.equals(" -[Instant Messaging]- ")) {
					Bundle bundle = new Bundle();
					Intent intent = new Intent(commScreen_context,
							InstantMessagingActivity.class);
					bundle.putString("share", receivedSharedData);
					intent.putExtras(bundle);
					commScreen_context.startActivity(intent);
				}

			} catch (IOException e) {
				Log.d("Error", "Connection Error!");
				// e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
