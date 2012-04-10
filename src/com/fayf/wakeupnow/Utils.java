package com.fayf.wakeupnow;

import com.google.android.maps.Projection;

public class Utils {
	public static int dp2px(int dp){
		return (int) (dp * G.density + 0.5f);
	}
	
	public static int m2px(float meters, double latitude, Projection proj) {
	    return (int) (proj.metersToEquatorPixels(meters) * (1/ Math.cos(Math.toRadians(latitude))));         
	}

}
