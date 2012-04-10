package com.fayf.wakeupnow.activity;

import java.io.IOException;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.fayf.wakeupnow.DBHelper;

public class WakeUpActivity extends Activity {
	private SoundPool soundPool;
	private int streamID;
	private Vibrator vibrator;
	private Button buttonStop;
	private DBHelper dbHelper;
	private PowerManager.WakeLock wakeLock;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		LocationManager locMan = (LocationManager) getSystemService(LOCATION_SERVICE);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		dbHelper = new DBHelper(this);

		// Wake screen up
		PowerManager powerMan = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = powerMan.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "BeeperActivity");
		wakeLock.acquire();

		// Unlock phone
		Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

		// Remove alert
		Intent intent = getIntent();
		if (intent.getAction() != null) {
			PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
			dbHelper.removeAlert(Long.parseLong(intent.getAction()));
			locMan.removeProximityAlert(pi);
		}

		// Sound
		AssetFileDescriptor fd = null;
		try {
			fd = getAssets().openFd("alert.mp3");
		} catch (IOException e) {
			e.printStackTrace();
		}

		soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);

		if (fd != null) {
			soundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
				@Override
				public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
					streamID = soundPool.play(sampleId, 1.0f, 1.0f, 1, -1, 1.0f);
					long[] arr = { 0l, 500l, 500l };
					vibrator.vibrate(arr, 0);
				}
			});
			soundPool.load(fd, 1);
		}

		// Layout
		buttonStop = new Button(this);
		buttonStop.setText("Wake up now!\nYOU ARE REACHING YOUR DESTINATION!\nPress to silence alarm!");

		// Button for stopping sound
		buttonStop.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				stop();
			}
		});
		setContentView(buttonStop);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		stop();
		dbHelper.close();
		wakeLock.release();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	private void stop() {
		soundPool.stop(streamID);
		soundPool.release();
		vibrator.cancel();
	}
}