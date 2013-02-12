package com.fayf.wakeupnow.overlays;

import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.fayf.wakeupnow.C;
import com.fayf.wakeupnow.Utils;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;

public class RadiusOverlay extends ItemizedOverlay<OverlayItem> {
	private List<IItemOverlay> overlays;
	private GeoPoint center,
			circum;
	private boolean isPinching;
	private int radius;
	private MapView.LayoutParams mapParams = new MapView.LayoutParams(MapView.LayoutParams.WRAP_CONTENT, MapView.LayoutParams.WRAP_CONTENT, null, 0, 0, MapView.LayoutParams.CENTER);

	private ImageView handle;
	private Drawable handleDrawable;

	private OverlayItem tappedItem = null;

	public RadiusOverlay(Context context, List<IItemOverlay> overlays, Drawable defaultMarker) {
		super(boundCenter(defaultMarker));
		this.overlays = overlays;
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
		int x = (int) e.getX(), y = (int) e.getY();
		Projection proj = mapView.getProjection();
		if (e.getAction() == MotionEvent.ACTION_DOWN) {
			isPinching = false;

			if (circum != null) {
				Point circumPoint = proj.toPixels(circum, null);
				if (center != null && hitTest(null, handleDrawable, x - circumPoint.x, y - circumPoint.y)) {
					// handle was touched
					isDragging = true;
					return true;
				}
			}

		} else if (e.getAction() == MotionEvent.ACTION_MOVE) {
			if (e.getPointerCount() > 1) isPinching = true;
			else if (isDragging) {
				// Move circum
				circum = proj.fromPixels((int) e.getX(), (int) e.getY());

				// Move handle
				mapParams.point = circum;
				handle.setLayoutParams(mapParams);

				// Find new radius
				float[] results = new float[1];
				Location.distanceBetween(center.getLatitudeE6() / 1e6, center.getLongitudeE6() / 1e6, circum.getLatitudeE6() / 1e6, circum.getLongitudeE6() / 1e6, results);
				radius = (int) results[0];

				return true;
			}
		} else if (e.getAction() == MotionEvent.ACTION_UP) {
			if (isDragging) {
				// End dragging
				isDragging = false;
				return true;
			} else {
				return false;
			}
		}

		return super.onTouchEvent(e, mapView);
	}

	@Override
	public boolean onTap(GeoPoint p, MapView mapView) {
		if (!isPinching && !isDragging) {

			// Get tapped item from list of other overlays
			tappedItem = null;
			for (IItemOverlay overlay : overlays) {
				OverlayItem temp = overlay.getTappedItem();
				if (temp != null) {
					tappedItem = temp;
					break;
				}
			}
			center = (tappedItem == null) ? p : tappedItem.getPoint();
			if (tappedItem != null && tappedItem instanceof Alert) radius = ((Alert) tappedItem).getRadius();
			else radius = mapView.getContext().getSharedPreferences(C.PREFS_NAME, Context.MODE_PRIVATE).getInt(C.PREF_DEFAULT_RADIUS, 1000);

			Projection proj = mapView.getProjection();

			// Find point on circumference
			Point centerPoint = proj.toPixels(center, null);
			centerPoint.x += Utils.m2px(radius, center.getLatitudeE6() / 1e6, proj);
			circum = proj.fromPixels(centerPoint.x, centerPoint.y);

			mapParams.point = circum;
			handle.setLayoutParams(mapParams);
			handle.setVisibility(View.VISIBLE);
			mapView.removeView(handle);
			mapView.addView(handle);
		}
		return false;
	}

	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		Projection proj = mapView.getProjection();

		if (!shadow) {
			if (center != null) {
				Point centerPt = proj.toPixels(center, null);

				Paint paint = new Paint();
				paint.setAntiAlias(true);

				int r = Utils.m2px(radius, center.getLatitudeE6() / 1e6, proj);

				// Draw fill
				paint.setColor(C.RADIUS_FILL_ACTIVE);
				paint.setStyle(Paint.Style.FILL);
				canvas.drawCircle((float) centerPt.x, (float) centerPt.y, r, paint);

				// Draw stroke
				paint.setColor(C.RADIUS_STROKE);
				paint.setStrokeWidth(2);
				paint.setStyle(Paint.Style.STROKE);
				canvas.drawCircle((float) centerPt.x, (float) centerPt.y, r, paint);

				// Draw radius text
				Point circumPt = proj.toPixels(circum, null);

				double dx = circumPt.x - centerPt.x, dy = circumPt.y - centerPt.y;
				double angle = Math.atan(dy / dx) + Math.PI * ((dx >= 0) ? 1 / 2.0 : 3 / 2.0); // shift to 0 <= angle < 2pi

				double y = 40, x = 60;
				dx = x * Math.sin(angle);
				dy = 5 + -y * Math.cos(angle);

				paint.setColor(C.RADIUS_TEXT);
				paint.setStyle(Paint.Style.FILL_AND_STROKE);
				paint.setStrokeWidth(1);
				paint.setTextSize(20);
				paint.setTextAlign(Align.CENTER);
				canvas.drawText(radius + "m", (float) (circumPt.x + dx), (float) (circumPt.y + dy), paint);

				if (tappedItem == null) {
					// Center dot
					float circleRadius = 10;

					// Draw fill
					paint.setColor(C.CENTER_FILL);
					paint.setStyle(Paint.Style.FILL);
					canvas.drawCircle((float) centerPt.x, (float) centerPt.y, circleRadius, paint);

					// Draw stroke
					paint.setColor(C.CENTER_STROKE);
					paint.setStrokeWidth(2);
					paint.setStyle(Paint.Style.STROKE);
					canvas.drawCircle((float) centerPt.x, (float) centerPt.y, circleRadius, paint);
				}
			}
		}
		super.draw(canvas, mapView, shadow);
	}

	public int getRadius() {
		return radius;
	}

	public void clearUI() {
		center = null;
		handle.setVisibility(View.GONE);
	}
}