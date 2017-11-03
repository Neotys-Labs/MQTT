package com.neotys.mqtt.mqttwebsocket;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import org.eclipse.paho.client.mqttv3.internal.wire.CountingInputStream;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttPubRel;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttWireMessage;

public class MqttNLPubRel {
	private int MessageID;
	private boolean isDuplicate;
	
	public MqttNLPubRel(MqttWireMessage mess)
	{
		MqttPubRel message=(MqttPubRel) mess;
		MessageID=message.getMessageId();
		
	}
	public MqttNLPubRel(MqttWireMessage mess,byte[] input)
	{
		MqttPubRel message=(MqttPubRel) mess;
		ByteArrayInputStream bis = new ByteArrayInputStream(input);
		CountingInputStream counter = new CountingInputStream(bis);
		DataInputStream in = new DataInputStream(counter);
		int first;
		try {
			first = in.readUnsignedByte();
			byte type = (byte) (first >> 4);
		    isDuplicate = (first & 0x08) == 0x08;
			MessageID=message.getMessageId();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public boolean IsDuplicate()
	{
		return isDuplicate;
	}
	public int GetMessageID() {
		// TODO Auto-generated method stub
		return MessageID;
	}
}
