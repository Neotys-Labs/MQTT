package com.neotys.mqtt.mqttwebsocket;


import org.eclipse.paho.client.mqttv3.internal.wire.MqttUnsubAck;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttWireMessage;

public class MqttNLUnSubAck {
private int MessageID;
public String NeoLoadIdentifier;
	
	public MqttNLUnSubAck(MqttWireMessage mess)
	{
		MqttUnsubAck message=(MqttUnsubAck) mess;
		MessageID=message.getMessageId();
		NeoLoadIdentifier="UNSUBSCRIBE-"+MessageID;
	}
}
