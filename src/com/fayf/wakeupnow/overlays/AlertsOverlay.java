package com.fayf.wakeupnow.overlays;

import java.util.List;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;

import com.fayf.wakeupnow.C;
import com.fayf.wakeupnow.DBHelper;
import com.fayf.wakeupnow.R;
import com.fayf.wakeupnow.Utils;
import com.fayf.wakeupnow.activity.AlertsMapActivity.PopupViewHolder;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;

public class AlertsOverlay extends ItemizedOverlay<ProximityAlert> implements IItemOverlay{
	private DBHelper dbHelper;
	private boolean isPinching = false;
	private int tappedIndex = -1;
	private OverlayItem tappedItem;
	private MapView.LayoutParams mapParams = new MapView.LayoutParams(MapView.LayoutParams.WRAP_CONTENT, MapView.LayoutParams.WRAP_CONTENT, null, 0, Utils.dp2px(-20), MapView.LayoutParams.BOTTOM_CENTER);

	private View popupView;

	public AlertsOverlay(DBHelper dbHelper, View popupView, Drawable defaultMarker) {
		super(boundCenterBottom(defaultMarker));
		this.dbHelper = dbHelper;
		this.popupView = popupView;
		populate();
	}

	@Override
	protected ProximityAlert createItem(int i) {
		return dbHelper.getAlert(i);
	}

	@Override
	public int size() {
		return (int) dbHelper.getCount();
	}

	public void itemsUpdated() {
		tappedIndex = -1;
		setLastFocusedIndex(-1);
		populate();
	}

	@Override
	public boolean onTouchEvent(MotionEvent e, MapView mapView) {
		if (e.getAction() == MotionEvent.ACTION_DOWN) {
			isPinching = false;
		}

		if (e.getAction() == MotionEvent.ACTION_MOVE) {
			if (e.getPointerCount() > 1) isPinching = true;
		}
		return super.onTouchEvent(e, mapView);
	}

	@Override
	public boolean onTap(GeoPoint p, MapView mapView) {
		tappedIndex = -1;
		boolean itemTapped = super.onTap(p, mapView);
		if (!isPinching) {
			PopupViewHolder popupVH = (PopupViewHolder) popupView.getTag();

			if (itemTapped) {
				// Tap on alert item
				ProximityAlert item = dbHelper.getAlert(tappedIndex);
				mapParams.point = item.getPoint();
				mapView.setTag(R.id.tag_item, item);
				popupVH.buttonCreate.setVisibility(View.GONE);
				popupVH.buttonDelete.setVisibility(View.VISIBLE);
				popupVH.buttonSave.setVisibility(View.VISIBLE);

				popupVH.editTitle.setText(item.getTitle());
				popupVH.editSnippet.setText(item.getSnippet());
				
				tappedItem = item;
			} else {
				// Tap on empty area
				mapParams.point = p;
				mapView.setTag(R.id.tag_geopoint, p);

				popupVH.buttonCreate.setVisibility(View.VISIBLE);
				popupVH.buttonDelete.setVisibility(View.GONE);
				popupVH.buttonSave.setVisibility(View.GONE);

				popupVH.editTitle.setText(null);
				popupVH.editSnippet.setText(null);
				
				tappedItem = null;
			}

			mapView.removeView(popupView);
			popupView.setLayoutParams(mapParams);
			mapView.addView(popupView);
		}
		return false;
	}

	@Override
	protected boolean onTap(int index) {
		tappedIndex = index;
		return true;
	}

	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		Projection projection = mapView.getProjection();

		if (shadow) {
			// Draw alert radius
			List<ProximityAlert> alerts = dbHelper.getAlerts();
			ProximityAlert tappedAlert = null;
			
			//Do not draw for tapped alert
			if(tappedIndex >= 0) tappedAlert = alerts.get(tappedIndex);
			
			for (ProximityAlert alert : alerts) {
				if(tappedAlert != null && alert.equals(tappedAlert)) continue;
				
				Point pt = new Point();
				projection.toPixels(alert.getPoint(), pt);

				Paint paint = new Paint();
				paint.setAntiAlias(true);

				int radius = Utils.m2px(alert.getRadius(), alert.getPoint().getLatitudeE6() / 1e6, projection);

				// Draw fill
				paint.setColor(C.RADIUS_FILL);
				paint.setStyle(Paint.Style.FILL);
				canvas.drawCircle((float) pt.x, (float) pt.y, radius, paint);

				// Draw stroke
				paint.setColor(C.RADIUS_STROKE);
				paint.setStrokeWidth(2);
				paint.setStyle(Paint.Style.STROKE);
				canvas.drawCircle((float) pt.x, (float) pt.y, radius, paint);
			}
		}
		super.draw(canvas, mapView, shadow);
	}

	@Override
	public List<? extends OverlayItem> getItems() {
		return dbHelper.getAlerts();
	}

	@Override
	public OverlayItem getTappedItem() {
		return tappedItem;
	}
}
