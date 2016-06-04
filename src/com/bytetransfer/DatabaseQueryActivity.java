package com.bytetransfer;

import java.util.ArrayList;
import java.util.StringTokenizer;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class DatabaseQueryActivity extends Activity {

	ListView var_listview;
	ArrayAdapter<String> arrayAdapter;
	ArrayList<String> records_list;
	ArrayList<ArrayList> all_data;
	String choice;
	Context dbContext;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_database_query);

		TextView tv = (TextView) findViewById(R.id.result_tv);
		var_listview = (ListView) findViewById(R.id.records_lv);
		dbContext = DatabaseQueryActivity.this;
		choice = getIntent().getExtras().getString("choice");
		all_data = new ArrayList<ArrayList>();

		HistoryLog info = new HistoryLog(this);
		info.open();

		String records = null;
		String peers_records;

		if (!choice.equals("")) {
			if (choice.equals("rf")) {
				records = info.getRfData();
				String id, file_name, source, file_type, date_received, completed, size;
				StringTokenizer stok = new StringTokenizer(records, "|");
				records_list = new ArrayList<String>();

				while (stok.hasMoreTokens()) {
					id = stok.nextToken();
					if (!id.equals("\n")) {
						file_name = stok.nextToken();
						source = stok.nextToken();
						file_type = stok.nextToken();
						date_received = stok.nextToken();
						completed = stok.nextToken();
						size = stok.nextToken();

						// add records to the arraylist
						ArrayList<String> list = new ArrayList<String>();
						list.add(id);
						list.add(file_name);
						list.add(source);
						list.add(file_type);
						list.add(date_received);
						list.add(completed);
						list.add(size);

						all_data.add(list);
					}
				}

				// textview allignment
				tv.setText("-[Received Files History]-\n-------------------------------");
				tv.setTextSize(22);
				tv.setTypeface(null, Typeface.BOLD_ITALIC);

			} else if (choice.equals("sf")) {
				records = info.getSfData();
				String id, file_name, destination, file_type, date_sent, completed, size;
				StringTokenizer stok = new StringTokenizer(records, "|");

				while (stok.hasMoreTokens()) {
					id = stok.nextToken();
					if (!id.equals("\n")) {
						file_name = stok.nextToken();
						destination = stok.nextToken();
						file_type = stok.nextToken();
						date_sent = stok.nextToken();
						completed = stok.nextToken();
						size = stok.nextToken();
														
						// add records to the arrayList
						ArrayList<String> list = new ArrayList<String>();
						list.add(id);
						list.add(file_name);
						list.add(destination);
						list.add(file_type);
						list.add(date_sent);
						list.add(completed);
						list.add(size);

						all_data.add(list);
					}
				}
				// textview allignment
				tv.setText("-[Sent Files History]-\n------------------------");
				tv.setTextSize(28);
				tv.setTypeface(null, Typeface.BOLD_ITALIC);
				
			} else if (choice.equals("im")) {
				peers_records = info.getPeersData();
				String id, peer_name;
				StringTokenizer stok = new StringTokenizer(peers_records, "|");
				
				while (stok.hasMoreTokens()) {
					id = stok.nextToken();
					if (!id.equals("\n")) {
						peer_name = stok.nextToken();

						// add records to the arrayList
						ArrayList<String> list = new ArrayList<String>();
						list.add(id);
						list.add(peer_name);

						all_data.add(list);
					}
				}

				// textview allignment
				tv.setText("-[Instant Messaging History]-\n---------------------------------");
				tv.setTextSize(20);
				tv.setTypeface(null, Typeface.BOLD_ITALIC);
			}
		}
		arrayAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1);
		var_listview.setAdapter(arrayAdapter);

		for (int i = 0; i < all_data.size(); i++) {
			String list_item = (String) all_data.get(i).get(0) + ": "
					+ (String) all_data.get(i).get(1);
			arrayAdapter.add(list_item);
		}
		
		var_listview
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					public void onItemClick(AdapterView<?> parentView,
							View childView, int position, long id) {
						// TODO Auto-generated method stub
						
						String info1, info2, info3, info4, info5, info6, info7, date = null, peer = null;
						String itemSelected = (((TextView) childView).getText())
								.toString();
						
						// retrieving from the ListView through the use of
						// (Position parameter)

						ArrayList<String> listo = all_data.get(position);
						if (!choice.equals("im")) {
							info1 = listo.get(0);
							info2 = listo.get(1);
							info3 = listo.get(2);
							info4 = listo.get(3);
							info5 = listo.get(4);
							info6 = listo.get(5);
							info7 = listo.get(6);

							if (choice.equals("rf")) {
								date = "Date Received: ";
								peer = "Source: ";
							} else if (choice.equals("sf")) {
								date = "Date Sent: ";
								peer = "Destination: ";
							}
							
							final String items[] = { "Log #: " + info1,
									"File name: " + info2, peer + info3,
									"File Type: " + info4, date + info5,
									"Completed: " + info6, "Size: " + info7 };
							
							AlertDialog.Builder ab = new AlertDialog.Builder(
									DatabaseQueryActivity.this);
							ab.setTitle("File Details");
							ab.setItems(items, null);
							ab.show();

						} else if (choice.equals("im")) {
							info1 = listo.get(1);
							info1 = info1.trim();
							Bundle b = new Bundle();
							Intent imIntent = new Intent(dbContext,
									ImPeerHistoryList.class);
							b.putString("peer_row_id", info1);
							imIntent.putExtras(b);
							startActivity(imIntent);
						}

					}

				});

		arrayAdapter.notifyDataSetChanged();
		info.close();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.layout.logs_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case R.id.clear_log_item:
			// clear from database
			if (choice.equals("rf")) {
				HistoryLog ex1 = new HistoryLog(this);
				ex1.open();

				int numberOfRecords = all_data.size();
				for (int rf_row = 0; rf_row < numberOfRecords; rf_row++) {
					ArrayList<String> listo = all_data.get(rf_row);
					String sNum = listo.get(0).trim();
					long num = Integer.parseInt(sNum);
					ex1.deleteEntryRf(num);
				}
				arrayAdapter.clear();
				arrayAdapter.notifyDataSetChanged();

				Toast.makeText(this,
						"[Received Files] Logs Cleared Sucessfully!",
						Toast.LENGTH_LONG).show();

				ex1.close();
			} else if (choice.equals("sf")) {
				HistoryLog ex1 = new HistoryLog(this);
				ex1.open();

				int numberOfRecords = all_data.size();
				if (numberOfRecords != 0) {
					for (int sf_row = 0; sf_row < numberOfRecords; sf_row++) {
						ArrayList<String> listo = all_data.get(sf_row);
						String sNum = listo.get(0).trim();
						long num = Integer.parseInt(sNum);
						ex1.deleteEntrySf(num);
					}
					arrayAdapter.clear();
					arrayAdapter.notifyDataSetChanged();

					Toast.makeText(this,
							"[Sent Files] Logs Cleared Sucessfully!",
							Toast.LENGTH_LONG).show();
				} else if (numberOfRecords == 0) {
					Toast.makeText(this, "No logs to delete!",
							Toast.LENGTH_LONG).show();
				}

				ex1.close();
			} else if (choice.equals("im")) {
				HistoryLog ex1 = new HistoryLog(this);
				ex1.open();

				int numberOfRecords = all_data.size();
				if (numberOfRecords != 0) {
					for (int im_row = 0; im_row < numberOfRecords; im_row++) {
						ArrayList<String> listo = all_data.get(im_row);
						String sNum = listo.get(0).trim();
						long num = Integer.parseInt(sNum);
						ex1.deleteEntryIm(num);
					}
					ex1.deleteAllEntryPeers();
					arrayAdapter.clear();
					arrayAdapter.notifyDataSetChanged();
				} else {
					Toast.makeText(this, "No logs to delete!",
							Toast.LENGTH_LONG).show();
				}

				ex1.close();
			}
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}

	}
}
