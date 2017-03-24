package com.wpx.printer.sample;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.wpx.WPXMain;
import com.wpx.WPXMain.SearchCallBack;
import com.wpx.util.WPXUtils;

public class MainActivity extends BaseActivity implements OnClickListener,
		OnItemClickListener, SearchCallBack {
	private Button btn_exit, btn_openBluetooth, btn_searchDevices;
	private ListView lv_unbondDevices, lv_bondDevices;
	private SimpleAdapter unbondAdapter, bondAdapter;
	private List<BluetoothDevice> bondDevices = new ArrayList<BluetoothDevice>();
	private List<BluetoothDevice> unBondDevices = new ArrayList<BluetoothDevice>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(R.string.app_name);
		setContentView(R.layout.activity_main);
		
		WPXMain.addSerachDeviceCallBack(this);

		initView();
		setListener();

//		handler.postDelayed(new Runnable() {
//			@Override
//			public void run() {
//				searchDevices();
//			}
//		}, 500);
	}

	/**
	 * 初始化view
	 */
	private void initView() {
		btn_exit = (Button) findViewById(R.id.btn_exit);
		btn_openBluetooth = (Button) findViewById(R.id.btn_openBluetooth);
		btn_searchDevices = (Button) findViewById(R.id.btn_searchDevices);

		lv_unbondDevices = (ListView) findViewById(R.id.lv_unbondDevices);
		lv_bondDevices = (ListView) findViewById(R.id.lv_bondDevices);
		
		resetStateView();
		resetBondDevice();
	}
	/**
	 * 添加事件监听器
	 */
	private void setListener() {
		btn_exit.setOnClickListener(this);
		btn_openBluetooth.setOnClickListener(this);
		btn_searchDevices.setOnClickListener(this);

		lv_unbondDevices.setOnItemClickListener(this);
		lv_bondDevices.setOnItemClickListener(this);
	}
	/**
	 * 重置打开/关闭蓝牙按钮
	 */
	private void resetStateView(){
		btn_openBluetooth.setText(WPXMain.isBluetoothEnabled() ? R.string.close_bluetooth : R.string.open_bluetooth);
	}
	/**
	 * 重置绑定蓝牙设备到列表
	 */
	private void resetBondDevice(){
		final Set<BluetoothDevice> set =  WPXMain.getBondedDevices();
		loadBondDevices(set);
	}
	/**
	 * 重置未绑定蓝牙设备到列表
	 */
	private void resetUnBondDevice(){
		final HashMap<String, BluetoothDevice> map = WPXMain.getSearchUnBondDevices();
		loadUnbondDevices(map.values());
	}
	/**
	 * 开始搜索蓝牙设备
	 */
	private void searchDevices(){
		if(WPXMain.isBluetoothEnabled()){
			showSearchProgressDialog();
			WPXMain.startSearchDevices();
		}else{
			WPXMain.openBluetooth();
			Toast.makeText(getApplicationContext(), R.string.bluetooth_not_open, Toast.LENGTH_SHORT).show();
		}
	}

	private final String[] from = new String[] { "name", "address" };
	private final int[] to = new int[] { R.id.tv_device_name,
			R.id.tv_device_address };
	/**
	 * 加载蓝牙设备到未绑定列表
	 * @param devices
	 */
	private void loadUnbondDevices(final Collection<BluetoothDevice> devices) {
		unBondDevices.clear();
		unBondDevices.addAll(devices);
		unbondAdapter = getSimpleAdapter(unBondDevices);
		lv_unbondDevices.setAdapter(unbondAdapter);
	}
	/**
	 * 加载蓝牙设备到绑定列表
	 * @param devices
	 */
	private void loadBondDevices(final Collection<BluetoothDevice> devices) {
		bondDevices.clear();
		bondDevices.addAll(devices);
		bondAdapter = getSimpleAdapter(bondDevices);
		lv_bondDevices.setAdapter(bondAdapter);
	}

	private SimpleAdapter getSimpleAdapter(
			final Collection<BluetoothDevice> devices) {
		final List<Map<String, String>> data = new ArrayList<Map<String, String>>();
		for (final BluetoothDevice device : devices) {
			final Map<String, String> map = new HashMap<String, String>();
			map.put("name", device.getName());
			map.put("address", device.getAddress());
			data.add(map);
		}
		final SimpleAdapter adapter = new SimpleAdapter(
				this, data, R.layout.layout_device_item,
				from, to);
		return adapter;
	}

	// 屏蔽返回键的代码:
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, final int position,
			long id) {
		final int parentId = parent.getId();
		switch (parentId) {
		case R.id.lv_unbondDevices:
			final BluetoothDevice device = unBondDevices.get(position);
			WPXUtils.bondDevice(device);
			handler.postDelayed(new Runnable(){
				private int count = 0;
				@Override
				public void run() {
					if(count++ >= 5){
						handler.removeCallbacks(this);
					}else{
						if(device.getBondState() == BluetoothDevice.BOND_BONDED){
							unBondDevices.remove(position);
						}
						handler.postDelayed(this, 1000);
					}
				}
			}, 1000);
			
			break;
		case R.id.lv_bondDevices:
			final BluetoothDevice bondDevice = bondDevices.get(position);
			final Intent intent = new Intent(MainActivity.this, BluetoothDeviceActivity.class);
			intent.putExtra("device", bondDevice);
			startActivity(intent);
			System.out.println("name="+bondDevice.getName()+", add="+bondDevice.getAddress());
			break;
		default:
			break;
		}
	}

	@Override
	public void onClick(View v) {
		final int id = v.getId();
		switch (id) {
		case R.id.btn_exit:
			exit();
			break;
		case R.id.btn_openBluetooth:
			if(WPXMain.isBluetoothEnabled()){
				WPXMain.closeBluetooth();
			}else{
				WPXMain.openBluetooth();
			}
			break;
		case R.id.btn_searchDevices:
			searchDevices();
			break;
		default:
			break;
		}
	}
	
	private void exit(){
		moveTaskToBack(true);
		finish();
		
		((MyApplication)getApplication()).exit();
	}

	@Override
	public void onStateChange() {
		resetStateView();
	}

	@Override
	public void searching(BluetoothDevice arg0) {
	}

	@Override
	public void startSearch() {
	}

	@Override
	public void stopSearch() {
		dismissProgressDialog();
		resetUnBondDevice();
	}
}
