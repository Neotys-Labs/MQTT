package com.neotys.action.jms.unsubscribe;

import static com.neotys.action.argument.DefaultArgumentValidator.NON_EMPTY;
import static com.neotys.action.argument.Option.AppearsByDefault.True;
import static com.neotys.action.argument.Option.OptionalRequired.Required;
import static com.neotys.extensions.action.ActionParameter.Type.TEXT;
import static com.neotys.jms.JmsParameters.TOPIC;

import com.neotys.action.argument.ArgumentValidator;
import com.neotys.action.argument.Option;
import com.neotys.extensions.action.ActionParameter.Type;

/**
 * @author srichert
 *
 */
enum UnsubscribeFromTopicOption implements Option {
	TopicName(TOPIC, Required, True, TEXT,
			"",
			"The JNDI topic name.",
			NON_EMPTY);

	private final String name;
	private final OptionalRequired optionalRequired;
	private final AppearsByDefault appearsByDefault;
	private final Type type;
	private final String defaultValue;
	private final String description;
	private final ArgumentValidator argumentValidator;

	private UnsubscribeFromTopicOption(final String name, final OptionalRequired optionalRequired,
			final AppearsByDefault appearsByDefault,
			final Type type, final String defaultValue, final String description,
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
	public Type getType() {
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
