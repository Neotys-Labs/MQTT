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

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.base.Supplier;
import com.neotys.action.argument.Option;
import com.neotys.action.mqtt.util.MqttClientWrapper;
import com.neotys.extensions.action.ActionParameter;
import com.neotys.extensions.action.engine.ActionEngine;
import com.neotys.extensions.action.engine.Context;
import com.neotys.extensions.action.engine.Logger;
import com.neotys.extensions.action.engine.SampleResult;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.neotys.action.argument.Arguments.getArgumentLogString;
import static com.neotys.action.argument.Arguments.parseArguments;
import static com.neotys.action.mqtt.connect.ConnectOption.*;
import static com.neotys.action.mqtt.util.MqttParameterUtilities.*;
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
			setResultAsError(sampleResult, STATUS_CODE_INVALID_PARAMETER, "Invalid parameter", iae);
			logger.error(sampleResult.getResponseContent());
			return sampleResult;
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Executing " + this.getClass().getName() + " with parameters: "
					+ getArgumentLogString(parsedArgs, ConnectOption.values()));
		}

		final String clientId = getParameter(parsedArgs, ParamClientId, () -> UUID.randomUUID().toString());
		final String userName = getParameter(parsedArgs, ParamUserName, null);
		final String password = getParameter(parsedArgs, ParamPassword, null);
		final String protocol = getParameter(parsedArgs, ParamProtocol, null);

		final boolean trustAllCa = getParameter(parsedArgs, ParamClientTrustAll, ()->"false").equalsIgnoreCase("true");

		final String caPath = getParameter(parsedArgs, ParamCACert,null);
		final String certfile = getParameter(parsedArgs, ParamClientCertFile, null);



		final String mqttBrokerURL = extractBrokerURL(parsedArgs);

		// Ensure backward compatibility: if no brokerAlias is specified consider the brokerURL as the alias
		final String brokerAlias = getParameter(parsedArgs, ParamBrokerAlias, () -> mqttBrokerURL);


		// Construct and prepare connect options
		final MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();

		// TODO make these public and configurable
		mqttConnectOptions.setCleanSession(true);
		mqttConnectOptions.setKeepAliveInterval(30);

		if (isSecureProtocol(protocol)) {
			try {
				if (trustAllCa || caPath != null || certfile != null) {
					SSLFactoryUtils sslFactoryUtils = new SSLFactoryUtils();
					if (trustAllCa) {
						sslFactoryUtils.trustAll();
					}
					if (caPath != null) {
						sslFactoryUtils.trustCa(caPath);
					}

					if (certfile != null) {
						final String clientPrivateKey = validateFileParameter(parsedArgs, ParamClientPrivateKey);
						final String privateKeyPassword = getParameter(parsedArgs, ParamClientPrivateKeyPassword, () -> "");
						sslFactoryUtils.clientCert(certfile, clientPrivateKey, privateKeyPassword);
					}

					mqttConnectOptions.setSocketFactory(sslFactoryUtils.build());
				} else if (parsedArgs.get(ParamTrustStoreFile.getName()).isPresent()) {
					final Properties sslProperties = newSslPropertiesFromKeyStore(parsedArgs);
					mqttConnectOptions.setSSLProperties(sslProperties);
				}
			} catch (final Exception e) {
				setResultAsError(sampleResult, STATUS_CODE_INVALID_PARAMETER, e.getMessage());
				logger.error(sampleResult.getResponseContent());
				return sampleResult;
			}
		}
		if (!isNullOrEmpty(userName)) {
			mqttConnectOptions.setUserName(userName);
		}
		if(!isNullOrEmpty(password)){
			mqttConnectOptions.setPassword(Strings.nullToEmpty(password).toCharArray());
		}
		sampleResult.sampleStart();
		try {
			// Peek into the context to check that there isn't already an existing mqttClient for this VU and the specified brokerAlias
			Map<String, MqttClientWrapper> mqttVUContext = (Map<String, MqttClientWrapper>) context.getCurrentVirtualUser().get(NL_MQTT_VU_CONTEXT);

			// if no mqtt context has been associated to the current UP do it
			if (mqttVUContext == null) {
				mqttVUContext = new Hashtable<>();
				context.getCurrentVirtualUser().put(NL_MQTT_VU_CONTEXT, mqttVUContext);
			}
			MqttClientWrapper mqttClientWrapper = mqttVUContext.get(brokerAlias);
			if (mqttClientWrapper != null) {
				if (mqttClientWrapper.isConnected()) {
					String errorMessage = "Already connected to the MQTT broker: " + mqttClientWrapper;
					setResultAsError(sampleResult, STATUS_CODE_ERROR_CONNECTION, errorMessage, null);

					logger.error(sampleResult.getResponseContent());
					sampleResult.sampleEnd();
					return sampleResult;
				}
			} else {
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
			setResultAsError(sampleResult, STATUS_CODE_ERROR_CONNECTION, errorMessage, mqttException);
			logger.error(sampleResult.getResponseContent(), mqttException);
		}
		sampleResult.sampleEnd();
		return sampleResult;
	}

	private String getParameter(final Map<String, Optional<String>> parsedArgs, final Option key, Supplier<String> defaultValue) {
		final String stringOptional = parsedArgs.get(key.getName()).orNull();
		if (isNullOrEmpty(stringOptional)) {
			if (defaultValue != null) {
				return defaultValue.get();
			} else {
				return null;
			}
		} else {
			return stringOptional;
		}
	}

	private static Properties newSslPropertiesFromKeyStore(final Map<String, Optional<String>> parsedArgs) throws FileNotFoundException {
		final String keyStoreFile = validateFileParameter(parsedArgs, ParamKeyStoreFile);
		final String keystorePassword = validateParameter(parsedArgs, ParamKeyStorePassword);
		final String truststorePassword = validateParameter(parsedArgs, ParamTrustStorePassword);
		final String truststoreFile = validateFileParameter(parsedArgs, ParamTrustStoreFile);

		return getSSLProperties(keyStoreFile, keystorePassword, truststoreFile, truststorePassword);
	}


	private static String validateParameter(final Map<String, Optional<String>> parsedArgs, final ConnectOption option) {
		final Optional<String> parameterOptional = parsedArgs.get(option.getName());
		if (parameterOptional.isPresent() && !isNullOrEmpty(parameterOptional.get())) {
			return parameterOptional.get();
		} else {
			throw new IllegalArgumentException("Missing parameter " + option.getName());
		}
	}

	private static String validateFileParameter(final Map<String, Optional<String>> parsedArgs,
	                                            final ConnectOption option) throws FileNotFoundException {
		final Optional<String> parameterOptional = parsedArgs.get(option.getName());
		if (parameterOptional.isPresent() && !isNullOrEmpty(parameterOptional.get())) {
			final String path = parameterOptional.get();
			if (!isFileExists(path)) {
				throw new FileNotFoundException(option.getName() + " does not exist " + path);
			}
			return path;
		} else {
			throw new IllegalArgumentException("Missing parameter" + option.getName());
		}
	}

	private static boolean isSecureProtocol(final String protocol) {
		return protocol != null
				&& (protocol.equalsIgnoreCase("ssl") || protocol.equalsIgnoreCase("tls"));
	}

	private static boolean isFileExists(final String filePath) {
		return new File(filePath).exists();
	}

	@Override
	public void stopExecute() { /* NOOP */ }

	static Properties getSSLProperties(final String keystore, final String keystorePassword, final String trustore,
	                                   final String truststorepassword) {
		Properties props = new Properties();

		props.setProperty("com.ibm.ssl.protocol", "SSL");
		props.setProperty("com.ibm.ssl.keyStoreType", "JKS");
		props.setProperty("com.ibm.ssl.keyStore", keystore);
		props.setProperty("com.ibm.ssl.keyStorePassword", keystorePassword);
		props.setProperty("com.ibm.ssl.trustStore", trustore);
		props.setProperty("com.ibm.ssl.trustStorePassword", truststorepassword);

		return props;
	}
}
