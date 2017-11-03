package com.neotys.mqtt.mqttwebsocket;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.lang.reflect.Field;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.internal.wire.CountingInputStream;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttUnsubscribe;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttWireMessage;

public class MqttNLUnsubscribe {
	private int MessageID;
	private String TopicName;
	private boolean isDupicate;
	public String NeoLoadIdentifier;

	public MqttNLUnsubscribe(MqttWireMessage mess)
	{
		MqttUnsubscribe message=(MqttUnsubscribe) mess;
		MessageID=message.getMessageId();
	/*	try {
			TopicName=GetTopicName(message.getPayload());
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		String[] name;
		
		Field f;
		try {
			f = message.getClass().getDeclaredField("names");
			f.setAccessible(true);
			name = (String[]) f.get(message);
			TopicName=name[0];
			NeoLoadIdentifier="UNSUBSCRIBE-"+MessageID;
		} catch (NoSuchFieldException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		

		
	}
	public MqttNLUnsubscribe(MqttWireMessage mess,byte[] input)
	{
		MqttUnsubscribe message=(MqttUnsubscribe) mess;
		MessageID=message.getMessageId();
	/*	try {
			TopicName=GetTopicName(message.getPayload());
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		String[] name;
		ByteArrayInputStream bis = new ByteArrayInputStream(input);
		CountingInputStream counter = new CountingInputStream(bis);
		DataInputStream in = new DataInputStream(counter);
		int first;
		Field f;
		try {
			f = message.getClass().getDeclaredField("names");
			f.setAccessible(true);
			name = (String[]) f.get(message);
			TopicName=name[0];
			first = in.readUnsignedByte();
			byte type = (byte) (first >> 4);
		    isDupicate = (first & 0x08) == 0x08;
			NeoLoadIdentifier="UNSUBSCRIBE-"+MessageID;
		} catch (NoSuchFieldException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		

		
	}
	public boolean IsDuplicate()
	{
		return isDupicate;
	}
	public int getMessageId()
	{
		return MessageID;
	}
	public String GetTopicName()
	{
		return TopicName;
	}
	private String GetTopicName(byte[] payload)
	{
		String Strtopic = null;
		int LenghtofBytes=payload[1];
		byte[] top = new byte[2];
		int j=0;
		for(int i=2;i<(2+LenghtofBytes);i++)
		{
			top[j]=payload[i];
			j++;
		}
		Strtopic=new String(top);
		
		return Strtopic;
		
	}
	
}
