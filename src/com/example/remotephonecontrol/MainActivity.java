package com.example.remotephonecontrol;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {

	Button normal;
	Button silence;
	Button vibrate;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		normal = (Button) this.findViewById(R.id.normal);
		silence = (Button) this.findViewById(R.id.silence);
		vibrate = (Button) this.findViewById(R.id.vibrate);

		normal.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				MainActivity.this.setNormal();
			}
		});

		silence.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				MainActivity.this.setSilence();
			}
		});

		vibrate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				long[] array = {500,500,500,500,500,500,500,500,1500,500,500,500};
				MainActivity.this.virbate(array);
			}
		});

		Intent intent = new Intent(this, RemoteControlService.class);
		startService(intent);
	}

	private void setNormal() {
		AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		audio.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
	}

	private void setSilence() {
		AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		audio.setRingerMode(AudioManager.RINGER_MODE_SILENT);
	}

	private void virbate(long[] array) {
		Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
		vibrator.vibrate(array, -1);
	}
}
