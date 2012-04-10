package com.fayf.wakeupnow.overlays;

import java.util.ArrayList;
import java.util.List;

import android.graphics.drawable.Drawable;
import android.location.Address;
import android.view.MotionEvent;
import android.view.View;

import com.fayf.wakeupnow.R;
import com.fayf.wakeupnow.Utils;
import com.fayf.wakeupnow.activity.AlertsMapActivity.PopupViewHolder;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;

public class SearchOverlay extends ItemizedOverlay<SearchResult>{
	private View popupView;
	private boolean isPinch;
	private int tappedIndex;
	private List<SearchResult> items = new ArrayList<SearchResult>();
	private MapView.LayoutParams mapParams = new MapView.LayoutParams(MapView.LayoutParams.WRAP_CONTENT, MapView.LayoutParams.WRAP_CONTENT, null, 0, Utils.dp2px(-20), MapView.LayoutParams.BOTTOM_CENTER);

	public SearchOverlay(View popupView, Drawable defaultMarker) {
		super(boundCenterBottom(defaultMarker));
		this.popupView = popupView;
		populate();
	}

	@Override
	protected SearchResult createItem(int i) {
		return items.get(i);
	}

	@Override
	public int size() {
		return items.size();
	}

	public void clear(){
		items.clear();
		setLastFocusedIndex(-1);
		populate();
	}

	public void addItem(SearchResult item){
		items.add(item);
		setLastFocusedIndex(-1);
		populate();
	}

	@Override
	public boolean onTouchEvent(MotionEvent e, MapView mapView){
		if( e.getAction()==MotionEvent.ACTION_DOWN ){
			isPinch=false;
		}

		if( e.getAction()==MotionEvent.ACTION_MOVE){
			mapView.removeView(popupView);
			if(e.getPointerCount() > 1) isPinch=true;
		}
		return super.onTouchEvent(e,mapView);
	}

	@Override
	public boolean onTap(GeoPoint p, MapView mapView) {
		tappedIndex = -1;
		boolean itemTapped = super.onTap(p, mapView);
		if(!isPinch && itemTapped){
			PopupViewHolder popupVH = (PopupViewHolder) popupView.getTag();

			SearchResult item = items.get(tappedIndex);
			mapParams.point = item.getPoint();
			mapView.setTag(R.id.tag_geopoint, item.getPoint());
			popupVH.buttonCreate.setVisibility(View.VISIBLE);
			popupVH.buttonDelete.setVisibility(View.GONE);
			popupVH.buttonSave.setVisibility(View.GONE);
			
			Address address = item.getAddress();
			String feature = address.getFeatureName();
			if(feature != null){
//				popupVH.editTitle.setVisibility(View.VISIBLE);
				popupVH.editTitle.setText(feature);
			}
			
			String addressStr = "";
			int lines = address.getMaxAddressLineIndex()+1;
			for(int i=0; i<lines; i++){
				addressStr += address.getAddressLine(i);
				if(i != lines-1) addressStr += "\n";
			}
//			popupVH.editSnippet.setVisibility(View.VISIBLE);
			popupVH.editSnippet.setText(addressStr);

			mapView.removeView(popupView);
			popupView.setLayoutParams(mapParams);
			mapView.addView(popupView);
		}
		return !isPinch && itemTapped;
	}

	@Override
	protected boolean onTap(int index) {
		tappedIndex = index;
		return true;
	}
}
