package com.fayf.wakeupnow.overlays;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.view.MotionEvent;
import android.widget.ImageView;

import com.fayf.wakeupnow.C;
import com.fayf.wakeupnow.Utils;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;

public class RadiusItemOverlay extends ItemizedOverlay<OverlayItem>{
	private GeoPoint center, circum;
	private boolean isPinching;
	private int currentRadius;
	private MapView.LayoutParams mapParams = new MapView.LayoutParams(MapView.LayoutParams.WRAP_CONTENT, MapView.LayoutParams.WRAP_CONTENT, null, 0, 0, MapView.LayoutParams.CENTER);
	
	private ImageView handle;
	private Drawable handleDrawable;

	public RadiusItemOverlay(Context context, Drawable defaultMarker) {
		super(boundCenter(defaultMarker));
		handleDrawable = defaultMarker;
		handle = new ImageView(context);
		handle.setImageDrawable(boundCenter(defaultMarker));
		populate();
	}

	@Override
	protected OverlayItem createItem(int i) {
		return null;
	}

	@Override
	public int size() {
		return 0;
	}

	private boolean isDragging;
	@Override
	public boolean onTouchEvent(MotionEvent e, MapView mapView) {
		int x = (int)e.getX(), y = (int)e.getY();
		Projection proj = mapView.getProjection();
		if (e.getAction() == MotionEvent.ACTION_DOWN) {
			isPinching = false;

			if(circum != null){
				Point circumPoint = proj.toPixels(circum, null);
				if(center != null && hitTest(null, handleDrawable, x-circumPoint.x, y-circumPoint.y)){
					// handle was touched
					isDragging = true;
					return true;
				}
			}

		}else if (e.getAction() == MotionEvent.ACTION_MOVE) {
			if (e.getPointerCount() > 1) isPinching = true;
			else if(isDragging){
				//Move circum
				circum = proj.fromPixels((int)e.getX(), (int)e.getY());

				//Move handle
				mapParams.point = circum;
				handle.setLayoutParams(mapParams);

				//Find new radius
				float[] results = new float[1];
				Location.distanceBetween(center.getLatitudeE6()/1e6, center.getLongitudeE6()/1e6, circum.getLatitudeE6()/1e6, circum.getLongitudeE6()/1e6, results);
				currentRadius = (int) results[0];
						
				return true;
			}
		}else if (e.getAction() == MotionEvent.ACTION_UP) {
			if(isDragging){
				//End dragging
				isDragging = false;
				return true;
			}else{
				return false;
			}
		}

		return super.onTouchEvent(e, mapView);
	}

	@Override
	public boolean onTap(GeoPoint p, MapView mapView) {
		//center = null;
		boolean itemTapped = super.onTap(p, mapView);
		if (!isPinching && !isDragging) {
			if(!itemTapped){
				center = p;
				currentRadius = C.DEFAULT_RADIUS;

				Projection proj = mapView.getProjection();

				//Find point on circumference
				Point centerPoint = proj.toPixels(p, null);
				centerPoint.x += Utils.m2px(currentRadius, center.getLatitudeE6()/ 1e6, proj);
				circum = proj.fromPixels(centerPoint.x, centerPoint.y);

				mapParams.point = circum;
				handle.setLayoutParams(mapParams);
				mapView.removeView(handle);
				mapView.addView(handle);
			}
		}
		return false;
	}

	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		Projection proj = mapView.getProjection();

		if (shadow) {
			if (center != null) {
				Point pt = proj.toPixels(center, null);

				Paint paint = new Paint();
				paint.setAntiAlias(true);

				// Draw fill
				paint.setColor(C.RADIUS_FILL);
				paint.setStyle(Paint.Style.FILL);
				canvas.drawCircle((float) pt.x, (float) pt.y, Utils.m2px(currentRadius, center.getLatitudeE6()/ 1e6, proj), paint);

				// Draw stroke
				paint.setColor(C.RADIUS_STROKE);
				paint.setStrokeWidth(2);
				paint.setStyle(Paint.Style.STROKE);
				canvas.drawCircle((float) pt.x, (float) pt.y, Utils.m2px(currentRadius, center.getLatitudeE6()/ 1e6, proj), paint);
			}
		}
		super.draw(canvas, mapView, shadow);
	}
}