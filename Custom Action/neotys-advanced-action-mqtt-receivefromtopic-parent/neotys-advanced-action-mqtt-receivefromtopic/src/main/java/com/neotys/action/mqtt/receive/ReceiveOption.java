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
package com.neotys.action.mqtt.receive;

import com.neotys.action.argument.ArgumentValidator;
import com.neotys.action.argument.Option;
import com.neotys.extensions.action.ActionParameter;

import static com.neotys.action.argument.DefaultArgumentValidator.*;
import static com.neotys.action.argument.Option.AppearsByDefault.Hided;
import static com.neotys.action.argument.Option.AppearsByDefault.True;
import static com.neotys.action.argument.Option.OptionalRequired.Optional;
import static com.neotys.action.argument.Option.OptionalRequired.Required;
import static com.neotys.action.mqtt.util.SharedParameterNames.*;
import static com.neotys.extensions.action.ActionParameter.Type.TEXT;

/**
 * MQTT receive messages option
 *
 * Created by smader on 02/11/16.
 */
enum ReceiveOption implements Option {
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
            /* Description */       "Alias identifying the broker." +
            "If User Path connects to one broker only, this parameter can be left out.",
            /* ArgumentValidator */ ALWAYS_VALID),

    /**
     * The name of the topic on which to publish the message.
     * As of version 1.0.1 of this action, the 'Topic' parameter contains the value.
     */
    ParamTopic(
            /* Name */              Topic.name(),
            /* OptionalRequired */  Required,
            /* AppearsByDefault */  True,
            /* Type */              TEXT,
            /* DefaultValue */      "",
            /* Description */       "The name of the topic on which to receive messages.",
            /* ArgumentValidator */ ALWAYS_VALID),

    /**
     * Number of messages
     */
    ParamMessageCount(
            /* Name */              MessageCount.name(),
            /* OptionalRequired */  Optional,
            /* AppearsByDefault */  True,
            /* Type */              TEXT,
            /* DefaultValue */      "1",
            /* Description */       "Number of messages expected to be received during the 'timeout' duration.",
            /* ArgumentValidator */ STRICT_POSITIVE_LONG_VALIDATOR),

    /**
     * Timeout
     */
    ParamTimeout(
            /* Name */              Timeout.name(),
            /* OptionalRequired */  Optional,
            /* AppearsByDefault */  True,
            /* Type */              TEXT,
            /* DefaultValue */      "2000",
            /* Description */       "Maximum duration in milliseconds to wait for the specified number of messages.",
            /* ArgumentValidator */ STRICT_POSITIVE_LONG_VALIDATOR),

    /**
     * Fail on Timeout ?
     */
    ParamFailOnTimeout(
            /* Name */              FailOnTimeout.name(),
            /* OptionalRequired */  Optional,
            /* AppearsByDefault */  True,
            /* Type */              TEXT,
            /* DefaultValue */      "true",
            /* Description */       "Set to 'true' this action will fail if at least 'MessageCount' messages have not been received during 'Timeout' ms.",
            /* ArgumentValidator */ BOOLEAN_VALIDATOR),

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
     * @param name
     * @param optionalRequired
     * @param appearsByDefault
     * @param type
     * @param defaultValue
     * @param description
     * @param argumentValidator
     */
    ReceiveOption(final String name, final OptionalRequired optionalRequired,
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
