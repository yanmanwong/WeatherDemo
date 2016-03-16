package com.weather.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class HttpUtil {
	public static void sendHttpRequest(final String address,
			final HttpCallbackListener listener){
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				HttpURLConnection connection = null;
				try {
					URL url = new URL(address);
					connection = (HttpURLConnection) url.openConnection();//��������
					connection.setRequestMethod("GET");//�������󷽷�
					connection.setConnectTimeout(8000);//�������ӳ�ʱʱ��
					connection.setReadTimeout(8000);//���ö�ȡ��ʱʱ��
					InputStream in = connection.getInputStream();//��ȡ���������ݣ��������ӣ�
					BufferedReader reader = new BufferedReader(new InputStreamReader(in));
					StringBuilder response = new StringBuilder();
					String line;
					
					//����������д��response
					while((line = reader.readLine()) != null){
						response.append(line);
					}
					//����ɹ�������ȡ���ݷ��ص�����sendHttpRequest��ʵ�ֵĽӿ��е�onFinish
					if (listener != null){
						listener.onFinish(response.toString());
					}
				} catch (Exception e) {
					//����ʧ�ܣ�����ȡ���ݷ��ص�����sendHttpRequest��ʵ�ֵĽӿ��е�onError
					if (listener != null){
						listener.onError(e);
					}
					e.printStackTrace();
				}finally{
					if (connection != null){
						connection.disconnect();
					}
				}
			}
		}).start();
	}
}
