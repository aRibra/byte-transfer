package com.bytetransfer;

import java.util.ArrayList;
import java.util.StringTokenizer;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ImPeerHistoryList extends ListActivity {
	ArrayList<String> myList;
	ArrayAdapter<String> arrayAdapter;
	ArrayList<ArrayList> all_data;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		String im_records;
		String peerId = getIntent().getExtras().getString("peer_row_id");
		ListView lv = (ListView) this.getListView();
		lv.setBackgroundResource(R.drawable.cyan_background);
		all_data = new ArrayList<ArrayList>();

		String id, message, source, destination, time, length;
		HistoryLog query = new HistoryLog(this);
		query.open();

		im_records = query.getImData(peerId);
		StringTokenizer stok = new StringTokenizer(im_records, "|");

		while (stok.hasMoreTokens()) {
			id = stok.nextToken().trim();
			if (!id.equals("")) {
				message = stok.nextToken().trim();
				source = stok.nextToken();
				destination = stok.nextToken();
				time = stok.nextToken();
				length = stok.nextToken();

				myList = new ArrayList<String>();

				myList.add(id);
				myList.add(message);
				myList.add(source);
				myList.add(destination);
				myList.add(time);
				myList.add(length);

				all_data.add(myList);

			}
		}

		arrayAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1);
		setListAdapter(arrayAdapter);

		for (int i = 0; i < all_data.size(); i++) {
			String list_item = (String) all_data.get(i).get(0) + ": "
					+ (String) all_data.get(i).get(1);
			arrayAdapter.add(list_item);
		}

		arrayAdapter.notifyDataSetChanged();
		query.close();

		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parentView, View childView,
					int position, long id) {
				// TODO Auto-generated method stub
				String itemSelected = (((TextView) childView).getText())
						.toString();
				String info1, info2, info3, info4, info5, info6;

				ArrayList<String> listo = all_data.get(position);
				info1 = listo.get(0);
				info2 = listo.get(1);
				info3 = listo.get(2);
				info4 = listo.get(3);
				info5 = listo.get(4);
				info5 = info5.replace("Current Time : ", "");
				info6 = listo.get(5);

				final String items[] = { "Log #: " + info1,
						"Message: " + info2, "Source: " + info3,
						"Destination: " + info4, "Time: " + info5,
						"Message Length: " + info6 };

				AlertDialog.Builder ab = new AlertDialog.Builder(
						ImPeerHistoryList.this);
				ab.setTitle("Message Details");
				ab.setItems(items, null);
				ab.show();
				
			}

		});

	}
}
