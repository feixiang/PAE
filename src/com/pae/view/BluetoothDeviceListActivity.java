/**
 * 
 */
package com.pae.view;

import java.util.Set;

import com.pae.R;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

/**
 * @author Administrator
 * 
 */
public class BluetoothDeviceListActivity extends Activity {

	private static final String TAG = "Bluetooth";// 控制台输出
	// Return Intent extra
	public static String EXTRA_DEVICE_ADDRESS = "device_address";

	private BluetoothAdapter btAdapter;

	// 已匹配设备列表
	private ArrayAdapter<String> pairedDevicesArrayAdapter;
	// 新发现的设备列表
	private ArrayAdapter<String> newDevicesArrayAdapter;

	private LoadingDialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		// 显示设备列表窗口
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.device_list);

		setResult(Activity.RESULT_CANCELED);

		// 获取本地蓝牙设备
		btAdapter = BluetoothAdapter.getDefaultAdapter();

		if (btAdapter == null) {
			Toast.makeText(this, R.string.no_bluetooth, Toast.LENGTH_SHORT);
			return;
		}
		if (!btAdapter.isEnabled())
			btAdapter.enable();
		// 注册监听器
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		this.registerReceiver(receiver, filter);
		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		this.registerReceiver(receiver, filter);

		// 通过findViewById找到activity中的控件
		Button scanButton = (Button) findViewById(R.id.button_scan);
		scanButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				doScan();
				// 隐藏扫描按钮，防止再按
				v.setVisibility(View.GONE);
			}
		});

		pairedDevicesArrayAdapter = new ArrayAdapter<String>(this,
				R.layout.device_name);
		newDevicesArrayAdapter = new ArrayAdapter<String>(this,
				R.layout.device_name);

		ListView pairedList = (ListView) findViewById(R.id.paired_devices);
		pairedList.setAdapter(pairedDevicesArrayAdapter);
		pairedList.setOnItemClickListener(deviceClickListener);

		ListView newDeviceList = (ListView) findViewById(R.id.new_devices);
		newDeviceList.setAdapter(newDevicesArrayAdapter);
		newDeviceList.setOnItemClickListener(deviceClickListener);

		// 初始化已匹配列表
		/**
		 * 在执行device discovery之前，最好在已配对的设备列表中查看所要发现的设备是否已经存在。
		 * 通过调用getBondedDevices()函数可以获得代表已经配对的设备的BluetoothDevice集合
		 */
		Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();

		if (pairedDevices.size() > 0) {
			findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
			for (BluetoothDevice device : pairedDevices) {
				pairedDevicesArrayAdapter.add(device.getName() + "\n"
						+ device.getAddress());
			}
		} else {
			findViewById(R.id.title_paired_devices).setVisibility(View.GONE);
		}
	}
     //成员变量 ，继承接口类，初始化一个实例
	private final BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				newDevicesArrayAdapter.add(device.getName() + "\n"
						+ device.getAddress());
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
					.equals(action)) {
				// setProgressBarIndeterminateVisibility(false);
				progressDialog.cancel();
				setTitle(R.string.select_device);
				if (newDevicesArrayAdapter.getCount() == 0) {
					Toast.makeText(BluetoothDeviceListActivity.this,
							"对不起，没有找到新设备", Toast.LENGTH_LONG).show();
				}
			}
		}
	};

	private OnItemClickListener deviceClickListener = new OnItemClickListener() {

		public void onItemClick(AdapterView<?> arg0, View v, int arg2, long arg3) {
			btAdapter.cancelDiscovery();
			// Get the device MAC address, which is the last 17 chars in the
			String info = ((TextView) v).getText().toString();
			String address = info.substring(info.length() - 17);
			// 建立一个消息传送器，将蓝牙设备地址发送回主界面
			Intent intent = new Intent();
			intent.putExtra(EXTRA_DEVICE_ADDRESS, address);

			// 关闭这个窗口
			setResult(Activity.RESULT_OK, intent);
			finish();
		}
	};

	public void doScan() {
		// 清空原来的列表
		newDevicesArrayAdapter.clear();
		// 设置进度条
		// setProgressBarIndeterminateVisibility(true);
		setTitle(R.string.select_device);

		findViewById(R.id.title_new_devices);

		if (btAdapter.isDiscovering()) {
			btAdapter.cancelDiscovery();
		}
		// 开始扫描
		showLoadingDialog();

		btAdapter.startDiscovery();
	}

	private void showLoadingDialog() {
		progressDialog = new LoadingDialog(this, "正在搜索设备，请稍后...");
		progressDialog.show();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (btAdapter != null) {
			btAdapter.cancelDiscovery();
		}
		// Unregister broadcast listeners
		this.unregisterReceiver(receiver);
	}
     //////////////???????????????????????????????????
	//////////////////////////////////////////////////
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		// 建立一个消息传送器，将蓝牙设备地址发送回主界面
		Intent intent = new Intent();
		// 关闭这个窗口
		setResult(Activity.RESULT_CANCELED, intent);
		finish();
	}

}
