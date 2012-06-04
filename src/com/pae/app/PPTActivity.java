package com.pae.app;

import com.pae.R;
import com.pae.core.Config;
import com.pae.core.Connector;
import com.pae.core.KeyCode;
import com.pae.core.PaeApplication;

import android.app.Activity;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageButton;

/**
 * PPT的放映的基本快捷键
 * 播放：F5     从当前页播放退出：ESC    下一页 ： 左箭头     上一页： 右箭头       隐藏鼠标指针： Ctrl+H
 */
public class PPTActivity extends Activity {

	private static final String TAG = "PPT";

	// 初始化控件
	private ImageButton playBt;
	private ImageButton playCurrentBt;
	private ImageButton escBt;
	private ImageButton nextPageBt;
	private ImageButton forePageBt;
	private ImageButton homeBt ; 

	/**
	 * 核心对象
	 */
	private Connector connector; // 连接方式接口，用来初始化具体的连接方式，在这里只是用来发送消息
	private PaeApplication application; // 全局对象，用此对象来传递socket对象

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ppt);

		// 先发送用户名
		initComponents();

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
	}

	private void initComponents() {
		playBt = (ImageButton) findViewById(R.id.bt_play);
		playCurrentBt = (ImageButton) findViewById(R.id.bt_playCurrent);
		escBt = (ImageButton) findViewById(R.id.bt_esc);
		nextPageBt = (ImageButton) findViewById(R.id.bt_next);
		forePageBt = (ImageButton) findViewById(R.id.bt_fore);
		homeBt = (ImageButton) findViewById(R.id.ppt_home);
		
		playBt.setOnClickListener(listener);
		playCurrentBt.setOnClickListener(listener);
		escBt.setOnClickListener(listener);
		nextPageBt.setOnClickListener(listener);
		forePageBt.setOnClickListener(listener);
		homeBt.setOnClickListener(listener);
	}

	private OnClickListener listener = new OnClickListener() {

		public void onClick(View v) {
			int id = v.getId();
			switch (id) {
			case R.id.bt_play:
				connector.sendMessage(KeyCode.KEY_F5 + "");
				break;
			case R.id.bt_playCurrent:
				// 两个按键要用 “ + ”　号连接
				connector.sendMessage(KeyCode.KEY_SHIFT + "+" + KeyCode.KEY_F5);
				break;
			case R.id.bt_esc:
				connector.sendMessage(KeyCode.KEY_ESC + "");
				break;
			case R.id.bt_next:
				connector.sendMessage(KeyCode.KEY_RIGHT_ARROW + "");
				break;
			case R.id.bt_fore:
				connector.sendMessage(KeyCode.KEY_LEFT_ARROW + "");
				break;
			case R.id.ppt_home:
				backHome();
				break;
			default:
				break;
			}
		}
	};
	
	public void backHome()
	{
		finish();
	}
	/**
	 * 防止按错，屏蔽所有功能键
	 */
	@Override
	public void onBackPressed() {
		
	}
	
	
	//屏蔽手机上所有没有用到的按键
	@Override
	public void onAttachedToWindow() {
		this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);
		super.onAttachedToWindow();
	}
}
