package com.pae.core;

import android.widget.ArrayAdapter;

/*
 * 连接接口
 * 抽象蓝牙与wifi连接方式的共同方法
 */
public interface Connector {
	/*
	 * 最主要的功能：发送消息 如果发送不成功，不处理
	 */
	public abstract void sendMessage(String message);

	// 进行默认连接
	public abstract void doDefaultConnection();

	public abstract void openConnection(String address);

	public abstract void closeConnection();

	public abstract void reconnect();
	
	//退出时关闭设备
	public abstract void closeDevice();
}
