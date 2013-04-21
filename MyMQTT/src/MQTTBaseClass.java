import java.awt.EventQueue;

import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.internal.MemoryPersistence;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.RaspiPin;

public class MQTTBaseClass {

/*This class runs on the Raspberry Pi. To use this class you need to install Java JDK 8 (with JavaFX)
 * and the j4pi libraries. There is a great tutorial here http://www.savagehomeautomation.com/projects/raspberry-pi-installing-oracle-java-se-8-with-javafx-develop.html
 * that shows how to do install Java on the Raspberry Pi. For the j4pi installation go here http://pi4j.com/install.html.
 */

	private static GpioPinDigitalOutput pin;
	private static GpioController gpio;
	/**
	 * @param args
	 */

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			gpio = GpioFactory.getInstance();
			MqttClient client = new MqttClient("tcp://m2m.eclipse.org:1883",
					"eorasppi", new MemoryPersistence());
			MqttConnectOptions conOpts = new MqttConnectOptions();
			conOpts.setKeepAliveInterval(30);
			conOpts.setWill(client.getTopic("Error"),
					"something bad happened".getBytes(), 1, true);
			client.setCallback(new MqttCallback() {

				@Override
				public void messageArrived(MqttTopic arg0, MqttMessage arg1)
						throws Exception {
					
					// TODO Auto-generated method stub
				try{
					if (arg1.toString().equalsIgnoreCase("ON")) {

						// provision gpio pin #01 as an output pin and turn
						// on
					//MQTTBaseClass.onOrOff=true;
						EventQueue.invokeLater(new Runnable() { 
							  @Override
							  public void run() {
							     // your UI code
									pin.high();
							  }
							});	
				

					} else {
						EventQueue.invokeLater(new Runnable() { 
							  @Override
							  public void run() {
							     // your UI code
									pin.low();
							  }
							});	
						//MQTTBaseClass.onOrOff=false;
					}
				}catch(Exception e){
					e.printStackTrace();
				}
				}

				@Override
				public void deliveryComplete(MqttDeliveryToken arg0) {
					// TODO Auto-generated method stub

				}

				@Override
				public void connectionLost(Throwable arg0) {
					// TODO Auto-generated method stub

				}
			});
			MqttMessage msg = new MqttMessage("/House/Kitchen/LED".getBytes());
			msg.setQos(0);
		//	msg.setRetained(true); add if you want messages to be delivered by the broker if somebody subsribes to the topic after the message was sent originally, it will deliver all messages to "keep the subsriber up-to-date"
			client.connect(conOpts);
			pin = gpio.provisionDigitalOutputPin(
					RaspiPin.GPIO_01, "MyLED");
		
			MqttTopic topic = client.getTopic("com.jstnow.mqtt.topic/Home/Kitchen/LED");
			
		client.subscribe("com.jstnow.mqtt.topic/Home/Kitchen/LED");
			
			topic.publish(msg);


		} catch (MqttPersistenceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
