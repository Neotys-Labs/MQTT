package com.neotys.mqtt.mqttwebsocket;


import org.eclipse.paho.client.mqttv3.internal.wire.MqttPingResp;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttWireMessage;

public class MqttNLPingResp {
		private int MessageID;
		public String NeoLoadIdentifier;
		
		public MqttNLPingResp(MqttWireMessage mess)
		{
			MqttPingResp message = (MqttPingResp)mess;

			MessageID=message.getMessageId();
			NeoLoadIdentifier="PINGREQ-"+MessageID;
			
		}
	
}
