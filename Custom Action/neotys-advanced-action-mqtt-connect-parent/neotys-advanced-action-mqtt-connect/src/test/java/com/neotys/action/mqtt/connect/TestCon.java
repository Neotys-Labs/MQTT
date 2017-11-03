package com.neotys.action.mqtt.connect;

import java.util.Properties;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import com.neotys.action.mqtt.connect.SSLUtil;
import org.junit.Test;

public class TestCon
{
    @Test
    public void TestSSL()
    {
        try
        {
            String Cacert="/home/hrexed/Mes documents sauvegardés/MQTT/certs/ca.crt";
            String Certfile="/home/hrexed/Mes documents sauvegardés/MQTT/certs/metro.crt";
            String ClientPrivateKey="/home/hrexed/Mes documents sauvegardés/MQTT/certs/metro.key";


           /* SSLContext context;
            KeyStore ts = KeyStore.getInstance("bks");
            ts.load(getResources().openRawResource(R.raw.ca), "123456".toCharArray());
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
            tmf.init(ts);
            TrustManager[] tm = tmf.getTrustManagers();
            context = SSLContext.getInstance("TLSv1.2");
            context.init(null, tm, null);
            SocketFactory factory = context.getSocketFactory();
            //conOpt.setSocketFactory(factory);
*/
            MqttClient client = new MqttClient("ssl://localhost:8883", "testclient");
            MqttConnectOptions connectOptions = new MqttConnectOptions();
            connectOptions.setUserName("metro");
           // connectOptions.setPassword("metro".toCharArray());
            //Properties props = new Properties();
            connectOptions.setConnectionTimeout(60);
            connectOptions.setKeepAliveInterval(60);
            connectOptions.setSocketFactory(SSLUtil.getSocketFactory(Cacert, Certfile, ClientPrivateKey, ""));
          //  connectOptions.setSocketFactory(factory);
          //  props.setProperty("com.ibm.ssl.keyStore", "jksFilePath.jks");
          //  props.setProperty("com.ibm.ssl.keyStorePassword","jksPassword");
           // connectOptions.setSSLProperties(props);
            client.connect(connectOptions);
            MqttMessage message = new MqttMessage(); message.setPayload("test".getBytes());
            client.publish("topic", message);
            client.disconnect();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}