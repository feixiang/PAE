package com.pae;

import com.pae.app.AppConfig;
import com.pae.app.GameHandleActivity;
import com.pae.app.PPTActivity;
import com.pae.core.BluetoothConnector;
import com.pae.core.Config;
import com.pae.core.Connector;
import com.pae.core.PaeApplication;
import com.pae.core.WifiConnector;
import com.pae.util.Util;
import com.pae.view.AboutDialog;
import com.pae.view.BluetoothDeviceListActivity;
import com.pae.view.LoadingDialog;
import com.pae.view.WifiDeviceListActivity;

import android.R.anim;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class PaeActivity extends Activity implements OnClickListener {

	private static final String TAG = "MAIN_ACTIVITY";

	/**
	 * 全局变量，用来传递复杂对象
	 */
	private PaeApplication application;
	/**
	 * 所有应用的按钮
	 */
	private ImageButton gameHandleBt;
	private ImageButton pptBt;
	private ImageButton movieBt;
	private ImageButton musicBt;
	private ImageButton painterBt;
	private ImageButton penBt;

	// 设置当前的应用ID，点击某个应用的按钮时就会被设置为该应用的编号，编号在AppConfig类中有定义
	private int selectedAppID;

	/**
	 * 核心类连接工厂
	 */
	private Connector connector;

	/**
	 * 请求号request Code ，用于对应返回的数据处理
	 */
	public static final int REQUEST_BLUETOOTH_DEIVCE_LIST = 1;
	public static final int REQUEST_WIFI_DEIVCE_LIST = 2;
	/**
	 * handler的事件处理号,随便定义，区分各事件即可
	 */
	public static final int DEFAULT_CONNECTION_FAIL = 0;
	public static final int WIFI_DEFAULT_CONNECTION_FAIL = 5;
	public static final int CONNECTION_FAIL = 1;
	public static final int CONNECTION_LOST = 2;
	public static final int CONNECTION_SUCCESS = 3;
	public final static int REQUEST_DEVICE_LIST = 4;
	public final static int REQUEST_WIFI_SETTING = 5;
	/**
	 * 等待对话框
	 */
	public LoadingDialog loadingDialog;

	/** Activity 入口 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// 所以Activity都得加这两句
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		/**
		 * 获得全局变量的实例，在后面便可以对application类的get,set方法进行操作
		 */
		application = (PaeApplication) getApplication();
		/**
		 * 初始化控件
		 */
		initComponents();
	}

	// 初始化控件
	private void initComponents() {
		gameHandleBt = (ImageButton) findViewById(R.id.bt_gamehandle);
		gameHandleBt.setOnClickListener(this);
		pptBt = (ImageButton) findViewById(R.id.bt_ppt);
		pptBt.setOnClickListener(this);
		movieBt = (ImageButton) findViewById(R.id.bt_movie);
		movieBt.setOnClickListener(this);
		musicBt = (ImageButton) findViewById(R.id.bt_music);
		musicBt.setOnClickListener(this);
		painterBt = (ImageButton) findViewById(R.id.bt_painter);
		painterBt.setOnClickListener(this);
		penBt = (ImageButton) findViewById(R.id.bt_writer);
		penBt.setOnClickListener(this);
	}

	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.bt_gamehandle:
			selectedAppID = AppConfig.GAMEHANDLE;
			initChoiceDialog();
			break;
		case R.id.bt_ppt:
			selectedAppID = AppConfig.PPT;
			initChoiceDialog();// 弹出选择方式对话框
			break;

		default:
			application.showToastShort("正在开发中，敬请期待");
			break;
		}
	}

	/**
	 * 连接方式选择对话框
	 */
	public void initChoiceDialog() {
		AlertDialog dialog = new AlertDialog.Builder(this)
				.setTitle(R.string.connection_choice_title)
				.setIcon(R.drawable.connectway)
				// 这里设置单选列表按钮
				.setSingleChoiceItems(AppConfig.CHOICE, 0, chooseDialogListener)
				.setNegativeButton("取消", null).create();// 创建选择方式对话框
		// 设置对话框透明度
		Util.setAlphaDialog(dialog, (float) 0.6);
		dialog.show();
	}

	/**
	 * 上面选择对话框的监听器
	 */
	private DialogInterface.OnClickListener chooseDialogListener = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			// which由0开始，代表各个选项，下面的选项是自己定义的
			case AppConfig.BT_CONNECTION:
				dialog.dismiss();// 取消对话框
				showLoadingDialog("正在进行自动连接，请稍后...");// 马上进行默认连接
				autoBluetoothConnection();
				break;
			case AppConfig.WIFI_CONNECTION:
				dialog.dismiss();
				showWifiSetting();
				break;
			default:
				break;
			}
		}
	};

	public void showWifiSetting() {
		startActivityForResult(new Intent(Settings.ACTION_WIFI_SETTINGS),
				REQUEST_WIFI_SETTING);
	}

	/**
	 * 进行WIFI连接，即直接进行局域网连接， 需要用户先连接好Wifi
	 */
	public void makeWifiConnection() {

		String address = Util.getLocalGateWay(this);
		application.showToastShort("address: " + address);
		// 这里设置连接方式
		connector = new WifiConnector(this, handler);
		connector.openConnection(address);
	}

	/**
	 * 显示蓝牙列表后，将用户选择的蓝牙地址传回来，在OnActivityForResult中接收，并调用该方法
	 * 
	 * @param data
	 *            存放数据的intent
	 */
	protected void makeBluetoothConnection(Intent data) {
		// 接收传回的蓝牙地址
		Log.d(TAG, "正在进行蓝牙连接");
		String address = data.getExtras().getString(
				BluetoothDeviceListActivity.EXTRA_DEVICE_ADDRESS);
		// 这里设置连接方式
		connector = new BluetoothConnector(this, handler);// 创建实例对象，实例化
		connector.openConnection(address);
	}

	/**
	 * 进行自动连接
	 */
	public void autoBluetoothConnection() {
		// 这里设置连接方式
		connector = new BluetoothConnector(this, handler);
		connector.doDefaultConnection();
	}

	/**
	 * 自动连接失败后，显示设备列表
	 */
	protected void showBlueToothDeviceList() {
		Intent intent = new Intent(this, BluetoothDeviceListActivity.class);
		startActivityForResult(intent, REQUEST_BLUETOOTH_DEIVCE_LIST);
	}

	/**
	 * 手机底部菜单按钮
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.option_menu, menu);
		return true;
	}

	/**
	 * 上面菜单的子选项事件
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.option_exit:
			// 退出程序
			finish();
			break;
		case R.id.option_about:
			// 弹出关于对话框
			showAboutDialog();
			break;
		default:
			break;
		}
		return false;
	}

	private void showAboutDialog() {
		AlertDialog dialog = new AlertDialog.Builder(this)
				.setTitle(R.string.about)
				// 这里设置单选列表按钮
				.setMessage(Config.ABOUT_US)
				.setIcon(android.R.drawable.sym_action_chat)
				.setNegativeButton("确定", null).create();// 创建选择方式对话框
		// 设置对话框透明度
		Util.setAlphaDialog(dialog, (float) 0.6);
		dialog.show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		// 接收选择对话框的选择，判断是蓝牙还是WiFi连接
		showLoadingDialog("正在连接中，请稍后...");
		switch (requestCode) {
		case REQUEST_BLUETOOTH_DEIVCE_LIST:
			if (resultCode == Activity.RESULT_OK) {
				makeBluetoothConnection(intent);
			} else {
				loadingDialog.cancel();
			}
			break;
		case REQUEST_WIFI_SETTING:
			if (resultCode == Activity.RESULT_CANCELED) {
				loadingDialog.cancel();
				makeWifiConnection();
			}
		default:
			// loadingDialog.cancel();
			break;
		}
	}

	/**
	 * 显示等待对话框
	 */
	private void showLoadingDialog(String msg) {
		loadingDialog = new LoadingDialog(this, msg);// 实例化对象
		loadingDialog.show();
	}

	/**
	 * 实时接收连接器传送回来的消息
	 */
	private final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// 不管收到什么消息，都要先把loadingDialog给取消
			loadingDialog.cancel();
			switch (msg.what) {
			// 默认连接失败，显示蓝牙列表，让用户选择蓝牙设备
			case DEFAULT_CONNECTION_FAIL:
				// 取消等待对话框
				application.showToastShort("查找默认服务器失败，正在加载设备列表");
				showBlueToothDeviceList();
				break;
			case CONNECTION_FAIL:
				application.showToastShort("连接失败，请重新尝试");
				showBlueToothDeviceList();
				break;
			case CONNECTION_LOST:
				loadingDialog.cancel();
				application.showToastShort("连接丢失，即将重新连接");
				// connector.reconnect();
				break;
			// 连接成功后启动应用界面
			case CONNECTION_SUCCESS:
				loadingDialog.cancel();
				startApp();
				// startGameHandle();
				break;
			case WIFI_DEFAULT_CONNECTION_FAIL:
				application.showToastShort("查找默认服务器失败");
				break;
			default:
				break;
			}
		}
	};

	/**
	 * 在点击某个应用的按钮时会将selectedAppID设置为该应用的ID， 然后这里根据selectedAppID启动指定应用
	 */
	private void startApp() {
		Log.d("selected APPID", selectedAppID + "");
		// 先将核心的连接类设置好
		application.setConnector(connector);// 就是把所有连接做好才跳转

		Intent intent = new Intent();
		switch (selectedAppID) {
		case AppConfig.GAMEHANDLE:
			intent.setClass(this, GameHandleActivity.class);
			break;
		case AppConfig.PPT:
			intent.setClass(this, PPTActivity.class);
			break;
		default:
			break;
		}
		startActivity(intent); // 使用当前界面启动另一个界面
		// finish();
	}

	/**
	 * 调用finish()时执行
	 */
	@Override
	protected void onDestroy() {
		if (connector != null) {
			// connector.closeDevice();
		}
		super.onDestroy();
		System.exit(0);
	}

	/**
	 * 屏幕关闭时调用
	 */
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	/**
	 * 屏幕开启时调用
	 */
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	/**
	 * 按下手机返回键时的操作
	 */
	@Override
	public void onBackPressed() {
		finish();
		super.onBackPressed();
	}

}