package com.fayf.wakeupnow.overlays;

import java.util.List;

import com.google.android.maps.OverlayItem;

public interface IItemOverlay {

	public List<? extends OverlayItem> getItems();
	public int getLatSpanE6();
	public int getLonSpanE6();
}
