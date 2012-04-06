package com.fayf.beeper.activity;

import java.io.IOException;

import com.fayf.beeper.DBHelper;
import com.fayf.beeper.R;
import com.fayf.beeper.R.id;
import com.fayf.beeper.R.layout;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class BeeperActivity extends Activity {
	private SoundPool soundPool;
	private int streamID;
	private Vibrator vibrator;
	private AssetManager assetMan;
	private Button buttonStop;
	private DBHelper dbHelper;
	private LocationManager locMan;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		assetMan = getAssets();
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		
		locMan = (LocationManager) getSystemService(LOCATION_SERVICE);
		dbHelper = new DBHelper(this);
		
		//Remove alert
		Intent intent = getIntent();
		if(intent.getAction() != null){
			PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
			dbHelper.removeAlert(Long.parseLong(intent.getAction()));
			locMan.removeProximityAlert(pi);
		}
		
		//Sound
		AssetFileDescriptor fd = null;
		try {
			fd = assetMan.openFd("alert.mp3");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
		
		if (fd != null){
			soundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
				@Override
				public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
					streamID = soundPool.play(sampleId, 1.0f, 1.0f, 1, -1, 1.0f);
					long[] arr = {0l, 500l, 500l};
					vibrator.vibrate(arr, 0);
				}
			});
			soundPool.load(fd, 1);
		}
		
		//Layout
		setContentView(R.layout.activity_beeper);
		buttonStop = (Button) findViewById(R.id.button_stop);
		
		//Button for stopping sound
		buttonStop.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				stop();
			}
		});
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		stop();
	}
	
	private void stop(){
		soundPool.stop(streamID);
		vibrator.cancel();
	}
}