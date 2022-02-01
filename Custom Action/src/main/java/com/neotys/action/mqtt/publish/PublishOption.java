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
package com.neotys.action.mqtt.publish;

import com.neotys.action.argument.ArgumentValidator;
import com.neotys.action.argument.Option;
import com.neotys.extensions.action.ActionParameter;

import static com.neotys.action.argument.DefaultArgumentValidator.ALWAYS_VALID;
import static com.neotys.action.argument.DefaultArgumentValidator.POSITIVE_LONG_VALIDATOR;
import static com.neotys.action.argument.Option.AppearsByDefault.Hided;
import static com.neotys.action.argument.Option.AppearsByDefault.True;
import static com.neotys.action.argument.Option.OptionalRequired.Optional;
import static com.neotys.action.argument.Option.OptionalRequired.Required;
import static com.neotys.action.mqtt.util.MqttArgumentValidator.QOS_ARGUMENT_VALIDATOR;
import static com.neotys.action.mqtt.util.SharedParameterNames.*;
import static com.neotys.extensions.action.ActionParameter.Type.TEXT;

/**
 * Publish to MQTT topic parameters
 *
 * Created by smader on 16/11/16.
 */
enum PublishOption implements Option {
    /**
     * Alias identifying the broker to publish to.
     *
     * <p>Assumes and requires that, for the current VU, a connection (using the ConnectAction) has been established and associated to the
     * specified BrokerAlias.
     *
     * <p>Optional when User Path only refers to and uses a unique broker.
     */
    ParamBrokerAlias(
            /* Name */              BrokerAlias.name(),
            /* OptionalRequired */  Optional,
            /* AppearsByDefault */  True,
            /* Type */              TEXT,
            /* DefaultValue */      "",
            /* Description */       "Alias identifying the broker." +
            "Assumes and requires that, for the current User Path, a connection (using the ConnectAction) has been established and associated to the " +
            "specified BrokerAlias. If User Path connects to one broker only, this parameter can be left out.",
            /* ArgumentValidator */ ALWAYS_VALID),

    /**
     * The name of the topic on which to publish the message.
     *
     * <p>Optional for backward compatibility with the MQTTtopic parameter.
     */
    ParamTopic(
            /* Name */              Topic.name(),
            /* OptionalRequired */  Optional,
            /* AppearsByDefault */  True,
            /* Type */              TEXT,
            /* DefaultValue */      "",
            /* Description */       "The name of the topic on which to publish the message.",
            /* ArgumentValidator */ ALWAYS_VALID),

    /**
     * Content of the message to publish.
     */
    ParamMessage(
            /* Name */              Message.name(),
            /* OptionalRequired */  Required,
            /* AppearsByDefault */  True,
            /* Type */              TEXT,
            /* DefaultValue */      "",
            /* Description */       "Content of the message to publish.",
            /* ArgumentValidator */ ALWAYS_VALID),

    /**
     * Qos of the message being sent.
     *
     * The level (0,1 or 2) determines the guarantee of a message reaching the other end (client or broker).
     * <p>Default is 0, the lowest QoS
     */
    ParamQoS(
            /* Name */              QoS.name(),
            /* OptionalRequired */  Optional,
            /* AppearsByDefault */  True,
            /* Type */              TEXT,
            /* DefaultValue */      "0",
            /* Description */       "A Quality of Service Level (QoS) for this message, the level (0,1 or 2) determines the level of guarantee " +
            "of a message reaching the other end.",
            /* ArgumentValidator */ QOS_ARGUMENT_VALIDATOR),

    /**
     * The Retain flag determines if the message will be saved by the broker for the specified topic as last known good value.
     * New clients that subscribe to that topic will receive the last retained message on that topic instantly after subscribing.
     */
//    ParamRetained(
//            /* Name */              Retained.name(),
//            /* OptionalRequired */  Optional,
//            /* AppearsByDefault */  True,
//            /* Type */              TEXT,
//            /* DefaultValue */      "false",
//            /* Description */       "Determines if the message will be saved by the broker for the specified topic as last known good value.",
//            /* ArgumentValidator */ BOOLEAN_VALIDATOR),

    /**
     * Deprecated: The name of the topic on which to publish the message.
     * As of version 1.0.1 of this action, the 'Topic' parameter contains the value.
     */
    ParamMQTTTopic(
            /* Name */              "MQTTtopic",
            /* OptionalRequired */  Optional,
            /* AppearsByDefault */  Hided,
            /* Type */              TEXT,
            /* DefaultValue */      null,
            /* Description */       "Deprecated: The name of the topic on which to publish the message. " +
            "As of version 1.0.1 of this action use the 'Topic' parameter.",
            /* ArgumentValidator */ ALWAYS_VALID),

    /**
     * Deprecated: Qos of the message being sent.
     *
     * The level (0,1 or 2) determines the guarantee of a message reaching the other end (client or broker).
     * <p>Default is 0, the lowest QoS
     */
    ParamMQTTQos(
            /* Name */              "MQTTQos",
            /* OptionalRequired */  Optional,
            /* AppearsByDefault */  Hided,
            /* Type */              TEXT,
            /* DefaultValue */      "0",
            /* Description */       "Deprecated: A Quality of Service Level (QoS) for this message. " +
            "As of version 1.0.1 of this action, use the 'QoS' parameter.",
            /* ArgumentValidator */ POSITIVE_LONG_VALIDATOR),

    /**
     * Deprecated: Connection protocol. As of version 1.0.1 of this action the 'BrokerAlias' parameter
     * replaces the usage of the Protocol, Host and Port parameters.
     */
    ParamProtocol(
            /* Name */              Protocol.name(),
            /* OptionalRequired */  Optional,
            /* AppearsByDefault */  Hided,
            /* Type */              TEXT,
            /* DefaultValue */      "tcp",
            /* Description */       "Deprecated: Protocol used to communicate with the MQTT broker. " +
            "As of version 1.0.1 of this action, use the 'BrokerAlias' parameter to identify the required broker.",
            /* ArgumentValidator */ ALWAYS_VALID),

    /**
     * Deprecated: Broker host. As of version 1.0.1 of this action the 'BrokerAlias' parameter
     * replaces the usage of the Protocol, Host and Port parameters.
     */
    ParamHost(
            /* Name */              Host.name(),
            /* Optional Required */ Optional,
            /* AppearsByDefault */  Hided,
            /* Type */              TEXT,
            /* DefaultValue */      "localhost",
            /* Description */       "Deprecated: Host of the MQTT broker. " +
            "As of version 1.0.1 of this action, use the 'BrokerAlias' parameter to identify the required broker.",
            /* ArgumentValidator */ ALWAYS_VALID),
    /**
     * Deprecated: Broker host. As of version 1.0.1 of this action the 'BrokerAlias' parameter
     * replaces the usage of the Protocol, Host and Port parameters.
     */
    ParamPort(
            /* Name */              Port.name(),
            /* Optional Required */ Optional,
            /* AppearsByDefault */  Hided,
            /* Type */              TEXT,
            /* DefaultValue */      "1883",
            /* Description */       "Deprecated: Port number of the MQTT broker. " +
            "As of version 1.0.1 of this action, use the 'BrokerAlias' parameter to identify the required broker.",
            /* ArgumentValidator */ ALWAYS_VALID),

    ParamCompression(
            /* Name */              "Compression",
            /* Optional Required */ Optional,
            /* AppearsByDefault */  Hided, // Not officially tested and documented.
            /* Type */              TEXT,
            /* DefaultValue */      "false",
            /* Description */       "Determine if the message should be compressed in deflate." +
            "Value Possibles are True and False. Default is false.",
            /* ArgumentValidator */ ALWAYS_VALID);

    private final String name;
    private final OptionalRequired optionalRequired;
    private final AppearsByDefault appearsByDefault;
    private final ActionParameter.Type type;
    private final String defaultValue;
    private final String description;
    private final ArgumentValidator argumentValidator;

    /**
     * Ctr
     *
     */
    PublishOption(final String name, final OptionalRequired optionalRequired,
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
