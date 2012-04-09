package com.fayf.beeper;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.Projection;

public class BeeperItemOverlay extends ItemizedOverlay<ProximityAlertItem>{
	private Activity activity;
	private List<ProximityAlertItem> items = new ArrayList<ProximityAlertItem>();

	private boolean isPinch = false;
	private int tappedIndex;
	private GeoPoint tappedPoint;
	private MapView.LayoutParams mapParams = new MapView.LayoutParams(MapView.LayoutParams.WRAP_CONTENT, MapView.LayoutParams.WRAP_CONTENT, null, 0, Utils.dp2px(-20), MapView.LayoutParams.BOTTOM_CENTER);

	private View popupView;

	public BeeperItemOverlay(Activity activity, View popup, Drawable defaultMarker) {
		super(boundCenterBottom(defaultMarker));
		this.activity = activity;
		this.popupView = popup;

		popup.setLayoutParams(mapParams);
		refresh();
	}	

	@Override
	protected ProximityAlertItem createItem(int i) {
		return items.get(i);
	}

	@Override
	public int size() {
		return items.size();
	}

	public void addItem(ProximityAlertItem item) {
		items.add(item);
		setLastFocusedIndex(-1);
		populate();
	}

	public void removeItem(int index){
		items.remove(index);
		setLastFocusedIndex(-1);
		populate();
	}

	public void removeItem(ProximityAlertItem alertItem){
		items.remove(alertItem);
		setLastFocusedIndex(-1);
		populate();
	}

	public void clear(){
		items.clear();
		setLastFocusedIndex(-1);
		populate();
	}

	public void resetTappedPoint(){
		tappedPoint = null;
	}

	public void refresh(){
		items.clear();
		DBHelper helper = new DBHelper(activity);

		Cursor c = helper.getAlertsCursor();
		if(c.moveToFirst()){
			do{
				int lati = c.getInt(c.getColumnIndex(DBHelper.KEY_LATITUDE));
				int longi = c.getInt(c.getColumnIndex(DBHelper.KEY_LONGITUDE));
				long id = c.getInt(c.getColumnIndex(DBHelper.KEY_ID));
				ProximityAlertItem alertItem = new ProximityAlertItem(new GeoPoint(lati, longi), "", "", id);
				addItem(alertItem);
			}while(c.moveToNext());	
		}else{
			populate();
		}
		c.close();
		helper.close();
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

			mapView.setTag(R.id.tag_item_tapped, new Boolean(itemTapped));
			if(itemTapped){
				ProximityAlertItem item = items.get(tappedIndex);
				mapParams.point = item.getPoint();
				mapView.setTag(R.id.tag_item, items.get(tappedIndex));
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
		super.draw(canvas, mapView, shadow);

		if(tappedPoint != null){
			Projection projection = mapView.getProjection();

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
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeWidth(2);
			canvas.drawCircle((float)pt.x, (float)pt.y, circleRadius, paint);
		}
	}
}
