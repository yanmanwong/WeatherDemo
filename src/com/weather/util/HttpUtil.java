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
					connection = (HttpURLConnection) url.openConnection();//建立连接
					connection.setRequestMethod("GET");//设置请求方法
					connection.setConnectTimeout(8000);//设置连接超时时间
					connection.setReadTimeout(8000);//设置读取超时时间
					InputStream in = connection.getInputStream();//获取服务器数据（真正连接）
					BufferedReader reader = new BufferedReader(new InputStreamReader(in));
					StringBuilder response = new StringBuilder();
					String line;
					
					//将输入数据写到response
					while((line = reader.readLine()) != null){
						response.append(line);
					}
					//请求成功，将获取数据返回到调用sendHttpRequest中实现的接口中的onFinish
					if (listener != null){
						listener.onFinish(response.toString());
					}
				} catch (Exception e) {
					//请求失败，将获取数据返回到调用sendHttpRequest中实现的接口中的onError
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
