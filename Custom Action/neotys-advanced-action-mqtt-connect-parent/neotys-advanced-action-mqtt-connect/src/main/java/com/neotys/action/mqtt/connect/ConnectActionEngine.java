/*
 * Copyright (c) 2016, Neotys
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Neotys nor the names of its contributors may be
 *       used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL NEOTYS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.neotys.action.mqtt.connect;

import java.io.File;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.base.Optional;
import com.neotys.action.mqtt.util.MqttClientWrapper;
import com.neotys.extensions.action.engine.Logger;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

import com.google.common.base.Strings;
import com.neotys.extensions.action.ActionParameter;
import com.neotys.extensions.action.engine.ActionEngine;
import com.neotys.extensions.action.engine.Context;
import com.neotys.extensions.action.engine.SampleResult;

import static com.neotys.action.argument.Arguments.getArgumentLogString;
import static com.neotys.action.argument.Arguments.parseArguments;

import static com.neotys.action.mqtt.connect.ConnectOption.*;
import static com.neotys.action.mqtt.util.MqttParameterUtilities.STATUS_CODE_ERROR_CONNECTION;
import static com.neotys.action.mqtt.util.MqttParameterUtilities.STATUS_CODE_INVALID_PARAMETER;
import static com.neotys.action.mqtt.util.MqttParameterUtilities.NL_MQTT_VU_CONTEXT;
import static com.neotys.action.mqtt.util.MqttParameterUtilities.SetResultAsError;
import static com.neotys.action.mqtt.util.MqttParameterUtilities.ExtractBrokerURL;
import static com.neotys.action.result.ResultFactory.STATUS_CODE_OK;

/**
 * Connect to an MQTT broker
 */
public class ConnectActionEngine implements ActionEngine {
	@Override
	public SampleResult execute(final Context context, final List<ActionParameter> parameters) {
        final Logger logger = context.getLogger();
        SampleResult sampleResult = new SampleResult();

        final Map<String, Optional<String>> parsedArgs;
        try {
			parsedArgs = parseArguments(parameters, ConnectOption.values());
		} catch (final IllegalArgumentException iae) {
            SetResultAsError(sampleResult, STATUS_CODE_INVALID_PARAMETER, "Invalid parameter", iae);
            logger.error(sampleResult.getResponseContent());
            return sampleResult;
        }

		if (logger.isDebugEnabled()) {
			logger.debug("Executing " + this.getClass().getName() + " with parameters: "
					+ getArgumentLogString(parsedArgs, ConnectOption.values()));
		}

		String clientId;
		final Optional<String> clientIdOptional = parsedArgs.get(ConnectOption.ParamClientId.getName());
		if (clientIdOptional.isPresent() && !Strings.isNullOrEmpty(clientIdOptional.get())) {
			clientId = clientIdOptional.get();
		}
		else clientId = UUID.randomUUID().toString();

		String userName = null;
		final Optional<String> userNameOptional = parsedArgs.get(ConnectOption.ParamUserName.getName());
		if (userNameOptional.isPresent() && !Strings.isNullOrEmpty(userNameOptional.get())) {
            userName = userNameOptional.get();
		}

		String password = null;
        final Optional<String> passwordOptional = parsedArgs.get(ConnectOption.ParamPassword.getName());
		if (passwordOptional.isPresent() && !Strings.isNullOrEmpty(passwordOptional.get())) {
			password = passwordOptional.get();
		}
        String protocol = null;
        final Optional<String> protocolOptional = parsedArgs.get(ParamProtocol.getName());
        if (protocolOptional.isPresent() && !Strings.isNullOrEmpty(protocolOptional.get())) {
            protocol = protocolOptional.get();
        }

		String mqttBrokerURL = ExtractBrokerURL(parsedArgs);
        String brokerAlias;
        final Optional<String> brokerAliasOptional = parsedArgs.get(ConnectOption.ParamBrokerAlias.getName());
        if (brokerAliasOptional.isPresent() && !Strings.isNullOrEmpty(brokerAliasOptional.get())) {
            brokerAlias = brokerAliasOptional.get();
        }
        // Ensure backward compatibility: if no brokerAlias is specified consider the brokerURL as the alias
        else brokerAlias = mqttBrokerURL;

        // Construct and prepare connect options
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();

        // TODO make these public and configurable
		mqttConnectOptions.setCleanSession(true);
		mqttConnectOptions.setKeepAliveInterval(30);
        if(protocol.equalsIgnoreCase("ssl")||protocol.equalsIgnoreCase("tls"))
        {
            String Cacert = null;
            final Optional<String> CaCertOptional = parsedArgs.get(ParamCACert.getName());
            if (CaCertOptional.isPresent() && !Strings.isNullOrEmpty(CaCertOptional.get())) {
                Cacert = CaCertOptional.get();
                if(!IsFileExists(Cacert)) {
                    SetResultAsError(sampleResult, STATUS_CODE_INVALID_PARAMETER, "CACERT does not exist " + Cacert);
                    logger.error(sampleResult.getResponseContent());
                    return sampleResult;
                }

            }
            else
            {
                SetResultAsError(sampleResult, STATUS_CODE_INVALID_PARAMETER, "Invalid parameter CACERT");
                logger.error(sampleResult.getResponseContent());
                return sampleResult;
            }

            String Certfile = null;
            final Optional<String> CertfileOptional = parsedArgs.get(ParamCertFile.getName());
            if (CertfileOptional.isPresent() && !Strings.isNullOrEmpty(CertfileOptional.get())) {
                Certfile = CertfileOptional.get();
                if(!IsFileExists(Certfile)) {
                    SetResultAsError(sampleResult, STATUS_CODE_INVALID_PARAMETER, "Certfile does not exist " + Certfile);
                    logger.error(sampleResult.getResponseContent());
                    return sampleResult;
                }
            }
            else
            {
                SetResultAsError(sampleResult, STATUS_CODE_INVALID_PARAMETER, "Invalid parameter CERTFILE");
                logger.error(sampleResult.getResponseContent());
                return sampleResult;
            }

            String ClientPrivateKey = null;
            final Optional<String> ClientPrivateKeyOptional = parsedArgs.get(ParamClientPrivateKey.getName());
            if (ClientPrivateKeyOptional.isPresent() && !Strings.isNullOrEmpty(ClientPrivateKeyOptional.get())) {
                ClientPrivateKey = ClientPrivateKeyOptional.get();
                if(!IsFileExists(ClientPrivateKey)) {
                    SetResultAsError(sampleResult, STATUS_CODE_INVALID_PARAMETER, "ClientPrivateKey does not exist " + ClientPrivateKey);
                    logger.error(sampleResult.getResponseContent());
                    return sampleResult;
                }
            }
            else
            {
                SetResultAsError(sampleResult, STATUS_CODE_INVALID_PARAMETER, "Invalid parameter CLIENTPRIVATEKEY");
                logger.error(sampleResult.getResponseContent());
                return sampleResult;
            }
            try
            {
                System.out.println("Cacert :"+Cacert);
                System.out.println("crtert :"+Certfile);
                System.out.println("key :"+ ClientPrivateKey);

                if(password == null)
                    password="";

                mqttConnectOptions.setSocketFactory(SSLUtil.getSocketFactory(Cacert, Certfile, ClientPrivateKey, password));
            }
            catch(Exception e)
            {
                SetResultAsError(sampleResult, STATUS_CODE_INVALID_PARAMETER, "Invalid parameter", e);
                logger.error(sampleResult.getResponseContent());
                return sampleResult;
            }

        }
        else {

            if (!Strings.isNullOrEmpty(userName)) {
                mqttConnectOptions.setUserName(userName);
                mqttConnectOptions.setPassword(password.toCharArray());
            }
        }
        sampleResult.sampleStart();
        try {
            // Peek into the context to check that there isn't already an existing mqttClient for this VU and the specified brokerAlias
            Map<String, MqttClientWrapper> mqttVUContext = (Map<String, MqttClientWrapper>) context.getCurrentVirtualUser().get(NL_MQTT_VU_CONTEXT);

            // if no mqtt context has been associated to the current UP do it
            if (mqttVUContext == null) {
                mqttVUContext = new Hashtable<String, MqttClientWrapper>();
                context.getCurrentVirtualUser().put(NL_MQTT_VU_CONTEXT, mqttVUContext);
            }
            MqttClientWrapper mqttClientWrapper = mqttVUContext.get(brokerAlias);
            if (mqttClientWrapper != null) {
                if (mqttClientWrapper.isConnected()) {
                    String errorMessage = "Already connected to the MQTT broker: " + mqttClientWrapper;
                    SetResultAsError(sampleResult, STATUS_CODE_ERROR_CONNECTION, errorMessage, null);

                    logger.error(sampleResult.getResponseContent());
                    sampleResult.sampleEnd();
                    return sampleResult;
                }
            }
            else {
                 mqttClientWrapper = new MqttClientWrapper(new MqttClient(mqttBrokerURL, clientId), brokerAlias);
            }

            // Do the actual connection to the MQTT broker
            mqttClientWrapper.getMqttClient().connect(mqttConnectOptions);

            // Put the new MQTT client in the VU context only if connection succeeds
            mqttVUContext.put(brokerAlias, mqttClientWrapper);

            // Connection succeeded
            sampleResult.setStatusCode(STATUS_CODE_OK);
            sampleResult.setResponseContent("Successfully connected to MQTT broker: " + mqttClientWrapper);
            if (logger.isDebugEnabled()) {
                logger.debug(sampleResult.getResponseContent());
            }

        } catch (final MqttException mqttException) {
            String errorMessage =
                    "Connection to MQTT broker: " + brokerAlias + " / " + mqttBrokerURL +
                            ") failed : " + mqttException.getMessage();
            SetResultAsError(sampleResult, STATUS_CODE_ERROR_CONNECTION, errorMessage, mqttException);
            logger.error(sampleResult.getResponseContent(), mqttException);
        }
        sampleResult.sampleEnd();
        return sampleResult;
 	}
    public boolean  IsFileExists(String strfile)
    {
        boolean result;
        result = false;
        File tmp = new File(strfile);
        if(tmp.exists())
            result= true;

        return result;
    }
	@Override
	public void stopExecute() { /* NOOP */ }
}
