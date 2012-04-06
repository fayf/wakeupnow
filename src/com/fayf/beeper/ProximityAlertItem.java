package com.fayf.beeper;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

public class ProximityAlertItem extends OverlayItem{
	private long id;
	public ProximityAlertItem(GeoPoint point, String title, String snippet, long id) {
		super(point, title, snippet);
		this.id = id;
	}

	public long getId(){
		return id;
	}
}
