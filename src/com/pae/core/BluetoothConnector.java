package com.pae.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

import com.pae.PaeActivity;
import com.pae.R;
import com.pae.app.AppConfig;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

/**
 * 蓝牙核心类： --> 封装蓝牙的基本操作 蓝牙基本步骤：1, 获取本地蓝牙设备 ; 2, 连接设备 ；3 ,传输数据 -->
 * 由接口openConnection(address) 进行连接，通过获取传入的蓝牙MAC地址使用 PS: 查找远程设备
 * 配对将在BtDeviceListActivity中实现
 * 
 * 连接成功与否的消息由handler发送到主界面
 * 
 * @author Administrator
 */
public class BluetoothConnector implements Connector {

	// 输出debug信息
	private static final String TAG = "BluetoothCore";
	// 本机蓝牙适配器
	private final BluetoothAdapter btAdapter;

	// 已匹配设备列表，用来进行默认连接时找匹配列表
	private Set<BluetoothDevice> pairedDevices;

	/**
	 * 蓝牙的状态常量，用来判断蓝牙状态，以进行相应操作
	 */
	private static final int BT_NONE = 0; // 空闲状态
	private static final int BT_CONNECTING = 1; // 正在连接中
	private static final int BT_CONNECTED = 2; // 已连接
	private int state; // 当前状态

	// 蓝牙连接线程
	private ConnectThread connectThread;
	// 管理已连接进程
	private ConnectedThread connectedThread;

	// 处理不同线程消息句柄
	private final Handler handler;

	private BluetoothDevice serverDevice = null;
	// 保存当前连接的设备，以备连接丢失时重新连接
	private BluetoothDevice connectedDevice = null;

	public BluetoothConnector(Context context, Handler handler) {

		// 用handler来传递线程间的消息
		this.handler = handler;
		// 获取本地蓝牙设备
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		// 先打开蓝牙
		openBtWithoutRequest();
		// 取得已匹配设备列表
		try {
			pairedDevices = btAdapter.getBondedDevices();
			// 设置连接状态
			state = BT_NONE;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void openBtWithoutRequest() {
		// 直接打开蓝牙
		if (!btAdapter.isEnabled())
		{
			btAdapter.enable();
			//等待蓝牙开启
//			try {
//    			Thread.sleep(2000);
//    		} catch (InterruptedException e) {
//    			// nothing
//    		}
		}
	}

	public void disableBluetooth() {
		if (btAdapter.isEnabled()) {
			btAdapter.disable();
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// nothing
			}
		}
	}

	/**
	 * 从已匹配设备中查找默认服务器PAESERVER
	 */
	public BluetoothDevice getServerFromBondedList() {

		if (pairedDevices.size() == 0)
			return null;
		else {
			for (BluetoothDevice device : pairedDevices) {
				if (device.getName().equals(AppConfig.SERVERNAME)) {
					return device;
				}
			}
			return null;
		}
	}

	/**
	 * 进行默认连接，查找服务器名称，找到服务器的蓝牙设备 如果连接失败则用Handler传回消息，显示设备列表让用户选择
	 */
	public void doDefaultConnection() {
		openBtWithoutRequest();
		/**
		 * 只在已匹配列表中查找
		 */
		if ((serverDevice = getServerFromBondedList()) != null) {
			connectDevice(serverDevice);
		} else {
			handler.obtainMessage(PaeActivity.DEFAULT_CONNECTION_FAIL)
					.sendToTarget();
		}

	}

	/**
	 * 启动线程进行连接 连环调用 通过connectThread连接成功后，启动connectedThread管理连接成功的socket
	 * 
	 * @param device
	 */
	private void connectDevice(BluetoothDevice device) {

		ConnectThread thread = new ConnectThread(device);
		thread.start();
		Log.d(TAG, "正在连接中");
		setState(BT_CONNECTING);
	}

	/**
	 * 核心方法，得到MAC地址进行连接 连接成功与否通过handler传递消息给主界面
	 */
	public void openConnection(String address) {

		// 先打开蓝牙
		openBtWithoutRequest();
		Log.d(TAG, "服务器地址是：address:" + address);
		// 再通过MAC地址连接服务器
		BluetoothDevice device = btAdapter.getRemoteDevice(address);
		connectedDevice = device; // 保存已连接的对象，以便reconnect

		connectDevice(device);
	}

	public void sendMessage(String message) {
		// 先判断是否已连接，在进行任何操作基于蓝牙的操作前都要检查
		if (state != BT_CONNECTED) {
			return;
		}
		if (message.length() > 0) {
			byte[] ms = message.getBytes();
			write(ms);
		}
	}

	public void write(byte[] bytes) {
		ConnectedThread t;
		synchronized (this) {
			if (state != BT_CONNECTED)
				return;
			t = connectedThread;
		}
		t.write(bytes);
	}

	public void closeConnection() {
		// 释放线程
		closeExistedConnection();
	}

	/**
	 * 连接丢失后重连
	 */
	public void reconnect() {
		if (BT_NONE == state) {
			connectDevice(connectedDevice);
		}
	}

	// -----一下面使用线程来连接指定的蓝牙设备（内部类）----//
	private class ConnectThread extends Thread {
		private BluetoothDevice device;
		private BluetoothSocket socket;

		public ConnectThread(BluetoothDevice btDevice) {

			device = btDevice;
			BluetoothSocket tmp = null;

			try {
				// 建立带UUID的蓝牙连接
				tmp = device.createRfcommSocketToServiceRecord(Config.uuid);
			} catch (IOException e) {
				connectionFail();
			}
			socket = tmp;
		}

		@Override
		public void run() {
			try {
				Log.d(TAG, "开始进行连接");
				socket.connect();
				/**
				 * 连接成功后，管理这个socket通信
				 */
				manageConnection(socket);

			} catch (IOException e) {
				Log.d(TAG, "连接失败");
				// 通过handler发送消息到UI界面显示相关消息
				connectionFail();
				try {
					socket.close();
				} catch (IOException e1) {
				}

			}
			synchronized (BluetoothConnector.this) {
				connectThread = null;
			}
		}

		public void cancel() {
			try {
				socket.close();
			} catch (IOException e) {
			}
		}
	}

	private class ConnectedThread extends Thread {
		private BluetoothSocket socket = null;
		private InputStream is = null;
		private OutputStream os = null; // 只需要写消息即可

		public ConnectedThread(BluetoothSocket msocket) {
			socket = msocket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			is = tmpIn;
			os = tmpOut;
		}

		@Override
		public void run() {
			// 由于命令将短，所以字符串缓冲区不需要太大
			byte[] buffer = new byte[512];
			// 记录接收了多少个字符
			int bytes;
			while (true) {
				try {
					// Read from the InputStream
					bytes = is.read(buffer);
					Log.d(TAG, "读取的消息 ：" + buffer);

				} catch (IOException e) {
					Log.e(TAG, "disconnected", e);
					connectionLost();
					break;
				}
			}
		}

		public void write(byte[] buffer) {
			try {
				os.write(buffer);
				Log.d(TAG, "消息：" + buffer + " 发送成功");
			} catch (IOException e) {
				e.printStackTrace();
				Log.e(TAG, "消息：" + buffer + " 发送失败");
			}
		}

		public void cancel() {
			try {
				socket.close();
			} catch (IOException e) {
				Log.e(TAG, "close() of connect socket failed", e);
			}
		}

	}

	/**
	 * 管理已连接的Socket进行通信
	 * 
	 * @param socket
	 */
	public void manageConnection(BluetoothSocket socket) {

		closeExistedConnection();

		connectedThread = new ConnectedThread(socket);
		connectedThread.start();
		/**
		 * 发送连接成功消息给主界面
		 */
		connectionSuccess();
	}

	/**
	 * 下面三个方法通过handler向主界面发送连接信息：连接成功，连接失败与连接丢失
	 */
	public void connectionFail() {
		Log.e(TAG, "连接失败");
		setState(BT_NONE);
		handler.obtainMessage(PaeActivity.CONNECTION_FAIL).sendToTarget();
	}

	public void connectionLost() {
		Log.e(TAG, "连接丢失");
		setState(BT_NONE);
		handler.obtainMessage(PaeActivity.CONNECTION_LOST).sendToTarget();
	}

	public void connectionSuccess() {
		Log.d(TAG, "连接成功");
		setState(BT_CONNECTED);
		handler.obtainMessage(PaeActivity.CONNECTION_SUCCESS).sendToTarget();
	}

	/**
	 * 用synchronized对state进行加锁，防止多个线程同时修改状态
	 * 
	 * @param mstate
	 */
	private synchronized void setState(int mstate) {
		state = mstate;
	}

	private synchronized int getState() {
		return state;
	}

	/**
	 * 关闭所有已经存在的线程
	 */
	private void closeExistedConnection() {
		if (connectedThread != null) {
			connectedThread.cancel();
			connectedThread = null;
		}
		if (connectThread != null) {
			connectThread.cancel();
			connectThread = null;
		}
		setState(BT_NONE);
	}

	public void closeDevice() {
		// 关闭蓝牙
		disableBluetooth();
	}

}
