package com.fayf.beeper;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper{
	private static final String DB_NAME = "Beeper";
	private static final int DB_VERSION = 3;
	
	public static final String KEY_ID = "_id";
	public static final String KEY_LATITUDE = "lat";
	public static final String KEY_LONGITUDE = "long";
	public static final String KEY_EXPIRY = "expiry";
	public static final String KEY_ACTIVE = "active";

	private static final String TABLE_NAME = "alerts";
	private static final String DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

	private static final String CREATE_TABLE =
		"CREATE TABLE " + TABLE_NAME + " ("
			+ KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ KEY_LATITUDE + " INTEGER NOT NULL, "
			+ KEY_LONGITUDE + " INTEGER NOT NULL, "
			+ KEY_EXPIRY + " INTEGER NOT NULL, "
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
	
	public ProximityAlert getAlert(long id){
		Cursor c = db.query(TABLE_NAME, null, "rowid="+id, null, null, null, null);
		if(c.moveToFirst()){
			ProximityAlert alert = new ProximityAlert(
				c.getInt(c.getColumnIndex(KEY_LATITUDE))/1e6,
				c.getInt(c.getColumnIndex(KEY_LONGITUDE))/1e6,
				1000,
				c.getLong(c.getColumnIndex(KEY_EXPIRY)));
			c.close();
			return alert;
		}else{
			c.close();
			return null;
		}
	}
	
	public long addAlert(ProximityAlert alert){
		ContentValues values = new ContentValues();
		values.put(KEY_LATITUDE, alert.getLatitude()*1e6);
		values.put(KEY_LONGITUDE, alert.getLongitude()*1e6);
		values.put(KEY_EXPIRY, alert.getExpiration());
		values.put(KEY_ACTIVE, 1);
		return db.insert(TABLE_NAME, null, values);
	}
	
	public int removeAlert(long id){
		return db.delete(TABLE_NAME, "_id="+id, null);
	}
	
	public List<ProximityAlert> getAlerts(){
		List<ProximityAlert> alerts = new ArrayList<ProximityAlert>();
		String[] columns = {KEY_ID, KEY_LATITUDE, KEY_LONGITUDE, KEY_EXPIRY};
		Cursor c = db.query(TABLE_NAME, columns, null, null, null, null, null);
		
		while(c.moveToNext()){
			ProximityAlert alert = new ProximityAlert(
					c.getInt(c.getColumnIndex(KEY_LATITUDE))/1e6,
					c.getInt(c.getColumnIndex(KEY_LONGITUDE))/1e6,
					1000,
					c.getLong(c.getColumnIndex(KEY_EXPIRY)));
			
			alerts.add(alert);
		}
		
		c.close();
		return alerts;
	}
	
	public Cursor getAlertsCursor(){
		String[] columns = {KEY_ID, KEY_LATITUDE, KEY_LONGITUDE, KEY_EXPIRY};
		return db.query(TABLE_NAME, columns, null, null, null, null, null);
	}
	
	public void clearData(){
		db.execSQL(DROP_TABLE);
		db.execSQL(CREATE_TABLE);
	}
	
//	
//	public void saveData(List<DataRow> rows){
//		db.execSQL(DROP_TABLE);
//		db.execSQL(CREATE_TABLE);
//		
//		for(DataRow row: rows){
//			ContentValues values = new ContentValues();
//			values.put(KEY_TYPE, row.type.getVal());
//			values.put(KEY_PAYING_FOR_ALL, row.payingForAll);
//			values.put(KEY_WINNER, row.winner);
//			values.put(KEY_GUILTY, row.shooter);
//			values.put(KEY_POINTS, row.points);
//			
//			db.replace(TABLE_NAME, null, values);
//		}
//	}
}