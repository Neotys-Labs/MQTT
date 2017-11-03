package com.neotys.mqtt.mqttwebsocket;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.internal.wire.CountingInputStream;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttPublish;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttWireMessage;

import io.netty.buffer.ByteBuf;

public class MqttNLPublish {
	private String TopciName;
	private int MessageID;
	private int Qos;
	private String Message;
	private boolean Isretained;
	private boolean IsDuplicate;
	
	
	public MqttNLPublish(MqttWireMessage mess)
	{
		MqttPublish message = (MqttPublish)mess;

		try {
			Qos=GetQos(message.getHeader());
			Message=new String(message.getPayload());
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		TopciName=message.getTopicName();
		MessageID=message.getMessageId();
		
	
	}
	public MqttNLPublish(MqttWireMessage mess,byte[] input)
	{
		MqttPublish message = (MqttPublish)mess;
		
		ByteArrayInputStream bis = new ByteArrayInputStream(input);
		CountingInputStream counter = new CountingInputStream(bis);
		DataInputStream in = new DataInputStream(counter);
		int first;
		try {
			first = in.readUnsignedByte();
			byte type = (byte) (first >> 4);
		    IsDuplicate = (first & 0x08) == 0x08;
			Isretained = (first & 0x01) != 0;
			Qos=GetQos(message.getHeader());
			Message=new String(message.getPayload());
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		TopciName=message.getTopicName();
		MessageID=message.getMessageId();
		
	
	}
	public boolean IsDpulicate()
	{
		return IsDuplicate;
	}
	public boolean IsRetained()
	{
		return Isretained;
		
	}
	public byte[] GetMessageByte()
	{
		return Message.getBytes();
	}
	public String GetTopicName()
	{
		return TopciName;
	}
	public int GetQos()
	{
		return Qos;
	}
	public int getMessageId()
	{
		return MessageID;
	}
	@SuppressWarnings("unused")
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
	
	public byte getBit(byte content,int position)
	{
	   return (byte) ((content >> position) & 1);
	}
	private int GetQos(byte[] headers)
	{
		int qo;
		return qo=(getBit(headers[0],2)*2) + getBit(headers[0],1)  ;
		
	}
}
