package com.pae.app;

import com.pae.core.KeyCode;

/**
 * 游戏手柄的配置文件
 * 可以做成读取文件的方式 这里只是简单地定义相应的字符串，这些字符串是和服务器端协商好的
 * 
 * @author FEI
 * 
 */
public class GameHandleConfig {

	public static String CLIENT_NAME = "GameHandler";
	public static String CMD_PRE = "cmd|";

	public static String DIR_UP = "" + KeyCode.KEY_W;
	public static String DIR_DOWN = "" + KeyCode.KEY_S;
	public static String DIR_LEFT = "" + KeyCode.KEY_A;
	public static String DIR_RIGHT = "" + KeyCode.KEY_D;
	
	public static String FUN_TRIANGLE = "" + KeyCode.KEY_I ; 
	public static String FUN_SQUARE = "" + KeyCode.KEY_J ; 
	public static String FUN_CIRCLE = "" + KeyCode.KEY_L ; 
	public static String FUN_X = "" + KeyCode.KEY_K ; 
	public static String FUN_L1 = "" + KeyCode.KEY_SHIFT ; 
	public static String FUN_L2 = "" + KeyCode.KEY_XIEGANG ; 
	public static String FUN_R1 = "" + KeyCode.KEY_O ; 
	public static String FUN_R2 = "" + KeyCode.KEY_DIAN ; 
	
	public static String FUN_START = "" + KeyCode.KEY_F1 ; 
	

}
