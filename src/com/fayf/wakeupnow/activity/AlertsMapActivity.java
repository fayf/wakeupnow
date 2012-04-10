package com.fayf.wakeupnow.activity;

import java.io.IOException;
import java.util.List;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.fayf.wakeupnow.C;
import com.fayf.wakeupnow.DBHelper;
import com.fayf.wakeupnow.G;
import com.fayf.wakeupnow.R;
import com.fayf.wakeupnow.overlays.AlertsOverlay;
import com.fayf.wakeupnow.overlays.ProximityAlert;
import com.fayf.wakeupnow.overlays.SearchOverlay;
import com.fayf.wakeupnow.overlays.SearchResult;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;

public class AlertsMapActivity extends MapActivity {
	public class PopupViewHolder {
		public Button buttonDelete, buttonCreate, buttonSave;
		public EditText editTitle, editSnippet;
	}

	private Geocoder geocoder;
	private MapView mapView;
	private LocationManager locMan;
	private DBHelper dbHelper;
	private MenuInflater menuInflater;
	private View popupView;

	private MapController controller;

	private MyLocationOverlay myLocOverlay;
	private AlertsOverlay alertsOverlay;
	private SearchOverlay searchOverlay;

	private ProgressDialog pd;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dbHelper = new DBHelper(this);
		menuInflater = getMenuInflater();
		locMan = (LocationManager)getSystemService(LOCATION_SERVICE);
		geocoder = new Geocoder(this);

		G.density = getResources().getDisplayMetrics().density;

		//Create popup
		popupView = getLayoutInflater().inflate(R.layout.map_popup, null);
		

		Button popupCreate, popupDelete, popupSave;
		popupCreate = (Button) popupView.findViewById(R.id.button_create);
		popupCreate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				GeoPoint point = (GeoPoint) mapView.getTag(R.id.tag_geopoint);
				PopupViewHolder popupVH = (PopupViewHolder) popupView.getTag();
				String title = popupVH.editTitle.getText().toString(), snippet = popupVH.editSnippet.getText().toString();
				ProximityAlert alert = new ProximityAlert(point, title, snippet, C.DEFAULT_RADIUS, C.DEFAULT_EXPIRATION);

				//Add to db
				long id = dbHelper.addAlert(alert);
				//TODO allow user to set radius and expiration
				//TODO styling for edittexts in popup
				//TODO styling for listalertsactivity
				//TODO allow timetabling

				//Register proximity alert with locman
				double lati = point.getLatitudeE6()/1e6, longi = point.getLongitudeE6()/1e6;
				locMan.addProximityAlert(lati, longi, C.DEFAULT_RADIUS, C.DEFAULT_EXPIRATION, createAlertPI(id));
				alertsOverlay.itemsUpdated();
				searchOverlay.clear();

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
				alertsOverlay.itemsUpdated();

				mapView.postInvalidate();
				mapView.removeView(popupView);
			}
		});
		popupSave = (Button) popupView.findViewById(R.id.button_save);
		popupSave.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				PopupViewHolder popupVH = (PopupViewHolder) popupView.getTag();
				String title = popupVH.editTitle.getText().toString(), snippet = popupVH.editSnippet.getText().toString();
				ProximityAlert alertItem = (ProximityAlert)mapView.getTag(R.id.tag_item);
				ProximityAlert newAlert = new ProximityAlert(alertItem.getPoint(), title, snippet, C.DEFAULT_RADIUS, C.DEFAULT_EXPIRATION);
				newAlert.setId(alertItem.getId());
				
				dbHelper.updateAlert(newAlert);
				alertsOverlay.itemsUpdated();

				mapView.postInvalidate();
				mapView.removeView(popupView);
				
				Toast.makeText(AlertsMapActivity.this, R.string.changes_saved, Toast.LENGTH_SHORT).show();
			}
		});

		PopupViewHolder popupVH = new PopupViewHolder();
		popupVH.buttonCreate = popupCreate;
		popupVH.buttonDelete = popupDelete;
		popupVH.buttonSave = popupSave;
		popupVH.editTitle = (EditText) popupView.findViewById(R.id.text_title);
		popupVH.editSnippet = (EditText) popupView.findViewById(R.id.text_snippet);
		popupView.setTag(popupVH);

		//Create mapview
		mapView = new MapView(this, C.API_KEY_DEBUG);
		setContentView(mapView);

		//Configure mapview
		controller = mapView.getController();
		controller.setZoom(C.DEFAULT_ZOOM_LEVEL);
		mapView.setClickable(true); //Enables map panning/zooming controls
		mapView.setBuiltInZoomControls(true);

		Location loc = locMan.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		if(loc != null) controller.setCenter(location2GeoPoint(loc));

		//Overlays
		List<Overlay> overlays = mapView.getOverlays();

		Drawable alertMarker = getResources().getDrawable(R.drawable.marker_alert);
		alertsOverlay = new AlertsOverlay(dbHelper, popupView, alertMarker);
		overlays.add(alertsOverlay);

		myLocOverlay = new MyLocationOverlay(this, mapView);
		myLocOverlay.enableMyLocation();
		overlays.add(myLocOverlay);

		Drawable searchMarker = getResources().getDrawable(R.drawable.marker_search);
		searchOverlay = new SearchOverlay(popupView, searchMarker);
		overlays.add(searchOverlay);
	}


	@Override
	public void onNewIntent(Intent newIntent) {
		if (Intent.ACTION_SEARCH.equals(newIntent.getAction())) {
			String query = newIntent.getStringExtra(SearchManager.QUERY);
			doSearch(query);
		}

	}

	private void doSearch(String query){
		//TODO save searches
		pd = ProgressDialog.show(this, null, getString(R.string.searching));
		searchOverlay.clear();

		new AsyncTask<String, Void, Boolean>(){

			@Override
			protected Boolean doInBackground(String... params) {
				List<Address> results = null;
				try {
					results = geocoder.getFromLocationName(params[0], 5, C.SG_BOUND_SOUTH, C.SG_BOUND_WEST, C.SG_BOUND_NORTH, C.SG_BOUND_EAST);
				} catch (IOException e) {
					e.printStackTrace();
				}

				if(results != null && results.size()> 0){
					for(Address result : results) searchOverlay.addItem(new SearchResult(result));

					//Zoom/pan map to fit all results
					controller.zoomToSpan(searchOverlay.getLatSpanE6(), searchOverlay.getLonSpanE6());
					controller.animateTo(searchOverlay.getCenter());
					return true;
				}
				return false;
			}
			
			protected void onPostExecute(Boolean success) {
				if(!success) Toast.makeText(AlertsMapActivity.this, R.string.no_results_returned, Toast.LENGTH_LONG).show();

				//Hide spinner
				if (pd != null)	pd.cancel();
			}
			

		}.execute(query);
	}

	@Override
	protected void onResume() {
		super.onResume();
		alertsOverlay.itemsUpdated();
		myLocOverlay.enableMyLocation();
	}

	@Override
	protected void onPause() {
		super.onPause();
		myLocOverlay.disableMyLocation();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		dbHelper.close();
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
		switch (item.getItemId()) {
		case R.id.menu_clear:
			List<ProximityAlert> alerts = dbHelper.getAlerts();
			for(ProximityAlert alert : alerts)
				locMan.removeProximityAlert(createAlertPI(alert.getId()));

			dbHelper.clearData();
			alertsOverlay.itemsUpdated();
			mapView.postInvalidate();
			mapView.removeView(popupView);
			return true;
		case R.id.menu_test:
			Intent intent = new Intent(this, WakeUpActivity.class);
			startActivity(intent);
			return true;
		case R.id.menu_list_alerts:
			intent = new Intent(this, ListAlertsActivity.class);
			startActivity(intent);
			return true;
		case R.id.menu_clear_search:
			searchOverlay.clear();
			mapView.postInvalidate();
			return true;
		case R.id.menu_search:
			onSearchRequested();
			return true;
		case R.id.menu_zoom_alerts:
			if(alertsOverlay.size() > 0){
				controller.zoomToSpan(alertsOverlay.getLatSpanE6(), alertsOverlay.getLonSpanE6());
				controller.animateTo(alertsOverlay.getCenter());
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}


	private PendingIntent createAlertPI(long id){
		Intent intent = new Intent(getApplicationContext(), WakeUpActivity.class);
		intent.setAction(""+id);
		PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);

		return pi;
	}
	/**
	 * Converts a Location to a GeoPoint
	 * @param loc
	 *            The Location to convert.
	 * @return A GeoPoint representation of the Location.
	 */
	private GeoPoint location2GeoPoint(Location loc){
		return new GeoPoint((int)(loc.getLatitude()*1e6), (int)(loc.getLongitude()*1e6));
	}
}