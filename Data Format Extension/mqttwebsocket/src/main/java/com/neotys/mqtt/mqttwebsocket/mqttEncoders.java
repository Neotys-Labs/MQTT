package com.neotys.mqtt.mqttwebsocket;


import com.neotys.extensions.codec.functions.Encoder;

public class mqttEncoders implements Encoder {
	@Override
	public byte[] apply(Object input) {
		// TODO add your encoding code
		try
		{
			final MqttNLMessage Message= (MqttNLMessage) input;
			return Message.GetByteMessage();
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	
	}

}
