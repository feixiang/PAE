package com.pae.core;

/**
 * Wifi连接的核心类，负责局域网的UDP连接
 * 通过Connector接口来初始化一个UDP连接
 */
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import com.pae.PaeActivity;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

public class WifiConnector implements Connector {

	private static final String TAG = "BluetoothCore";// 输出debug信息
	private ConnectThread connectThread = null;
	private ConnectedThread connectedThread = null; // 管理已经连接的线程
	private final Handler handler;
	
	private String serverIp = "" ; 

	public WifiConnector(Context context, Handler handler) {
		this.handler = handler;
	}

	/**
	 * 调用已连接线程的UDP socket发送消息，统一用sendMessage发送
	 */
	public void sendMessage(String message) {
		ConnectedThread t;
		synchronized (this) {
			t = connectedThread;
		}
		t.write(message);
	}

	public void openConnection(String address) {
		/**
		 * 这里用户已经在系统中连接了Wifi，直接进行局域网通信
		 * 参数address是服务器端的IP地址，这里没有用到
		 * 服务器的IP地址与端口号只有在建立数据的时候才有用
		 */
		serverIp = address ; 
		connectThread = new ConnectThread();
		connectThread.start();
		Log.d(TAG, "正在连接中");
	}

	/**
	 * 使用UDP通信，不会出现粘包现象 
	 * 这里检测连接成功并没有用，因为UDP是无连接方式的，即它只会向指定主机的指定端口发送数据 而不会与主机建立连接
	 */
	class ConnectThread extends Thread {
		// 用于udp连接的socket
		private DatagramSocket das = null;

		public void run() {
			try {
				Log.d(TAG, "正在连接中");
				das = new DatagramSocket();
				// 向主界面发送连接成功的消息
				connectedThread = new ConnectedThread(das);
				connectedThread.start();
				Log.d(TAG, "连接成功");
				sendMessageToUI(PaeActivity.CONNECTION_SUCCESS);
			} catch (Exception e) {
				// 向主界面发送连接失败的消息
				Log.d(TAG, "连接失败");
				sendMessageToUI(PaeActivity.CONNECTION_FAIL);
			}
		}
		public void cancel() {
			try {
				das.close();
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
	}

	/**
	 * 管理已连接的udp socket , 用来发送udp数据 核心语句：建立数据报，然后发送 datagramPacket = new
	 * DatagramPacket(bytes[], length,hostIp, port);
	 * DatagramSocket.send(datagramPacket);
	 */
	class ConnectedThread extends Thread {
		// 使用UDP连接
		private DatagramSocket das; // UDP协议的Socket
		private DatagramPacket p; // UDP数据报
		private InetAddress ipAddress; // 主机的IP地址

		public ConnectedThread(DatagramSocket sc) {
			das = sc;
			try {
				// 这里是将IP地址转换成固定的InetAddress格式
				ipAddress = InetAddress.getByName(serverIp);
			} catch (UnknownHostException e) {
				// 如果IP地址格式错误，则报错
				e.printStackTrace();
			}
		}

		public void write(String msg) {
			int msg_length = msg.length();
			byte[] messageByte = msg.getBytes();
			/**
			 * 下面是建立一个UDP数据报 原型是：DatagramPacket(byte[] data, int length,
			 * InetAddress host, int port) data是指要发送的数据，转换成字节流 length指数据的长度
			 * host指要接收端的IP地址 port用来指定接收端应用程序的端口号，服务器端会监听此端口，从而接收消息
			 */
			p = new DatagramPacket(messageByte, msg_length, ipAddress,
					Config.SERVER_PORT);
			try {
				das.send(p);
			} catch (IOException e) {
				// 根据UDP的特点，发送消息失败，不进行任何处理
			}
		}

		public void cancel() {
			try {
				das.close();
			} catch (Exception e) {
				// 服务器端不能与客户端断开连接，只能客户端自己关闭自己
			}

		}
	}
	
	/**
	 * 在必要的时候向主界面发送消息
	 * @param msg
	 */
	public void sendMessageToUI(int msg) {
		handler.obtainMessage(msg).sendToTarget();
	}
	/**
	 * 关闭所有已经存在的线程
	 */
	private void closeExistedConnection() {
		if (connectThread != null) {
			connectThread.cancel();
			connectedThread = null;
		}
		if (connectedThread != null) {
			connectedThread.cancel();
			connectedThread = null;
		}
	}
	public void closeConnection() {
		closeExistedConnection();
	}
	/**
	 * 下面几个都是继承接口的函数，不需要用到
	 */
	public void doDefaultConnection() {

	}
	public void reconnect() {

	}

	public void closeDevice() {
	}

}
