 package com.MeadowEast.audiotest;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class Email extends Activity implements View.OnClickListener {

	
	EditText personsEmail;  //Declaring the message the user will write on the email	
	EditText Message;		 //Adding the email
	String emailAdd;          //Adding the email
	Button sendEmail;
	EditText Subject;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_email);
		initializeVars();
		sendEmail.setOnClickListener(this);
		
		
		
		
	}

	private void initializeVars() {
		// TODO Auto-generated method stub
		personsEmail = (EditText) findViewById(R.id.etEmails);
		
		Message = (EditText) findViewById(R.id.editTextMessage);
       
		sendEmail = (Button) findViewById(R.id.bSentEmail);
		Subject = (EditText) findViewById(R.id.editTextSubject);
		
	}
	private void setHanzi(String s){
		TextView t  = (TextView) findViewById(R.id.hanziTextView);
		t.setText(s);
	}

	public void onClick(View v) {
		// TODO Auto-generated method stub

		convertEditTextVarsIntoStrings();
		String emailaddress[] = { emailAdd };
		String message = "我们在进入一所新学校的时候，都要向别人介绍自己。";
		
	   // message = Message.getText().toString();	
		String subject = Subject.getText().toString();
				
				
		Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, emailaddress);
		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Clip");
	
		//The following line allowed me to attach a file to the mail from any place on phone/computer 
		emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File("/mnt/sdcard/Data/Meaddown.audiotest/files/clip/00100.mp3")));
		
		emailIntent.setType("plain/text");
		emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, message);
		
		startActivity(emailIntent);
		

	}

	private void convertEditTextVarsIntoStrings() {
		// TODO Auto-generated method stub
		emailAdd = personsEmail.getText().toString();
		
		
	}

	
	
	            


}