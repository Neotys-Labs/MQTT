package com.neotys.action.jms.disconnect;

import static com.neotys.action.argument.Arguments.getArgumentLogString;
import static com.neotys.action.argument.Arguments.parseArguments;
import static com.neotys.action.jms.disconnect.DisconnectOption.DestinationName;
import static com.neotys.jms.JmsResultFactory.newErrorResult;
import static com.neotys.jms.JmsResultFactory.newOkResult;
import static com.neotys.jms.context.MessageUtils.getTopicOrQueue;

import java.util.List;
import java.util.Map;

import javax.jms.JMSException;

import com.google.common.base.Optional;
import com.neotys.extensions.action.ActionParameter;
import com.neotys.extensions.action.engine.ActionEngine;
import com.neotys.extensions.action.engine.Logger;
import com.neotys.extensions.action.engine.SampleResult;
import com.neotys.jms.context.JmsQueueContext;

public final class DisconnectActionEngine implements ActionEngine {

	private static final String STATUS_CODE_INVALID_PARAMETER = "NL-DISCONNECT-ACTION-01";
	private static final String STATUS_CODE_ERROR_DISCONNECTION = "NL-DISCONNECT-ACTION-02";

	@Override
	public SampleResult execute(final com.neotys.extensions.action.engine.Context context, final List<ActionParameter> parameters) {
		final Map<String, Optional<String>> parsedArgs;
		try {
			parsedArgs = parseArguments(parameters, DisconnectOption.values());
		} catch (final IllegalArgumentException iae) {
			return newErrorResult(context, STATUS_CODE_INVALID_PARAMETER, "Could not parse arguments: ", iae);
		}

		final Logger logger = context.getLogger();
		if (logger.isDebugEnabled()) {
			logger.debug("Executing " + this.getClass().getName() + " with parameters: "
					+ getArgumentLogString(parsedArgs, DisconnectOption.values()));
		}
		final String destinationName = parsedArgs.get(DestinationName.getName()).get();

		final Object objectContext = context.getCurrentVirtualUser().remove(destinationName);
		if (!(objectContext instanceof JmsQueueContext)) {
			return newErrorResult(context, STATUS_CODE_ERROR_DISCONNECTION, "Connection to destination " + destinationName + " has not been created.");
		}

		final JmsQueueContext jmsQueueContext = (JmsQueueContext) objectContext;

		try {
			jmsQueueContext.closeConnection();
			return newOkResult(context, "Disconnected from " + getTopicOrQueue(jmsQueueContext.getDestination()) + " " + destinationName + ".");
		} catch (final JMSException exception) {
			return newErrorResult(context, STATUS_CODE_ERROR_DISCONNECTION, "Could not disconnect from destination: ", exception);
		}
	}

	@Override
	public void stopExecute() {
		// Not implemented
	}

}
