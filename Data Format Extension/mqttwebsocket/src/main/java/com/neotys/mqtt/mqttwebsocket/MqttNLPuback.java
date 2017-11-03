package com.neotys.mqtt.mqttwebsocket;


import org.eclipse.paho.client.mqttv3.internal.wire.MqttPubAck;

import org.eclipse.paho.client.mqttv3.internal.wire.MqttWireMessage;

public class MqttNLPuback {

	private int MessageID;
	
	public MqttNLPuback(MqttWireMessage mess)
	{
		MqttPubAck message = (MqttPubAck)mess;

		MessageID=message.getMessageId();
	
		
	}
	public int getMessageId()
	{
		return MessageID;
	}
}
