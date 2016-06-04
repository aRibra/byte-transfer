package com.bytetransfer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

public class MainByteTransferActivity extends Activity implements
		OnClickListener {

	static Button var_pingButton;
	static TextView connectedDevices_textView, availableIpAddresses_textView;
	static TextView wifi_info_textView;
	static ListView var_listview;
	static ProgressDialog progressDialog;
	
	static Handler handler;
	Thread pingo;
	static ServerSocket receivePingServer;
	static Socket connectionClient;
	static Socket connectionServer;
	private final static int REQUEST_ENABLE_BT = 1;
	BluetoothAdapter mBluetoothAdapter;
	DhcpInfo dhcp_info;
	WifiManager wifi_manager;
	String readableNetmask;
	static String readableIPAddress;
	static Boolean tOrF = null;
	static boolean shouldContinue;
	// static boolean activityChanged;
	static String sharedDeviceName;
	static String sharedDataOnClient, sharedDataOnServer;
	static String receivedOption;
	static String pingedDeviceName;
	static String bluetoothName;
	static String selectedIpAddress;
	static String socketMessage;
	static String syncString, syncMsg, syncIp, syncBluetoothName;
	static String wifiScrollViewText;
	static String to_ping_ip;
	static ArrayList<String> ips_List;
	static List<String> wifiMultipleUpdates;
	static ArrayAdapter<String> arrayAdapter;
	static AlertDialog connectOrNot;
	static ObjectInputStream serverInput;
	static ObjectOutputStream serverOutput;
	static ObjectInputStream clientInput;
	static ObjectOutputStream clientOutput;
	public static Activity fa;

	static RequestConnectionThread RequestConnectionThread;
	static Context main_context;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_byte_transfer);

		reset();
		fa = this;
		main_context = MainByteTransferActivity.this;
		var_pingButton = (Button) findViewById(R.id.v_pingButton);
		connectedDevices_textView = (TextView) findViewById(R.id.connectedDevices_textView);
		wifi_info_textView = (TextView) findViewById(R.id.v_textView2);
		var_listview = (ListView) findViewById(R.id.iplist);
		connectOrNot = new AlertDialog.Builder(MainByteTransferActivity.this)
				.create();

		ips_List = new ArrayList<String>();
		wifiMultipleUpdates = new ArrayList<String>();

		ReceivePingThread ReceivePingThread = new ReceivePingThread();
		ReceivePingThread.start();
		// handler object to update the interface
		handler = new Handler();
		var_pingButton.setOnClickListener(this);

		// Check if device is connected to the WiFi or not
		ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (mWifi.isConnected()) {
			Toast.makeText(this, "Wi-Fi Enabled! :) ", Toast.LENGTH_LONG)
					.show();
		} else {
			Toast.makeText(this, "Not Connected", Toast.LENGTH_LONG).show();
		}

		wifi_manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		dhcp_info = wifi_manager.getDhcpInfo();
		int netmask = dhcp_info.netmask;
		int ipAddress = dhcp_info.ipAddress;
		readableIPAddress = String.valueOf(convertNetworkAddress(ipAddress));
		readableNetmask = String.valueOf(convertNetworkAddress(netmask));
		bluetoothName = getLocalBluetoothName();

		wifiMultipleUpdates.add("Network Subnet Mask: " + readableNetmask
				+ "\n");
		wifiMultipleUpdates.add("Your IP Address: " + readableIPAddress + "\n");
		handler.post(new UpdateWiFiInfoTextViewRunnable());

		arrayAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1);
		var_listview.setAdapter(arrayAdapter);

		// Client request from pressing one item of the IPs list
		var_listview
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					public void onItemClick(AdapterView<?> parentView,
							View childView, int position, long id) {

						shouldContinue = false;
						StringTokenizer listIPStok;
						String listIPStokString;

						listIPStokString = (((TextView) childView).getText())
								.toString();
						listIPStok = new StringTokenizer(listIPStokString);
						sharedDeviceName = listIPStok.nextToken();
						selectedIpAddress = listIPStok.nextToken();
						selectedIpAddress = listIPStok.nextToken();

						RequestConnectionThread = new RequestConnectionThread();
						RequestConnectionThread.start();

						ConnectingProgressBarAsyncTask ConnectingProgressBarAsyncTask = new ConnectingProgressBarAsyncTask();
						ConnectingProgressBarAsyncTask.execute();

					}

					public void onNothingSelected(AdapterView parentView) {
					}
				});
	}

	public void reset() {

		handler = null;
		readableIPAddress = null;
		tOrF = null;
		shouldContinue = false;
		sharedDeviceName = null;
		sharedDataOnClient = null;
		sharedDataOnServer = null;
		receivedOption = null;
		pingedDeviceName = null;
		bluetoothName = null;
		selectedIpAddress = null;
		socketMessage = null;
		syncString = null;
		syncMsg = null;
		syncIp = null;
		syncBluetoothName = null;
		wifiScrollViewText = null;
		to_ping_ip = null;
		ips_List = null;
		wifiMultipleUpdates = null;
		arrayAdapter = null;
		connectOrNot = null;
		serverInput = null;
		serverOutput = null;
		clientInput = null;
		clientOutput = null;
		RequestConnectionThread = null;
		main_context = null;
		receivePingServer = null;
		connectionClient = null;
		connectionServer = null;
		connectedDevices_textView = null;
		var_pingButton = null;

	}

	protected void onStop() { // TODO Auto-generated method stub
		super.onStop();

	}

	protected void onPause() {
		super.onPause();
		// finish();
	}

	protected void onResume() {
		super.onResume();

	}

	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {

		case R.id.v_pingButton:
			handler.post(new DisablePingButtonRunnable());
			shouldContinue = true;
			pingo = new Thread(new Runnable() {
				public void run() {
					while (shouldContinue) {

						if (readableNetmask.equals("255.255.255.0")) {

							for (int i = 1; i <= 20; i++) {

								String ip_address = readableIPAddress;
								String oct1 = "", oct2 = "", oct3 = "", oct4 = "";

								StringTokenizer ipStok = new StringTokenizer(
										ip_address, ".");

								while (ipStok.hasMoreTokens()) {
									oct1 = ipStok.nextToken();
									oct2 = ipStok.nextToken();
									oct3 = ipStok.nextToken();
									oct4 = ipStok.nextToken();
								}

								to_ping_ip = oct1 + "." + oct2 + "." + oct3
										+ "." + String.valueOf(i);
								// if (!to_ping_ip.equals(readableIPAddress)) {
								if (pingAddress(to_ping_ip, 6789)) {
									ips_List.add(pingedDeviceName + " on: "
											+ to_ping_ip);
								}
								// }
							}
						}

						// delay 10 seconds, then re-ping
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							break;
						}
						handler.post(new UpdateIPListViewRunnable());

					}
				}
			});
			pingo.start();

			// Bundle list_bundle = new Bundle();
			// Intent ip_list_intent = new Intent(this, IPListActivity.class);
			// list_bundle.putStringArrayList("lists", ips_List);
			// ip_list_intent.putExtras(list_bundle);
			// startActivity(ip_list_intent);
			break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.layout.menu, menu);
		return super.onCreateOptionsMenu(menu);

	}

	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		String choice = null;

		switch (item.getItemId()) {
		case R.id.sf_log_item:
			choice = "sf";
			Bundle b1 = new Bundle();
			Intent sf_logs_intent = new Intent(this,
					DatabaseQueryActivity.class);
			b1.putString("choice", choice);
			sf_logs_intent.putExtras(b1);
			startActivity(sf_logs_intent);
			return true;

		case R.id.rf_log_item:
			choice = "rf";
			Bundle b2 = new Bundle();
			Intent rf_logs_intent = new Intent(this,
					DatabaseQueryActivity.class);
			b2.putString("choice", choice);
			rf_logs_intent.putExtras(b2);
			startActivity(rf_logs_intent);
			return true;

		case R.id.im_log_item:
			choice = "im";
			Bundle b3 = new Bundle();
			Intent im_logs_intent = new Intent(this,
					DatabaseQueryActivity.class);
			b3.putString("choice", choice);
			im_logs_intent.putExtras(b3);
			startActivity(im_logs_intent);
			return true;

		case R.id.about_item:
			Intent intent = new Intent(this, AboutUs.class);
			startActivity(intent);
			return true;

		case R.id.exit_item:
			finish();
			// android.os.Process.killProcess(android.os.Process.myPid());
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			// android.os.Process.killProcess(android.os.Process.myPid());
			System.exit(0);
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	static public class DoInBackgroundThread extends Thread {
		public void run() {

			while (receivedOption == null) {

			}
			if (receivedOption.equals("accepted")) {
				// open communication screen
				tOrF = true;
			} else if (receivedOption.equals("denied")) {
				// return to ping screen
				tOrF = false;
			}

		}
	}

	// //// **** AsyncTask - START **** \\\ \\

	public class ConnectingProgressBarAsyncTask extends
			AsyncTask<Integer, Integer, Boolean> {
		ProgressDialog progressDialog;

		@Override
		protected void onPreExecute() {

			progressDialog = ProgressDialog.show(MainByteTransferActivity.this,
					"", "Connecting...");

		}

		@Override
		protected Boolean doInBackground(Integer... params) {

			DoInBackgroundThread DoInBackgroundThread = new DoInBackgroundThread();
			DoInBackgroundThread.start();
			while (tOrF == null) {

			}
			return tOrF;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			progressDialog.dismiss();

			AlertDialog.Builder b = new AlertDialog.Builder(
					MainByteTransferActivity.this);
			b.setTitle(android.R.string.dialog_alert_title);
			if (result) {
				b.setMessage("Connection succeeded!");

				try {
					receivePingServer.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else {
				b.setMessage("Connection failed!");
			}

			b.setPositiveButton(getString(android.R.string.ok),
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dlg, int arg1) {
							dlg.dismiss();
							// open communication screen
							if (tOrF) {

								sharedDataOnClient = sharedDeviceName + "__"
										+ selectedIpAddress + "__" + "Client";
								Bundle list_bundle = new Bundle();
								Intent intent = new Intent(main_context,
										CommunicationScreenActivity.class);
								list_bundle.putString("share",
										sharedDataOnClient);
								intent.putExtras(list_bundle);
								main_context.startActivity(intent);
							} else {
								// ////////////////////////////////////////
								finish();
								Intent intent = new Intent(main_context,
										MainByteTransferActivity.class);
								main_context.startActivity(intent);
							}
						}
					});
			b.create().show();
		}
	}

	// ////**** AsyncTask - END **** \\\ \\

	public boolean pingAddress(String ip, int port) {

		Socket socket = new Socket();
		try {
			socket.connect(new InetSocketAddress(ip, port), 200);

			ObjectOutputStream out = new ObjectOutputStream(
					socket.getOutputStream());
			out.writeObject("justping");
			out.flush();
			ObjectInputStream in = new ObjectInputStream(
					socket.getInputStream());
			pingedDeviceName = (String) in.readObject();
			socket.close();
		} catch (IOException e) {
			return false;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		return true;

	}

	static public class UpdateIPListViewRunnable implements Runnable {
		public void run() {
			arrayAdapter.clear();
			for (int i = 0; i < ips_List.size(); i++) {
				arrayAdapter.add(ips_List.get(i));
				arrayAdapter.notifyDataSetChanged();
			}
			ips_List.clear();
		}
	}

	// Client
	static public class RequestConnectionThread extends Thread {
		public void run() {

			try {
				connectionClient = new Socket();
				connectionClient.connect(new InetSocketAddress(
						selectedIpAddress, 6789), 200);

				clientOutput = new ObjectOutputStream(
						connectionClient.getOutputStream());

				socketMessage = "connect" + "***" + readableIPAddress + "***"
						+ bluetoothName;
				clientOutput.writeObject(socketMessage);
				clientOutput.flush();

				clientInput = new ObjectInputStream(
						connectionClient.getInputStream());

				receivedOption = (String) clientInput.readObject();
				connectionClient.close();

				// progressDialog until string (accepted or denied) received

			} catch (IOException e) {
				Log.d("IO EXCEPTION", "NO SOCKET CONNECTED!");
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	static public class UpdateWiFiInfoTextViewRunnable implements Runnable {
		public void run() {
			for (int i = 0; i < wifiMultipleUpdates.size(); i++) {
				wifi_info_textView.append(wifiMultipleUpdates.get(i));
			}
			wifiMultipleUpdates.clear();
		}
	}

	// Server
	static public class ReceivePingThread extends Thread {

		public void run() {
			try {

				receivePingServer = new ServerSocket(6789, 100);
				connectionServer = receivePingServer.accept();

				serverInput = new ObjectInputStream(
						connectionServer.getInputStream());
				syncString = (String) serverInput.readObject();

				serverOutput = new ObjectOutputStream(
						connectionServer.getOutputStream());

				if (syncString.equals("justping")) {

					serverOutput.writeObject(bluetoothName);

					receivePingServer.close();
					ReceivePingThread ReceivePingThread = new ReceivePingThread();
					ReceivePingThread.start();
				} else {
					receivePingToConnect();
				}

			} catch (IOException e) {

				// receivePingToConnect();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		// Server receiving connection request
		public void receivePingToConnect() {

			try {

				StringTokenizer syncStok = new StringTokenizer(syncString,
						"***");
				syncMsg = syncStok.nextToken();
				syncIp = syncStok.nextToken();
				syncBluetoothName = syncStok.nextToken();

				if (syncMsg.equals("connect")) {
					connectOrNot.setTitle("Connection request from: "
							+ syncBluetoothName + "  " + "[" + syncIp + "]");
					connectOrNot
							.setMessage("Do you wish to accept the connection?");

					// Button 1
					connectOrNot.setButton("No Prob baby, Connect!",
							new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int which) {
									// send string; connection accepted
									try {
										shouldContinue = false;
										serverOutput.writeObject("accepted");
										serverOutput.flush();

										// open communication screen
										sharedDataOnServer = syncBluetoothName
												+ "__" + syncIp + "__"
												+ "Server";
										Bundle list_bundle = new Bundle();
										Intent intent = new Intent(
												main_context,
												CommunicationScreenActivity.class);
										list_bundle.putString("share",
												sharedDataOnServer);
										intent.putExtras(list_bundle);
										main_context.startActivity(intent);

									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}

								}
							});

					// Button 2
					connectOrNot.setButton2("No, neglect it",
							new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int which) {
									// send string; connection denied
									try {
										serverOutput.writeObject("denied");
										serverOutput.flush();

									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}

								}
							});
					handler.post(new ShowConnectionAlertDialogRunnable());
					receivePingServer.close();

				} else {
					receivePingServer.close();
					ReceivePingThread ReceivePingThread = new ReceivePingThread();
					ReceivePingThread.start();
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	public String getLocalBluetoothName() {
		if (mBluetoothAdapter == null) {
			mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		}

		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}

		String name = mBluetoothAdapter.getName();
		if (name == null) {
			name = mBluetoothAdapter.getAddress();
		}
		return name;
	}

	static public class ShowConnectionAlertDialogRunnable implements Runnable {
		public void run() {
			connectOrNot.show();
		}
	}

	static public class DisablePingButtonRunnable implements Runnable {
		public void run() {
			var_pingButton.setEnabled(false);
		}
	}

	static public class UpdateTextViewRunnable implements Runnable {
		public void run() {
			connectedDevices_textView.setText(syncMsg);
		}
	}

	public String convertNetworkAddress(int address) {
		// converting to IP Address Format
		int intMyIp3 = address / 0x1000000;
		int intMyIp3mod = address % 0x1000000;

		int intMyIp2 = intMyIp3mod / 0x10000;
		int intMyIp2mod = intMyIp3mod % 0x10000;

		int intMyIp1 = intMyIp2mod / 0x100;
		int intMyIp0 = intMyIp2mod % 0x100;

		String readableAddress = String.valueOf(intMyIp0) + "."
				+ String.valueOf(intMyIp1) + "." + String.valueOf(intMyIp2)
				+ "." + String.valueOf(intMyIp3);
		return readableAddress;
	}

}
