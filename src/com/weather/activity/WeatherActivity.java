package com.weather.activity;

import com.weather.util.HttpCallbackListener;
import com.weather.util.HttpUtil;
import com.weather.util.Utility;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

public class WeatherActivity extends Activity {
	private LinearLayout weatherInfoLayout;
	private TextView cityNameText;// ������
	private TextView publishText;// ����ʱ��
	private TextView weatherDespText;// ����������Ϣ
	private TextView temp1Text;// ����1
	private TextView temp2Text;// ����2
	private TextView currentDataText;// ��ǰʱ��

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.weather_layout);
		init();
		initdata();
	}

	// ��ʼ��
	private void init() {
		weatherInfoLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
		publishText = (TextView) findViewById(R.id.poublish_text);
		weatherDespText = (TextView) findViewById(R.id.weather_desp);
		temp1Text = (TextView) findViewById(R.id.temp1);
		temp2Text = (TextView) findViewById(R.id.temp2);
		currentDataText = (TextView) findViewById(R.id.current_data);
		cityNameText = (TextView) findViewById(R.id.city_name);

	}

	// ������ѯ
	private void initdata() {
		String countyCode = getIntent().getStringExtra("county_code");
		Log.e("countyCode", countyCode + "");
		if (!TextUtils.isEmpty(countyCode)){
			//���ؼ�����ʱ��ȥ��ѯ����
			Log.e("hehehehheheh", countyCode + "");
			publishText.setText("ͬ����...");
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			cityNameText.setVisibility(View.INVISIBLE);
			queryWeatherCode(countyCode);
		}else{
			/*
			 * û���ؼ����ž�ֱ����ʾ��������
			 * ���ڱ��ش���������Ϣ��ֱ��ȡ������ʾ
			 */
			Log.e("showshow", countyCode + "");
			showWeather();
		}
	}

	/*
	 * ��sharedpreferences�ļ��ж�ȡ �洢��������Ϣ������ʾ�ڽ�����
	 */
	private void showWeather() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		cityNameText.setText(prefs.getString("city_name", ""));
		temp1Text.setText(prefs.getString("temp1", ""));
		temp2Text.setText(prefs.getString("temp2", ""));
		weatherDespText.setText(prefs.getString("weather_desp", ""));
		publishText.setText("����" + prefs.getString("publish_time" + "����", ""));
		currentDataText.setText(prefs.getString("current_date", ""));
		weatherInfoLayout.setVisibility(View.VISIBLE);
		cityNameText.setVisibility(View.VISIBLE);
	}

	// ��ѯ�ؼ���������Ӧ����������
	private void queryWeatherCode(String countyCode) {
		String address = "http://www.weather.com.cn/data/list3/city"
				+ countyCode + ".xml";
		queryFromServer(address, "countyCode");
	}

	// ���ݴ���ĵ�ַ������ȥ���������ѯ�������Ż���������Ϣ
	private void queryFromServer(final String address,final String type) {
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			
			@Override
			public void onFinish(String response) {
				if ("countyCode".equals(type)){
					if (!TextUtils.isEmpty(response)){
						//�ӷ��������ص����ݽ�������������
						String[] array = response.split("\\|");
						if (array != null && array.length == 2){
							String weatherCode = array[1];
							Log.e("weatherCode", weatherCode + "");
							queryWeatherInfo(weatherCode);
						}
					}
				}else if ("weatherCode".equals(type)){
					//������������ص�������Ϣ
					Utility.handleWeatherResponse(WeatherActivity.this, response);
					//����UI�߳�ˢ�½���
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							showWeather();
						}
					});
				}
			}
			

			@Override
			public void onError(Exception e) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						publishText.setText("ͬ��ʧ��");
					}
				});
			}
		});
	}
	
	/*
	 * ��ѯ������������Ӧ������
	 * ����ԭ�ȵ�http://www.weather.com.cn/data/cityinfo/��Ϊ
	 *        http://www.weather.com.cn/adat/cityinfo/
	 */
	private void queryWeatherInfo(String weatherCode) {
		String address = "http://www.weather.com.cn/adat/cityinfo/"
						+ weatherCode + ".html";
		queryFromServer(address, "weatherCode");
	}
	
	
}
