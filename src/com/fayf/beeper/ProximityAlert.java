package com.fayf.beeper;

import java.io.Serializable;

public class ProximityAlert implements Serializable{
	private static final long serialVersionUID = -7468763890677521211L;
	
	private double latitude, longitude;
	private float radius;
	private long expiration;
	private boolean enabled = true;
	
	public ProximityAlert(double latitude, double longitude, float radius, long expiration) {
		super();
		this.latitude = latitude;
		this.longitude = longitude;
		this.radius = radius;
		this.expiration = expiration;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public float getRadius() {
		return radius;
	}

	public long getExpiration() {
		return expiration;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}
