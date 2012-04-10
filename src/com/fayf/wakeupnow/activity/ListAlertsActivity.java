package com.fayf.wakeupnow.activity;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;

import com.fayf.wakeupnow.C;
import com.fayf.wakeupnow.DBHelper;
import com.fayf.wakeupnow.R;

public class ListAlertsActivity extends ListActivity{
	private static final String[] FROM = {DBHelper.KEY_ID, DBHelper.KEY_LATITUDE, DBHelper.KEY_LONGITUDE, DBHelper.KEY_EXPIRY};
	private static final int[] TO = {R.id.text_id, R.id.text_lat, R.id.text_long, R.id.text_expiry};
	
	private DBHelper dbHelper;
	private SimpleCursorAdapter adapter;
	
	private BroadcastReceiver updatedReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			setListAdapter(new SimpleCursorAdapter(ListAlertsActivity.this, R.layout.list_item, dbHelper.getAlertsCursor(), FROM, TO));
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		registerReceiver(updatedReceiver, new IntentFilter(C.ACTION_ALERTS_UDPATED));
		
		dbHelper = new DBHelper(this);
		
		adapter = new SimpleCursorAdapter(this, R.layout.list_item, dbHelper.getAlertsCursor(), FROM, TO);
			
		adapter.setViewBinder(new ViewBinder() {
			
			@Override
			public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
				if(columnIndex == cursor.getColumnIndex(DBHelper.KEY_LATITUDE) || columnIndex == cursor.getColumnIndex(DBHelper.KEY_LONGITUDE)){
					((TextView)view).setText(""+cursor.getInt(columnIndex)/1e6);
					return true;
				}
				return false;
			}
		});
		
//		getListView().setOnItemLongClickListener(new OnItemLongClickListener() {
//			@Override
//			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//				// Remove alert
//				Cursor c = adapter.getCursor();
//				c.moveToPosition(position);
//
//				long alertId = c.getLong(c.getColumnIndex(DBHelper.KEY_ID));
//				Intent intent = new Intent(C.ACTION_REMOVE_ALERT);
//				intent.putExtra(C.EXTRA_ID, alertId);
//				sendBroadcast(intent);
//				return true;
//			}
//		});

		setListAdapter(adapter);
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}
	
	@Override
	protected void onDestroy() {
		unregisterReceiver(updatedReceiver);
		dbHelper.close();
		super.onDestroy();
	}
}
