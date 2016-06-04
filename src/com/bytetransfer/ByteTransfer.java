package com.bytetransfer;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;

public class ByteTransfer extends Activity {

	MediaPlayer var_splashSound;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_splash);
		var_splashSound = MediaPlayer.create(ByteTransfer.this, R.raw.splash_sound);
		var_splashSound.start();
		Thread timer = new Thread() {
			public void run() {
				try{
					sleep(3000);
				}catch(InterruptedException ie) {
					ie.printStackTrace();
				}finally{
					Intent openMainActivity = new Intent("com.MAINBYTETRANSFERACTIVITY");
					startActivity(openMainActivity);
				}
			}
		};
		timer.start();
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		var_splashSound.release();
		finish();
	}

}
