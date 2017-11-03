package com.neotys.mqtt.mqttwebsocket;



import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttAck;

import org.eclipse.paho.client.mqttv3.internal.wire.MqttWireMessage;

public class MqttNeotysPubRec extends MqttAck {

	
	public MqttNeotysPubRec(int messageid)
	{
		super(MqttWireMessage.MESSAGE_TYPE_PUBREC);
		msgId=messageid;
	}

	@Override
	protected byte[] getVariableHeader() throws MqttException {
		// TODO Auto-generated method stub
		return encodeMessageId();

	}
}
