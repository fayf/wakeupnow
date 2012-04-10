package com.fayf.wakeupnow.overlays;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

public class ProximityAlert extends OverlayItem{
	private long id, expiration;
	private float radius;
	
	public ProximityAlert(GeoPoint point, String title, String snippet, float radius, long expiration) {
		super(point, title, snippet);
		this.radius = radius;
		this.expiration = expiration;
	}

	public void setId(long id){
		this.id = id;
	}

	public long getId(){
		return id;
	}

	public long getExpiration() {
		return expiration;
	}

	public float getRadius() {
		return radius;
	}
}
