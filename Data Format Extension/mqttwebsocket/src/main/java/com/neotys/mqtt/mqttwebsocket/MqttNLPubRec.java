package com.neotys.mqtt.mqttwebsocket;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttPubRec;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttWireMessage;
public class MqttNLPubRec {
	private int MessageID;

	
	public MqttNLPubRec(MqttWireMessage mess)
	{
		MqttPubRec message=(MqttPubRec) mess;
		MessageID=message.getMessageId();
		
	}
	public int GetMessageID()
	{
		return MessageID;
	}
}
