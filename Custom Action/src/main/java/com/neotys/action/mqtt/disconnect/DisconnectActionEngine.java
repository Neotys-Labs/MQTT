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
package com.neotys.action.mqtt.disconnect;

import java.util.List;
import java.util.Map;

import com.google.common.base.Optional;
import com.neotys.action.mqtt.util.MqttClientWrapper;
import com.neotys.extensions.action.engine.Logger;

import com.neotys.extensions.action.ActionParameter;
import com.neotys.extensions.action.engine.ActionEngine;
import com.neotys.extensions.action.engine.Context;
import com.neotys.extensions.action.engine.SampleResult;
import org.eclipse.paho.client.mqttv3.MqttException;

import static com.neotys.action.argument.Arguments.getArgumentLogString;
import static com.neotys.action.argument.Arguments.parseArguments;
import static com.neotys.action.mqtt.util.MqttParameterUtilities.*;
import static com.neotys.action.mqtt.util.MqttParameterUtilities.NL_MQTT_VU_CONTEXT;
import static com.neotys.action.result.ResultFactory.STATUS_CODE_OK;

public class DisconnectActionEngine implements ActionEngine {
	@Override
	public SampleResult execute(Context context, List<ActionParameter> actionParameters) {
		final Logger logger = context.getLogger();
		SampleResult sampleResult = new SampleResult();

		final Map<String, Optional<String>> parsedArgs;
		try {
			parsedArgs = parseArguments(actionParameters, DisconnectOption.values());
		} catch (final IllegalArgumentException iae) {
			SetResultAsError(sampleResult, STATUS_CODE_INVALID_PARAMETER, "Invalid parameter", iae);
			return sampleResult;
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Executing " + this.getClass().getName() + " with parameters: "
					+ getArgumentLogString(parsedArgs, DisconnectOption.values()));
		}

		// Get MQTT client wrapper in the User Path context and check that the connection is ok
		// Considered to be an error to attemp to disconnect if the client is already disconnected or never has been connected
		MqttClientWrapper mqttClientWrapper = GetAndCheckConnection(context, parsedArgs, sampleResult);
		if (mqttClientWrapper == null) {
			logger.error(sampleResult.getResponseContent());
			return sampleResult;
		}

		sampleResult.sampleStart();
		try {
			mqttClientWrapper.getMqttClient().disconnect();

			// get the mqtt context associated to the current VU and remove the associated connection
			Map<String, MqttClientWrapper> mqttVUContext = (Map<String, MqttClientWrapper>) context.getCurrentVirtualUser().get(NL_MQTT_VU_CONTEXT);
			mqttVUContext.remove(mqttClientWrapper.getBrokerAlias());

            sampleResult.setStatusCode(STATUS_CODE_OK);
            sampleResult.setResponseContent("Successfully disconnected from MQTT broker: " + mqttClientWrapper);

            if (logger.isDebugEnabled()) {
                logger.debug(sampleResult.getResponseContent());
            }
		}
		catch (MqttException mqttException) {
            String errorMessage = "Error occurred disconnecting from MQTT Broker: " + mqttClientWrapper;
            SetResultAsError(sampleResult, STATUS_CODE_ERROR_CONNECTION, errorMessage, mqttException);

            logger.error(sampleResult.getResponseContent());
		}

        sampleResult.sampleEnd();
        return sampleResult;
	}

	@Override
	public void stopExecute() {
		// NOOP
	}

}
