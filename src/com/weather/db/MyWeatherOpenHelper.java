package com.weather.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class MyWeatherOpenHelper extends SQLiteOpenHelper {

	public MyWeatherOpenHelper(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}
/*
 * ����Province��City��County�������������
 */
	public static final String CREATE_PROVINCE = "create table Province(" +
									"_id  integer primary key autoincrement," +
									"province_name text,province_code text)";
	public static final String CREATE_CITY = "create table City(" + 
									"_id integer primary key autoincrement,"+
									"city_name text,city_code text,province_id integer)";
	public static final String CREATE_COUNTY = "create table County (" +
									"_id integer primary key autoincrement,"+
									"county_name text,county_code text,city_id integer)";
	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL(CREATE_PROVINCE);//����Province��
		db.execSQL(CREATE_CITY);//����City��
		db.execSQL(CREATE_COUNTY);//����County��
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

}
