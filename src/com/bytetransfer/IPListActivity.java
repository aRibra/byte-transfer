package com.bytetransfer;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class IPListActivity extends Activity {

	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_iplist);

		ListView lv = (ListView) findViewById(R.id.iplist);
	
		ArrayList<String> var_ipsList = new ArrayList<String>();

		var_ipsList = getIntent().getExtras().getStringArrayList("lists");

		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
				R.layout.ip_list_row, var_ipsList);
		
		lv.setAdapter(arrayAdapter);
		
		lv.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() 
		{
		    public void onItemSelected(AdapterView parentView, View childView, int position, long id) 
		    {
		        String text = (((TextView) childView).getText()).toString();
		        
		    }
		    public void onNothingSelected(AdapterView parentView) 
		    {
		    }
		});

	}
	
	
	/*@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		setListAdapter(new ArrayAdapter<String>(IPListActivity.this,
				android.R.layout.ip_list_row));
	}
*/
	
}
