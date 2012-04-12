package com.fayf.wakeupnow;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;

import com.fayf.wakeupnow.overlays.Alert;
import com.google.android.maps.GeoPoint;

public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		DBHelper dbHelper = new DBHelper(context);
		SharedPreferences prefs = context.getSharedPreferences(C.PREFS_NAME, Context.MODE_PRIVATE);

		List<Alert> alerts = dbHelper.getAlerts();
		if (prefs.getBoolean(C.PREF_RESTORE_ON_BOOT, true)) {
			LocationManager locMan = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

			for (Alert alert : alerts) {
				GeoPoint p = alert.getPoint();
				locMan.addProximityAlert(p.getLatitudeE6() / 1e6, p.getLongitudeE6() / 1e6, alert.getRadius(), alert.getExpiration(), Utils.createAlertPI(context.getApplicationContext(), alert.getId()));
			}
		} else {
			dbHelper.clearData();
		}
		
		dbHelper.close();
	}
}
