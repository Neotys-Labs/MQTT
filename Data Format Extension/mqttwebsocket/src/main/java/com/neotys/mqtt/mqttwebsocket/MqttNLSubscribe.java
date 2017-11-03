package com.neotys.mqtt.mqttwebsocket;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.lang.reflect.Field;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.internal.wire.CountingInputStream;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttSubscribe;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttWireMessage;

public class MqttNLSubscribe {
	private int MessageID;
	private int Qos;
	private String TopicName;
	private boolean isDuplicate;
	public String NeoLoadIdentifier;
	
	public MqttNLSubscribe(MqttWireMessage mess)
	{
		String[] name;
		int[] qos;
		
		MqttSubscribe message=(MqttSubscribe)mess;
		MessageID=message.getMessageId();
		try {
			Field f = message.getClass().getDeclaredField("names"); 
			f.setAccessible(true);
			name = (String[]) f.get(message);
			f = message.getClass().getDeclaredField("qos"); 
			f.setAccessible(true);
			qos = (int[]) f.get(message);
			
			Qos=qos[0];
			TopicName=name[0];
			NeoLoadIdentifier="SUBSCRIBE-"+MessageID;
		} catch ( NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}
	public MqttNLSubscribe(MqttWireMessage mess,byte[] input)
	{
		String[] name;
		int[] qos;
		
		MqttSubscribe message=(MqttSubscribe)mess;
		MessageID=message.getMessageId();

		ByteArrayInputStream bis = new ByteArrayInputStream(input);
		CountingInputStream counter = new CountingInputStream(bis);
		DataInputStream in = new DataInputStream(counter);
		int first;
		try {
			Field f = message.getClass().getDeclaredField("names"); 
			f.setAccessible(true);
			name = (String[]) f.get(message);
			f = message.getClass().getDeclaredField("qos"); 
			f.setAccessible(true);
			qos = (int[]) f.get(message);
			
			first = in.readUnsignedByte();
			byte type = (byte) (first >> 4);
		    isDuplicate = (first & 0x08) == 0x08;
		
			Qos=qos[0];
			TopicName=name[0];
			NeoLoadIdentifier="SUBSCRIBE-"+MessageID;
		} catch ( NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}
	public boolean IsDuplicate()
	{
		return isDuplicate;
	}
	public String GetTopicName()
	{
		return TopicName;
	}
	public int GetQos()
	{
		return Qos;
	}
	public int getMessageId()
	{
		return MessageID;
	}
	private String GetTopicName(byte[] payload)
	{
		String Strtopic = null;
		int LenghtofBytes=payload[1];
		byte[] top = null;
		int j=0;
		for(int i=2;i<(2+LenghtofBytes);i++)
		{
			top[j]=payload[i];
			j++;
		}
		Strtopic=new String(top);
		
		return Strtopic;
		
	}
	private int GetQos(byte[] payload)
	{
		Byte tmp=payload[1];
		int LenghtofBytes=tmp.intValue();
		tmp = payload[2+LenghtofBytes];
		int qos=tmp.intValue();
		return qos;
		
	}
}
