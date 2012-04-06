package com.fayf.beeper;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.Projection;

public class BeeperItemOverlay extends ItemizedOverlay<ProximityAlertItem>{
	private DBHelper helper;
	private Activity activity;
	private List<ProximityAlertItem> items = new ArrayList<ProximityAlertItem>();

	private int tappedIndex;
	private GeoPoint tappedPoint;

	public BeeperItemOverlay(Activity activity, Drawable defaultMarker) {
		super(boundCenterBottom(defaultMarker));
		this.activity = activity;

		helper = new DBHelper(activity);

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
		helper = new DBHelper(activity);

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
	public boolean onTap(GeoPoint p, MapView mapView) {
		tappedIndex = -1;
		tappedPoint = null;
		boolean itemTapped = super.onTap(p, mapView);
		mapView.setTag(R.id.tag_item_tapped, new Boolean(itemTapped));
		if(itemTapped){
			mapView.setTag(R.id.tag_item, items.get(tappedIndex));
			activity.openContextMenu(mapView);
		}else{
			tappedPoint = p;
			mapView.setTag(R.id.tag_geopoint, p);
			activity.openContextMenu(mapView);
		}
		return super.onTap(p, mapView);
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

			float circleRadius = 15;
			Point pt = new Point();

			projection.toPixels(tappedPoint, pt);

			Paint innerCirclePaint;

			innerCirclePaint = new Paint();
			innerCirclePaint.setARGB(255, 255, 0, 0);
			innerCirclePaint.setAntiAlias(true);

			innerCirclePaint.setStyle(Paint.Style.FILL);

			canvas.drawCircle((float)pt.x, (float)pt.y, circleRadius, innerCirclePaint);
		}
	}
}
