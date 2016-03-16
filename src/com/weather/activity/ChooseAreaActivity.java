package com.weather.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.ViewDebug.IntToString;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.weather.db.WeatherDB;
import com.weather.model.City;
import com.weather.model.County;
import com.weather.model.Province;
import com.weather.util.HttpCallbackListener;
import com.weather.util.HttpUtil;
import com.weather.util.Utility;

public class ChooseAreaActivity extends Activity {
	public static final int LEVEL_PROVINCE = 0;
	public static final int LEVEL_CITY = 1;
	public static final int LEVEL_COUNTY = 2;	
	 
	private ProgressDialog progressDialog;
	private TextView titleText;
	private ListView listView;
	private ArrayAdapter<String> adapter;
	private WeatherDB weatherDB;
	private List<String> dataList = new ArrayList<String>();
	
	//省列表
	private List<Province> provinceList;
	//市列表
	private List<City> cityList;
	//县列表
	private List<County> countyList;
	int i = 0,j = 0;
	private Province selectedProvince;//选中的省份
	private City selectedCity;//选中的城市
	
	private int currentLevel;//当前选中的级别 默认为0，即省份
	
	private boolean isFromWeatherActivity;//是否从WeatherActivity中传来
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);
		
		init();
		
		isFromWeatherActivity = getIntent().getBooleanExtra("from_weather_activity", false);
		//查看在本地是否存有天气信息，若有跳到显示WeatherActivity界面
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		if (prefs.getBoolean("city_selected", false) && !isFromWeatherActivity){
			Intent intent = new Intent(this, WeatherActivity.class);
			startActivity(intent);
			finish();
			return ;
		}
		
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (currentLevel == LEVEL_PROVINCE){
					
					selectedProvince = provinceList.get(position);
					Log.e("position" + ++i, position + "");
					Log.e("proid" + ++j, selectedProvince.getId() + "");
					queryCities();
				}else if (currentLevel == LEVEL_CITY){
					selectedCity = cityList.get(position);
//					Log.e("position", " " + position);
//					Log.e("cityid", " " + selectedCity.getId());
					
					queryCounties();
				}else if (currentLevel == LEVEL_COUNTY){
					String countyCode = countyList.get(position).getCountyCode();
					Intent intent = new Intent(ChooseAreaActivity.this, WeatherActivity.class);
					intent.putExtra("county_code", countyCode);
					startActivity(intent);
					finish();
				}
			}
			
		});
		queryProvinces();
	}
	//初始化
	private void init() {
		listView = (ListView) findViewById(R.id.list_view);
		titleText = (TextView) findViewById(R.id.title_text);
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,dataList);
		listView.setAdapter(adapter);
		weatherDB = WeatherDB.getInstance(this);
	}

	//显示进度对话框
	private void showProgressDialog(){
		if (progressDialog == null){
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("正在加载中...");//设置显示信息
			progressDialog.setCanceledOnTouchOutside(false);//设置按返回键失效
		}
		progressDialog.show();//显示进度对话框
	}
	
	//关闭进度对话框
	private void closeProgressDialog(){
		if (progressDialog != null){
			progressDialog.dismiss();
		}
	}
	
	//捕获back按键，由当前级别来判断，此时应当返回市列表，省列表，还是直接退出
	@Override
	public void onBackPressed() {
		if (currentLevel == LEVEL_CITY){
			queryProvinces();
		}else if (currentLevel == LEVEL_COUNTY){
			queryCities();
		}else{
			//此处由于在现实全国省份列表中，按返回键时，返回weatheractivity界面
			if (isFromWeatherActivity){
				Intent intent = new Intent(this, WeatherActivity.class);
				startActivity(intent);
			}
			finish();
		}
	}
	
	
	/*
	 * 根据传进来的代号和类型从服务器上查询省市县的数据
	 * code|type
	 * 01|北京
	 */
	private void queryFromService (final String code,final String type){
		String address;
		if (!TextUtils.isEmpty(code)){
			address = "http://www.weather.com.cn/data/list3/city" + code + ".xml";
		}else{
			address = "http://www.weather.com.cn/data/list3/city.xml";
		}
		
		showProgressDialog();
		
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			
			@Override
			public void onFinish(String response) {
				boolean result = false;
				if ("province".equals(type)){//
					result = Utility.handleProvincesResponse(weatherDB, response);
				}else if ("city".equals(type)){
					result = Utility.handleCItiesResponse(weatherDB, response, selectedProvince.getId());
				}else if ("county".equals(type)){
					result = Utility.handleCountiesResponse(weatherDB, response, selectedCity.getId());
				}
				
				if (result){
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							closeProgressDialog();
							if ("province".equals(type)){
								queryProvinces();//保存数据到数据库后再从中取出显示到列表中
							}else if ("city".equals(type)){
								queryCities();//保存数据到数据库后再从中取出显示到列表中
							}else if ("county".equals(type)){
								queryCounties();//保存数据到数据库后再从中取出显示到列表中
							}
						}
					});
				}
			}
			
			
			@Override
			public void onError(Exception e) {
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "加载失败", Toast.LENGTH_LONG).show();
					}
				});
			}
		});
	}
	/*
	 * 查询全国所有省，优先从数据库查询，若没，则从服务器查询
	 */
	private void queryProvinces() {
		provinceList = weatherDB.loadProvince();
		if (provinceList.size() > 0){
			dataList.clear();
			for (Province p : provinceList){
				dataList.add(p.getProvinceName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText("中国");
			currentLevel = LEVEL_PROVINCE;
		}else{
			queryFromService(null, "province");
		}
	}
	
	/*
	 * 查询选中的省内所有的市，优先从数据库中查询，若没，则从服务器上查询
	 */
	private void queryCities(){
		cityList = weatherDB.loadCity(selectedProvince.getId());
		Log.e("proId + i", selectedProvince.getId() + "");
		if (cityList.size() > 0){
			dataList.clear();
			for (City c : cityList){
				dataList.add(c.getCityName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedProvince.getProvinceName());
			currentLevel = LEVEL_CITY;
		}else{
			
			queryFromService(selectedProvince.getProvinceCode(), "city");
		}
	}
	/*
	 * 查询选中的市内所有的县，优先从数据库中查询，若没，则从服务器上查询
	 */
	private void queryCounties(){
		countyList = weatherDB.loadCounty(selectedCity.getId());
//		Log.e("cityId", selectedCity.getId() + "");
		if (countyList.size() > 0){
			dataList.clear();
			for (County co : countyList){
				dataList.add(co.getCountyName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedCity.getCityName());
			currentLevel = LEVEL_COUNTY;
		}else{
			
			queryFromService(selectedCity.getCityCode(), "county");
		}
	}
}
