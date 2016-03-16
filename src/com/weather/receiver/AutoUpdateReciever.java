package com.weather.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.weather.service.AutoUpdateService;

public class AutoUpdateReciever extends BroadcastReceiver {

	//广播接收后开启服务
	@Override
	public void onReceive(Context context, Intent intent) {
		Intent in = new Intent(context, AutoUpdateService.class);
		context.startService(intent);
	}

}
