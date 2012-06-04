package com.pae.core;

import java.net.Socket;

import com.pae.view.LoadingDialog;

import android.app.Application;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Toast;

/**
 * 全局变量类，用来传递复杂对象 需要在AndroidMainfest.xml中添加相应的application
 * 
 * @author Administrator
 * 
 */
public class PaeApplication extends Application {

	/**
	 * 全局socket变量
	 */
	public Connector connector;

	private static PaeApplication paeApplication;


	public static PaeApplication getInstance() {
		return paeApplication;
	}
	/**
	 * 设置连接工厂
	 */
	public Connector getConnector() {
		return connector;
	}

	public void setConnector(Connector connector) {
		this.connector = connector;
	}


	@Override
	public Context getApplicationContext() {
		// TODO Auto-generated method stub
		return super.getApplicationContext();
	}
	
	/**
	 * 工具类，通用Toast
	 * @param msg
	 */
	//PaeActivity Handler
	public void showToastShort(String msg) {
		Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
	}
	
	public void showToastLong(String msg) {
		Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
	}
	/**
	 * 通用加载进度条
	 */
	public LoadingDialog loadingDialog;
	public void showLoadingDialog(String msg) {
		loadingDialog = new LoadingDialog(getApplicationContext(), msg);
		loadingDialog.show();
	}
	public void cancelLoadingDialog()
	{
		if( loadingDialog!=null )
			loadingDialog.cancel();
	}

}
