package com.fayf.wakeupnow;

import android.graphics.Color;

//Constants
public class C {
	public static final String API_KEY_DEBUG = "0A19afdIbyAvK-T4zkmx8oFN6mwOODNUSa9-Ynw";
	public static final long DEFAULT_EXPIRATION = -1;
	public static final int DEFAULT_ZOOM_LEVEL = 15;
	
	public static final String PACKAGE_NAME = "com.fayf.wakeupnow";
	public static final String EXTRA_LATITUDE = PACKAGE_NAME + ".EXTRA_LATITUDE";
	public static final String EXTRA_LONGITUDE = PACKAGE_NAME + ".EXTRA_LONGITUDE";
	public static final String EXTRA_ID = PACKAGE_NAME + ".EXTRA_ID";
	public static final String ACTION_ADD_ALERT = PACKAGE_NAME + ".ACTION_ADD_ALERT";
	public static final String ACTION_REMOVE_ALERT = PACKAGE_NAME + ".ACTION_REMOVE_ALERT";
	public static final String ACTION_FIRE_ALERT = PACKAGE_NAME + ".ACTION_FIRE_ALERT";
	public static final String ACTION_ALERTS_UDPATED = PACKAGE_NAME + ".ACTION_ALERTS_UDPATED";
	
	//For Geocoder, taken from www.geonames.com
//	public static final int SG_BOUND_WEST = (int) (103.638275*1e6);
//	public static final int SG_BOUND_NORTH = (int) (1.471278*1e6);
//	public static final int SG_BOUND_EAST = (int) (104.007469*1e6);
//	public static final int SG_BOUND_SOUTH = (int) (1.258556*1e6);
//	public static final int SG_BOUND_WEST = (int) (103.638275*1e6);
	public static final double SG_BOUND_NORTH = 1.471278;
	public static final double SG_BOUND_EAST = 104.007469;
	public static final double SG_BOUND_SOUTH = 1.258556;
	public static final double SG_BOUND_WEST = 103.638275;

	public static final int RADIUS_FILL_ACTIVE = 0x6400ff00;
	public static final int RADIUS_FILL = 0x64ff6400;
	public static final int RADIUS_STROKE = 0x64000000;
	public static final int CENTER_FILL = Color.WHITE;
	public static final int CENTER_STROKE = Color.BLACK;

	public static final String PREFS_NAME = PACKAGE_NAME + ".PREFS_NAME";
	public static final String PREF_DEFAULT_RADIUS = PACKAGE_NAME + ".PREF_DEFAULT_RADIUS";
	public static final String PREF_RESTORE_ON_BOOT = PACKAGE_NAME + ".PREF_RESTORE_ON_BOOT";
}
