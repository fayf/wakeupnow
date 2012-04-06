package com.fayf.beeper.activity;

import java.util.List;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.fayf.beeper.BeeperOverlay;
import com.fayf.beeper.C;
import com.fayf.beeper.DBHelper;
import com.fayf.beeper.ProximityAlert;
import com.fayf.beeper.R;
import com.fayf.beeper.R.id;
import com.fayf.beeper.R.menu;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;

public class BeeperMapActivity extends MapActivity {
	private final String API_KEY_DEBUG = "0A19afdIbyAvK-T4zkmx8oFN6mwOODNUSa9-Ynw";

	private final long EXPIRATION_DURATION = 60*60*1000; // 1 hour
	private final int ALERT_RADIUS = 1000;

	private MapView mapView;
	private MapController controller;
	private LocationManager locMan;
	private List<Overlay> overlays;
	private DBHelper dbHelper;
	private MenuInflater menuInflater;
	
	private MyLocationOverlay myLocOverlay;
	
	private BroadcastReceiver removeReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle e = intent.getExtras();
			long alertId = e.getLong(C.EXTRA_ID);
			locMan.removeProximityAlert(createAlertPI(alertId));
			dbHelper.removeAlert(alertId);
			
			Intent newIntent = new Intent(C.ACTION_ALERTS_UDPATED);
			sendBroadcast(newIntent);
		}
	};
	
	private BroadcastReceiver addReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			//add to list of alerts
			Bundle e = intent.getExtras();
			double latitude = e.getInt(C.EXTRA_LATITUDE)/1e6, longitude = e.getInt(C.EXTRA_LONGITUDE)/1e6;
			long id = dbHelper.addAlert(new ProximityAlert(latitude, longitude, 1000, EXPIRATION_DURATION));
			locMan.addProximityAlert(latitude, longitude, ALERT_RADIUS, EXPIRATION_DURATION, createAlertPI(id));
			
			mapView.postInvalidate();
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mapView = new MapView(this, API_KEY_DEBUG);
		
		dbHelper = new DBHelper(this);
		menuInflater = getMenuInflater();
		registerReceiver(addReceiver, new IntentFilter(C.ACTION_ADD_ALERT));
		registerReceiver(removeReceiver, new IntentFilter(C.ACTION_REMOVE_ALERT));

		setContentView(mapView);

		controller = mapView.getController();
		locMan = (LocationManager)getSystemService(LOCATION_SERVICE);
		Location loc = locMan.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		if(loc != null) controller.setCenter(location2GeoPoint(loc));
		controller.setZoom(15);

		mapView.setClickable(true); //Enables map panning/zooming controls
		mapView.setBuiltInZoomControls(true);
		overlays = mapView.getOverlays();

		overlays.add(new BeeperOverlay());
		myLocOverlay = new MyLocationOverlay(this, mapView);
		myLocOverlay.enableMyLocation();
		
		overlays.add(myLocOverlay);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		myLocOverlay.disableMyLocation();
		unregisterReceiver(addReceiver);
		unregisterReceiver(removeReceiver);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menuInflater.inflate(R.menu.menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {		
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.menu_clear:
			Cursor c = dbHelper.getAlertsCursor();
			if(c.moveToFirst()){
				do{
					locMan.removeProximityAlert(createAlertPI(c.getLong(c.getColumnIndex(DBHelper.KEY_ID))));
				}while(c.moveToNext());
			}
			
			dbHelper.clearData();
			mapView.postInvalidate();
			return true;
		case R.id.menu_test:
			Intent intent = new Intent(this, BeeperActivity.class);
			startActivity(intent);
			return true;
		case R.id.menu_list_alerts:
			//Toast.makeText(this, "Not yet implemented.", Toast.LENGTH_SHORT).show();

			intent = new Intent(this, ListAlertsActivity.class);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private PendingIntent createAlertPI(long id){
		Intent intent = new Intent(getApplicationContext(), BeeperActivity.class);
		intent.setAction(""+id);
		PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
		
		return pi;
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	/**
	 * Converts a Location to a GeoPoint
	 * @param loc
	 *            The Location to convert.
	 * @return A GeoPoint representation of the Location.
	 */
	private GeoPoint location2GeoPoint(Location loc){
		return new GeoPoint((int)(loc.getLatitude()*1E6), (int)(loc.getLongitude()*1E6));
	}
}