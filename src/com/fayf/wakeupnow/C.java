package com.fayf.wakeupnow;

//Constants
public class C {
	public static final String PACKAGE_NAME = "com.fayf.wakeupnow";
	public static final String EXTRA_LATITUDE = PACKAGE_NAME + ".EXTRA_LATITUDE";
	public static final String EXTRA_LONGITUDE = PACKAGE_NAME + ".EXTRA_LONGITUDE";
	public static final String EXTRA_ID = PACKAGE_NAME + ".EXTRA_ID";
	public static final String ACTION_ADD_ALERT = PACKAGE_NAME + ".ACTION_ADD_ALERT";
	public static final String ACTION_REMOVE_ALERT = PACKAGE_NAME + ".ACTION_REMOVE_ALERT";
	public static final String ACTION_FIRE_ALERT = PACKAGE_NAME + ".ACTION_FIRE_ALERT";
	public static final String ACTION_ALERTS_UDPATED = PACKAGE_NAME + ".ACTION_ALERTS_UDPATED";
	
	public static final long DEFAULT_EXPIRATION = 2*60*60*1000; //2 hours
}
