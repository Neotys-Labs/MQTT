package com.neotys.mqtt.mqttwebsocket;

import org.eclipse.paho.client.mqttv3.internal.wire.MqttPingReq;

import org.eclipse.paho.client.mqttv3.internal.wire.MqttWireMessage;

public class MqttNLPingReq {
	private int MessageID;
	public String NeoLoadIdentifier;
	
	public MqttNLPingReq(MqttWireMessage mess)
	{
		MqttPingReq message = (MqttPingReq)mess;

		MessageID=message.getMessageId();
		NeoLoadIdentifier="PINGREQ-"+MessageID;
		
	}
	public int getMessageId()
	{
		return MessageID;
	}
}
