package com.fayf.wakeupnow.activity;

import java.io.IOException;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
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
import com.fayf.wakeupnow.RecentSearchProvider;
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
	private static final int DIALOG_CONFIRM_REMOVE = 0;
	private static final int DIALOG_CONFIRM_SAVE = 1;
	
	public class PopupViewHolder {
		public Button buttonDelete,
				buttonCreate,
				buttonSave;
		public EditText editTitle,
				editSnippet;
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
		locMan = (LocationManager) getSystemService(LOCATION_SERVICE);
		geocoder = new Geocoder(this);

		G.density = getResources().getDisplayMetrics().density;

		// Create popup
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
				
				// Add to db
				long id = dbHelper.addAlert(alert);
				// TODO styling for listalertsactivity
				// TODO allow user to set radius and expiration
				// TODO preview radius
				// TODO styling for edittexts in popup
				// TODO scheduling

				if (id >= 0) {
					// Register proximity alert with locman
					double lati = point.getLatitudeE6() / 1e6, longi = point.getLongitudeE6() / 1e6;
					locMan.addProximityAlert(lati, longi, C.DEFAULT_RADIUS, C.DEFAULT_EXPIRATION, createAlertPI(id));
					alertsOverlay.itemsUpdated();
					searchOverlay.clear();

					mapView.postInvalidate();
					mapView.removeView(popupView);

					Toast.makeText(AlertsMapActivity.this, R.string.alert_added, Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(AlertsMapActivity.this, R.string.error_saving_alert, Toast.LENGTH_SHORT).show();
				}
			}
		});
		popupDelete = (Button) popupView.findViewById(R.id.button_delete);
		popupDelete.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showDialog(DIALOG_CONFIRM_REMOVE);
			}
		});
		popupSave = (Button) popupView.findViewById(R.id.button_save);
		popupSave.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showDialog(DIALOG_CONFIRM_SAVE);
			}
		});

		PopupViewHolder popupVH = new PopupViewHolder();
		popupVH.buttonCreate = popupCreate;
		popupVH.buttonDelete = popupDelete;
		popupVH.buttonSave = popupSave;
		popupVH.editTitle = (EditText) popupView.findViewById(R.id.text_title);
		popupVH.editSnippet = (EditText) popupView.findViewById(R.id.text_snippet);
		popupView.setTag(popupVH);

		// Create mapview
		mapView = new MapView(this, C.API_KEY_DEBUG);
		setContentView(mapView);

		// Configure mapview
		controller = mapView.getController();
		controller.setZoom(C.DEFAULT_ZOOM_LEVEL);
		mapView.setClickable(true); // Enables map panning/zooming controls
		mapView.setBuiltInZoomControls(true);

		// Show last known location if available
		Location loc = locMan.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		if (loc != null) controller.setCenter(location2GeoPoint(loc));

		// Overlays
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

	private void doSearch(final String query) {
		pd = ProgressDialog.show(this, null, getString(R.string.searching));
		searchOverlay.clear();

		new AsyncTask<String, Void, Boolean>() {
			private List<Address> results = null;

			@Override
			protected Boolean doInBackground(String... params) {
				try {
					results = geocoder.getFromLocationName(params[0], 5, C.SG_BOUND_SOUTH, C.SG_BOUND_WEST, C.SG_BOUND_NORTH, C.SG_BOUND_EAST);
				} catch (IOException e) {
					e.printStackTrace();
				}

				if (results != null && results.size() > 0) {
					for (Address result : results)
						searchOverlay.addItem(new SearchResult(result));

					// Zoom/pan map to fit all results
					controller.zoomToSpan(searchOverlay.getLatSpanE6(), searchOverlay.getLonSpanE6());
					controller.animateTo(searchOverlay.getCenter());
					return true;
				}
				return false;
			}

			protected void onPostExecute(Boolean success) {
				if (!success) Toast.makeText(AlertsMapActivity.this, R.string.no_results_returned, Toast.LENGTH_LONG).show();

				// Hide spinner
				if (pd != null) pd.cancel();

				SearchRecentSuggestions suggestions = new SearchRecentSuggestions(AlertsMapActivity.this, RecentSearchProvider.AUTHORITY, RecentSearchProvider.MODE);
				suggestions.saveRecentQuery(query, results.size() + " result" + ((results.size() == 1)?"":"s"));
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
			for (ProximityAlert alert : alerts)
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
			if (alertsOverlay.size() > 0) {
				controller.zoomToSpan(alertsOverlay.getLatSpanE6(), alertsOverlay.getLonSpanE6());
				controller.animateTo(alertsOverlay.getCenter());
			}else{
				Toast.makeText(this, R.string.no_alerts_to_show, Toast.LENGTH_SHORT).show();
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {
		switch(id){
		case DIALOG_CONFIRM_REMOVE:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Remove alert?")
				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						ProximityAlert alertItem = (ProximityAlert) mapView.getTag(R.id.tag_item);

						locMan.removeProximityAlert(createAlertPI(alertItem.getId()));
						int numRemoved = dbHelper.removeAlert(alertItem.getId());
						if (numRemoved > 0) {
							alertsOverlay.itemsUpdated();

							mapView.postInvalidate();
							mapView.removeView(popupView);

							Toast.makeText(AlertsMapActivity.this, R.string.alert_removed, Toast.LENGTH_SHORT).show();
						} else {
							Toast.makeText(AlertsMapActivity.this, R.string.error_removing_alert, Toast.LENGTH_SHORT).show();
						}
					}
				})
				.setNegativeButton("Cancel", null);
			return builder.show();
		case DIALOG_CONFIRM_SAVE:
			builder = new AlertDialog.Builder(this);
			builder.setTitle("Save changes?")
				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						PopupViewHolder popupVH = (PopupViewHolder) popupView.getTag();
						String title = popupVH.editTitle.getText().toString(), snippet = popupVH.editSnippet.getText().toString();
						ProximityAlert alertItem = (ProximityAlert) mapView.getTag(R.id.tag_item);
						ProximityAlert newAlert = new ProximityAlert(alertItem.getPoint(), title, snippet, C.DEFAULT_RADIUS, C.DEFAULT_EXPIRATION);
						newAlert.setId(alertItem.getId());

						int numUpdated = dbHelper.updateAlert(newAlert);
						if (numUpdated > 0) {
							alertsOverlay.itemsUpdated();

							mapView.postInvalidate();
							mapView.removeView(popupView);

							Toast.makeText(AlertsMapActivity.this, R.string.changes_saved, Toast.LENGTH_SHORT).show();
						} else {
							Toast.makeText(AlertsMapActivity.this, R.string.error_saving_changes, Toast.LENGTH_SHORT).show();
						}
					}
				})
				.setNegativeButton("Cancel", null);
			return builder.show();
			
		default:
			return super.onCreateDialog(id, args);
		}
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	private PendingIntent createAlertPI(long id) {
		Intent intent = new Intent(getApplicationContext(), WakeUpActivity.class);
		intent.setAction("" + id);
		PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);

		return pi;
	}

	/**
	 * Converts a Location to a GeoPoint
	 * 
	 * @param loc
	 *            The Location to convert.
	 * @return A GeoPoint representation of the Location.
	 */
	private GeoPoint location2GeoPoint(Location loc) {
		return new GeoPoint((int) (loc.getLatitude() * 1e6), (int) (loc.getLongitude() * 1e6));
	}
}