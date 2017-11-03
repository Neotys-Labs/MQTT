package com.neotys.mqtt.mqttwebsocket;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttConnack;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttWireMessage;

public class MqttNLConnack {
	private String ReturnCode;
	private int MessageID;
	private static String CONNREFID="Connection Refused: identifier rejected";
	private static String CONNREFPROTO="Connection Refused: unacceptable protocol version";
	private static String CONNREFSERV="Connection Refused: server unavailable";
	private static String CONNREFPASS="Connection Refused: bad user name or password";
	private static String CONNREFACC="Connection Accepted";
	private static String CONNREFAUTH="Connection Refused: not authorized";
	public String NeoLoadIdentifier;
	
	public MqttNLConnack(MqttWireMessage mess)
	{
		MqttConnack conn=(MqttConnack)mess;
		try {
			byte[] headers=conn.getHeader();
			ReturnCode=GetReturnCode(headers[1]);
			MessageID=conn.getMessageId();
			NeoLoadIdentifier="CONNECT-0";
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	private String GetReturnCode(Byte head)
	{
		
		if(head.equals( (byte)0x00))
			return CONNREFACC;
		if(head.equals( (byte)0x01))
			return CONNREFPROTO;
		if(head.equals( (byte)0x02))
			return CONNREFID;
		if(head.equals( (byte)0x03))
			return CONNREFSERV;
		if(head.equals( (byte)0x04))
			return CONNREFPASS;
		if(head.equals( (byte)0x05))
			return CONNREFAUTH;
		
		return null;
		
	}
	
}
