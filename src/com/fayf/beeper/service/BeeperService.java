package com.fayf.beeper;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

public class BeeperService extends Service {
	
	private BroadcastReceiver receiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			//Remove from db & start beeping
			Intent beepIntent = new Intent(BeeperService.this, BeeperActivity.class);
			startActivity(beepIntent);
			
//			PendingIntent pi = PendingIntent.getActivity(BeeperService.this, 0, beepIntent, 0);
		}
	};

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		init();
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	private void init(){
//		locMan = (LocationManager) getSystemService(LOCATION_SERVICE);
//		notifMan = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		
		//Receiver for proximity alert
		registerReceiver(receiver, new IntentFilter(C.ACTION_FIRE_ALERT));
		
		//Notification
		Notification notif = new Notification(R.drawable.ic_launcher, null, 0);
		
		Intent newIntent = new Intent(this, BeeperMapActivity.class);
		PendingIntent pi = PendingIntent.getActivity(this, 5, newIntent, 0);
		notif.setLatestEventInfo(this, "Beeper", "x alerts active.", pi);
		startForeground(3, notif);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		unregisterReceiver(receiver);
	}
}