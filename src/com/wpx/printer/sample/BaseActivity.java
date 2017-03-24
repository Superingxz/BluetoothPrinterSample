package com.wpx.printer.sample;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;

public class BaseActivity extends Activity {
	protected ProgressDialog pd;
	protected Handler handler;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		handler = new Handler(getMainLooper());
		
		((MyApplication)getApplication()).addActivity(this);
	}
	
	protected void showSearchProgressDialog() {
		showProgressDialog(R.string.search_devices, R.string.search, false);
	}
	protected void showProgressDialog(int titleId, int msgId, boolean cancel) {
		showProgressDialog(getString(titleId), getString(msgId), cancel);
	}

	protected void showProgressDialog(String title, String msg, boolean cancel) {
		if (pd == null) {
			pd = ProgressDialog.show(this, title, msg);
		} else {
			pd.setTitle(title);
			pd.setMessage(msg);
		}

		pd.setCanceledOnTouchOutside(cancel);
		pd.setCancelable(cancel);
		
		if (!pd.isShowing()) {
			pd.show();
		}
	}
	
	protected void dismissProgressDialog(){
		if(pd != null){
			if (pd.isShowing()) {
				pd.dismiss();
				pd.cancel();
			}
		}
	}
}
