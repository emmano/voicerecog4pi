package com.example.mqttphone;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import org.eclipse.paho.client.mqttv3.MqttClient;

import android.app.Activity;
import android.app.IntentService;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.Engine;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

/*
 * This is the main entry point of the program. This activity connects to an IntentService,
 * to publish to the MQTT broker (I used the RSMB for this project). I decided to use an
 * IntentService for two reasons: 1) it runs on a separate Thread so long Network operations,
 * do not run on the UI thread. 2) IntentService stops itself after it finishes the desired operation.
 * 
 * For setting up the Raspberry Pi circuit go to the bottom of http://pi4j.com/example/control.html
 * 
 * Thanks to Andy Piper and Dominik Obermaier for their support, and the j4pi team for their great library.
 */
public class Main extends Activity implements OnClickListener {

	public MqttClient client;
	private ServiceConnection connection;
	private int VOICE_RECOGNITION_SUCCESS = 1;
	private int TTS_LIGHT = 1;
	private Button btn;
	MQTTService remoteService;
	TextToSpeech tts;
	String words = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		btn = (Button) findViewById(R.id.button1);
		btn.setOnClickListener(this);

		// service connection to be used by IntentService. Once we are connected
		// we can call methods on the remote service via a Binder.
		connection = new ServiceConnection() {

			@Override
			public void onServiceDisconnected(ComponentName name) {
				// TODO Auto-generated method stub
				Log.e("SERVICE", "Service Disconnected");
			}

			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				// TODO Auto-generated method stub
				remoteService = ((MQTTService.MyBinder) service).getService();
				remoteService.get();
				if (remoteService.isMessageDelivered()) {
					Intent intent = new Intent(
							TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
					startActivityForResult(intent, TTS_LIGHT);
				}

			}
		};
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == VOICE_RECOGNITION_SUCCESS && resultCode == RESULT_OK) {
			ArrayList<String> results;
			results = data
					.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			words = "";

			// Did not use confidence for this project. It can be easily implemented.
			// float[] confidence;
			// String confidenceExtra =
			// RecognizerIntent.EXTRA_CONFIDENCE_SCORES;
			// confidence =
			// data.getFloatArrayExtra(confidenceExtra);
			for (String phrase : results) {

				words = words + phrase + " ";
			}
			Toast.makeText(this, words, Toast.LENGTH_SHORT).show();
			if (words.equalsIgnoreCase("turn light on ")) {
				Intent intent = new Intent(this, MQTTService.class);
				intent.putExtra("STATE", "ON");
				bindService(intent, connection, IntentService.BIND_AUTO_CREATE);

			} else if (words.equalsIgnoreCase("turn light off ")) {
				Intent intent = new Intent(this, MQTTService.class);
				intent.putExtra("STATE", "OFF");
				bindService(intent, connection, IntentService.BIND_AUTO_CREATE);

			} 
			//startActivityForResutl() will call this method again. Since no command was recognized.
			//use the requesID so TextToSpeach can let the user know.
			else {
				Intent intent = new Intent(
						TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
				startActivityForResult(intent, TTS_LIGHT);
			}

		} else if (requestCode == TTS_LIGHT
				&& resultCode == Engine.CHECK_VOICE_DATA_PASS
				&& remoteService != null) {
			tts = new TextToSpeech(this, new OnInitListener() {

				@Override
				public void onInit(int status) {
					// TODO Auto-generated method stub
					if (status == TextToSpeech.SUCCESS) {
						if (words.equalsIgnoreCase("turn light on ")) {
							HashMap<String, String> parameters = null;
							tts.speak("Turning Light on",
									TextToSpeech.QUEUE_ADD, parameters);
							unbindService(connection);
						} else if (words.equalsIgnoreCase("turn light off ")) {
							HashMap<String, String> parameters = null;
							tts.speak("Turning Light off",
									TextToSpeech.QUEUE_ADD, parameters);
							unbindService(connection);
						} else {
							HashMap<String, String> parameters = null;
							tts.speak(
									words
											+ "is not a recognized command. Please try Again",
									TextToSpeech.QUEUE_ADD, parameters);
						}
					}

				}
			});

		} else if (requestCode == TTS_LIGHT
				&& resultCode == Engine.CHECK_VOICE_DATA_PASS
				&& remoteService == null) {
			tts = new TextToSpeech(this, new OnInitListener() {

				@Override
				public void onInit(int status) {
					// TODO Auto-generated method stub
					if (status == TextToSpeech.SUCCESS) {
						HashMap<String, String> parameters = null;
						tts.speak(words + "is not a known command. Try again",
								TextToSpeech.QUEUE_ADD, parameters);
					}

				}
			});
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		// Specify free form input
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
				"or forever hold your peace");
		intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ENGLISH);
		startActivityForResult(intent, VOICE_RECOGNITION_SUCCESS);
	}

}
