package com.fayf.beeper;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class BeeperOverlay extends Overlay{
	private DBHelper helper;
	
	@Override
	public boolean onTap(GeoPoint p, MapView mapView) {
		Intent intent = new Intent(C.ACTION_ADD_ALERT);
		intent.putExtra(C.EXTRA_LATITUDE, p.getLatitudeE6());
		intent.putExtra(C.EXTRA_LONGITUDE, p.getLongitudeE6());
		mapView.getContext().sendBroadcast(intent);
		return true;
	}

	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		super.draw(canvas, mapView, shadow);

		if(helper == null) helper = new DBHelper(mapView.getContext());
		
		Projection projection = mapView.getProjection();

		float circleRadius = 15;
		for(ProximityAlert alert : helper.getAlerts()){
			Point pt = new Point();

			GeoPoint geo = new GeoPoint((int) (alert.getLatitude() *1e6), (int)(alert.getLongitude() * 1e6));

			projection.toPixels(geo ,pt);

			Paint innerCirclePaint;

			innerCirclePaint = new Paint();
			innerCirclePaint.setARGB(255, 255, 0, 0);
			innerCirclePaint.setAntiAlias(true);

			innerCirclePaint.setStyle(Paint.Style.FILL);

			canvas.drawCircle((float)pt.x, (float)pt.y, circleRadius, innerCirclePaint);
		}
	}
}
