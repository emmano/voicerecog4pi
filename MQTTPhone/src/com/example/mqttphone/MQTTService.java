package com.example.mqttphone;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import android.app.IntentService;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class MQTTService extends IntentService {
	public MQTTService(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	public MQTTService() {
		super("Service");
		// TODO Auto-generated constructor stub
	}

	private Intent intent;
	protected static String msg;
	public MqttClient client;
	protected boolean messageDelivered;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		this.intent = intent;
		// get();
		return new MyBinder();

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub

	}

	public void get() {
		// TODO Auto-generated method stub
		try {
			
			
// Make sure you type the IP where the broker is running. If you did not download the Really Small Message Broker(RSMB)
// (https://www14.software.ibm.com/webapp/iwm/web/reg/pick.do?source=swg-rlsmmsbk&lang=en_US)
//or Mosquitto (http://mosquitto.org), you can use m2m.eclipse.org. It is an online server that runs a MQTT broker (just to let you know, if someone knows the
// topics to which you are subscribed, they will receive your messages)
			
			
			client = new MqttClient("tcp://m2m.eclipse.org:1883", "eoandroidphone",
					new MemoryPersistence());

			MqttConnectOptions conOpts = new MqttConnectOptions();
			conOpts.setKeepAliveInterval(30);
			conOpts.setWill(client.getTopic("Error"),
					"something bad happened".getBytes(), 1, true);
			
			//MQTT Callback not used. This service only publishes messages to the broker
			//it is not subsribed to any topic. It can be easily implemented to let the Activity
			//know that the message was delivered. One quick note, the getMessage() in IMqttDeliveryToken
			//returns null if the message was delivered to the server (not a 1 or int RESULT_OK like one might think).
			
			// client.setCallback( new MqttCallback() {
			//
			//
			//
			// private Object TTS_DATA_CHECK=1;
			//
			// @Override
			// public void connectionLost(Throwable arg0) {
			// // TODO Auto-generated method stub
			//
			// }
			//
			// @Override
			// public void deliveryComplete(IMqttDeliveryToken arg0) {
			// // TODO Auto-generated method stub
			// try {
			// if(arg0.getMessage()==null){
			// messageDelivered = true;
			// }
			// } catch (MqttException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			// }
			//
			// @Override
			// public void messageArrived(String arg0, MqttMessage arg1)
			// throws Exception {
			// // TODO Auto-generated method stub
			// MQTTService.msg = arg1.toString();
			//
			// Log.e("MESSAGE DELIVERED", arg1.toString());
			//
			// }
			// });

			client.connect(conOpts);

			if (client.isConnected()) {

				// client.subscribe("/Home/Kitchen/LED");
				if (intent.getStringExtra("STATE").equals("ON")) {
					MqttMessage msg = new MqttMessage("ON".getBytes());
					client.getTopic("com.jstnow.mqtt.topic/Home/Kitchen/LED").publish(msg);
					messageDelivered = true;
				} else {
					MqttMessage msg = new MqttMessage("OFF".getBytes());
					client.getTopic("com.jstnow.mqtt.topic/Home/Kitchen/LED").publish(msg);
					messageDelivered = true;
				}

			}

		} catch (MqttException e) {
			// TODO Auto-generated catch block
			Log.e("ERROR", "NOT CONNECTED");
			e.printStackTrace();
		}

	}

	public boolean isMessageDelivered() {
		return messageDelivered;
	}

	//getMessage() can be called from Main to get the actual message sent by the publisher
	//once it is broadcasted by the server. Note that for this to work, this IntentService needs to,
	//be subsribed to the topic to which the message was published, i.e. "/Home/Kitchen/LED" in this case.
	//uncomment the MQttCallback() from above to use and the client.subscribe() inside the if on line 115.
	
//	public String getMessage() {
//
//		return msg;
//
//	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		try {
			client.disconnect();
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public class MyBinder extends Binder {

		public MQTTService getService() {
			return MQTTService.this;
		}
	}

}
