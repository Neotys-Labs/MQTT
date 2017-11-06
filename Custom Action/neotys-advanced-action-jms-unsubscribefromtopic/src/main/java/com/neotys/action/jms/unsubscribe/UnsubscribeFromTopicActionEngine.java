package com.neotys.action.jms.unsubscribe;

import static com.neotys.action.argument.Arguments.getArgumentLogString;
import static com.neotys.action.argument.Arguments.parseArguments;
import static com.neotys.action.jms.unsubscribe.UnsubscribeFromTopicOption.TopicName;
import static com.neotys.jms.JmsResultFactory.newErrorResult;
import static com.neotys.jms.JmsResultFactory.newOkResult;

import java.util.List;
import java.util.Map;

import javax.jms.JMSException;

import com.google.common.base.Optional;
import com.neotys.extensions.action.ActionParameter;
import com.neotys.extensions.action.engine.ActionEngine;
import com.neotys.extensions.action.engine.Context;
import com.neotys.extensions.action.engine.Logger;
import com.neotys.extensions.action.engine.SampleResult;
import com.neotys.jms.context.JmsTopicContext;
import com.neotys.jms.context.TopicSubscriptionException;

public final class UnsubscribeFromTopicActionEngine implements ActionEngine {

	private static final String STATUS_CODE_INVALID_PARAMETER = "NL-UNSUBSCRIBEFROMTOPIC-ACTION-01";
	private static final String STATUS_CODE_ERROR_UNSUBSCRIBE_FROM_TOPIC = "NL-UNSUBSCRIBEFROMTOPIC-ACTION-02";

	@Override
	public SampleResult execute(final Context context, final List<ActionParameter> parameters) {
		final Map<String, Optional<String>> parsedArgs;
		try {
			parsedArgs = parseArguments(parameters, UnsubscribeFromTopicOption.values());
		} catch (final IllegalArgumentException iae) {
			return newErrorResult(context, STATUS_CODE_INVALID_PARAMETER, "Could not parse arguments:", iae);
		}

		final Logger logger = context.getLogger();
		if (logger.isDebugEnabled()) {
			logger.debug(
					"Executing UnsubscribeFromTopicActionEngine with parameters: "
							+ getArgumentLogString(parsedArgs, UnsubscribeFromTopicOption.values()));
		}
		final String topicName = parsedArgs.get(TopicName.getName()).get();
		final Object objectContext = context.getCurrentVirtualUser().get(topicName);
		if (!(objectContext instanceof JmsTopicContext)) {
			return newErrorResult(context, STATUS_CODE_ERROR_UNSUBSCRIBE_FROM_TOPIC, "Connection to topic " + topicName + " has not been created.");
		}

		final JmsTopicContext jmsTopicContext = (JmsTopicContext) objectContext;

		try {
			jmsTopicContext.unsubscribeFromTopic();
			return newOkResult(context, "Unsubscribed from topic " + topicName + ".");
		} catch (final JMSException | TopicSubscriptionException exception) {
			return newErrorResult(context, STATUS_CODE_ERROR_UNSUBSCRIBE_FROM_TOPIC, "Could not unsubscribe from topic: ", exception);
		}
	}

	@Override
	public void stopExecute() {
		// Not implemented
	}

}
