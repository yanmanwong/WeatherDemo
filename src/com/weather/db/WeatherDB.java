package com.weather.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.weather.model.City;
import com.weather.model.County;
import com.weather.model.Province;

public class WeatherDB {
	public final static  String DB_NAME = "weather";//数据库名
	public final static int VERSION = 1;//数据库版本号
	private static WeatherDB weatherDB;
	private SQLiteDatabase db;
	
	/*
	 * 构造方法私有化
	 */
	private WeatherDB(Context context){
		MyWeatherOpenHelper dbHelper = new MyWeatherOpenHelper(context, DB_NAME, null, VERSION);
		db = dbHelper.getWritableDatabase();
	}
	
	//获取WeatherDB实例
	public synchronized static WeatherDB getInstance (Context context){
		if (weatherDB == null){
			weatherDB = new WeatherDB(context);
		}
		return weatherDB;
	}
	
//	//test
//	private void test(){
//		List<Province> list = new ArrayList<Province>();
//		Cursor cursor = db.query("Province", null, null, null, null, null, null);
//		if (cursor.moveToFirst()){
//			do{
//				Province province = new Province();
//				province.setId(cursor.getInt(cursor.getColumnIndex("_id")));
//				province.setProvinceName(cursor.getString(cursor.getColumnIndex("province_name")));
//				province.setProvinceCode(cursor.getString(cursor.getColumnIndex("province_code")));
//				list.add(province);
//			 	
//			}while(cursor.moveToNext());
//		}
//		if (cursor != null){
//			cursor.close();
//		}
//		for (int i = 0;i < list.size();i++){
//		Log.e("test", list.get(i).getId() + "name" + list.get(i).getProvinceName()
//							+ " code " + list.get(i).getProvinceCode());
//		}
//	}
	//test1
//		private void test1(){
//			List<City> list = new ArrayList<City>();
//			Cursor cursor = db.query("City", null, null, null, null, null, null);
//			if (cursor.moveToFirst()){
//				do{
//					City city = new City();
//					city.setId(cursor.getInt(cursor.getColumnIndex("_id")));
//					city.setCityName(cursor.getString(cursor.getColumnIndex("city_name")));
//					city.setCityCode(cursor.getString(cursor.getColumnIndex("city_code")));
//					city.setProvinceId(cursor.getInt(cursor.getColumnIndex("province_id")));
//					list.add(city);
//					
//				}while(cursor.moveToNext());
//			}
//			if (cursor != null){
//				cursor.close();
//			}
//			for (int i = 0;i < list.size();i++){
//			Log.e("test1",        "id = " + list.get(i).getId() 
//					            + " name " +   list.get(i).getCityName()
//								+ " code " + list.get(i).getCityCode()
//								+ " proId =" + list.get(i).getProvinceId());
//			}
//		}
	
	//将Province存入数据库
	public void saveProvince (Province province){
		if (province != null){
			ContentValues values = new ContentValues();
			values.put("province_name", province.getProvinceName());
			values.put("province_code", province.getProvinceCode());
			db.insert("Province", null, values);
		}
		
	}
	
	//将City存入数据库
	public void saveCity (City city){
		if (city != null){
			ContentValues values = new ContentValues();
			values.put("city_name", city.getCityName());
			values.put("city_code", city.getCityCode());
			values.put("province_id", city.getProvinceId());
			db.insert("City", null, values);
//			test();
		}
	}
	
	//将County存入数据库
	public void savaCounty (County county){
		if (county != null){
			ContentValues values = new ContentValues();
			values.put("county_name", county.getCountyName());
			values.put("county_code", county.getCountyCode());
			values.put("city_id", county.getCityId());
			db.insert("County", null, values);
//			test1();
		}
	}
	
	
	//从数据库读取全国省份信息
	public List<Province> loadProvince (){
		List<Province> list = new ArrayList<Province>();
		Cursor cursor = db.query("Province", null, null, null, null, null, null);
		if (cursor.moveToFirst()){
			do{
				Province province = new Province();
				province.setId(cursor.getInt(cursor.getColumnIndex("_id")));
				province.setProvinceName(cursor.getString(cursor.getColumnIndex("province_name")));
				province.setProvinceCode(cursor.getString(cursor.getColumnIndex("province_code")));
				list.add(province);
			 	
			}while(cursor.moveToNext());
		}
		if (cursor != null){
			cursor.close();
		}
		return list;
	}
	
	//从数据库读取某省全市的信息
	public List<City> loadCity(int provinceId){
		List<City> list = new ArrayList<City>();
		Cursor cursor = db.query("City", null, "province_id=?", new String[]{String.valueOf(provinceId)}, null, null, null);
		if (cursor.moveToFirst()){
			do{
				City city = new City();
				city.setId(cursor.getInt(cursor.getColumnIndex("_id")));
				city.setCityName(cursor.getString(cursor.getColumnIndex("city_name")));
				city.setCityCode(cursor.getString(cursor.getColumnIndex("city_code")));
				city.setProvinceId(provinceId);
				list.add(city);
			}while(cursor.moveToNext());
		}
		if (cursor != null){
			cursor.close();
		}
		return list;
	}
	
	//从数据库读取某市全县城的信息
	public List<County> loadCounty(int cityId){
		List<County> list = new ArrayList<County>();
		Cursor cursor = db.query("County", null, "city_id=?", new String[]{String.valueOf(cityId)}, null, null, null);
		if (cursor.moveToFirst()){
			do{
				County county = new County();
				county.setId(cursor.getInt(cursor.getColumnIndex("_id")));
				county.setCountyName(cursor.getString(cursor.getColumnIndex("county_name")));
				county.setCountyCode(cursor.getString(cursor.getColumnIndex("county_code")));
				county.setCityId(cityId);
				list.add(county);
			}while(cursor.moveToNext());
		}
		if (cursor != null){
			cursor.close();
		}
		return list;
	}
}
