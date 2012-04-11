package com.fayf.wakeupnow.overlays;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.view.MotionEvent;
import android.view.View;

import com.fayf.wakeupnow.Utils;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;

public class RadiusOverlay extends Overlay{
	private OverlayItem[] items = new OverlayItem[2];
	private int tappedIndex;
	private GeoPoint tappedPoint;
	private boolean isPinching;
	private MapView.LayoutParams mapParams = new MapView.LayoutParams(MapView.LayoutParams.WRAP_CONTENT, MapView.LayoutParams.WRAP_CONTENT, null, 0, Utils.dp2px(-20), MapView.LayoutParams.BOTTOM_CENTER);
	private View v;
	private int currentRadius;
	
	public RadiusOverlay(View v) {
		this.v = v;
	}
	
//	public RadiusOverlay(Drawable defaultMarker) {
//		super(boundCenter(defaultMarker));
//		items[0] = new OverlayItem(new GeoPoint(0, 0), "", "");
//		items[1] = new OverlayItem(new GeoPoint(0, 0), "", "");
//		populate();
//	}
//
//	@Override
//	protected OverlayItem createItem(int i) {
//		return items[i];
//	}
//
//	@Override
//	public int size() {
//		return 2;
//	}

	private boolean isDragging;
	@Override
	public boolean onTouchEvent(MotionEvent e, MapView mapView) {
		if (e.getAction() == MotionEvent.ACTION_DOWN) {
			isPinching = false;
			isDragging = true;
		}else if (e.getAction() == MotionEvent.ACTION_MOVE) {
			tappedPoint = null;
			if (e.getPointerCount() > 1) isPinching = true;
		}else if (e.getAction() == MotionEvent.ACTION_UP) {
			isDragging = false;
		}
		
		
		return super.onTouchEvent(e, mapView);
	}

	@Override
	public boolean onTap(GeoPoint p, MapView mapView) {
		tappedIndex = -1;
		tappedPoint = null;
		if (!isPinching) {
			tappedPoint = p;
			currentRadius = 50;
		}
		return super.onTap(p, mapView);
	}
	
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		Projection projection = mapView.getProjection();

		if (shadow) {
			if (tappedPoint != null) {
				float circleRadius = 10;
				Point pt = new Point();
				projection.toPixels(tappedPoint, pt);

				Paint paint = new Paint();
				paint.setAntiAlias(true);

				// Draw fill
				paint.setARGB(255, 255, 255, 255);
				paint.setStyle(Paint.Style.FILL);
				canvas.drawCircle((float) pt.x, (float) pt.y, circleRadius, paint);

				// Draw stroke
				paint.setARGB(255, 0, 0, 0);
				paint.setStrokeWidth(2);
				paint.setStyle(Paint.Style.STROKE);
				canvas.drawCircle((float) pt.x, (float) pt.y, circleRadius, paint);
				
				// Draw radius circle 
				canvas.drawCircle((float) pt.x, (float) pt.y, currentRadius, paint);
			}
		}
		super.draw(canvas, mapView, shadow);
	}

//	@Override
//	protected boolean onTap(int index) {
//		tappedIndex = index;
//		return true;
//	}
}