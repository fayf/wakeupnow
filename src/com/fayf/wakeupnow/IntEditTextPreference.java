package com.fayf.wakeupnow;

import android.content.Context;
import android.preference.EditTextPreference;
import android.util.AttributeSet;
import android.widget.Toast;

public class IntEditTextPreference extends EditTextPreference {

	public IntEditTextPreference(Context context) {
		super(context);
	}

	public IntEditTextPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public IntEditTextPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected String getPersistedString(String defaultReturnValue) {
		return String.valueOf(getPersistedInt(-1));
	}

	@Override
	protected boolean persistString(String value) {
		try{
			return persistInt(Integer.valueOf(value));
		}catch(NumberFormatException e){
			Toast.makeText(getContext(), "Invalid value!", Toast.LENGTH_SHORT).show();
			
			int p = getPersistedInt(-1);
			setText(""+p);
			return persistInt(p);
		}
	}
}
