package com.fayf.wakeupnow.activity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
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
import com.fayf.wakeupnow.Utils;
import com.fayf.wakeupnow.overlays.AlertsOverlay;
import com.fayf.wakeupnow.overlays.IItemOverlay;
import com.fayf.wakeupnow.overlays.ProximityAlert;
import com.fayf.wakeupnow.overlays.RadiusOverlay;
import com.fayf.wakeupnow.overlays.SearchOverlay;
import com.fayf.wakeupnow.overlays.SearchResult;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class AlertsMapActivity extends MapActivity {
	private static final int DIALOG_CONFIRM_REMOVE = 0;
	private static final int DIALOG_CONFIRM_SAVE = 1;
	
	private static final double zoomPadding = 1.2;

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
	private RadiusOverlay radiusOverlay;

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
				ProximityAlert alert = new ProximityAlert(point, title, snippet, radiusOverlay.getRadius(), C.DEFAULT_EXPIRATION);

				// Add to db
				long id = dbHelper.addAlert(alert);
				// TODO show button to clear ui
				// TODO allow user to set expiration
				// TODO select vibrate/alert tone
				// TODO styling for edittexts in popup
				// TODO scheduling
				// TODO styling for listalertsactivity

				if (id >= 0) {
					// Register proximity alert with locman
					double lat = point.getLatitudeE6() / 1e6, lon = point.getLongitudeE6() / 1e6;
					locMan.addProximityAlert(lat, lon, radiusOverlay.getRadius(), C.DEFAULT_EXPIRATION, Utils.createAlertPI(getApplicationContext(), id));
					alertsOverlay.itemsUpdated();
					searchOverlay.clear();

					mapView.postInvalidate();
					mapView.removeView(popupView);
					radiusOverlay.clearUI();

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

		myLocOverlay = new MyLocationOverlay(this, mapView);
		myLocOverlay.enableMyLocation();

		Drawable searchMarker = getResources().getDrawable(R.drawable.marker_search);
		searchOverlay = new SearchOverlay(popupView, searchMarker);

		List<IItemOverlay> itemOverlays = new ArrayList<IItemOverlay>();
		itemOverlays.add(alertsOverlay);
		itemOverlays.add(searchOverlay);
		Drawable radiusMarker = getResources().getDrawable(R.drawable.radius_handle);
		radiusOverlay = new RadiusOverlay(this, itemOverlays, radiusMarker);
		
		overlays.add(radiusOverlay);
		overlays.add(searchOverlay);
		overlays.add(alertsOverlay);
		overlays.add(myLocOverlay);
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
					zoomToFit(searchOverlay);
					return true;
				}
				return false;
			}

			protected void onPostExecute(Boolean success) {
				if (!success) Toast.makeText(AlertsMapActivity.this, R.string.no_results_returned, Toast.LENGTH_LONG).show();

				// Hide spinner
				if (pd != null) pd.cancel();

				SearchRecentSuggestions suggestions = new SearchRecentSuggestions(AlertsMapActivity.this, RecentSearchProvider.AUTHORITY, RecentSearchProvider.MODE);
				suggestions.saveRecentQuery(query, results.size() + " result" + ((results.size() == 1) ? "" : "s"));
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
				locMan.removeProximityAlert(Utils.createAlertPI(getApplicationContext(), alert.getId()));

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
				zoomToFit(alertsOverlay);
			} else {
				Toast.makeText(this, R.string.no_alerts_to_show, Toast.LENGTH_SHORT).show();
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {
		switch (id) {
		case DIALOG_CONFIRM_REMOVE:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.confirm_remove).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					ProximityAlert alertItem = (ProximityAlert) mapView.getTag(R.id.tag_item);

					locMan.removeProximityAlert(Utils.createAlertPI(getApplicationContext(), alertItem.getId()));
					int numRemoved = dbHelper.removeAlert(alertItem.getId());
					if (numRemoved > 0) {
						alertsOverlay.itemsUpdated();

						mapView.postInvalidate();
						mapView.removeView(popupView);
						radiusOverlay.clearUI();

						Toast.makeText(AlertsMapActivity.this, R.string.alert_removed, Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(AlertsMapActivity.this, R.string.error_removing_alert, Toast.LENGTH_SHORT).show();
					}
				}
			}).setNegativeButton("Cancel", null);
			return builder.show();
		case DIALOG_CONFIRM_SAVE:
			builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.confirm_save).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					PopupViewHolder popupVH = (PopupViewHolder) popupView.getTag();
					String title = popupVH.editTitle.getText().toString(), snippet = popupVH.editSnippet.getText().toString();
					ProximityAlert alertItem = (ProximityAlert) mapView.getTag(R.id.tag_item);
					ProximityAlert newAlert = new ProximityAlert(alertItem.getPoint(), title, snippet, radiusOverlay.getRadius(), C.DEFAULT_EXPIRATION);
					newAlert.setId(alertItem.getId());

					int numUpdated = dbHelper.updateAlert(newAlert);
					if (numUpdated > 0) {
						alertsOverlay.itemsUpdated();

						mapView.postInvalidate();
						mapView.removeView(popupView);
						radiusOverlay.clearUI();

						Toast.makeText(AlertsMapActivity.this, R.string.changes_saved, Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(AlertsMapActivity.this, R.string.error_saving_changes, Toast.LENGTH_SHORT).show();
					}
				}
			}).setNegativeButton("Cancel", null);
			return builder.show();

		default:
			return super.onCreateDialog(id, args);
		}
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	private <T extends IItemOverlay> void zoomToFit(T overlay) {
		int minLat = Integer.MAX_VALUE;
		int maxLat = Integer.MIN_VALUE;
		int minLon = Integer.MAX_VALUE;
		int maxLon = Integer.MIN_VALUE;

		List<? extends OverlayItem> items = overlay.getItems();
		for (OverlayItem item : items) {
			GeoPoint p = item.getPoint();
			
			int lat = p.getLatitudeE6();
			int lon = p.getLongitudeE6();

			maxLat = Math.max(lat, maxLat);
			minLat = Math.min(lat, minLat);
			maxLon = Math.max(lon, maxLon);
			minLon = Math.min(lon, minLon);
		}

		controller.zoomToSpan((int)(zoomPadding*overlay.getLatSpanE6()), (int)(zoomPadding*overlay.getLonSpanE6()));
		controller.animateTo(new GeoPoint((maxLat + minLat) / 2, (maxLon + minLon) / 2));
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