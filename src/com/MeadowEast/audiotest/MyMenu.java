package com.MeadowEast.audiotest;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class MyMenu extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_main);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		super.onCreateOptionsMenu(menu);
		MenuInflater NightMode = getMenuInflater();
		NightMode.inflate(R.menu.menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch (item.getItemId()){
		case R.id.actioNIGHT:
			startActivity(new Intent("com.MeadowEast.audiotest.MYMENU"));
			return true;
		}
		return false;
	}
	public class DialogActivity extends Activity {
	    @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
	        alertDialog.setTitle(" ");
	        alertDialog.setMessage("");
	        //alertDialog.setIcon(R.drawable.icon);
	        alertDialog.setButton("Accept", new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int which) {                
	            }
	        });
	        alertDialog.setButton2("Deny", new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int which) {
	            }
	        });
	        alertDialog.show();
	    }
	}
}
