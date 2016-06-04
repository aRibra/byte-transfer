package com.bytetransfer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.view.View.OnClickListener;

public class InstantMessagingActivity extends Activity implements
		OnClickListener {

	static String DeviceName;
	static String sharedIp;
	static String pairedDeviceStatus;

	static ObjectOutputStream Output;
	static ObjectInputStream Input;
	// static ObjectOutputStream clientOutput;
	// static ObjectInputStream clientInput;
	static Handler handler;
	static String threadMessage;
	static ServerSocket server;
	static Socket serverConnection;
	static Socket clientConnection;
	ScrollView scrollView;
	static EditText userText;
	static Button sendButton, startChatting;
	static Button clearConversationButton;
	static TextView chatLog;
	static boolean sendButtonTof;
	static boolean editTextTof;
	static Context mainImContext;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_instant_messaging);

		reset();
		mainImContext = InstantMessagingActivity.this;
		String receivedSharedData = getIntent().getExtras().getString("share");
		StringTokenizer stok = new StringTokenizer(receivedSharedData, "__");
		DeviceName = stok.nextToken();
		sharedIp = stok.nextToken();
		pairedDeviceStatus = stok.nextToken();

		CommunicationScreenActivity.fa.finish();

		chatLog = (TextView) findViewById(R.id.chat_Log);
		userText = (EditText) findViewById(R.id.user_Text);
		sendButton = (Button) findViewById(R.id.button_send);
		startChatting = (Button) findViewById(R.id.start_chat_button);
		clearConversationButton = (Button) findViewById(R.id.clear_button);

		handler = new Handler();
		sendButton.setOnClickListener(this);
		startChatting.setOnClickListener(this);
		clearConversationButton.setOnClickListener(this);

		userText.setEnabled(false);
		sendButton.setEnabled(false);

		ServerConnectThread serverConnectThread = new ServerConnectThread();
		serverConnectThread.start();
	}

	private void reset() {
		// TODO Auto-generated method stub
		DeviceName = null;
		sharedIp = null;
		pairedDeviceStatus = null;
		Output = null;
		Input = null;
		handler = null;
		threadMessage = null;
		server = null;
		serverConnection = null;
		clientConnection = null;
		scrollView = null;
		userText = null;
		sendButton = null; startChatting = null;
		clearConversationButton = null;
		chatLog = null;
		sendButtonTof = false;
		editTextTof = false;
		mainImContext = null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_instant_messaging, menu);
		return true;
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		try {
			server.close();
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

		Intent intent = new Intent(mainImContext,
				MainByteTransferActivity.class);
		mainImContext.startActivity(intent);

	}

	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.start_chat_button:
			handler.post(new DisableStartChatButtonRunnable());
			try {
				server.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ConnectToServerThread ConnectToServerThread = new ConnectToServerThread();
			ConnectToServerThread.start();
			break;

		case R.id.button_send:
			threadMessage = userText.getText().toString();
			SendMessageThread SendMessageThread = new SendMessageThread();
			SendMessageThread.start();

			/*
			 * if (pairedDeviceStatus.equals("Client")) {
			 * ServerSendMessageThread serverSendMessageThread = new
			 * ServerSendMessageThread(); serverSendMessageThread.start(); }
			 * else if (pairedDeviceStatus.equals("Server")) {
			 * ClientSendMessageThread ClientSendMessageThread = new
			 * ClientSendMessageThread(); ClientSendMessageThread.start(); }
			 */

			break;

		case R.id.clear_button:
			handler.post(new ServerClearChatLogRunnable());
			break;
		}
	}

	// Server Stuff

	static public class ServerConnectThread extends Thread {
		@Override
		public void run() {

			try {
				server = new ServerSocket(6789, 200);

				threadMessage = "\n" + "Waiting for someone to connect... \n";
				handler.post(new ServerAppendMessageRunnable());

				serverConnection = server.accept();

				threadMessage = "Now connected to "
						+ serverConnection.getInetAddress().getHostName();

				Output = new ObjectOutputStream(
						serverConnection.getOutputStream());
				Output.flush();
				Input = new ObjectInputStream(serverConnection.getInputStream());

				sendButtonTof = true;
				handler.post(new ServerAbleToSendRunnable());

				editTextTof = true;
				handler.post(new ServerAbleToTypeRunnable());

				handler.post(new DisableStartChatButtonRunnable());

				threadMessage = "\n Streams are now setup! \n";

				// save connected device name to database
				String peer_name = DeviceName;
				HistoryLog entry = new HistoryLog(mainImContext);
				entry.open();
				entry.peerCreateEntry(peer_name);
				entry.close();

				handler.post(new ServerAppendMessageRunnable());

				// receiving message \|\|\|\|\|\|\||\||||\||
				do {
					try {
						threadMessage = "\n" + DeviceName + " - "
								+ (String) Input.readObject();

						// Save message to database
						String message, source, destination, time, characters;
						message = threadMessage;
						source = DeviceName;
						destination = "Me";
						time = getCurrentTime();
						characters = String.valueOf(threadMessage.length());

						HistoryLog entry2 = new HistoryLog(mainImContext);
						entry.open();
						entry.imCreateEntry(message, source, destination, time,
								characters);
						entry.close();

						handler.post(new ServerAppendMessageRunnable());
					} catch (ClassNotFoundException classNotFoundException) {
						threadMessage = "\n I don't know that object type";
						handler.post(new ServerAppendMessageRunnable());

					} catch (OptionalDataException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} while (!threadMessage.equals("END"));

			} catch (IOException e) {
				Log.d("ERROR! ",
						"Connection Error, or ConnectToServerThread have been started!");
			}

		}
	}

	static public class ServerClearChatLogRunnable implements Runnable {
		public void run() {
			chatLog.setText("");
		}
	}

	static public class ServerAppendMessageRunnable implements Runnable {
		public void run() {
			chatLog.append(threadMessage);
			userText.setText("");
		}
	}

	static public class ServerAbleToTypeRunnable implements Runnable {
		public void run() {
			userText.setEnabled(editTextTof);
		}
	}

	static public class ServerAbleToSendRunnable implements Runnable {

		public void run() {
			sendButton.setEnabled(sendButtonTof);
		}

	}

	// ////// End Server Stuff \\\\\\\\
	// ***************************** \\
	// ////// Client Stuff \\\\\\\\

	static public class ConnectToServerThread extends Thread {

		@Override
		public void run() {

			threadMessage = "\n Attempting connection...";
			handler.post(new ClientAppendMessageRunnable());

			clientConnection = new Socket();
			try {
				clientConnection.connect(new InetSocketAddress(sharedIp, 6789),
						200);
				threadMessage = "\nConnected to: "
						+ clientConnection.getInetAddress().getHostName();

				// save connected device name to database
				String peer_name = DeviceName;
				HistoryLog entry = new HistoryLog(mainImContext);
				entry.open();
				entry.peerCreateEntry(peer_name);
				entry.close();

				handler.post(new ClientAppendMessageRunnable());
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			try {
				Output = new ObjectOutputStream(
						clientConnection.getOutputStream());
				Output.flush();
				Input = new ObjectInputStream(clientConnection.getInputStream());

			} catch (StreamCorruptedException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			sendButtonTof = true;
			handler.post(new ClientAbleToSendRunnable());

			editTextTof = true;
			handler.post(new ClientAbleToTypeRunnable());

			// receiving message
			do {
				try {
					threadMessage = "\n" + DeviceName + " - "
							+ (String) Input.readObject();

					// Save message to database
					String message, source, destination, time, characters;
					message = threadMessage;
					source = DeviceName;
					destination = "Me";
					time = getCurrentTime();
					characters = String.valueOf(threadMessage.length());

					HistoryLog entry = new HistoryLog(mainImContext);
					entry.open();
					entry.imCreateEntry(message, source, destination, time,
							characters);
					entry.close();

					handler.post(new ClientAppendMessageRunnable());
				} catch (ClassNotFoundException classNotFoundException) {
					threadMessage = "\n I don't know that object type";
					handler.post(new ClientAppendMessageRunnable());

				} catch (OptionalDataException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} while (!threadMessage.equals("END"));
		}
	}

	/*
	 * static public class ClientSendMessageThread extends Thread {
	 * 
	 * @Override public void run() { try {
	 * clientOutput.writeObject(threadMessage); clientOutput.flush();
	 * threadMessage = "\n Me - " + threadMessage; handler.post(new
	 * ServerAppendMessageRunnable()); } catch (IOException ioException) {
	 * ioException.printStackTrace(); threadMessage =
	 * "\n ERROR: I can't send this message"; handler.post(new
	 * ServerAppendMessageRunnable()); } } }
	 */

	static public class ClientAppendMessageRunnable implements Runnable {
		public void run() {
			chatLog.append(threadMessage);
		}
	}

	static public class ClientAbleToSendRunnable implements Runnable {
		public void run() {
			sendButton.setEnabled(sendButtonTof);
		}

	}

	static public class ClientAbleToTypeRunnable implements Runnable {
		public void run() {
			userText.setEnabled(editTextTof);
		}
	}

	// ////// End ClientStuff \\\\\\\\

	static public class SendMessageThread extends Thread {
		@Override
		public void run() {
			try {
				Output.writeObject(threadMessage);
				Output.flush();
				threadMessage = "\n Me - " + threadMessage;

				// Save message to database
				String message, source, destination, time, characters;
				message = threadMessage;
				source = "Me";
				destination = DeviceName;
				time = getCurrentTime();
				characters = String.valueOf(threadMessage.length());

				HistoryLog entry = new HistoryLog(mainImContext);
				entry.open();
				entry.imCreateEntry(message, source, destination, time,
						characters);
				entry.close();

				handler.post(new ServerAppendMessageRunnable());
			} catch (IOException ioException) {
				ioException.printStackTrace();
				threadMessage = "\n ERROR: I can't send this message";
				handler.post(new ServerAppendMessageRunnable());
			}
		}
	}

	static public class DisableStartChatButtonRunnable implements Runnable {

		public void run() {
			// TODO Auto-generated method stub
			startChatting.setVisibility(View.GONE);
		}

	}

	public static String getCurrentTime() {
		Calendar calendar = new GregorianCalendar();
		String am_pm, time;
		int hour = calendar.get(Calendar.HOUR);
		int minute = calendar.get(Calendar.MINUTE);
		int second = calendar.get(Calendar.SECOND);
		if (calendar.get(Calendar.AM_PM) == 0)
			am_pm = "AM";
		else
			am_pm = "PM";
		time = "Current Time : " + hour + ":" + minute + ":" + second + " "
				+ am_pm;

		return time;
	}
}
