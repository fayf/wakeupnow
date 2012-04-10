package com.fayf.wakeupnow.overlays;

import android.location.Address;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

public class SearchResult extends OverlayItem {
	private Address address;

	public SearchResult(Address address) {
		super(new GeoPoint((int) (address.getLatitude() * 1e6), (int) (address.getLongitude() * 1e6)), null, null);
		this.address = address;
	}

	public Address getAddress() {
		return address;
	}
}
