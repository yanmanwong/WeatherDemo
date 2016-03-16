package com.weather.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.weather.db.WeatherDB;
import com.weather.model.City;
import com.weather.model.County;
import com.weather.model.Province;

/**
 * 
 * @author heheda
 * 
 * 获取数据形式：
 * http://www.weather.com.cn./data/list3/city.xml
 *   省级数据:  01|北京,02|上海
 * http://www.weather.com.cn./data/list3/city19.xml
 * 	 市级数据：        1901|南京,1902|无锡
 * http://www.weather.com.cn./data/list3/city1904.xml
 * 	 县级数据:	  190401|苏州,190402|常熟
 * http://www.weather.com.cn./data/list3/city190404.xml
 * 	最终返回数据: 190404|101190404
 * 
 *	解析和处理服务器返回的数据
 */
public class Utility {

	//解析省级数据
	public synchronized static boolean handleProvincesResponse(WeatherDB weatherDB,String response){
		if (!TextUtils.isEmpty(response)){
			String[] allProvince = response.split(",");
			if (allProvince != null && allProvince.length > 0){
				for (String p : allProvince){
					String[] array = p.split("\\|");
					Province province = new Province();
					province.setProvinceCode(array[0]);
					province.setProvinceName(array[1]);
					weatherDB.saveProvince(province);
				}
				return true;
			}
		}
		return false;
	}
	
	//解析市级数据
	public synchronized static boolean handleCItiesResponse(WeatherDB weatherDB,
			String response,int provinceId){
		if (!TextUtils.isEmpty(response)){
			String[] allCity = response.split(",");
			if (allCity != null && allCity.length > 0){
				for (String c : allCity){
					String[] array = c.split("\\|");
					City city = new City();
					city.setCityCode(array[0]);
					city.setCityName(array[1]);
					city.setProvinceId(provinceId);
					weatherDB.saveCity(city);
				}
				return true;
			}
		}
		return false;
	}
	
	//解析县级数据
	public synchronized static boolean handleCountiesResponse(WeatherDB weatherDB,
			String response,int cityId){
		if (!TextUtils.isEmpty(response)){
			String[] allCounty = response.split(",");
			if (allCounty != null && allCounty.length > 0){
				for (String co : allCounty){
					String[] array = co.split("\\|");
					County county = new County();
					county.setCountyCode(array[0]);
					county.setCountyName(array[1]);
					county.setCityId(cityId);
					weatherDB.savaCounty(county);
				}
				return true;
			}
		}
		return false;
	}
	
	
	/*
	 * 解析服务器返回的JSON数据,并将数据存到本地中
	 *   {"weatherinfo":{"city":"深圳","cityid":"101280601",
	 *    "temp1":"27℃","temp2":"21℃","weather":"多云","img1":"d1.gif",
	 *    "img2":"n1.gif","ptime":"08:00"}}
	 */
	
	public static void handleWeatherResponse(Context context,String response){
		try {
			JSONObject jsonObject = new JSONObject(response);
			JSONObject weatherInfo = jsonObject.getJSONObject("weatherinfo");
			String cityName = weatherInfo.getString("city");
			String weatherCode = weatherInfo.getString("cityid");
			String temp1 = weatherInfo.getString("temp1");
			String temp2 = weatherInfo.getString("temp2");
			String weatherDesp = weatherInfo.getString("weather");
			String publishTime = weatherInfo.getString("ptime");
			savaWeatherInfo(context, cityName, weatherCode, temp1, temp2, weatherDesp, publishTime);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/*
	 * 将服务器返回的所有天气信息存储到sharePreference中
	 * 
	 */
	public static void savaWeatherInfo(Context context,String cityName,
							String weatherCode,String temp1,String temp2,
							String weatherDesp,String publishTime){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy年M月d日", Locale.CHINA);
		SharedPreferences.Editor editor = PreferenceManager.
											getDefaultSharedPreferences(context).edit();
		editor.putBoolean("city_selected", true);
		editor.putString("city_name", cityName);
		editor.putString("weather_code", weatherCode);
		editor.putString("temp1", temp1);
		editor.putString("temp2", temp2);
		editor.putString("weather_desp", weatherDesp);
		editor.putString("publish_time", publishTime);
		editor.putString("current_date",sdf.format(new Date()));
		editor.commit();
	}
	
	
	
	
	
	
	
	
	
	
}
