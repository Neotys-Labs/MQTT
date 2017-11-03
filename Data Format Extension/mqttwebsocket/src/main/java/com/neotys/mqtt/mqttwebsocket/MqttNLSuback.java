package com.neotys.mqtt.mqttwebsocket;

import org.eclipse.paho.client.mqttv3.internal.wire.MqttSuback;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttWireMessage;

public class MqttNLSuback {
	private int MessageID;
	public String NeoLoadIdentifier;
	
	public MqttNLSuback(MqttWireMessage mess)
	{
		MqttSuback message=(MqttSuback) mess;
		MessageID=message.getMessageId();
		NeoLoadIdentifier="SUBSCRIBE-"+MessageID;
	}
}
