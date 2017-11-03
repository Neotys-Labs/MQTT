
package com.neotys.mqtt.mqttwebsocket;



import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttConnack;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttConnect;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttDisconnect;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttPingReq;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttPingResp;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttPubAck;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttPubComp;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttPubRec;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttPubRel;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttPublish;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttSuback;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttSubscribe;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttUnsubAck;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttUnsubscribe;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttWireMessage;

public class MqttNLMessage {
	private Object MQTTMessage;
	private String Type;
	

	
	public MqttNLMessage(final byte[] input) throws MqttException  
	{
		
			
			MqttWireMessage message = MqttWireMessage.createWireMessage(input);
			Type=getType(message.getType());
			
			
			if(message instanceof MqttPublish)
			{
				MQTTMessage= new MqttNLPublish(message,input);
			}
			else 
			{
				if(message instanceof MqttConnect)
				{	
					MQTTMessage= new MqttNLConnect(message,input);	
				}
				else if(message instanceof MqttSubscribe)
				{
					MQTTMessage= new MqttNLSubscribe(message,input);
				
				}
				else if (message instanceof MqttUnsubscribe)
				{
					MQTTMessage= new MqttNLUnsubscribe(message,input);
				}
				else if (message instanceof MqttDisconnect)
				{
					MQTTMessage= new MqttNLDisconnect(message);
				}
				else if (message instanceof MqttConnack)
				{
					MQTTMessage= new MqttNLConnack(message);
				}
				else if (message instanceof MqttPingReq)
				{
					MQTTMessage= new MqttNLPingReq(message);
				}
				else if (message instanceof MqttPingResp)
				{
					MQTTMessage= new MqttNLPingResp(message);
				}
				else if (message instanceof MqttPubAck)
				{
					MQTTMessage= new MqttNLPuback(message);
				}
				
				else if (message instanceof MqttSuback)
				{
					MQTTMessage= new MqttNLSuback(message);
				}
				else if (message instanceof MqttUnsubAck)
				{
					MQTTMessage= new MqttNLUnSubAck(message);
				}
				
				else if (message instanceof MqttPubRel)
				{
					MQTTMessage= new MqttNLPubRel(message,input);
				}
				else if (message instanceof MqttPubComp)
				{
					MQTTMessage= new MqttNLPubComp(message);
				}
				else if (message instanceof MqttPubRec)
				{
					MQTTMessage= new MqttNLPubRec(message);
				}
				else
				{
					System.out.println("Type trouvé :" +  Type);
				}
				
			}
			

		
	}
	
	
	
	private int GetDuplicate(byte[] headers)
	{
		return headers[3];
	}
	
	@SuppressWarnings("null")
	private MqttWireMessage GetBytesFromType(String t)
	{
		MqttWireMessage result = null;
		String[] name = new String[1];
	
		int[] q = new int[1];
	
		switch(t)
		{
		case "PUBLISH":
			MqttNLPublish messobj=(MqttNLPublish)MQTTMessage;
			MqttMessage mess=new MqttMessage(messobj.GetMessageByte());
			mess.setRetained(messobj.IsRetained());
			result=new MqttPublish(messobj.GetTopicName(), mess);
			result.setMessageId(messobj.getMessageId());
		//	result.setDuplicate(messobj.IsDpulicate());
			break;
		
		case "SUBSCRIBE":
			MqttNLSubscribe messobj1=(MqttNLSubscribe)MQTTMessage;
			name[0]=messobj1.GetTopicName();
			q[0]=messobj1.GetQos();
			result=new MqttSubscribe(name,q);
			result.setMessageId(messobj1.getMessageId());
		//	result.setDuplicate(messobj1.IsDuplicate());
			break;
		
		case "UNSUBSCRIBE":
			MqttNLUnsubscribe messobj11=(MqttNLUnsubscribe)MQTTMessage;
			name[0]=messobj11.GetTopicName();
			result=new MqttUnsubscribe(name);
			result.setMessageId(messobj11.getMessageId());
		//	result.setDuplicate(messobj11.IsDuplicate());
			break;

		case "CONNECT":
			MqttNLConnect messobj111=(MqttNLConnect)MQTTMessage;
			if(messobj111.willMessage!=null)
				result=new MqttConnect(messobj111.clientId, messobj111.MqttVersion, messobj111.cleanSession, messobj111.keepAliveInterval, messobj111.userName, messobj111.password, new MqttMessage(messobj111.willMessage.getBytes()), messobj111.willDestination);
			else
				result=new MqttConnect(messobj111.clientId, messobj111.MqttVersion, messobj111.cleanSession, messobj111.keepAliveInterval, messobj111.userName, messobj111.password, null, messobj111.willDestination);
			break;
		
		case "DISCONNECT":
			result=new MqttDisconnect();
			break;
			
		case "PINGREQ":
			result=new MqttPingReq();
			break;
		
		
		case "PUBCOMP":
			MqttNLPubComp messobj1111=(MqttNLPubComp)MQTTMessage;
			result=new MqttPubComp(messobj1111.GetMessageID());
			break;
			
		case "PUBREL":
			MqttNLPubRel messobj11111=(MqttNLPubRel)MQTTMessage;
			result=new MqttNeotysPubRel(messobj11111.GetMessageID());
		//	result.setDuplicate(messobj11111.IsDuplicate());
			break;
			
			
		case "PUBREC":
			MqttNLPubRec messobj111111=(MqttNLPubRec)MQTTMessage;
			result=new MqttNeotysPubRec(messobj111111.GetMessageID());
			break;

		}
		return result;
	}
	private String getType(byte t)
	{
		String result=null ;
		
		if (t == MqttWireMessage.MESSAGE_TYPE_PUBLISH) {
			result="PUBLISH";
		}
		else if (t == MqttWireMessage.MESSAGE_TYPE_PUBACK) {
			result = "PUBACK";
		}
		else if (t == MqttWireMessage.MESSAGE_TYPE_PUBREC) {
			result = "PUBREC";
		}
		else if (t == MqttWireMessage.MESSAGE_TYPE_PUBCOMP) {
			result = "PUBCOMP";
		}
		
		else if (t == MqttWireMessage.MESSAGE_TYPE_PINGRESP) {
			result = "PINGRESP";
		}
		else if (t == MqttWireMessage.MESSAGE_TYPE_PINGREQ) {
			result = "PINGREQ";
		}
		else if (t == MqttWireMessage.MESSAGE_TYPE_SUBACK) {
			result = "SUBACK";
		}
		else if (t == MqttWireMessage.MESSAGE_TYPE_UNSUBACK) {
			result = "UNSUBACK";
		}
		else if (t == MqttWireMessage.MESSAGE_TYPE_PUBREL) {
			result = "PUBREL";
		}
		else if (t == MqttWireMessage.MESSAGE_TYPE_SUBSCRIBE) {
			result = "SUBSCRIBE";
		}
		else if (t == MqttWireMessage.MESSAGE_TYPE_UNSUBSCRIBE) {
			result = "UNSUBSCRIBE";
		}
		else if (t == MqttWireMessage.MESSAGE_TYPE_CONNECT) {
			result = "CONNECT";
		}
		else if (t == MqttWireMessage.MESSAGE_TYPE_CONNACK) {
			result = "CONNACK";
		}
		
		else if (t == MqttWireMessage.MESSAGE_TYPE_DISCONNECT) {
			result = "DISCONNECT";
		}
		
		return result;
	}
	public byte[] GetByteMessage() 
	{
		MqttWireMessage message=GetBytesFromType(Type);
		
		byte[] pay;
		byte[] header;
		try {
				pay = message.getPayload();			
				header=message.getHeader();
		
		
			return GenerateOutput(header, pay);
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		

	}
	
	@SuppressWarnings("null")
	private byte[] GenerateOutput(byte[] header,byte[] payload)
	{
		int heardersize=header.length;
		int payloadsize=payload.length;
		
		byte[] result = new byte[heardersize+payloadsize];
		int i=0;
		for(i=0;i<heardersize;i++)
		{
			result[i]=header[i];
		}
		for(int j=0;j<payloadsize;j++)
		{
			result[i]=payload[j];
			i++;
		}
		return result;
	}
	
}
