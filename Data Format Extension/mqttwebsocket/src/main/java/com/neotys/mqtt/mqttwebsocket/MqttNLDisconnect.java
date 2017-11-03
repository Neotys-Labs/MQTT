package com.neotys.mqtt.mqttwebsocket;

import org.eclipse.paho.client.mqttv3.internal.wire.MqttDisconnect;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttWireMessage;

public class MqttNLDisconnect {
	private int MessageID;
	public MqttNLDisconnect(MqttWireMessage mess)
	{
		MqttDisconnect message=(MqttDisconnect) mess;
		MessageID=message.getMessageId();
	}
}
