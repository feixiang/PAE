package com.pae.core;

import java.util.UUID;

public class Config {

	/**
	 *  标准蓝牙串口服务SPP的uuid，勿改
	 */
	public static final UUID uuid = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");
	
	public final static String CLIENT_NAME = "PAECLIENT_2" ; 
	/**
	 * wifi连接时用到的信息
	 */
	public static final String SERVER_SSID = "192.168.2.1"; 
	public static final String SERVER_PASS = "PAESERVER" ;
	public static final int SERVER_PORT = 9090 ; 
	
	public static final String ABOUT_US = "开发者：林恒权、伍飞翔" ; 

	
}
