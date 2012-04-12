package com.fayf.wakeupnow;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;

import com.fayf.wakeupnow.overlays.ProximityAlert;
import com.google.android.maps.GeoPoint;

public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		DBHelper dbHelper = new DBHelper(context);
		LocationManager locMan = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

		List<ProximityAlert> alerts = dbHelper.getAlerts();

		for (ProximityAlert alert : alerts) {
			GeoPoint p = alert.getPoint();
			locMan.addProximityAlert(p.getLatitudeE6() / 1e6, p.getLongitudeE6() / 1e6, alert.getRadius(), alert.getExpiration(), Utils.createAlertPI(context.getApplicationContext(), alert.getId()));
		}
	}

}
