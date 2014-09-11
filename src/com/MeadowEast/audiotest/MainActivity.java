package com.MeadowEast.audiotest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.gesture.Gesture;
import android.gesture.GestureOverlayView;
import android.gesture.GestureOverlayView.OnGestureListener;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class MainActivity extends Activity implements OnClickListener, OnLongClickListener, OnGesturePerformedListener, OnGestureListener {
	String emailAdd;          
	String hAZI;
	Button sendEmail;
	EditText Subject;
	private int mThemeId = -1;
	private MediaPlayer mp;
	private String[] cliplist;
	private File sample;
	private File mainDir;
	private File clipDir;
	private Random rnd;
	private Handler clockHandler;
	private Runnable updateTimeTask;
	private boolean clockRunning;
	private boolean clockWasRunning;
	private Long elapsedMillis;
	private Long start;
	private Map<String, String> hanzi;  // needed for setting hanzi to email, as required by professor 
	private Map<String, String> instructions;
	private String key;	
	static final String TAG = "CAT";
	
	//------------Adds On -------------------------
//	private int mThemeId = -1;
	private boolean didUpdateClips = false;
	public int language = 0; //0 for Chinese, 1 for English
	ProgressDialog mProgressDialog;
	//File currClip;
	 /*------------Add on 2--------------------- */
	// not needed , now implemented on settings
		private CheckBox mCheckBox1;
		private Button button1;
		private boolean nightModeOn = true;
		
		private GestureDetector gesture1;
	//---------------------------------------------
	//----****---------------------***-----------/
	/*
	 *		

	private void emailClip(){
		if(sample!=null){
			Intent i = new Intent(Intent.ACTION_SEND);
			i.setType("vnd.android.cursor.dir/email");
			i.setType("Audio/mp3");
		//	i.putExtra(Intent.EXTRA_EMAIL, new String[]{email_add});
			i.putExtra(Intent.EXTRA_SUBJECT, "TienShao Clip");
			i.putExtra(Intent.EXTRA_TEXT   , hanzi.get(key));
			i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(sample.getAbsolutePath())));
			try {
			    startActivity(Intent.createChooser(i, "Send mail..."));
			} catch (android.content.ActivityNotFoundException ex) {
			    Toast.makeText(MainActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
			}
			
			
		//	return true;
			
			
	//		Intent intent = new Intent(this, EmailActivity.class);
		//	intent.putExtra(CLIP_ATT, sample.getName());
	//		intent.putExtra(hanzi_ATT, hanzi.get(key));
	//		startActivity(intent);
		}
	}
	
	
	private void setLanguage(int savedLanguage){
		 Resources res = getResources();
         DisplayMetrics dm = res.getDisplayMetrics();
         Configuration conf = res.getConfiguration();
         if(savedLanguage==0){
        	 conf.locale = Locale.getDefault();
         }else {conf.locale = Locale.UK;}
         res.updateConfiguration(conf, dm); 
   
	} 
	 * */	
	private void readClipInfo(){
		hanzi = new HashMap<String, String>();
		instructions = new HashMap<String, String>();
		File file =  new File(mainDir, "clipinfo.txt");
		Log.d(TAG, "before");
		Log.d(TAG, "after");
		try {
			FileReader fr = new FileReader ( file );
			BufferedReader in = new BufferedReader( fr );
			String line;
			while ((line = in.readLine(  )) != null){
				String fixedline = new String(line.getBytes(), "utf-8");
				String [] fields = fixedline.split("\\t");
				if (fields.length == 3){
					hanzi.put(fields[0], fields[1]);
					instructions.put(fields[0], fields[2]);
				} else {
					Log.d(TAG, "Bad line: "+fields.length+" elements");
					Log.d(TAG, fixedline);
				}
			}
			in.close();
		}
		catch ( Exception e ) {
			Log.d(TAG, "Problem reading clipinfo");
		}
	}
	
	private String getInstruction(String key){
		String instructionCodes = instructions.get(key);
		int n = instructionCodes.length();
		if (n == 0){
			return "No instruction codes for " + key;
		}
		int index = rnd.nextInt(n);
		switch (instructionCodes.charAt(index)){
		case 'C':
			return "continue the conversation";
		case 'A':
			return "answer the question";
		case 'R':
			return "repeat";
		case 'P':
			return "paraphrase";
		case 'Q':
			return "ask questions";
		case 'V':
			return "create variations";
		default:
			return "Bad instruction code " + instructionCodes.charAt(index) + " for " + key;
		}
	}
	
	private void toggleClock(){
		if (clockRunning){
			elapsedMillis += System.currentTimeMillis() - start;
			setHanzi("");
		}
		else
			start = System.currentTimeMillis();			
		clockRunning = !clockRunning;
		clockHandler.removeCallbacks(updateTimeTask);
		if (clockRunning) clockHandler.postDelayed(updateTimeTask, 200);
	}
	
	private void showTime(Long totalMillis){
		int seconds = (int) (totalMillis / 1000);
		int minutes = seconds / 60;
		seconds     = seconds % 60;
		TextView t = (TextView) findViewById(R.id.timerTextView);
		if (seconds < 10)
			t.setText("" + minutes + ":0" + seconds);
		else
			t.setText("" + minutes + ":" + seconds);		
	}
	
	private void createUpdateTimeTask(){
        updateTimeTask = new Runnable() {
        	public void run() {
        		Long totalMillis = elapsedMillis + System.currentTimeMillis() - start;
        		showTime(totalMillis);
        		clockHandler.postDelayed(this, 1000);
        	}
        };
	}
	
	private String setHanzi(String s){
		TextView t  = (TextView) findViewById(R.id.hanziTextView);
		t.setText(s);
		return s;
	}
	
/////////////////////// ONCREATE() //////////////////////////////////////////
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null && savedInstanceState.getInt("theme", -1) != -1) {
            mThemeId = savedInstanceState.getInt("theme");
            this.setTheme(mThemeId);
        }
        /*if(savedInstanceState!=null){                  // save old status
			didUpdateClips = savedInstanceState.getBoolean("update");
			language = savedInstanceState.getInt("newLanguage");	
		}*/

        Log.d(TAG, "testing only");
        // File filesDir = getFilesDir();  // Use on virtual device
        
        File sdCard = Environment.getExternalStorageDirectory();
               
        mainDir = new File (sdCard.getAbsolutePath() + "/Android/Data/com.MeadowEast.audiotest/files");
        
        // old
        //File filesDir = new File (sdCard.getAbsolutePath() + "/Android/data/com.MeadowEast.audiotest/files");      
        //mainDir = new File(filesDir, "ChineseAudioTrainer");
        
        clipDir = new File(mainDir, "clips");
        cliplist = clipDir.list();
        readClipInfo();
        rnd = new Random();
        //--------------------
        /*
         * mProgressDialog = new ProgressDialog(MainActivity.this);
		mProgressDialog.setMessage("Downloading Updated Clips");
		mProgressDialog.setIndeterminate(true);
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		mProgressDialog.setCancelable(true);
         * */
        //-------------------
        
        setContentView(R.layout.activity_main);   //HERE
        
        findViewById(R.id.playButton).setOnClickListener(this);
        findViewById(R.id.repeatButton).setOnClickListener(this);
        findViewById(R.id.hanziButton).setOnClickListener(this);
        findViewById(R.id.timerTextView).setOnClickListener(this);
        findViewById(R.id.hanziTextView).setOnLongClickListener(this);
        
        //-------------------------------------
        
        
        
        
        
        
        //------------------------------------		//--------------------------NightMode()--------------------------------
		// not needed, it should be done from setting, But I will keep it anyway
	 
        mCheckBox1 =(CheckBox) findViewById(R.id.CheckBox1);
        
        // if checked then do the following
        mCheckBox1.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(nightModeOn){
				findViewById(R.id.LinearLayout1).setBackgroundColor(getResources().getColor(R.color.black));
				findViewById(R.id.CheckBox1).setBackgroundColor(getResources().getColor(R.color.darkGray));
				((TextView) findViewById(R.id.CheckBox1)).setTextColor(getResources().getColor(R.color.lightGray));
				findViewById(R.id.playButton).setBackgroundColor(getResources().getColor(R.color.darkGray));
				((TextView) findViewById(R.id.playButton)).setTextColor(getResources().getColor(R.color.lightGray));
				findViewById(R.id.hanziButton).setBackgroundColor(getResources().getColor(R.color.darkGray));
				((TextView) findViewById(R.id.hanziButton)).setTextColor(getResources().getColor(R.color.lightGray));
				findViewById(R.id.repeatButton).setBackgroundColor(getResources().getColor(R.color.darkGray));
				((TextView) findViewById(R.id.repeatButton)).setTextColor(getResources().getColor(R.color.lightGray));
				findViewById(R.id.pauseButton).setBackgroundColor(getResources().getColor(R.color.darkGray));
				((TextView) findViewById(R.id.pauseButton)).setTextColor(getResources().getColor(R.color.lightGray));
				((TextView) findViewById(R.id.timerTextView)).setTextColor(getResources().getColor(R.color.darkOrange));
				((TextView) findViewById(R.id.hanziTextView)).setTextColor(getResources().getColor(R.color.mediumGray));
				((TextView) findViewById(R.id.instructionTextView)).setTextColor(getResources().getColor(R.color.mediumGray));
				nightModeOn=false;
				}
				else{
					findViewById(R.id.LinearLayout1).setBackgroundColor(getResources().getColor(R.color.white));
					findViewById(R.id.CheckBox1).setBackgroundColor(getResources().getColor(R.color.lightGray));
					((TextView) findViewById(R.id.CheckBox1)).setTextColor(getResources().getColor(R.color.black));
					findViewById(R.id.playButton).setBackgroundColor(getResources().getColor(R.color.lightGray));
					((TextView) findViewById(R.id.playButton)).setTextColor(getResources().getColor(R.color.black));
					findViewById(R.id.hanziButton).setBackgroundColor(getResources().getColor(R.color.lightGray));
					((TextView) findViewById(R.id.hanziButton)).setTextColor(getResources().getColor(R.color.black));
					findViewById(R.id.repeatButton).setBackgroundColor(getResources().getColor(R.color.lightGray));
					((TextView) findViewById(R.id.repeatButton)).setTextColor(getResources().getColor(R.color.black));
					findViewById(R.id.pauseButton).setBackgroundColor(getResources().getColor(R.color.lightGray));
					((TextView) findViewById(R.id.pauseButton)).setTextColor(getResources().getColor(R.color.black));
					((TextView) findViewById(R.id.timerTextView)).setTextColor(getResources().getColor(R.color.black));
					((TextView) findViewById(R.id.hanziTextView)).setTextColor(getResources().getColor(R.color.black));
					((TextView) findViewById(R.id.instructionTextView)).setTextColor(getResources().getColor(R.color.black));
				nightModeOn=true;
				}
			}
		});  // end of setOnClickListner for night mode check box
        
		//-------------------End NightMode() Using a check box----------------------------
		
		
		
		
		
      //-------------Setting The Pause Button on click listener and its definition		
        clockHandler = new Handler();
        start = System.currentTimeMillis();
        elapsedMillis = 0L;
        clockRunning = false;
        createUpdateTimeTask();
        findViewById(R.id.pauseButton).setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		toggleClock();
        	}
        });
        if (savedInstanceState != null){
        	elapsedMillis = savedInstanceState.getLong("elapsedMillis");
        	Log.d(TAG, "elapsedMillis restored to"+elapsedMillis);
        	key = savedInstanceState.getString("key");
        	String sampleName = savedInstanceState.getString("sample");
        	if (sampleName.length() > 0)
        		sample = new File(clipDir, sampleName);
        	if (savedInstanceState.getBoolean("running"))
        		toggleClock();
        	else 
        		showTime(elapsedMillis);
        	Log.d(TAG, "About to restore instruction");
        	String instruction = savedInstanceState.getString("instruction");
        	if (instruction.length() > 0){
        		Log.d(TAG, "Restoring instruction value of "+instruction);
    			TextView t  = (TextView) findViewById(R.id.instructionTextView);
    			t.setText(instruction);
        	}
        }
    }
    
    @Override
	public void onPause(){
    	super.onPause();
    	Log.d(TAG, "!!!! onPause is being run");
    	clockWasRunning = clockRunning;
    	if (clockRunning) toggleClock();
    }
  //---------------End of ofPause() and time toggle--------------------------
	
	
  	//--------------Saving Instances States------------------------------------

    
    @Override
	public void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);
    	String sampleName = "";
    	if (sample != null) sampleName = sample.getName();
    	outState.putString("sample", sampleName);
    	// onPause has stopped the clock if it was running, so we just save elapsedMillis
    	outState.putLong("elapsedMillis", elapsedMillis);
    	TextView t  = (TextView) findViewById(R.id.instructionTextView);
		outState.putString("instruction", t.getText().toString());
		outState.putString("key", key);
		outState.putBoolean("running", clockWasRunning);
       //-------------------------------
		outState.putInt("theme", mThemeId);
		outState.putBoolean("update",didUpdateClips);
		outState.putInt("newLanguage", language);
    
    }
    
    public void reset(){
    	TextView t;
		if (clockRunning) toggleClock();
		start = 0L;
		elapsedMillis = 0L;
		sample = null;
		t = (TextView) findViewById(R.id.timerTextView);
		t.setText("0:00");
		setHanzi("");
		t  = (TextView) findViewById(R.id.instructionTextView);
		t.setText("");
    }
    
    /*--- onLongClick(View)-----------------------
     * Now it is set to toast Clip Key and toggle clock --> pause like----------- */
    
    public boolean onLongClick(View v){
    	switch (v.getId()){
    	case R.id.hanziTextView:
    		Toast.makeText(this, "Clip: "+key+ "paused", Toast.LENGTH_LONG).show();
    		Log.d(TAG, "Long clicked");
    		toggleClock();      // pause function
    	//	break;	
    	}
    	return true;
    }
    
      //=============================Night Mode Function Definition, will be used on settings====================================================
	 public void turnOnNightMode(MenuItem item){
	    	if(nightModeOn){
				findViewById(R.id.LinearLayout1).setBackgroundColor(getResources().getColor(R.color.black));
				findViewById(R.id.CheckBox1).setBackgroundColor(getResources().getColor(R.color.darkGray));
				((TextView) findViewById(R.id.CheckBox1)).setTextColor(getResources().getColor(R.color.lightGray));
				findViewById(R.id.playButton).setBackgroundColor(getResources().getColor(R.color.darkGray));
				((TextView) findViewById(R.id.playButton)).setTextColor(getResources().getColor(R.color.lightGray));
				findViewById(R.id.hanziButton).setBackgroundColor(getResources().getColor(R.color.darkGray));
				((TextView) findViewById(R.id.hanziButton)).setTextColor(getResources().getColor(R.color.lightGray));
				findViewById(R.id.repeatButton).setBackgroundColor(getResources().getColor(R.color.darkGray));
				((TextView) findViewById(R.id.repeatButton)).setTextColor(getResources().getColor(R.color.lightGray));
				findViewById(R.id.pauseButton).setBackgroundColor(getResources().getColor(R.color.darkGray));
				((TextView) findViewById(R.id.pauseButton)).setTextColor(getResources().getColor(R.color.lightGray));
				((TextView) findViewById(R.id.timerTextView)).setTextColor(getResources().getColor(R.color.darkOrange));
				((TextView) findViewById(R.id.hanziTextView)).setTextColor(getResources().getColor(R.color.mediumGray));
				((TextView) findViewById(R.id.instructionTextView)).setTextColor(getResources().getColor(R.color.mediumGray));
				nightModeOn=false;
				String msg = " Night Mode On";
				
				Toast.makeText(this,msg,Toast.LENGTH_LONG).show();
				}
	    	
				else
				
				{
					findViewById(R.id.LinearLayout1).setBackgroundColor(getResources().getColor(R.color.white));
					findViewById(R.id.CheckBox1).setBackgroundColor(getResources().getColor(R.color.lightGray));
					((TextView) findViewById(R.id.CheckBox1)).setTextColor(getResources().getColor(R.color.black));
					findViewById(R.id.playButton).setBackgroundColor(getResources().getColor(R.color.lightGray));
					((TextView) findViewById(R.id.playButton)).setTextColor(getResources().getColor(R.color.black));
					findViewById(R.id.hanziButton).setBackgroundColor(getResources().getColor(R.color.lightGray));
					((TextView) findViewById(R.id.hanziButton)).setTextColor(getResources().getColor(R.color.black));
					findViewById(R.id.repeatButton).setBackgroundColor(getResources().getColor(R.color.lightGray));
					((TextView) findViewById(R.id.repeatButton)).setTextColor(getResources().getColor(R.color.black));
					findViewById(R.id.pauseButton).setBackgroundColor(getResources().getColor(R.color.lightGray));
					((TextView) findViewById(R.id.pauseButton)).setTextColor(getResources().getColor(R.color.black));
					((TextView) findViewById(R.id.timerTextView)).setTextColor(getResources().getColor(R.color.black));
					((TextView) findViewById(R.id.hanziTextView)).setTextColor(getResources().getColor(R.color.black));
					((TextView) findViewById(R.id.instructionTextView)).setTextColor(getResources().getColor(R.color.black));
				   nightModeOn=true;
				
				String msg = " Night Mode Off";
				Toast.makeText(this,msg,Toast.LENGTH_LONG).show();
				}
			}
	    	
	    
	//====================End of Night Mode()=====================================
	
     
   
    
    
    public void onClick(View v){
    	switch (v.getId()){
    	case R.id.playButton:
    		Integer index = rnd.nextInt(cliplist.length);
    		sample = new File(clipDir, cliplist[index]);
			key = sample.getName();
			key = key.substring(0, key.length()-4);
			TextView t  = (TextView) findViewById(R.id.instructionTextView);
			t.setText(getInstruction(key));
			
			//TextView viewM = (TextView) findViewById(R.id.hanziTextView);
			 //setHanzi(findViewById(R.id.hanziTextView));
			
			 

    	case R.id.repeatButton:
    		if (!clockRunning) toggleClock();
    		if (sample != null){
    			setHanzi("");
    			if (mp != null){
    				mp.stop();
    				mp.release();
    			}
    			mp = new MediaPlayer();
    			mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
    			try {
    				mp.setDataSource(getApplicationContext(), Uri.fromFile(sample));
    				mp.prepare();
    				mp.start();
    			} catch (Exception e) {
    				Log.d(TAG, "Couldn't get mp3 file");
    			}
    		}
    		break;
    	case R.id.hanziButton:
    		
    		if (!clockRunning) toggleClock();
    		if (sample != null) setHanzi(hanzi.get(key)); // Should add default value: error message if no hanzi for key
            break;
    	case R.id.hanziTextView:
    		/*	Integer index2 = rnd.nextInt(cliplist.length);
    		sample = new File(clipDir, cliplist[index2]);
			key = sample.getName();
			key = key.substring(0, key.length()-4);
			TextView t2  = (TextView) findViewById(R.id.instructionTextView);
			t2.setText(getInstruction(key));
            */
    		
    		if (!clockRunning) toggleClock();
    		if (sample != null) setHanzi(hanzi.get(key)); // Should add default value: error message if no hanzi for key
    		break;
    		
    		
    	case R.id.timerTextView:
            new AlertDialog.Builder(this)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle(R.string.reset)
            .setMessage(R.string.reallyReset)
            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    MainActivity.this.reset();    
                }
            })
            .setNegativeButton(R.string.no, null)
            .show();
            break;            
    	}
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK){
            Log.d(TAG, "llkj");
            new AlertDialog.Builder(this)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle(R.string.quit)
            .setMessage(R.string.reallyQuit)
            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) { 
                    MainActivity.this.finish();    
                }
            })
            .setNegativeButton(R.string.no, null)
            .show();
            return true;
        } else {
        	return super.onKeyDown(keyCode, event);
        }
    }
    
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		super.onCreateOptionsMenu(menu);
		MenuInflater NightMode = getMenuInflater();
		NightMode.inflate(R.menu.my_menu, menu);
		
		return true;
	}
	/*
	//------------------------
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.main, menu);
		//return true;
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return super.onCreateOptionsMenu(menu);
		
	}
	//------------------------
	*/
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		String message1;
	
	
		switch (item.getItemId()){
		
		
		case R.id.actioNIGHT:
			
				
		getWindow().setBackgroundDrawable(new ColorDrawable(8));
		getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.GRAY));
		
			
			 Toast.makeText(this, "NightMode ", Toast.LENGTH_SHORT).show();
		
			turnOnNightMode(item);
		
			
			 
			 
			 return true;
					
			 
		case R.id.actionEmail: 
			String emailaddress[] = { emailAdd };
			Integer index = rnd.nextInt(cliplist.length);
			TextView viewM = (TextView) findViewById(R.id.hanziTextView);
			
			 message1  = viewM.getText().toString();
			 String newHanzi =  hanzi.get(key);//In order to attach hanzi without being have to be shown
			 hAZI = newHanzi;
			 if(message1!= ""){
			 hAZI = message1;
			 
			 }
			 
			 
			//String message =(String) findViewById(R.id.hanziTextView).toString() ;   //"我们在进入一所新学校的时候，都要向别人介绍自己。";		
			// item.setIcon(R.drawable.ic_dialog_email);
			
			//startActivity(new Intent ("com.MeadowEast.audiotest.EMAIL"));
			Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
			emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, emailaddress);
			emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Clip");
			//emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:/mnt/sdcard/Data/Meaddown.audiotest/files/clip/00100.mp3"));
			//The following line allowed me to attach a file to the mail from any place on phone/computer 
			emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(key)));
			
			emailIntent.setType("plain/text");
			emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, hAZI );
			
			startActivity(emailIntent);
			   
			return true;
		case R.id.lookup:
			
			TextView viewm = (TextView) findViewById(R.id.hanziTextView);
			
			 message1 = viewm.getText().toString();
			//Intent intent = new Intent(Intent.ACTION_VIEW);
			//intent.setData(Uri.parse("market://details?id=com.example.android"));
			//startActivity(intent);
			StringBuilder strBuilder= new StringBuilder();
			strBuilder.toString();
			String mass = "http://www.mdbg.net/chindict/chindict.php?page=worddict&wdrst=0&wdqb=" + message1;
			strBuilder.append("http://www.mdbg.net/chindict/chindict.php?page=worddict&wdrst=0&wdqb=").append(message1)  ;
			Uri uri = Uri.parse(mass);
			Intent it  = new Intent(Intent.ACTION_VIEW,uri);
			
			startActivity(it);
			return true;
			
		case R.id.history:
			
			//First 10 clips history
			return true;
		case R.id.english_chinese:
			startActivity(new Intent ("com.MeadowEast.audiotest.ENGLISH") );
			
			//Translate the original main into English
			return true ;
			
		case R.id.track:
			String track;
			if (sample != null){
    			setHanzi("");
    			if (mp != null){
    				mp.stop();
    				mp.release();
    			}
		
			}
			return true;
			
		case R.id.display:
			
			
			
			return true;
		//----------------
		/*
		 * 	//	case R.id.action_search:
	//		openSearch();
	//		return true;
	//	case R.id.action_settings:
		//	openSettings();
			//return true;
		case R.id.day_night:
			 if (mThemeId == R.style.AppTheme_Dark) {
	                mThemeId = R.style.AppTheme;
	            } else {
	                mThemeId = R.style.AppTheme_Dark;
	            }
			 this.recreate();
			 this.invalidateOptionsMenu();
			 return true;
		case R.id.download_clips:
			startDownload();
			return true;
		case R.id.location:
			if(language == 1) language = 0;
			else language = 1;
			this.recreate();
			return true;
		case R.id.action_email:
			emailClip();
			
			//--------------
			
			case R.id.item1:
				
			//	startActivity(new Intent("com.MeadowEast.audiotest.MAINMENU"));
				// if (mThemeId == R.style.AppTheme) {
		         //       mThemeId = R.style.AppT;
		         //   } else {
		          //      mThemeId = R.style.AppT;
		          //  }
			//	 Toast.makeText(this, "NightMode ", Toast.LENGTH_SHORT).show();
		       //     this.recreate();
				return true;
				
				// In case of NightMode, string ID is item5. If checked then it will
				// call the turnOnNightMode() that will set the backgrounds colors
			case R.id.item5:   // night mode using colors
				turnOnNightMode(item);
			//	Toast.makeText(this, message, Toast.LENGTH_LONG).show();
				return true;
				
			
			//
			/*
			 * case R.id.download_clips:
					startDownload();
					return true;
					
				case R.id.location:
					if(language == 1) language = 0;
					else language = 1;
					this.recreate();
					return true;
					
				case R.id.action_email:
					emailClip();
				default:
					return super.onOptionsItemSelected(item);
					
			 * 
			 * */
			
			
		
			//-------------*/	
		//----------------
			
			
		case R.id.usage:
			
		
		default:
		}
		return false;
	}
	
	/* review : http://developer.android.com/reference/android/view/GestureDetector.html
	 * http://www.codeproject.com/Articles/319401/Simple-Gestures-on-Android
	 * https://code.google.com/p/libgdx/wiki/InputGestureDetection
*/
	public void onGesture(GestureOverlayView arg0, MotionEvent arg1) {
		// TODO Auto-generated method stub
		
	}

	public void onGestureCancelled(GestureOverlayView overlay, MotionEvent event) {
		// TODO Auto-generated method stub
		
	}

	public void onGestureEnded(GestureOverlayView overlay, MotionEvent event) {
		// TODO Auto-generated method stub
		
	}

	public void onGestureStarted(GestureOverlayView overlay, MotionEvent event) {
		// TODO Auto-generated method stub
		
	}

	public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
		// TODO Auto-generated method stub
		
	}
}



/*
 * 	//------------Update Clips ---------------------------------------------
 *  Review:
* http://digitaldumptruck.jotabout.com/?p=920
* http://www.linkedin.com/groups/How-can-i-download-zip-86481.S.223003670 // on the bottom comment
 * 
 *   
	//--I used AsyncTask because it is ideal for short operations and done on background
	private class DownloadTask extends AsyncTask<String, Integer, String> {

	    private Context context;
	    private PowerManager.WakeLock mWakeLock;  // to avoid going inactive or locked during update

	    public DownloadTask(Context context) {
	        this.context = context;
	    }

	    @Override
	    protected void onPreExecute() {  // to do before going in background
	        super.onPreExecute();
	        // take CPU lock to prevent CPU from going off if the user 
	        // presses the power button during download
	        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
	        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
	             getClass().getName());   // to enable CPU to run...
	        mWakeLock.acquire();
	        mProgressDialog.show();   // show progress
	    }
	    @Override
	    protected void onProgressUpdate(Integer... progress) {
	        super.onProgressUpdate(progress);
	        // if we get here, length is known, now set indeterminate to false
	        mProgressDialog.setIndeterminate(false);
	        mProgressDialog.setMax(100);   // for 100 clips
	        mProgressDialog.setProgress(progress[0]);
	    }

	    
	    @Override
	    protected String doInBackground(String... sUrl) {
	        InputStream input = null;
	        OutputStream output = null;
	        HttpURLConnection connection = null;
	        try {
	            URL url = new URL(sUrl[0]);
	            connection = (HttpURLConnection) url.openConnection();
	            connection.connect();

	            // expect HTTP 200 OK, so we don't mistakenly save error report
	            // instead of the file
	            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
	                return "Server returned HTTP " + connection.getResponseCode()
	                        + " " + connection.getResponseMessage();
	            }

	            // this will be useful to display download percentage
	            // might be -1: server did not report the length
	            int fileLength = connection.getContentLength();

	            // download the file
	            input = connection.getInputStream();
	            output = new FileOutputStream(Environment.getExternalStorageDirectory().getPath() + "/newclips.zip");

	            byte data[] = new byte[4096];
	            long total = 0;
	            int count;
	            while ((count = input.read(data)) != -1) {
	                // allow canceling with back button
	                if (isCancelled()) {
	                    input.close();
	                    return null;
	                }
	                total += count;
	                // publishing the progress....
	                if (fileLength > 0) // only if total length is known
	                    publishProgress((int) (total * 100 / fileLength));
	                    output.write(data, 0, count);
	            }
	        } catch (Exception e) {
	            return e.toString();
	        } finally {
	            try {
	                if (output != null)
	                    output.close();
	                if (input != null)
	                    input.close();
	            } catch (IOException ignored) {
	            }

	            if (connection != null)
	                connection.disconnect();
	        }
	        return null;
	    }
	
	
	    
	    @Override
	    protected void onPostExecute(String result) {
	        mWakeLock.release();
	        mProgressDialog.dismiss();
	        if (result != null)
	            Toast.makeText(context,"Download error: "+result, Toast.LENGTH_LONG).show();
	        else
	            Toast.makeText(context,"File downloaded", Toast.LENGTH_SHORT).show();
	    }
	
	
	}
	
	private void startDownload() {
	final DownloadTask downloadTask = new DownloadTask(MainActivity.this);
		downloadTask.execute("http://www.meadoweast.com/capstone/clips.zip");

		mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
		    public void onCancel(DialogInterface dialog) {
		        downloadTask.cancel(true);    // in case if cancel needed , return key
		    }
		});
		
	}
    
	    
	// still need to download the hanzi text file
	
	
	
	/**
	 * @param context used to check the device version and DownloadManager information
	 * @return true if the download manager is available
	 
	public static boolean isDownloadManagerAvailable(Context context) {
	    try {
	        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
	            return false;
	        }
	        Intent intent = new Intent(Intent.ACTION_MAIN);
	        intent.addCategory(Intent.CATEGORY_LAUNCHER);
	        intent.setClassName("com.android.providers.downloads.ui", "com.android.providers.downloads.ui.DownloadList");
	        List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent,
	                PackageManager.MATCH_DEFAULT_ONLY);
	        return list.size() > 0;
	    } catch (Exception e) {
	        return false;
	    }
	}
	/**
	
	public boolean downloadFile() throws IOException{
		String url = "http://www.meadoweast.com/capstone/clips.zip";
		DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
		request.setDescription("New clips");
		request.setTitle("New Clips");
		// in order for this if to run, you must use the android 3.2 to compile your app
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
		    request.allowScanningByMediaScanner();
		    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
		}
		//System.out.println("REACHED!");
		request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "clips.zip");
		File newFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"clips.zip");
	    
		
		//copy(newFile, clipDir);
		
		//unpack the zip files
		//unpackZip(newFile.getAbsolutePath(),"clips.zip");
		
		// get download service and enqueue file
		DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
		manager.enqueue(request);
		return true;
	}
	**/
/*
	public void copy(File src, File dst) throws IOException {
	    InputStream in = new FileInputStream(src);
	    OutputStream out = new FileOutputStream(dst);

	    // Transfer bytes from in to out
	    byte[] buf = new byte[1024];
	    int len;
	    while ((len = in.read(buf)) > 0) {
	        out.write(buf, 0, len);
	    }
	    in.close();
	    out.close();
	}
	
	//----------------
	public boolean unpackZip(String path, String zipname)
	{       
	     InputStream is;
	     ZipInputStream zips;
	     try 
	     {
	         String filename;
	         is = new FileInputStream(path + zipname);
	         zips = new ZipInputStream(new BufferedInputStream(is));          
	         ZipEntry zipent;
	         byte[] buffer = new byte[1024];
	         int count;

	         while ((zipent = zips.getNextEntry()) != null) 
	         {
	             // zapis do souboru
	             filename = zipent.getName();

	             // Need to create directories if not exists, or
	             // it will generate an Exception...
	             if (zipent.isDirectory()) {
	                File fmd = new File(path + filename);
	                fmd.mkdirs();
	                continue;
	             }

	             FileOutputStream fout = new FileOutputStream(path + filename);

	             // cteni zipu a zapis
	             while ((count = zips.read(buffer)) != -1) 
	             {
	                 fout.write(buffer, 0, count);             
	             }

	             fout.close();               
	             zips.closeEntry();
	         }

	         zips.close();
	     } 
	     catch(IOException e)
	     {
	         e.printStackTrace();
	         return false;
	     }

	    return true;
	    
	    */

/*
public class Decompress {

	private ZipFile zipFile;
	private String _zipFilename;
	private Context _context;

	public Decompress(String zipFile, Context context) {
		_zipFilename = zipFile;
		_context = context;
	}

	public void unzip() {
		try {
			// Using assets
			// InputStream in = _context.getAssets().open(_zipFile);

			// Using external file system
			File mainDir = new File(Environment.getExternalStorageDirectory()
					.getAbsolutePath()
					+ "/Android/data/com.MeadowEast.audiotest/files");
			File zipfile = new File(mainDir, _zipFilename);
			FileInputStream in = new FileInputStream(zipfile);

			ZipInputStream zipn = new ZipInputStream(in);
			ZipEntry zipen = null;
			
			
			
			while ((zipen = zipn.getNextEntry()) != null) {
				Log.v("Decompress", "Unzipping " + ze.getName());

				if (zipen.isDirectory()) {
					// Sub-directories do not work if we're using private file
					// storage
					continue;
				} else {
					FileOutputStream fileout = _context.openFileOutput(
							ze.getName(), Context.MODE_PRIVATE);
					byte[] buffer = new byte[4096];
					for (int c = zin.read(buffer); c != -1; c = zin
							.read(buffer)) {
						fileout.write(buffer, 0, c);
					}

					zin.closeEntry();
					fileout.close();
				}

			}
			zin.close();
		} catch (Exception e) {
			Log.e("Decompress", "unzip", e);
		}

	}

	public InputStream getSample(String filename) throws IOException {
		InputStream sample = null;

		sample = zipFile.getInputStream(zipFile.getEntry(filename));

		return sample;
	}

	public String[] getList() {
		// Open the zip file if it didn't happen yet (probably didn't)
		if (zipFile == null) {
			try {
				// Using external file system
				File mainDir = new File(Environment
						.getExternalStorageDirectory().getAbsolutePath()
						+ "/Android/data/com.MeadowEast.audiotest/files");
				zipFile = new ZipFile(new File(mainDir, _zipFilename));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		
		Enumeration e = zipFile.entries();
		ArrayList<String> list = new ArrayList<String>();
		while (e.hasMoreElements()) {
			ZipEntry entry = (ZipEntry) e.nextElement();
			list.add(entry.getName());
		}

		//  to an array
		return list.toArray(new String[0]);
	}
}*/

