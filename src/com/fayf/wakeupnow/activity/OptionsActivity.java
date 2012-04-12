package com.fayf.wakeupnow.activity;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.fayf.wakeupnow.C;
import com.fayf.wakeupnow.R;

public class OptionsActivity extends PreferenceActivity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getPreferenceManager().setSharedPreferencesName(C.PREFS_NAME);
		addPreferencesFromResource(R.xml.prefs);
	}
}
