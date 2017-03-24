package com.wpx.printer.sample;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Application;

import com.wpx.WPXMain;

public class MyApplication extends Application {
	private List<Activity> list = new ArrayList<Activity>();
	public void addActivity(Activity activity){
	}
	public void clearActivity(){
		for(Activity activity : list){
			if(activity != null && !activity.isFinishing()){
				activity.finish();
			}
		}
	}
	@Override
	public void onCreate() {
		super.onCreate();
		// 初始化蓝牙适配器
		WPXMain.init(this);
	}
	public void exit() {
		clearActivity();
		
		System.exit(0);
		android.os.Process.killProcess(android.os.Process.myPid());
	}
}
