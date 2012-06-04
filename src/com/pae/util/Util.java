package com.pae.util;

import com.pae.view.LoadingDialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.view.Window;
import android.view.WindowManager;

/**
 * 工具类，收集常用的方法
 * 
 * @author Administrator
 * 
 */
public class Util {

	/**
	 * 获得服务器IP地址
	 
	public static String getLocalIPAddress(Context context) {
		WifiManager wifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		int ipAddress = wifiInfo.getIpAddress();
		return parseIPAddress(ipAddress);
	}
*/
	public static String getLocalGateWay(Context context) {
		WifiManager wifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();//连接以后，获得wifi信息
		int ipAddress = wifiInfo.getIpAddress();
		return parseGateWayAddress(ipAddress);
	}

	/**
	 * 
	 * @param intIp
	 *            格式是由每一位的2进制串起来的，共32位 通过不断右移
	 * @return
	 */
	public static String parseGateWayAddress(long intIp) {
		StringBuilder sb = new StringBuilder();
		sb.append(intIp & 0xFF).append(".");
		intIp = intIp >> 8;
		sb.append(intIp & 0xFF).append(".");
		intIp = intIp >> 8;
		sb.append(intIp & 0xFF).append(".");
		// 网关最后一位加1
		sb.append("1");
		return sb.toString();
	}

	/**
	 * 
	 * @param intIp
	 *            格式是由每一位的2进制串起来的，共32位 通过不断右移
	 * @return
	 * 
	 *         public static String parseIPAddress(long intIp) { StringBuilder
	 *         sb = new StringBuilder(); sb.append(intIp & 0xFF).append(".");
	 *         intIp = intIp >> 8; sb.append(intIp & 0xFF).append("."); intIp =
	 *         intIp >> 8; sb.append(intIp & 0xFF).append("."); intIp = intIp >>
	 *         8; sb.append(intIp & 0xFF).append("."); return sb.toString(); }
	 */
	/**
	 * 设置对话框的透明度
	 * 
	 * @param dialog
	 * @param alpha
	 */
	public static void setAlphaDialog(Dialog dialog, float alpha) {
		Window window = dialog.getWindow();
		WindowManager.LayoutParams lp = window.getAttributes();
		lp.alpha = alpha;
	}

	/**
	 * 设置屏幕常亮
	 */
	public static void setScreenOn(Activity activity) {
		activity.getWindow().addFlags(
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

}
