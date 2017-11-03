package com.neotys.mqtt.mqttwebsocket;

import org.eclipse.paho.client.mqttv3.MqttException;

import com.neotys.extensions.codec.functions.Decoder;

public class MqttDecoder implements Decoder {

	@Override
	public Object apply(byte[] input) {
		// TODO add your decoding code
				try {
					return  new MqttNLMessage(input);
				} catch (MqttException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return null;
	}

}
