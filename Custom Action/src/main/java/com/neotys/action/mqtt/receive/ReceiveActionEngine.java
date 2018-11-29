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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

import com.google.common.base.Strings;
import com.google.common.base.Optional;

import com.neotys.extensions.action.ActionParameter;
import com.neotys.extensions.action.engine.ActionEngine;
import com.neotys.extensions.action.engine.Context;
import com.neotys.extensions.action.engine.SampleResult;
import com.neotys.extensions.action.engine.Logger;

import static com.neotys.action.result.ResultFactory.STATUS_CODE_OK;
import static com.neotys.action.argument.Arguments.getArgumentLogString;
import static com.neotys.action.argument.Arguments.parseArguments;
import static com.neotys.action.mqtt.util.MqttParameterUtilities.*;

import com.neotys.action.mqtt.util.MqttClientWrapper;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * Receive messages from an MQTT broker on a topic
 */
public class ReceiveActionEngine implements ActionEngine {
    static String STATUS_CODE_DUPLICATE="NL_MQTT_DUPLICATE_MESSAGE";
    @Override
    public SampleResult execute(Context context, List<ActionParameter> actionParameters) {
        final Logger logger = context.getLogger();
        SampleResult sampleResult = new SampleResult();

        final Map<String, Optional<String>> parsedArgs;
        try {
            parsedArgs = parseArguments(actionParameters, ReceiveOption.values());
        } catch (final IllegalArgumentException iae) {
            setResultAsError(sampleResult, STATUS_CODE_INVALID_PARAMETER, "Invalid parameter", iae);
            return sampleResult;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Executing " + this.getClass().getName() + " with parameters: "
                    + getArgumentLogString(parsedArgs, ReceiveOption.values()));
        }

        // Get MQTT client wrapper in the User Path context and check that the connection is ok
        MqttClientWrapper mqttClientWrapper = GetAndCheckConnection(context, parsedArgs, sampleResult);
        if (mqttClientWrapper == null) {
            logger.error(sampleResult.getResponseContent());
            return sampleResult;
        }

        // Topic name param is mandatory so no sanity check
        String topicName = GetTopicName(parsedArgs);

        // Get the corresponding message queue to get messages from
        // TODO maybe move waiting logic into the callback and keep the knowledge of the type of used queue inside that class
        LinkedBlockingQueue<MqttMessage> messageQueue = (LinkedBlockingQueue<MqttMessage>) mqttClientWrapper.getQueueForTopic(topicName);

        if (messageQueue == null) {
            //----not error but return the message in the sampleresult response----
            String errorMessage = "No subscription on topic '" + topicName +
                    "' found to receive messages from MQTT broker: " + mqttClientWrapper;

            setResultAsError(sampleResult, STATUS_CODE_INVALID_PARAMETER, errorMessage);
            //----send the message in the
            //logger.error(sampleResult.getResponseContent());

            return sampleResult;
        }

        int expectedMessageCount = GetMessageCount(parsedArgs);
        long timeout = GetTimeout(parsedArgs);
        boolean failOnTimeout = GetFailOnTimeout(parsedArgs);

        int numberMessagesReceived = 0;
        long deadLine = System.currentTimeMillis() + timeout;
        long timeLeftToWait = deadLine - System.currentTimeMillis();

        MqttMessage mqttMessage = null;

        sampleResult.sampleStart();
        StringBuilder statusMessage = new StringBuilder();
        // exit with an error if the contract is broken and failOnTimeout is true
        // otherwise exit with the payload of the last received message otherwise
        while(true) {


            // contract fulfilled
            if (numberMessagesReceived == expectedMessageCount) {
                sampleResult.sampleEnd();
                //----if duplicated message then generate an error-----------
                if (mqttMessage != null) {
                    if (mqttMessage.isDuplicate()) {
                        setResultAsError(sampleResult, STATUS_CODE_DUPLICATE, "Message is duplicated with the following payload " + GetStringMessageContent(mqttMessage));
                        //---------------------------
                    } else {
	                    sampleResult.setStatusCode(STATUS_CODE_OK);

                    }
	                // TODO message payload is in bytes, so for the time being TINA: transform to string.
	                sampleResult.setResponseContent(statusMessage.toString());
	                return sampleResult;
                }
            }

            // contract broken
            if (timeLeftToWait <= 0) {
                sampleResult.sampleEnd();

                statusMessage.append("Message reception contract not met on topic '");
                statusMessage.append(topicName).append("' of MQTT Broker: ").append(mqttClientWrapper);
                statusMessage.append(". Expected to receive ").append(expectedMessageCount).append(" message(s) in less than ");
                statusMessage.append(timeout).append("ms, received only ");
                statusMessage.append(numberMessagesReceived).append(" message(s).");
                sampleResult.setResponseContent(statusMessage.toString());

                if(failOnTimeout) {
                    sampleResult.setError(true);
                    sampleResult.setStatusCode(STATUS_CODE_ERROR_CONNECTION);
                    logger.error(sampleResult.getResponseContent());
                } else {
                    sampleResult.setError(false);
                    sampleResult.setStatusCode(STATUS_CODE_OK);
                    if (logger.isDebugEnabled()) {
                        logger.debug(sampleResult.getResponseContent());
                    }
                }
                return sampleResult;
            }

            try {
                mqttMessage = null;
                mqttMessage = messageQueue.poll(timeLeftToWait, TimeUnit.MILLISECONDS);
                if (mqttMessage != null) {
                    numberMessagesReceived++;
                    statusMessage.append("Message received "+mqttMessage.getId()+"\n");
                    statusMessage.append("Content of the Message "+GetStringMessageContent(mqttMessage)+"\n");

                    if (logger.isDebugEnabled()) {
                        logger.debug("Received message on topic '" + topicName + "' on broker: " + mqttClientWrapper +
                                ", message is '" + mqttMessage +
                                "', payload is '" + GetStringMessageContent(mqttMessage) + "'.");


                    }
                }
            } catch (InterruptedException ie) { /* NOOP  timeLeftToWait will be <= 0 */ }
            timeLeftToWait = deadLine - System.currentTimeMillis();
        }
    }

    /**
     * Timeout
     *
     * NumberFormatException not handled because Option explicitly guarantees that it's a positive integer
     *
     * @return timeout
     */
    private static long GetTimeout(final Map<String, Optional<String>> parsedArgs) {
        Optional<String> timeoutOptional = parsedArgs.get(ReceiveOption.ParamTimeout.getName());
        if (timeoutOptional.isPresent() && !Strings.isNullOrEmpty(timeoutOptional.get())) {
            return Long.parseLong(timeoutOptional.get());
        }
        return Long.parseLong(ReceiveOption.ParamTimeout.getDefaultValue());
    }

    /**
     * Timeout
     *
     * NumberFormatException not handled because Option explicitly guarantees that it's a positive integer
     *
     * @return timeout
     */
    private static boolean GetFailOnTimeout(final Map<String, Optional<String>> parsedArgs) {
        Optional<String> failOnTimeoutOptional = parsedArgs.get(ReceiveOption.ParamFailOnTimeout.getName());
        if (failOnTimeoutOptional.isPresent() && !Strings.isNullOrEmpty(failOnTimeoutOptional.get())) {
            if(failOnTimeoutOptional.get().equalsIgnoreCase("false"))
                return false;
            else
                return true;
        }
        else
            return Boolean.parseBoolean(ReceiveOption.ParamFailOnTimeout.getDefaultValue());
    }
    /**
     * GetStringContent of the message
     *
     * get the payload the message. Try to decompressed it if exception then return the string
     *
     * @return String
     */
    private static String GetStringMessageContent(MqttMessage mqttmess)
    {
        String decompressedData = null;
        byte[] buffer = new byte[8192];
        try {
            ByteArrayInputStream byteStream = new ByteArrayInputStream(mqttmess.getPayload());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try {
                GZIPInputStream zipStream = new GZIPInputStream(byteStream);
                try {

                    int c = 0;
                    while ((c = zipStream.read(buffer)) > 0) {
                        out.write(buffer, 0, c);
                    }
                } finally {
                    zipStream.close();
                }
            } finally {
                byteStream.close();
            }
            decompressedData = out.toString("UTF-8");
            return decompressedData;
        } catch (Exception e) {
            //----not gzip format then return the string format of the payload------
            decompressedData=new String(mqttmess.getPayload());
            return decompressedData;
        }

    }
    /**
     * MessageCount
     *
     * NumberFormatException not handled because Option explicitly guarantees that it's a positive integer
     *
     * @return timeout
     */
    private static int GetMessageCount(final Map<String, Optional<String>> parsedArgs) {
        Optional<String> messageCountOptional = parsedArgs.get(ReceiveOption.ParamMessageCount.getName());
        if (messageCountOptional.isPresent() && !Strings.isNullOrEmpty(messageCountOptional.get())) {
            return Integer.parseInt(messageCountOptional.get());
        }
        return Integer.parseInt(ReceiveOption.ParamMessageCount.getDefaultValue());
    }

    @Override
    public void stopExecute() { /* NOOP */ }
}
