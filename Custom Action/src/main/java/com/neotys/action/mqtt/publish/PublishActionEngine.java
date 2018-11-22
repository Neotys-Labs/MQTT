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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;
import com.google.common.base.Optional;
import com.neotys.action.mqtt.util.MqttClientWrapper;
import com.neotys.extensions.action.engine.Logger;
import org.eclipse.paho.client.mqttv3.*;

import com.google.common.base.Strings;
import com.neotys.extensions.action.ActionParameter;
import com.neotys.extensions.action.engine.ActionEngine;
import com.neotys.extensions.action.engine.Context;
import com.neotys.extensions.action.engine.SampleResult;

import static com.neotys.action.argument.Arguments.getArgumentLogString;
import static com.neotys.action.argument.Arguments.parseArguments;
import static com.neotys.action.mqtt.util.MqttParameterUtilities.*;
import static com.neotys.action.result.ResultFactory.STATUS_CODE_OK;

/**
 * Publish a message on a topic to an MQTT broker
 */
public class PublishActionEngine implements ActionEngine  {

    @Override
    public SampleResult execute(Context context, List<ActionParameter> actionParameters) {
        final Logger logger = context.getLogger();
        final StringBuilder responseBuilder = new StringBuilder();
        SampleResult sampleResult = new SampleResult();

        final Map<String, Optional<String>> parsedArgs;
        try {
            parsedArgs = parseArguments(actionParameters, PublishOption.values());
        } catch (final IllegalArgumentException iae) {
            setResultAsError(sampleResult, STATUS_CODE_INVALID_PARAMETER, "Invalid parameter", iae);
            return sampleResult;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Executing " + this.getClass().getName() + " with parameters: "
                    + getArgumentLogString(parsedArgs, PublishOption.values()));
        }

        // Get MQTT client wrapper in the User Path context and check that the connection is ok
        MqttClientWrapper mqttClientWrapper = GetAndCheckConnection(context, parsedArgs, sampleResult);
        if (mqttClientWrapper == null) {
            logger.error(sampleResult.getResponseContent());
            return sampleResult;
        }

        // Topic name ? Look for value using the standard param or the deprecated one
        String topicName = GetTopicName(parsedArgs);
        if (topicName == null) {
            String errorMessage = "Topic name expected to publish a message on an MQTT broker: " + mqttClientWrapper;
            setResultAsError(sampleResult, STATUS_CODE_INVALID_PARAMETER, errorMessage);
            logger.error(sampleResult.getResponseContent());
            return sampleResult;
        }



        // QoS ? Look for value using the standard param or the deprecated one
        int QoS = GetQoS(parsedArgs);




        try {
            MqttMessage mqttMessage = new MqttMessage(GetByteMessage(parsedArgs,responseBuilder));
            mqttMessage.setQos(QoS);
            // TODO retained param
            mqttMessage.setRetained(false);

            MqttTopic topic = mqttClientWrapper.getMqttClient().getTopic(topicName);

            // Actual publication
            sampleResult.sampleStart();
            MqttDeliveryToken token = topic.publish(mqttMessage);
            token.waitForCompletion();

            sampleResult.setStatusCode(STATUS_CODE_OK);
            appendLineToStringBuilder(responseBuilder,"Published message to topic '" + topicName + "' on broker: " + mqttClientWrapper +
                    ", message is '" + mqttMessage +
                    "', payload is '" + mqttMessage.getPayload() + "'.");
            sampleResult.setResponseContent(responseBuilder.toString());

            if (logger.isDebugEnabled()) {
                logger.debug(sampleResult.getResponseContent());
            }
        } catch (MqttException mqttException) {
            String errorMessage = "Error occurred publishing to topic '" + topicName + "' on broker: " + mqttClientWrapper;

            setResultAsError(sampleResult, STATUS_CODE_ERROR_CONNECTION, errorMessage, mqttException);
            logger.error(sampleResult.getResponseContent());
        }
        catch (IOException e) {
            String errorMessage = "Error occurred compressing the content on  '" + topicName + "' on broker: " + mqttClientWrapper;

            setResultAsError(sampleResult, STATUS_CODE_ERROR_CONNECTION, errorMessage, e);
            logger.error(sampleResult.getResponseContent());
        }
        sampleResult.sampleEnd();
        return sampleResult;
    }

    /**
     * Compression tag ? Look for value using the standard param or the deprecated one
     *
     * @return boolean specifying if compression of the message ( string) is required
     */
    private static boolean IsCompressionRequired(Map<String, Optional<String>> parsedArgs) {
        Optional<String> CompressBool = parsedArgs.get(PublishOption.ParamCompression.getName());
        if (CompressBool.isPresent() && !Strings.isNullOrEmpty(CompressBool.get())) {
            if(CompressBool.get().equalsIgnoreCase("true"))
                return true;
            else
                return false;
        }
        return false;
    }

    /**
     * Topic name ? Look for value using the standard param or the deprecated one
     *
     * @return topic name or null if not found
     */
    private static String GetTopicName(Map<String, Optional<String>> parsedArgs) {
        Optional<String> topicNameOptional = parsedArgs.get(PublishOption.ParamTopic.getName());
        if (topicNameOptional.isPresent() && !Strings.isNullOrEmpty(topicNameOptional.get())) {
            return topicNameOptional.get();
        }
        topicNameOptional = parsedArgs.get(PublishOption.ParamMQTTTopic.getName());
        if (topicNameOptional.isPresent() && !Strings.isNullOrEmpty(topicNameOptional.get())) {
            return topicNameOptional.get();
        }
        return null;
    }

    /**
     * QoS ? Look for value using the standard param or the deprecated one
     *
     * NumberFormatException not handled because Option explicitly guarantees that it's 0,1 or 2
     *
     * @return QoS
     */
    private static int GetQoS(Map<String, Optional<String>> parsedArgs) {
        Optional<String> QoSOptional = parsedArgs.get(PublishOption.ParamQoS.getName());
        if (QoSOptional.isPresent() && !Strings.isNullOrEmpty(QoSOptional.get())) {
            return Integer.parseInt(QoSOptional.get());
        }
        QoSOptional = parsedArgs.get(PublishOption.ParamMQTTQos.getName());
        if (QoSOptional.isPresent() && !Strings.isNullOrEmpty(QoSOptional.get())) {
            return Integer.parseInt(QoSOptional.get());
        }
        return Integer.parseInt(PublishOption.ParamQoS.getDefaultValue());
    }
    /**
     * Compression of the message ? compressing the byte in gzip format
     *
     *
     * @return byte[]
     */
    public static byte[] getcompressdata(byte[] rawdata) throws IOException {
        byte[] compresseddata = null;
        ByteArrayOutputStream bytestream = new ByteArrayOutputStream(rawdata.length);
        try {
            GZIPOutputStream zipstream = new GZIPOutputStream(bytestream);
            try {
                zipstream.write(rawdata);
            } finally {
                zipstream.finish();
                zipstream.close();
            }
        } finally {
            bytestream.close();
        }

        compresseddata = bytestream.toByteArray();


        return compresseddata;
    }
    private static void appendLineToStringBuilder(final StringBuilder sb, final String line){
        sb.append(line).append("\n");
    }

    /**
     * Get byte for mqttmessage. generating the byte[] for mqttmessage
     * input : message string
     *
     * @return byte[]
     */
    public static byte[] GetByteMessage(Map<String, Optional<String>> parsedArgs,StringBuilder sample)  throws IOException  {
        byte[] bytemesage = null;
        // Message ?
        // TODO Seems that an empty message is acceptable, need to test and check
        String message = "";
        Optional<String> messageOptional = parsedArgs.get(PublishOption.ParamMessage.getName());
        if (messageOptional.isPresent() && !Strings.isNullOrEmpty(messageOptional.get())) {
            message = messageOptional.get();
        }
        else
            message="";

        if(IsCompressionRequired(parsedArgs)) {
            bytemesage = getcompressdata(message.getBytes());
            appendLineToStringBuilder(sample,"Message Compressed in Gzip :Done ");
        }
        else
            bytemesage=message.getBytes();

        return bytemesage;
    }

    @Override
    public void stopExecute() { /* NOOP */ }

}
