package com.pae.view;

import com.pae.R;

import android.app.AlertDialog;
import android.content.Context;

public class AboutDialog extends AlertDialog {

	private String title = "关于我们" ; 
	private String content = "关于我们的内容" ; 
	
	
	public AboutDialog(Context context) {
		super(context);
		
		this.setTitle(title) ; 
		this.setMessage(content);
		setContentView(R.layout.aboutus);
		// TODO Auto-generated constructor stub
	}
}
