package com.neotys.action.mqtt.subscribe;

import java.util.List;
import java.util.Map;
import java.util.Queue;

import com.google.common.base.Optional;
import com.neotys.action.mqtt.util.MqttClientWrapper;
import com.neotys.extensions.action.engine.Logger;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.google.common.base.Strings;
import com.neotys.extensions.action.ActionParameter;
import com.neotys.extensions.action.engine.ActionEngine;
import com.neotys.extensions.action.engine.Context;
import com.neotys.extensions.action.engine.SampleResult;

import static com.neotys.action.argument.Arguments.getArgumentLogString;
import static com.neotys.action.argument.Arguments.parseArguments;
import static com.neotys.action.mqtt.util.MqttParameterUtilities.*;
import static com.neotys.action.mqtt.util.MqttParameterUtilities.STATUS_CODE_ERROR_CONNECTION;
import static com.neotys.action.result.ResultFactory.STATUS_CODE_OK;

/**
 * MQTT subscription to a topic: hooks up a callback that queues incoming messages.
 *
 * <p>The queue is associated to the topic name and accessible through the MQTT client wrapper of the VU context.
 */
public class SubscribeActionEngine implements ActionEngine {
	@Override
	public SampleResult execute(Context context, List<ActionParameter> actionParameters) {
        final Logger logger = context.getLogger();
        SampleResult sampleResult = new SampleResult();

        final Map<String, Optional<String>> parsedArgs;
        try {
            parsedArgs = parseArguments(actionParameters, SubscribeOption.values());
        } catch (final IllegalArgumentException iae) {
            setResultAsError(sampleResult, STATUS_CODE_INVALID_PARAMETER, "Invalid parameter", iae);
            return sampleResult;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Executing " + this.getClass().getName() + " with parameters: "
                    + getArgumentLogString(parsedArgs, SubscribeOption.values()));
        }

        // Get MQTT client wrapper in the User Path context and check that the connection is ok
        MqttClientWrapper mqttClientWrapper = GetAndCheckConnection(context, parsedArgs, sampleResult);
        if (mqttClientWrapper == null) {
            logger.error(sampleResult.getResponseContent());
            return sampleResult;
        }

        String topicName = GetTopicName(parsedArgs);
        int QoS = GetQoS(parsedArgs);

        // check that subscription doesn't already exist and create its associated queue
        Queue<MqttMessage> subscriptionQueue = mqttClientWrapper.getQueueForTopic(topicName);
        if (subscriptionQueue != null) {
            String errorMessage = "Duplicate subscription on topic '" + topicName + "' of MQTT Broker: " + mqttClientWrapper;

            setResultAsError(sampleResult, STATUS_CODE_ERROR_CONNECTION, errorMessage);
            logger.error(sampleResult.getResponseContent());
            return sampleResult;
        }

        QueueReceivedMessages callback = new QueueReceivedMessages();
        mqttClientWrapper.registerQueueForTopic(topicName, callback.getQueue());
        mqttClientWrapper.getMqttClient().setCallback(callback);

        sampleResult.sampleStart();
        try {
            mqttClientWrapper.getMqttClient().subscribe(topicName, QoS);

            sampleResult.setStatusCode(STATUS_CODE_OK);
            sampleResult.setResponseContent("Subscribed to topic '" + topicName + "' on broker: " + mqttClientWrapper);
        }
        catch(MqttException mqttException) {
            String errorMessage = "Error occurred subscribing to topic '" + topicName + "' on MQTT Broker: " + mqttClientWrapper;

            setResultAsError(sampleResult, STATUS_CODE_ERROR_CONNECTION, errorMessage, mqttException);
            logger.error(sampleResult.getResponseContent());
        }
        sampleResult.sampleEnd();
        return sampleResult;
    }

	@Override
	public void stopExecute() {
		/** NOOP */
	}

	/**
	 * QoS ?
	 *
	 * NumberFormatException not handled because Option explicitly guarantees that it's 0,1 or 2
	 *
	 * @return QoS
	 */
	private static int GetQoS(Map<String, Optional<String>> parsedArgs) {
		Optional<String> QoSOptional = parsedArgs.get(SubscribeOption.ParamQoS.getName());
		if (QoSOptional.isPresent() && !Strings.isNullOrEmpty(QoSOptional.get())) {
			return Integer.parseInt(QoSOptional.get());
		}
		return Integer.parseInt(SubscribeOption.ParamQoS.getDefaultValue());
	}
}
