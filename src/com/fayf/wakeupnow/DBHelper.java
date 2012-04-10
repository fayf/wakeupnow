package com.fayf.wakeupnow;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.fayf.wakeupnow.overlays.ProximityAlert;
import com.google.android.maps.GeoPoint;

public class DBHelper extends SQLiteOpenHelper{
	private static final String DB_NAME = "wakeupnow";
	private static final int DB_VERSION = 1;

	public static final String KEY_ID = "_id";
	public static final String KEY_LATITUDE = "lat";
	public static final String KEY_LONGITUDE = "long";
	public static final String KEY_EXPIRY = "expiry";
	public static final String KEY_ACTIVE = "active";
	public static final String KEY_TITLE = "title";
	public static final String KEY_SNIPPET = "snippet";

	private static final String TABLE_NAME = "alerts";
	private static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

	private static final String CREATE_TABLE =
			"CREATE TABLE " + TABLE_NAME + " ("
					+ KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
					+ KEY_LATITUDE + " INTEGER NOT NULL, "
					+ KEY_LONGITUDE + " INTEGER NOT NULL, "
					+ KEY_EXPIRY + " INTEGER NOT NULL, "
					+ KEY_TITLE + " TEXT, "
					+ KEY_SNIPPET + " TEXT, "
					+ KEY_ACTIVE + " INTEGER NOT NULL);";

	private SQLiteDatabase db;

	public DBHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);

		db = this.getWritableDatabase();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(DROP_TABLE);
		db.execSQL(CREATE_TABLE);
	}

	public ProximityAlert getAlert(int index){
		Cursor c = db.query(TABLE_NAME, null, null, null, null, null, KEY_ID + " asc", index+",1");
		ProximityAlert alert = null;
		if(c.moveToFirst()) alert = alertFromCursor(c);
		c.close();
		return alert;
	}

	public long addAlert(ProximityAlert alert){
		ContentValues values = new ContentValues();
		values.put(KEY_LATITUDE, alert.getPoint().getLatitudeE6());
		values.put(KEY_LONGITUDE, alert.getPoint().getLongitudeE6());
		String title = alert.getTitle();
		if(title.length() > 0) values.put(KEY_TITLE, title);
		String snippet = alert.getSnippet();
		if(snippet.length() > 0) values.put(KEY_SNIPPET, snippet);
		values.put(KEY_EXPIRY, alert.getExpiration());
		values.put(KEY_ACTIVE, 1);

		long id = db.insert(TABLE_NAME, null, values);
		alert.setId(id);
		return id;
	}
	
	public int updateAlert(ProximityAlert alert){
		ContentValues values = new ContentValues();
		values.put(KEY_LATITUDE, alert.getPoint().getLatitudeE6());
		values.put(KEY_LONGITUDE, alert.getPoint().getLongitudeE6());
		String title = alert.getTitle();
		if(title.length() > 0) values.put(KEY_TITLE, title);
		String snippet = alert.getSnippet();
		if(snippet.length() > 0) values.put(KEY_SNIPPET, snippet);
		values.put(KEY_EXPIRY, alert.getExpiration());
		values.put(KEY_ACTIVE, 1);

		return db.update(TABLE_NAME, values, KEY_ID+"="+alert.getId(), null);
	}

	public int removeAlert(long id){
		return db.delete(TABLE_NAME, KEY_ID+"="+id, null);
	}

	public long getCount(){
		return DatabaseUtils.queryNumEntries(db, TABLE_NAME);
	}

	public List<ProximityAlert> getAlerts(){
		List<ProximityAlert> alerts = new ArrayList<ProximityAlert>();
		Cursor c = db.query(TABLE_NAME, null, null, null, null, null, null);

		while(c.moveToNext()){
			ProximityAlert alert = alertFromCursor(c);
			alerts.add(alert);
		}

		c.close();
		return alerts;
	}

	public Cursor getAlertsCursor(){
		return db.query(TABLE_NAME, null, null, null, null, null, null);
	}

	public int clearData(){
		return db.delete(TABLE_NAME, null, null);
	}

	private ProximityAlert alertFromCursor(Cursor c){
		ProximityAlert alert = new ProximityAlert(
				new GeoPoint(c.getInt(c.getColumnIndex(KEY_LATITUDE)), c.getInt(c.getColumnIndex(KEY_LONGITUDE))),
				c.getString(c.getColumnIndex(KEY_TITLE)),
				c.getString(c.getColumnIndex(KEY_SNIPPET)),
				1000,
				c.getLong(c.getColumnIndex(KEY_EXPIRY)));
		alert.setId(c.getLong(c.getColumnIndex(KEY_ID)));
		return alert;
	}
}