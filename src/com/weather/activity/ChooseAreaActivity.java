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
	
	//ʡ�б�
	private List<Province> provinceList;
	//���б�
	private List<City> cityList;
	//���б�
	private List<County> countyList;
	int i = 0,j = 0;
	private Province selectedProvince;//ѡ�е�ʡ��
	private City selectedCity;//ѡ�еĳ���
	
	private int currentLevel;//��ǰѡ�еļ��� Ĭ��Ϊ0����ʡ��
	
	private boolean isFromWeatherActivity;//�Ƿ��WeatherActivity�д���
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);
		
		init();
		
		isFromWeatherActivity = getIntent().getBooleanExtra("from_weather_activity", false);
		//�鿴�ڱ����Ƿ����������Ϣ������������ʾWeatherActivity����
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
	//��ʼ��
	private void init() {
		listView = (ListView) findViewById(R.id.list_view);
		titleText = (TextView) findViewById(R.id.title_text);
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,dataList);
		listView.setAdapter(adapter);
		weatherDB = WeatherDB.getInstance(this);
	}

	//��ʾ���ȶԻ���
	private void showProgressDialog(){
		if (progressDialog == null){
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("���ڼ�����...");//������ʾ��Ϣ
			progressDialog.setCanceledOnTouchOutside(false);//���ð����ؼ�ʧЧ
		}
		progressDialog.show();//��ʾ���ȶԻ���
	}
	
	//�رս��ȶԻ���
	private void closeProgressDialog(){
		if (progressDialog != null){
			progressDialog.dismiss();
		}
	}
	
	//����back�������ɵ�ǰ�������жϣ���ʱӦ���������б�ʡ�б�����ֱ���˳�
	@Override
	public void onBackPressed() {
		if (currentLevel == LEVEL_CITY){
			queryProvinces();
		}else if (currentLevel == LEVEL_COUNTY){
			queryCities();
		}else{
			//�˴���������ʵȫ��ʡ���б��У������ؼ�ʱ������weatheractivity����
			if (isFromWeatherActivity){
				Intent intent = new Intent(this, WeatherActivity.class);
				startActivity(intent);
			}
			finish();
		}
	}
	
	
	/*
	 * ���ݴ������Ĵ��ź����ʹӷ������ϲ�ѯʡ���ص�����
	 * code|type
	 * 01|����
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
								queryProvinces();//�������ݵ����ݿ���ٴ���ȡ����ʾ���б���
							}else if ("city".equals(type)){
								queryCities();//�������ݵ����ݿ���ٴ���ȡ����ʾ���б���
							}else if ("county".equals(type)){
								queryCounties();//�������ݵ����ݿ���ٴ���ȡ����ʾ���б���
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
						Toast.makeText(ChooseAreaActivity.this, "����ʧ��", Toast.LENGTH_LONG).show();
					}
				});
			}
		});
	}
	/*
	 * ��ѯȫ������ʡ�����ȴ����ݿ��ѯ����û����ӷ�������ѯ
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
			titleText.setText("�й�");
			currentLevel = LEVEL_PROVINCE;
		}else{
			queryFromService(null, "province");
		}
	}
	
	/*
	 * ��ѯѡ�е�ʡ�����е��У����ȴ����ݿ��в�ѯ����û����ӷ������ϲ�ѯ
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
	 * ��ѯѡ�е��������е��أ����ȴ����ݿ��в�ѯ����û����ӷ������ϲ�ѯ
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
