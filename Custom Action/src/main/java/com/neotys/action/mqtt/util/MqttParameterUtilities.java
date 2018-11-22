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
package com.neotys.action.mqtt.util;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.neotys.extensions.action.engine.Context;
import com.neotys.extensions.action.engine.SampleResult;

import java.util.Map;

import static com.neotys.action.mqtt.util.SharedParameterNames.*;

/**
 * Created by smader on 18/11/16.
 */
public class MqttParameterUtilities {
    // TODO Strange that the following 2 constants, although not declared in a shared location,
    // TODO seem to be described and referenced in NeoLoad doc as generic
    // TODO and associated to all advanced actions
    // TODO This is clearly not the right place to declare theses constants, move to a shared interface ...
    // "There was an issue parsing the parameters."
    public static final String STATUS_CODE_INVALID_PARAMETER = "NL-CONNECT-ACTION-01";
    // "There was an error while creating the connection to the destination."
    public static final String STATUS_CODE_ERROR_CONNECTION = "NL-CONNECT-ACTION-02";

    // Value of the key used to associate the MQTT context to the current VU
    public static final String NL_MQTT_VU_CONTEXT = "NL-MQTT-VU-CONTEXT";

    /**
     * Get and check to the mqtt connection in the context of the VU
     * @param context the VU context
     * @param parsedArgs parameters of the action
     * @param sampleResult a sample result modified if no connection is found
     * @return null if the connection is not found or or not connected and a wrapper on the connection otherwise
     */
    public static MqttClientWrapper GetAndCheckConnection(final Context context, final Map<String, Optional<String>> parsedArgs, SampleResult sampleResult) {
        // Get MQTT client wrapper  in the User Path context
        MqttClientWrapper mqttClientWrapper = GetMqttClientWrapper(context, parsedArgs);

        // No connection object, fail
        if (mqttClientWrapper == null) {
            setResultAsError(
                    sampleResult,
                    STATUS_CODE_ERROR_CONNECTION,
                    "Either a connection using the Connect Advanced Action has not been performed or 'BrokerAlias' is missing or wrong.");
            return null;
        }

        // Sanity check before going any further : is the connection ok ?
        if (! mqttClientWrapper.isConnected()) {
            String errorMessage = "Connection to the broker found but not in a connected state: " + mqttClientWrapper.getServerURI();

            setResultAsError(sampleResult, STATUS_CODE_ERROR_CONNECTION, errorMessage.toString());
            return null;
        }
        return mqttClientWrapper;

    }

    /**
     * Get MQTT client (connection) in the User Path (VU) context
     *
     * @return an mqttClient object and null if none found
     */
    public static MqttClientWrapper GetMqttClientWrapper(Context context, Map<String, Optional<String>> parsedArgs) {
        Map<String, MqttClientWrapper> mqttVUContext = (Map<String, MqttClientWrapper>) context.getCurrentVirtualUser().get(NL_MQTT_VU_CONTEXT);

        // A connection hasn't ever even been attempted
        if (mqttVUContext == null) {
            return null;
        }

        // Get to the connection using broker alias, the current standard way of doing it
        final Optional<String> brokerAliasOptional = parsedArgs.get(BrokerAlias.name());
        if (brokerAliasOptional.isPresent() && !Strings.isNullOrEmpty(brokerAliasOptional.get())) {
            return mqttVUContext.get(brokerAliasOptional.get());
        }

        // Deprecated: backward compatibility, use broker URL as alias i.e. as key to connection object
        String brokerURL = extractBrokerURL(parsedArgs);
        MqttClientWrapper mqttClientWrapper = mqttVUContext.get(brokerURL);
        if (mqttClientWrapper != null) {
            return mqttClientWrapper;
        }

        // Last resort: if the context contains a unique connection use that one
        if (mqttVUContext.size() == 1) {
            return mqttVUContext.entrySet().iterator().next().getValue();
        }

        // TINA
        return null;
    }


    /**
     * Extract the mqttBrokerURL from the specified parsedARgs
     * @param parsedArgs
     * @return
     */
    public static String extractBrokerURL(final Map<String, Optional<String>> parsedArgs) {
        StringBuilder mqttBrokerURL = new StringBuilder();
        final Optional<String> protocolOptional = parsedArgs.get(Protocol.name());
        if (protocolOptional != null && protocolOptional.isPresent() && !Strings.isNullOrEmpty(protocolOptional.get())) {
            mqttBrokerURL.append(protocolOptional.get());
        }
        else mqttBrokerURL.append("tcp");

        final boolean isTCP = "tcp".equalsIgnoreCase(mqttBrokerURL.toString());

        mqttBrokerURL.append("://").append(GetHost(parsedArgs, Host.name())).append(":");

        final Optional<String> portOptional = parsedArgs.get(Port.name());
        if (portOptional != null && portOptional.isPresent() && !Strings.isNullOrEmpty(portOptional.get())) {
            mqttBrokerURL.append(portOptional.get());
        }
        else mqttBrokerURL.append(isTCP ? "1883" : "8883");

        return mqttBrokerURL.toString();
    }

    /**
     * Get the host from the specified parsed args
     * @param parsedArgs
     * @return the host
     */
    public static String GetHost(final Map<String, Optional<String>> parsedArgs, String hostParamName) {
        final Optional<String> hostOptional = parsedArgs.get(hostParamName);
        if (hostOptional != null && hostOptional.isPresent() && !Strings.isNullOrEmpty(hostOptional.get())) {
            return hostOptional.get();
        }
        return "localhost";
    }

    /**
     * Topic name ?
     *
     * @return topic name or null if not found
     */
    public static String GetTopicName(Map<String, Optional<String>> parsedArgs) {
        Optional<String> topicNameOptional = parsedArgs.get(Topic.name());
        if (topicNameOptional.isPresent() && !Strings.isNullOrEmpty(topicNameOptional.get())) {
            return topicNameOptional.get();
        }
        return null;
    }


    /**
     * Set the specified result as an error with the specified params
     *
     * @param sampleResult
     * @param statusCode
     * @param errorMessage
     * @param exception
     */
    public static SampleResult setResultAsError(final SampleResult sampleResult,
                                                final String statusCode, final String errorMessage, final Exception exception) {
        final StringBuilder statusMessageWithException = new StringBuilder(errorMessage);
        if (exception != null) {
            statusMessageWithException.append(": ").append(exception);
            if (exception.getCause() != null) {
                statusMessageWithException.append(": ").append(exception.getCause());
            }
        }
        sampleResult.setError(true);
        sampleResult.setStatusCode(statusCode);
        sampleResult.setResponseContent(statusMessageWithException.toString());
        return sampleResult;
    }

    /**
     * Set the specified result as an error with the specified params
     *
     * @param sampleResult
     * @param statusCode
     * @param errorMessage
     */
    public static SampleResult setResultAsError(final SampleResult sampleResult,
                                                final String statusCode, final String errorMessage) {
        return setResultAsError(sampleResult, statusCode, errorMessage, null);
    }
}
