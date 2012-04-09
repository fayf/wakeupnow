package com.fayf.wakeupnow.activity;

import java.util.List;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.fayf.wakeupnow.AlertsOverlay;
import com.fayf.wakeupnow.DBHelper;
import com.fayf.wakeupnow.G;
import com.fayf.wakeupnow.PopupViewHolder;
import com.fayf.wakeupnow.ProximityAlert;
import com.fayf.wakeupnow.R;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;

public class BeeperMapActivity extends MapActivity {
	private final String API_KEY_DEBUG = "0A19afdIbyAvK-T4zkmx8oFN6mwOODNUSa9-Ynw";

//	private final long EXPIRATION_DURATION = 60*60*1000; // 1 hour
	private final long EXPIRATION_DURATION = -1; // indefinite
	private final int ALERT_RADIUS = 1000;

	private MapView mapView;
	private LocationManager locMan;
	private DBHelper dbHelper;
	private MenuInflater menuInflater;
	private View popupView;
	private Button popupCreate, popupDelete;
	
	private MyLocationOverlay myLocOverlay;
	private AlertsOverlay itemOverlay;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dbHelper = new DBHelper(this);
		menuInflater = getMenuInflater();
		locMan = (LocationManager)getSystemService(LOCATION_SERVICE);
		
		G.density = getResources().getDisplayMetrics().density;
		
		//Create popup
		popupView = getLayoutInflater().inflate(R.layout.map_popup, null);
		popupCreate = (Button) popupView.findViewById(R.id.button_create);
		popupCreate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				GeoPoint point = (GeoPoint) mapView.getTag(R.id.tag_geopoint);
				ProximityAlert alert = new ProximityAlert(point, "", "", 1000, EXPIRATION_DURATION);
				
				//Add to db
				long id = dbHelper.addAlert(alert);
				
				//Register proximity alert with locman
				double lati = point.getLatitudeE6()/1e6, longi = point.getLongitudeE6()/1e6;
				locMan.addProximityAlert(lati, longi, ALERT_RADIUS, EXPIRATION_DURATION, createAlertPI(id));
//				itemOverlay.addItem(alert);
				itemOverlay.itemsUpdated();
				
//				itemOverlay.resetTappedPoint();
				mapView.postInvalidate();
				mapView.removeView(popupView);
			}
		});
		popupDelete = (Button) popupView.findViewById(R.id.button_delete);
		popupDelete.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ProximityAlert alertItem = (ProximityAlert)mapView.getTag(R.id.tag_item);
				
				locMan.removeProximityAlert(createAlertPI(alertItem.getId()));
				dbHelper.removeAlert(alertItem.getId());
				itemOverlay.itemsUpdated();
//				itemOverlay.removeItem(alertItem);
				
//				itemOverlay.resetTappedPoint();
				mapView.postInvalidate();
				mapView.removeView(popupView);
			}
		});

		PopupViewHolder popupVH = new PopupViewHolder();
		popupVH.buttonCreate = popupCreate;
		popupVH.buttonDelete = popupDelete;
		popupView.setTag(popupVH);
		
		//Create mapview
		mapView = new MapView(this, API_KEY_DEBUG){
			@Override
			protected ContextMenuInfo getContextMenuInfo() {
				return super.getContextMenuInfo();
			}
		};
		setContentView(mapView);
		
		//Configure mapview
		MapController controller = mapView.getController();
		controller.setZoom(15);
		mapView.setClickable(true); //Enables map panning/zooming controls
		mapView.setBuiltInZoomControls(true);
		
		Location loc = locMan.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		if(loc != null) controller.setCenter(location2GeoPoint(loc));

		//Overlays
		List<Overlay> overlays = mapView.getOverlays();
		
		Drawable d = getResources().getDrawable(R.drawable.marker);
		itemOverlay = new AlertsOverlay(dbHelper, popupView, d);
		overlays.add(itemOverlay);
		
		myLocOverlay = new MyLocationOverlay(this, mapView);
		myLocOverlay.enableMyLocation();
		overlays.add(myLocOverlay);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		itemOverlay.itemsUpdated();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		dbHelper.close();
		myLocOverlay.disableMyLocation();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
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
			List<ProximityAlert> alerts = dbHelper.getAlerts();
			for(ProximityAlert alert : alerts)
				locMan.removeProximityAlert(createAlertPI(alert.getId()));
//			Cursor c = dbHelper.getAlertsCursor();
//			if(c.moveToFirst()){
//				do{
//					locMan.removeProximityAlert(createAlertPI(c.getLong(c.getColumnIndex(DBHelper.KEY_ID))));
//				}while(c.moveToNext());
//			}
//			c.close();
			
			dbHelper.clearData();
//			itemOverlay.clear();
			itemOverlay.itemsUpdated();
			mapView.postInvalidate();
			mapView.removeView(popupView);
			return true;
		case R.id.menu_test:
			Intent intent = new Intent(this, BeeperActivity.class);
			startActivity(intent);
			return true;
		case R.id.menu_list_alerts:
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