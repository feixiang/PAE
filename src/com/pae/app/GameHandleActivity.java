package com.pae.app;

import com.pae.PaeActivity;
import com.pae.R;
import com.pae.core.Config;
import com.pae.core.Connector;
import com.pae.core.PaeApplication;
import com.pae.util.Util;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 游戏手柄应用的Activity 对于不同用户，使用两套不同的配置让用户选择
 * 
 * @author Administrator
 * 
 */
public class GameHandleActivity extends Activity implements OnTouchListener,
		OnClickListener {

	private static final String TAG = "Bluetooth";
	/**
	 * 基本控件
	 */
	private ImageButton dir_left;
	private ImageButton dir_right;
	private ImageButton dir_up;
	private ImageButton dir_down;

	private ImageButton fun_circle; // 圆圈
	private ImageButton fun_triangle; // 三角形
	private ImageButton fun_x; // 叉
	private ImageButton fun_square; // 正方形

	private ImageButton fun_r1;
	private ImageButton fun_r2;
	private ImageButton fun_l1;
	private ImageButton fun_l2;

	private ImageButton fun_select;
	private ImageButton fun_start;

	private ImageButton fun_home; // 回到主界面，同时退出应用

	private TextView tips;

	/**
	 * 核心对象
	 */
	private Connector connector; // 连接方式接口，用来初始化具体的连接方式，在这里只是用来发送消息
	private PaeApplication application; // 全局对象，用此对象来传递socket对象

	private Vibrator vibrator; // 震动器
	// 震动模式，时间为毫秒,{1000,5000}表示等1秒，震5秒；另外vibrator.vibrate(pattern,
	// repeat);第二个参数为0表示重复，-1表示不重复
	private long[] pattern = { 1000, 5000 };

	// 重力感应器
	private SensorManager sensorMgr;
	private Sensor sensor;

	private String message = ""; // 发送的消息

	// handler用于传递消息给其他界面，这里没有用到
	private final Handler handler = new Handler();

	private boolean isLongPress = false;
	private LongPressThread thread;

	/*
	 * 初始化界面
	 */

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gamehandle);
		initComponents();
		// 从系统服务中获取震动器的服务
		vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

		/**
		 * 从全局变量中取出socket对象，以进行socket通信
		 */
		application = (PaeApplication) getApplication();
		connector = application.getConnector();

		// 先发送用户名
		connector.sendMessage("client|" + Config.CLIENT_NAME);

		/**
		 * 设置屏幕常亮
		 */
		this.getWindow().addFlags(
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		// 线程不断运行，监听长按事件
		thread = new LongPressThread();
		thread.start();
	}

	/**
	 * 初始化按钮
	 */
	public void initComponents() {
		dir_left = (ImageButton) findViewById(R.id.left);
		dir_left.setOnTouchListener(this);
		dir_right = (ImageButton) findViewById(R.id.right);
		dir_right.setOnTouchListener(this);
		dir_up = (ImageButton) findViewById(R.id.up);
		dir_up.setOnTouchListener(this);
		dir_down = (ImageButton) findViewById(R.id.down);
		dir_down.setOnTouchListener(this);

		fun_circle = (ImageButton) findViewById(R.id.circle);
		fun_circle.setOnTouchListener(this);
		fun_square = (ImageButton) findViewById(R.id.square);
		fun_square.setOnTouchListener(this);
		fun_triangle = (ImageButton) findViewById(R.id.triangle);
		fun_triangle.setOnTouchListener(this);
		fun_x = (ImageButton) findViewById(R.id.x);
		fun_x.setOnTouchListener(this);

		fun_l1 = (ImageButton) findViewById(R.id.l1);
		fun_l1.setOnTouchListener(this);
		fun_l2 = (ImageButton) findViewById(R.id.l2);
		fun_l2.setOnTouchListener(this);
		fun_r1 = (ImageButton) findViewById(R.id.r1);
		fun_r1.setOnTouchListener(this);
		fun_r2 = (ImageButton) findViewById(R.id.r2);
		fun_r2.setOnTouchListener(this);

		fun_home = (ImageButton) findViewById(R.id.home);
		fun_home.setOnClickListener(this);
		fun_start = (ImageButton) findViewById(R.id.start);
		fun_start.setOnClickListener(this);
		fun_select = (ImageButton) findViewById(R.id.select);
		fun_select.setOnClickListener(this);

		tips = (TextView) findViewById(R.id.gamehandletips);
	}

	// 以下三个函数结合起来处理按钮触摸事件，一直发送该按键消息
	public boolean onTouch(View v, MotionEvent event) {
		doButtonTouchEvent(v.getId(), event.getAction()
				& MotionEvent.ACTION_MASK);

		return true;
	}

	// 判断按钮
	private void doButtonTouchEvent(int buttonId, int action) {
		switch (buttonId) {
		case R.id.up:
			message = GameHandleConfig.DIR_UP;
			doButtonTouchAction(action, message);
			break;
		case R.id.down:
			message = GameHandleConfig.DIR_DOWN;
			doButtonTouchAction(action, message);
			break;
		case R.id.left:
			message = GameHandleConfig.DIR_LEFT;
			doButtonTouchAction(action, message);
			break;
		case R.id.right:
			message = GameHandleConfig.DIR_RIGHT;
			doButtonTouchAction(action, message);
			break;
		// 爆大绝的键要震动一下
		case R.id.circle:
			vibrator.vibrate(500);
			message = GameHandleConfig.FUN_CIRCLE;
			doButtonTouchAction(action, message);
			break;
		case R.id.square:
			message = GameHandleConfig.FUN_SQUARE;
			doButtonTouchAction(action, message);
			break;
		case R.id.triangle:
			message = GameHandleConfig.FUN_TRIANGLE;
			doButtonTouchAction(action, message);
			break;
		case R.id.x:
			message = GameHandleConfig.FUN_X;
			doButtonTouchAction(action, message);
			break;
		case R.id.l1:
			message = GameHandleConfig.FUN_L1;
			doButtonTouchAction(action, message);
			break;
		case R.id.l2:
			message = GameHandleConfig.FUN_L2;
			doButtonTouchAction(action, message);
			break;
		case R.id.r1:
			message = GameHandleConfig.FUN_R1;
			doButtonTouchAction(action, message);
			break;
		case R.id.r2:
			message = GameHandleConfig.FUN_R2;
			doButtonTouchAction(action, message);
			break;
		default:
			break;
		}
	}

	// 判断触摸事件
	private void doButtonTouchAction(int action, String msg) {
		message = msg;
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			// connector.sendMessage(message);
			isLongPress = true;
			break;
		case MotionEvent.ACTION_UP:
			// connector.sendMessage(message);
			isLongPress = false;
			break;
		default:
			break;
		}
	}

	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.home:
			backHome();
			break;
		case R.id.select:
			// 点击select开启重力感应
			openSensor();
			break;
		case R.id.start:
			// 点击select开启重力感应
			connector.sendMessage(GameHandleConfig.FUN_START);
			break;
		default:
			break;
		}
	}

	private void backHome() {
		finish();
	}

	private boolean isSensorOpen = false;
	private void openSensor() {
		if (!isSensorOpen) {
			// 获得加速传感器的实例，用于重力感应
			sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
			sensor = sensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			// 注册加速传感器，第3个参数为检测的精确度
			sensorMgr.registerListener(sensorlistener, sensor,
					SensorManager.SENSOR_DELAY_UI);//手柄反应灵敏度
			isSensorOpen = true;
			tips.setText("重力感应已开启");
		} else {
			sensorMgr.unregisterListener(sensorlistener);
			isSensorOpen = false;
			tips.setText("重力感应已关闭");
		}
	}

	protected void closeSensor() {
		if (isSensorOpen) {
			sensorMgr.unregisterListener(sensorlistener);
			isSensorOpen = false;
		}
	}

	/**
	 * 这里处理重力加速度的事件
	 */
	// 重力感应的判断条件
	private final static double minRight = 2;// 向右走的最小值，即手机抬起要令Y超过2 , 即y >2
	private final static double maxLeft = -2; // 向左走时，Y的最大值要小于-2，表示向左倾斜令y<-2
	private final static double minUp = 2; // 向前走时的最小值，可以不是正，因为这样舒服，即 -1 <x < 5
	private final static double minDown = 5; // 手机向前仰超过45度则表示向后走。即x>5
	private SensorEventListener sensorlistener = new SensorEventListener() {
		// 由于游戏手柄屏幕一直朝上（Z轴朝上，大于0），我们只需检测x轴与y轴的值
		public void onSensorChanged(SensorEvent event) {
			float x = event.values[SensorManager.DATA_X];
			float y = event.values[SensorManager.DATA_Y];
			// float z = event.values[SensorManager.DATA_Z];
			// 这里要发送组合键
			// 屏幕顶部被抬起时，前进，按下w , 此时 y > 0

			// 将复杂的检测放在前面，这样就不会因为条件太宽而导致后面的不能执行
			// 向前走时的最小值，可以不是正，因为这样舒服，即 -1 <x < 5
			if (x > minUp && x < minDown && y > minRight) // 手机上抬，且向右抬，同样表示向右走
				connector.sendMessage(GameHandleConfig.DIR_RIGHT);
			else if (x > minUp && x < minDown && y < maxLeft)// 手机上抬，且向左抬，同样表示向左走
				connector.sendMessage(GameHandleConfig.DIR_LEFT);
			else if (x > minUp && x < minDown)// 微上抬，不超过45度，则向前走
				connector.sendMessage(GameHandleConfig.DIR_UP);
			else if (x > minDown)// 向上抬超过45度表示向后走
				connector.sendMessage(GameHandleConfig.DIR_DOWN);
			else if (y > minRight) //
				connector.sendMessage(GameHandleConfig.DIR_RIGHT);
			else if (y < maxLeft)
				connector.sendMessage(GameHandleConfig.DIR_LEFT);
		}
		public void onAccuracyChanged(Sensor sensor, int accuracy) {

		}
	};

	@Override
	public void onBackPressed() {
		closeSensor();
	}

	@Override
	public void onAttachedToWindow() {
		this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);
		super.onAttachedToWindow();
	}

	@Override
	protected void onDestroy() {
		if (null != vibrator) {
			vibrator.cancel();
		}
		closeSensor();
		connector.closeConnection();
		super.onDestroy();
	}

	@Override
	protected void onStop() {
		if (null != vibrator) {
			vibrator.cancel();
		}
		closeSensor();
		connector.closeConnection();
		super.onStop();
	}

	@Override
	protected void onPause() {
		super.onPause();
		closeSensor();
	}

	// 添加一个内部线程类来处理按下键不松开事件
	class LongPressThread extends Thread {
		@Override
		public void run() {
			// 让线程不断运行
			while (true) {
				// 长按下某个按钮时，不断发送该消息
				if (isLongPress) {
					connector.sendMessage(message);
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

}
