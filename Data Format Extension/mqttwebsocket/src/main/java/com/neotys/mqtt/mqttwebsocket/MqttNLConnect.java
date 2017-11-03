package com.neotys.mqtt.mqttwebsocket;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.lang.reflect.Field;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.internal.wire.CountingInputStream;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttConnect;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttWireMessage;
import org.eclipse.paho.client.mqttv3.internal.wire.MultiByteInteger;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;

public class MqttNLConnect {

	public String clientId;
	public boolean cleanSession;
	public String willMessage;
	public String userName;
	public char[] password;
	public int keepAliveInterval;
	public String willDestination;
	public int MqttVersion;
	public String NeoLoadIdentifier;
	
	public MqttNLConnect(MqttWireMessage mess)
	{
		MqttConnect conn=(MqttConnect)mess;
		
	
		try {
			Field f = conn.getClass().getDeclaredField("clientId"); 
			f.setAccessible(true);
			clientId = (String) f.get(conn);
			f = conn.getClass().getDeclaredField("cleanSession"); 
			f.setAccessible(true);
			cleanSession = (boolean) f.get(conn);
			f = conn.getClass().getDeclaredField("userName");
			f.setAccessible(true);
			userName = (String) f.get(conn);
			 f = conn.getClass().getDeclaredField("password"); 
			 f.setAccessible(true);
			password = (char[]) f.get(conn);
			 f = conn.getClass().getDeclaredField("keepAliveInterval");
			 f.setAccessible(true);
			keepAliveInterval = (int) f.get(conn);
			 f = conn.getClass().getDeclaredField("willDestination");
			 f.setAccessible(true);
			willDestination = (String) f.get(conn);
			 f = conn.getClass().getDeclaredField("MqttVersion");
			 f.setAccessible(true);
			MqttVersion = (int) f.get(conn);
			NeoLoadIdentifier="CONNECT-0";
			
		
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
			
		
	}
	  private static Result<String> decodeString(ByteBuf buffer) {
	        return decodeString(buffer, 0, Integer.MAX_VALUE);
	    }
	  private static Result<String> decodeString(ByteBuf buffer, int minBytes, int maxBytes) {
	        final Result<Integer> decodedSize = decodeMsbLsb(buffer);
	        int size = decodedSize.value;
	        int numberOfBytesConsumed = decodedSize.numberOfBytesConsumed;
	        if (size < minBytes || size > maxBytes) {
	            buffer.skipBytes(size);
	            numberOfBytesConsumed += size;
	            return new Result<String>(null, numberOfBytesConsumed);
	        }
	        ByteBuf buf = buffer.readBytes(size);
	        numberOfBytesConsumed += size;
	        return new Result<String>(buf.toString(CharsetUtil.UTF_8), numberOfBytesConsumed);
	    }
	  private static Result<Integer> decodeMsbLsb(ByteBuf buffer) {
	        return decodeMsbLsb(buffer, 0, 65535);
	    }
	  private static Result<Integer> decodeMsbLsb(ByteBuf buffer, int min, int max) {
	        short msbSize = buffer.readUnsignedByte();
	        short lsbSize = buffer.readUnsignedByte();
	        final int numberOfBytesConsumed = 2;
	        int result = msbSize << 8 | lsbSize;
	    //   int result =msbSize ;
	        if (result < min || result > max) {
	            result = -1;
	        }
	        return new Result<Integer>(result, numberOfBytesConsumed);
	    }
	  
		protected static MultiByteInteger readMBI(DataInputStream in) throws IOException {
			byte digit;
			long msgLength = 0;
			int multiplier = 1;
			int count = 0;
			
			do {
				digit = in.readByte();
				count++;
				msgLength += ((digit & 0x7F) * multiplier);
				multiplier *= 128;
			} while ((digit & 0x80) != 0);
			
			return new MultiByteInteger(msgLength, count);
		}

	public MqttNLConnect(MqttWireMessage message, byte[] input) {
		// TODO Auto-generated constructor stub
		String tmp;
		MqttConnect conn=(MqttConnect)message;
		String connectFlags;
		ByteBuf buffer = null;
		
		ByteArrayInputStream bis = new ByteArrayInputStream(input);
		CountingInputStream counter = new CountingInputStream(bis);
		DataInputStream in = new DataInputStream(counter);
		int first;
		try {
			first = in.readUnsignedByte();
			byte type = (byte) (first >> 4);
			byte info = (byte) (first &= 0x0f);
			long remLen = readMBI(in).getValue();
			long totalToRead = counter.getCounter() + remLen;

			MqttWireMessage result;
			long remainder = totalToRead - counter.getCounter();
			byte[] data = new byte[0];
			// The remaining bytes must be the payload...
			if (remainder > 0) {
				data = new byte[(int) remainder];
				in.readFully(data, 0, data.length);
			}
			buffer=Unpooled.wrappedBuffer(data);
			 final Result<String> protoString = decodeString(buffer);
		       
		        int numberOfBytesConsumed = protoString.numberOfBytesConsumed;

		        final byte version = buffer.readByte();
		        final int b1 = buffer.readUnsignedByte();
		        numberOfBytesConsumed += 2;

		        final Result<Integer> keepAlive = decodeMsbLsb(buffer);
		        numberOfBytesConsumed += keepAlive.numberOfBytesConsumed;

		        boolean hasUserName = (b1 & 0x80) == 0x80;
		        boolean hasPassword = (b1 & 0x40) == 0x40;
		        boolean willRetain = (b1 & 0x20) == 0x20;
		        int willQos = (b1 & 0x18) >> 3;
		        boolean willFlag = (b1 & 0x04) == 0x04;
		        cleanSession  = (b1 & 0x02) == 0x02;
		
		        MqttVersion=version;
			
		        final Result<String> decodedClientId = decodeString(buffer);
		        clientId = decodedClientId.value;
		       
		         numberOfBytesConsumed = decodedClientId.numberOfBytesConsumed;

		        Result<String> decodedWillTopic = null;
		        Result<String> decodedWillMessage = null;
		        if (willFlag) {
		            decodedWillTopic = decodeString(buffer, 0, 32767);
		            willDestination=decodedWillTopic.value;
		            numberOfBytesConsumed += decodedWillTopic.numberOfBytesConsumed;
		            decodedWillMessage = decodeAsciiString(buffer);
		            willMessage=decodedWillMessage.value;
		            numberOfBytesConsumed += decodedWillMessage.numberOfBytesConsumed;
		        }
		        Result<String> decodedUserName = null;
		        Result<String> decodedPassword = null;
		        if (hasUserName) {
		            decodedUserName = decodeString(buffer);
		            numberOfBytesConsumed += decodedUserName.numberOfBytesConsumed;
		            userName=decodedUserName.value;
		        }
		        if (hasPassword) {
		            decodedPassword = decodeString(buffer);
		            numberOfBytesConsumed += decodedPassword.numberOfBytesConsumed;
		            password=(decodedPassword.value).toCharArray();
		            		}
		        Field f = conn.getClass().getDeclaredField("keepAliveInterval");
				 f.setAccessible(true);
				keepAliveInterval = (int) f.get(conn);
				NeoLoadIdentifier="CONNECT-0";
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
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
	  private static Result<String> decodeAsciiString(ByteBuf buffer) {
	        Result<String> result = decodeString(buffer, 0, Integer.MAX_VALUE);
	        final String s = result.value;
	        for (int i = 0; i < s.length(); i++) {
	            if (s.charAt(i) > 127) {
	                return new Result<String>(null, result.numberOfBytesConsumed);
	            }
	        }
	        return new Result<String>(s, result.numberOfBytesConsumed);
	    }
	protected String decodeUTF8(DataInputStream input) throws MqttException
	{
		int encodedLength;
		try {
			encodedLength = input.readUnsignedShort();

			byte[] encodedString = new byte[encodedLength];
				input.readFully(encodedString);

			return new String(encodedString, "UTF-8");
		} catch (IOException ex) {
			throw new MqttException(ex);
		}
	}
	public String getClientID(byte[] payload)
	{
		return new String(payload).substring(1,23);
	}
	
	public Byte getBit(byte content,int position)
	{
	   return (byte) ((content >> position) & 1);
	}
	private static final class Result<T> {

        private final T value;
        private final int numberOfBytesConsumed;

        Result(T value, int numberOfBytesConsumed) {
            this.value = value;
            this.numberOfBytesConsumed = numberOfBytesConsumed;
        }
    }
}
