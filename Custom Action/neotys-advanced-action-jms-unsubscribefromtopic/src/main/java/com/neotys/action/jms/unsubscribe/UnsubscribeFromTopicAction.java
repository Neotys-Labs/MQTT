package com.neotys.action.jms.unsubscribe;

import static com.neotys.action.argument.Arguments.getArgumentDescriptions;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.google.common.base.Optional;
import com.neotys.action.argument.Option.AppearsByDefault;
import com.neotys.extensions.action.Action;
import com.neotys.extensions.action.ActionParameter;
import com.neotys.extensions.action.engine.ActionEngine;

/**
 *
 * @author anouvel
 *
 */
public final class UnsubscribeFromTopicAction implements Action {
	private static final String BUNDLE_NAME = "com.neotys.action.jms.unsubscribe.bundle";
	private static final String DISPLAY_NAME = ResourceBundle.getBundle(BUNDLE_NAME, Locale.getDefault()).getString("displayName");
	private static final String DISPLAY_PATH = ResourceBundle.getBundle(BUNDLE_NAME, Locale.getDefault()).getString("displayPath");

	/** logo */
	private static final ImageIcon LOGO_ICON;
	static {
		final URL iconURL = UnsubscribeFromTopicAction.class.getResource("message.png");
		if (iconURL != null) {
			LOGO_ICON = new ImageIcon(iconURL);
		} else {
			LOGO_ICON = null;
		}
	}

	@Override
	public String getType() {
		return "Unsubscribe From Topic";
	}

	@Override
	public List<ActionParameter> getDefaultActionParameters() {
		final ArrayList<ActionParameter> parameters = new ArrayList<>();

		for (final UnsubscribeFromTopicOption option : UnsubscribeFromTopicOption.values()) {
			if (AppearsByDefault.True.equals(option.getAppearsByDefault())) {
				parameters.add(new ActionParameter(option.getName(), option.getDefaultValue(),
						option.getType()));
			}
		}

		return parameters;
	}

	@Override
	public boolean getDefaultIsHit(){
		return true;
	}

	@Override
	public Class<? extends ActionEngine> getEngineClass() {
		return UnsubscribeFromTopicActionEngine.class;
	}

	@Override
	public Icon getIcon() {
		return LOGO_ICON;
	}

	@Override
	public String getDescription() {
		return "Unsubscribes from a topic.\n\n" + getArgumentDescriptions(UnsubscribeFromTopicOption.values());
	}

	@Override
	public String getDisplayName() {
		return DISPLAY_NAME;
	}

	@Override
	public String getDisplayPath() {
		return DISPLAY_PATH;
	}

	@Override
	public Optional<String> getMinimumNeoLoadVersion() {
		return Optional.of("5.1");
	}

	@Override
	public Optional<String> getMaximumNeoLoadVersion() {
		return Optional.absent();
	}
}
