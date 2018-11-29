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

import com.neotys.action.argument.ArgumentValidator;
import com.neotys.action.argument.Option;
import com.neotys.extensions.action.ActionParameter;

import static com.neotys.action.argument.DefaultArgumentValidator.ALWAYS_VALID;
import static com.neotys.action.argument.DefaultArgumentValidator.POSITIVE_LONG_VALIDATOR;
import static com.neotys.action.argument.Option.AppearsByDefault.False;
import static com.neotys.action.argument.Option.AppearsByDefault.True;
import static com.neotys.action.argument.Option.OptionalRequired.Optional;
import static com.neotys.action.mqtt.util.MqttArgumentValidator.HOST_ARGUMENT_VALIDATOR;
import static com.neotys.action.mqtt.util.MqttArgumentValidator.PROTOCOL_ARGUMENT_VALIDATOR;
import static com.neotys.action.mqtt.util.SharedParameterNames.*;
import static com.neotys.extensions.action.ActionParameter.Type.PASSWORD;
import static com.neotys.extensions.action.ActionParameter.Type.TEXT;

/**
 * MQTT broker connection options
 * <p>
 * Created by smader on 02/11/16.
 */
enum ConnectOption implements Option {
	/**
	 * Alias identifying the broker to publish or to subscribe to.
	 *
	 * <p>Assumes and requires that, for the current VU, all subsequent calls to publish, subscribe or disconnect actions
	 * will refer to this alias to identify the broker they are using.
	 * <p>The same User Path (VU) can therefore connect to several brokers and, for example, publish on a topic on one of
	 * the brokers and subscribe to topics on others.
	 *
	 * <p>Optional when User Path only refers to and uses a unique broker.
	 */
	ParamBrokerAlias(
			/* Name */             BrokerAlias.name(),
			/* OptionalRequired */  Optional,
			/* AppearsByDefault */  True,
			/* Type */              TEXT,
			/* DefaultValue */      "",
			/* Description */       "Alias identifying the broker in all further interactions with it. " +
			"If User Path connects to one broker only, this parameter can be left out.",
			/* ArgumentValidator */ ALWAYS_VALID),

	/**
	 * Connection protocol
	 */
	ParamProtocol(
			/* Name */              Protocol.name(),
			/* OptionalRequired */  Optional,
			/* AppearsByDefault */  True,
			/* Type */              TEXT,
			/* DefaultValue */      "tcp",
			/* Description */       "Protocol used to communicate with the MQTT broker. Default is: \"tcp\".",
			/* ArgumentValidator */ PROTOCOL_ARGUMENT_VALIDATOR),

	/**
	 * Broker host
	 */
	ParamHost(
			/* Name */              Host.name(),
			/* Optional Required */ Optional,
			/* AppearsByDefault */  True,
			/* Type */              TEXT,
			/* DefaultValue */      "localhost",
			/* Description */       "Host of the MQTT broker (IP address or domain name).",
			/* ArgumentValidator */ HOST_ARGUMENT_VALIDATOR),
	/**
	 * Broker port
	 */
	ParamPort(
			/* Name */              Port.name(),
			/* Optional Required */ Optional,
			/* AppearsByDefault */  True,
			/* Type */              TEXT,
			/* DefaultValue */      "1883",
			/* Description */       "Port number of the MQTT broker. Defaults to 1883.",
			/* ArgumentValidator */ POSITIVE_LONG_VALIDATOR),
	/**
	 * User name
	 */
	ParamUserName(
			/* Name */              UserName.name(),
			/* Optional Required */ Optional,
			/* AppearsByDefault */  True,
			/* Type */              TEXT,
			/* DefaultValue */      "",
			/* Description */       "Username for authenticating and authorizing the client on the MQTT broker.",
			/* ArgumentValidator */ ALWAYS_VALID),
	/**
	 * Password
	 */
	ParamPassword(
			/* Name */              Password.name(),
			/* Optional Required */ Optional,
			/* AppearsByDefault */  True,
			/* Type */              PASSWORD,
			/* DefaultValue */      "",
			/* Description */       "Password for authenticating and authorizing the client on the MQTT broker.\n In the case of SSL specify the password of you private key",
			/* ArgumentValidator */ ALWAYS_VALID),

	/**
	 * Client cACERT
	 */
	ParamCACert(
			/* Name */              "CaCert",
			/* Optional Required */ Optional,
			/* AppearsByDefault */  False,
			/* Type */              TEXT,
			/* DefaultValue */      "",
			/* Description */       "Path to the Server Certificate",
			/* ArgumentValidator */ ALWAYS_VALID),


	/**
	 * certfile
	 */
	ParamClientCertFile(
			/* Name */              "ClientCertFile",
			/* Optional Required */ Optional,
			/* AppearsByDefault */  False,
			/* Type */              TEXT,
			/* DefaultValue */      "",
			/* Description */       "Path to the Client certificate",
			/* ArgumentValidator */ ALWAYS_VALID),
	/**
	 * ClientPrivateKey
	 */
	ParamClientPrivateKey(
			/* Name */              "ClientPrivateKey",
			/* Optional Required */ Optional,
			/* AppearsByDefault */  False,
			/* Type */              TEXT,
			/* DefaultValue */      "",
			/* Description */       "Path to the client private key file",
			/* ArgumentValidator */ ALWAYS_VALID),

	/**
	 * ClientPrivateKeyPassword
	 */
	ParamClientPrivateKeyPassword(
			/* Name */              "ClientPrivateKeyPassword",
			/* Optional Required */ Optional,
			/* AppearsByDefault */  False,
			/* Type */              PASSWORD,
			/* DefaultValue */      "",
			/* Description */       "The password of the private key",
			/* ArgumentValidator */ ALWAYS_VALID),

	/**
	 * keystore
	 */
	ParamKeyStoreFile(
			/* Name */              "KeyStoreFile",
			/* Optional Required */ Optional,
			/* AppearsByDefault */  False,
			/* Type */              TEXT,
			/* DefaultValue */      "",
			/* Description */       "Path to the keystore",
			/* ArgumentValidator */ ALWAYS_VALID),
	/**
	 * ClientPrivateKey
	 */
	ParamKeyStorePassword(
			/* Name */              "KeyStorePassword",
			/* Optional Required */ Optional,
			/* AppearsByDefault */  False,
			/* Type */              PASSWORD,
			/* DefaultValue */      "",
			/* Description */       "Password of the keystore",
			/* ArgumentValidator */ ALWAYS_VALID),
	/**
	 * keystore
	 */
	ParamTrustStoreFile(
			/* Name */              "TrustStoreFile",
			/* Optional Required */ Optional,
			/* AppearsByDefault */  False,
			/* Type */              TEXT,
			/* DefaultValue */      "",
			/* Description */       "Path to the truststore",
			/* ArgumentValidator */ ALWAYS_VALID),
	/**
	 * ClientPrivateKey
	 */
	ParamTrustStorePassword(
			/* Name */              "TrustStorePassword",
			/* Optional Required */ Optional,
			/* AppearsByDefault */  False,
			/* Type */              PASSWORD,
			/* DefaultValue */      "",
			/* Description */       "Password of the truststore",
			/* ArgumentValidator */ ALWAYS_VALID),
	ParamClientId(
			/* Name */              ClientId.name(),
			/* OptionalRequired */  Optional,
			/* AppearsByDefault */  True,
			/* Type */              TEXT,
			/* DefaultValue */      "",
			/* Description */       "Identifier of the MQTT client connecting to the MQTT broker (expected to be unique per broker, default is a randomly generated client identifier guaranteed to be unique per User Path and per Load Generator).",
			/* ArgumentValidator */ ALWAYS_VALID),
	ParamClientTrustAll(
			/* Name */              "TrustAllCert",
			/* OptionalRequired */  Optional,
			/* AppearsByDefault */  False,
			/* Type */              TEXT,
			/* DefaultValue */      "",
			/* Description */       "Trust all server certificates.",
			/* ArgumentValidator */ ALWAYS_VALID),
	;


	private final String name;
	private final OptionalRequired optionalRequired;
	private final AppearsByDefault appearsByDefault;
	private final ActionParameter.Type type;
	private final String defaultValue;
	private final String description;
	private final ArgumentValidator argumentValidator;

	ConnectOption(final String name, final OptionalRequired optionalRequired,
				  final AppearsByDefault appearsByDefault,
				  final ActionParameter.Type type, final String defaultValue, final String description,
				  final ArgumentValidator argumentValidator) {
		this.name = name;
		this.optionalRequired = optionalRequired;
		this.appearsByDefault = appearsByDefault;
		this.type = type;
		this.defaultValue = defaultValue;
		this.description = description;
		this.argumentValidator = argumentValidator;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public OptionalRequired getOptionalRequired() {
		return optionalRequired;
	}

	@Override
	public AppearsByDefault getAppearsByDefault() {
		return appearsByDefault;
	}

	@Override
	public ActionParameter.Type getType() {
		return type;
	}

	@Override
	public String getDefaultValue() {
		return defaultValue;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public ArgumentValidator getArgumentValidator() {
		return argumentValidator;
	}

}
