package com.neotys.mqtt.mqttwebsocket;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.neotys.extensions.codec.functions.Encoder;

public class mqttEncoder implements Encoder {
	@Override
	public byte[] apply(Object input) {
		// TODO add your encoding code
		try
		{
			final MqttMessage Message= (MqttMessage) input;
			return Message.getPayload();
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	
	}

}
