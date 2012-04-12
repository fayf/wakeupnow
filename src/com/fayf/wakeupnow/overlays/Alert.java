package com.fayf.wakeupnow.overlays;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

public class Alert extends OverlayItem {
	private long id,
			expiration;
	private int radius;

	public Alert(GeoPoint point, String title, String snippet, int radius, long expiration) {
		super(point, title, snippet);
		this.radius = radius;
		this.expiration = expiration;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

	public long getExpiration() {
		return expiration;
	}

	public int getRadius() {
		return radius;
	}
}
