package com.fayf.wakeupnow.overlays;

import java.util.List;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;

import com.fayf.wakeupnow.DBHelper;
import com.fayf.wakeupnow.R;
import com.fayf.wakeupnow.Utils;
import com.fayf.wakeupnow.R.id;
import com.fayf.wakeupnow.activity.AlertsMapActivity.PopupViewHolder;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.Projection;

public class AlertsOverlay extends ItemizedOverlay<ProximityAlert>{
	private DBHelper dbHelper;
	private boolean isPinch = false;
	private int tappedIndex;
	private GeoPoint tappedPoint;
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
	
	public void itemsUpdated(){
		tappedPoint = null;
		setLastFocusedIndex(-1);
		populate();
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent e, MapView mapView){
		if( e.getAction()==MotionEvent.ACTION_DOWN ){
			isPinch=false;
		}

		if( e.getAction()==MotionEvent.ACTION_MOVE){
			tappedPoint = null;
			mapView.removeView(popupView);
			if(e.getPointerCount() > 1) isPinch=true;
		}
		return super.onTouchEvent(e,mapView);
	}

	@Override
	public boolean onTap(GeoPoint p, MapView mapView) {
		tappedIndex = -1;
		tappedPoint = null;
		boolean itemTapped = super.onTap(p, mapView);
		if(!isPinch){
			PopupViewHolder popupVH = (PopupViewHolder) popupView.getTag();

			if(itemTapped){
				ProximityAlert item = dbHelper.getAlert(tappedIndex);
				mapParams.point = item.getPoint();
				mapView.setTag(R.id.tag_item, item);
				popupVH.buttonCreate.setVisibility(View.GONE);
				popupVH.buttonDelete.setVisibility(View.VISIBLE);
			}else{
				mapParams.point = p;
				tappedPoint = p;
				mapView.setTag(R.id.tag_geopoint, p);
				popupVH.buttonCreate.setVisibility(View.VISIBLE);
				popupVH.buttonDelete.setVisibility(View.GONE);
			}

			mapView.removeView(popupView);
			popupView.setLayoutParams(mapParams);
			mapView.addView(popupView);
		}
		return true;
	}

	@Override
	protected boolean onTap(int index) {
		tappedIndex = index;
		return true;
	}

	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		Projection projection = mapView.getProjection();
		
		if(shadow){
			//Draw alert radius
			List<ProximityAlert> alerts = dbHelper.getAlerts();
			for(ProximityAlert alert : alerts){
				Point pt = new Point();
				projection.toPixels(alert.getPoint(), pt);

				Paint paint = new Paint();
				paint.setAntiAlias(true);

				int radius = Utils.m2px(alert.getRadius(), alert.getPoint().getLatitudeE6()/1e6, projection);
				
				//Draw fill
				paint.setARGB(100, 0, 100, 255);
				paint.setStyle(Paint.Style.FILL);
				canvas.drawCircle((float)pt.x, (float)pt.y, radius, paint);

				//Draw stroke
				paint.setARGB(100, 0, 0, 0);
				paint.setStrokeWidth(2);
				paint.setStyle(Paint.Style.STROKE);
				canvas.drawCircle((float)pt.x, (float)pt.y, radius, paint);
			}			
		}else{
			if(tappedPoint != null){
				float circleRadius = 10;
				Point pt = new Point();
				projection.toPixels(tappedPoint, pt);

				Paint paint = new Paint();
				paint.setAntiAlias(true);

				//Draw fill
				paint.setARGB(255, 255, 255, 255);
				paint.setStyle(Paint.Style.FILL);
				canvas.drawCircle((float)pt.x, (float)pt.y, circleRadius, paint);

				//Draw stroke
				paint.setARGB(255, 0, 0, 0);
				paint.setStrokeWidth(2);
				paint.setStyle(Paint.Style.STROKE);
				canvas.drawCircle((float)pt.x, (float)pt.y, circleRadius, paint);
			}
		}
		super.draw(canvas, mapView, shadow);
	}
}
