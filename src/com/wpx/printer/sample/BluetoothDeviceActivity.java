package com.wpx.printer.sample;

import java.io.FileNotFoundException;
import java.io.IOException;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.wpx.IBluetoothPrint;
import com.wpx.IBluetoothPrint.Describe;
import com.wpx.WPXMain;
import com.wpx.util.GeneralAttributes;
import com.wpx.util.WPXUtils;

public class BluetoothDeviceActivity extends BaseActivity implements OnClickListener {
	private BluetoothDevice device;
	private String address;
	private TextView tv_name, tv_conn;
	private Button btn_selecte_img, btn_img_2bw, btn_print_img,
			btn_print;
	private ImageView iv_source, iv_bw;
	private Spinner spinner;
	private EditText et_text;
	
	private Bitmap bitmap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_device);

		final boolean isInit;
		final Intent intent = getIntent();
		if (intent != null && intent.hasExtra("device")) {
			device = intent.getParcelableExtra("device");
			if (device != null) {
				final String name = device.getName();
				address = device.getAddress();
				// WPXMain.connectDevice(address);
				isInit = true;
			} else {
				isInit = false;
			}
		} else {
			isInit = false;
		}

		if (!isInit) {
			finish();
			return;
		}

		initView();
		setLinstener();

		tv_name.setText(device.getAddress());
		tv_conn.setText(R.string.connecting);
		
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				boolean isConntected = WPXMain.connectDevice(address);
				Toast.makeText(getApplicationContext(),
						isConntected ? R.string.connect_s : R.string.connect_f,
								Toast.LENGTH_SHORT).show();
				tv_name.setText(device.getName());
				tv_conn.setText(isConntected ? R.string.connect_s : R.string.connect_f);
				handler.removeCallbacks(this);
			}
		}, 500);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		WPXMain.disconnectDevice();
	}

	private int index = 0;
	private void initView() {
		tv_name = (TextView) findViewById(R.id.tv_name);
		tv_conn = (TextView) findViewById(R.id.tv_conn);
		btn_selecte_img = (Button) findViewById(R.id.btn_selecte_img);
		btn_img_2bw = (Button) findViewById(R.id.btn_img_2bw);
		btn_print_img = (Button) findViewById(R.id.btn_print_img);
		btn_print = (Button) findViewById(R.id.btn_print);
		spinner = (Spinner) findViewById(R.id.spinner);
		iv_source = (ImageView) findViewById(R.id.iv_source);
		iv_bw = (ImageView) findViewById(R.id.iv_bw);
		et_text = (EditText) findViewById(R.id.et_text);
	}

	private void setLinstener() {
		btn_selecte_img.setOnClickListener(this);
		btn_img_2bw.setOnClickListener(this);
		btn_print_img.setOnClickListener(this);
		btn_print.setOnClickListener(this);
		
		final String[] array = getResources().getStringArray(R.array.code);
		spinner.setAdapter(new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, array){
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				final View view =  super.getView(position, convertView, parent);
				if(view instanceof TextView){
					((TextView)view).setTextColor(Color.BLACK);
				}
				return view;
			}
		});
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				index = position;
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
	}

	@Override
	public void onClick(View v) {
		final int id = v.getId();
		switch (id) {
		case R.id.btn_selecte_img:
			final Intent intent = new Intent(
					Intent.ACTION_PICK,
					android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			startActivityForResult(intent, IMAGE_SELECT);
			break;
		case R.id.btn_img_2bw:
			final AsyncTask<Void, Void, Bitmap> at = new AsyncTask<Void, Void, Bitmap>(){
				@Override
				protected void onPreExecute() {
					super.onPreExecute();
					showProgressDialog(getString(R.string.img_2_bw), null, false);
				}
				@Override
				protected Bitmap doInBackground(Void... params) {
					iv_source.setDrawingCacheEnabled(true);
					Bitmap bmp = iv_source.getDrawingCache();
//					Bitmap.createBitmap(iv_source.getDrawingCache());
					bmp = WPXUtils.convertBlackWhite(bmp);
					return bmp;
				}
				@Override
				protected void onPostExecute(Bitmap result) {
					super.onPostExecute(result);
					iv_bw.setImageBitmap(result);
					iv_source.setDrawingCacheEnabled(false);
					dismissProgressDialog();
				}
			};
			at.execute();
			break;
		case R.id.btn_print_img:
			try {
				final IBluetoothPrint imgbp = WPXMain.getBluetoothPrint();
				final byte gravity;
				if(index == 0){
					gravity = IBluetoothPrint.GRAVITY_LEFT;
				}else if(index == 1){
					gravity = IBluetoothPrint.GRAVITY_LEFT;
				}else if(index == 2){
					gravity = IBluetoothPrint.GRAVITY_CENTER;
				}else if(index == 3){
					gravity = IBluetoothPrint.GRAVITY_RIGHT;
				}else if(index == 4){
					gravity = IBluetoothPrint.GRAVITY_LEFT;
				}else{
					gravity = IBluetoothPrint.GRAVITY_LEFT;
				}
				final Describe des = new Describe();
				des.setGravity(gravity);
				iv_bw.setDrawingCacheEnabled(true);
				final Bitmap bitmap = iv_bw.getDrawingCache();
				imgbp.printBitmap(bitmap, des);
				iv_bw.setDrawingCacheEnabled(false);
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		case R.id.btn_print:
			final String text = et_text.getText().toString().trim();
			if(!TextUtils.isEmpty(text)){
				try {
					final IBluetoothPrint bp = WPXMain.getBluetoothPrint();
					if(index == 0){
						WPXMain.printCommand(GeneralAttributes.INSTRUCTIONS_ESC_INIT);
					}else if(index == 1){
						WPXMain.printCommand(GeneralAttributes.INSTRUCTIONS_GS_CHARACTER_SIZE_DEFUALT);
						WPXMain.printCommand(GeneralAttributes.INSTRUCTIONS_ESC_ALIGN_LEFT);
					}else if(index == 2){
						WPXMain.printCommand(GeneralAttributes.INSTRUCTIONS_GS_CHARACTER_SIZE_DEFUALT);
						WPXMain.printCommand(GeneralAttributes.INSTRUCTIONS_ESC_ALIGN_CENTER);
					}else if(index == 3){
						WPXMain.printCommand(GeneralAttributes.INSTRUCTIONS_GS_CHARACTER_SIZE_DEFUALT);
						WPXMain.printCommand(GeneralAttributes.INSTRUCTIONS_ESC_ALIGN_RIGHT);
					}else if(index == 4){
						WPXMain.printCommand(GeneralAttributes.INSTRUCTIONS_GS_CHARACTER_DOUBLE_SIZE);
					}else{
						WPXMain.printCommand(GeneralAttributes.INSTRUCTIONS_ESC_INIT);
					}
					bp.printText(text);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			break;
		default:
			break;
		}
	}
	
	private static final int IMAGE_SELECT = 101;
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			if (requestCode == IMAGE_SELECT) {
				final Uri uri = data.getData();
				try {
					final Bitmap bmp = MediaStore.Images.Media.getBitmap(
							this.getContentResolver(), uri);
					final int w = bmp.getWidth();
					final int h = bmp.getHeight();
					final float f = (w <= 384 ) ? 1 : 1f * 384 / w;
					final Bitmap bitmap = zoomBitmap(bmp, f, f);
					iv_source.setImageBitmap(bitmap);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				Toast.makeText(getApplicationContext(), "已保存图片地址", Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	public static Bitmap zoomBitmap(Bitmap bmp, float wFloat, float hFloat) {
		final int w = bmp.getWidth();
		final int h = bmp.getHeight();
		final Matrix matrix = new Matrix();
		matrix.postScale(wFloat, hFloat); // 长和宽放大缩小的比例
		
		final Bitmap bp = Bitmap.createBitmap(bmp, 0, 0, w, h, matrix, true);

		/**去除透明度*/ 
		// 获取这个图片的宽和高
		int ww = bp.getWidth();
		int hh = bp.getHeight();
		final Bitmap targetBmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);  
		final Canvas targetCanvas = new Canvas(targetBmp);
		targetCanvas.drawColor(0xffffffff);
		targetCanvas.drawBitmap(bp, new Rect(0, 0, ww, hh), new Rect(0, 0, ww, hh), null);
		
		return bp;
	}
	
}
