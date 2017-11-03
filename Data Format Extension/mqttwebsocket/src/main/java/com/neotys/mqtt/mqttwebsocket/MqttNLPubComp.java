package com.neotys.mqtt.mqttwebsocket;

import org.eclipse.paho.client.mqttv3.internal.wire.MqttPubComp;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttWireMessage;

public class MqttNLPubComp {
	private int MessageID;
	
	
	public MqttNLPubComp(MqttWireMessage mess)
	{
		MqttPubComp message=(MqttPubComp) mess;
		MessageID=message.getMessageId();
	
	}
	public int GetMessageID()
	{
		return MessageID;
	}
}
