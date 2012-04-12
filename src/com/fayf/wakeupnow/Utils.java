package com.fayf.wakeupnow;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.fayf.wakeupnow.activity.WakeUpActivity;
import com.google.android.maps.Projection;

public class Utils {
	public static int dp2px(int dp) {
		return (int) (dp * G.density + 0.5f);
	}

	public static int m2px(float meters, double latitude, Projection proj) {
		return (int) (proj.metersToEquatorPixels(meters) * (1 / Math.cos(Math.toRadians(latitude))));
	}

	public static PendingIntent createAlertPI(Context context, long id) {
		Intent intent = new Intent(context, WakeUpActivity.class);
		intent.setAction("" + id);
		PendingIntent pi = PendingIntent.getActivity(context, 0, intent, 0);

		return pi;
	}

}
