package com.neotys.action.mqtt.util;

import com.google.common.base.Optional;
import com.neotys.extensions.action.Action;

import javax.swing.*;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

public abstract class MqttAction implements Action {
	protected static String BUNDLE_NAME = "com.neotys.action.mqtt.bundle";
	private static String DISPLAY_PATH = ResourceBundle.getBundle(BUNDLE_NAME, Locale.getDefault()).getString("displayPath");

	protected static final ImageIcon CONNECT_ICON;
	protected static final ImageIcon DISCONNECT_ICON;
	protected static final ImageIcon MESSAGE_ICON;

	static {
		final URL connectURL = MqttAction.class.getResource("connect.png");
		CONNECT_ICON = connectURL != null ? new ImageIcon(connectURL) : null;
		final URL disconnectURL = MqttAction.class.getResource("disconnect.png");
		DISCONNECT_ICON = disconnectURL != null ? new ImageIcon(disconnectURL) : null;
		final URL messageURL = MqttAction.class.getResource("disconnect.png");
		MESSAGE_ICON = messageURL != null ? new ImageIcon(messageURL) : null;
	}

	@Override
	public String getDisplayPath() {
		return DISPLAY_PATH;
	}

	@Override
	public boolean getDefaultIsHit() {
		return true;
	}

	@Override
	public Optional<String> getMaximumNeoLoadVersion() {
		return Optional.absent();
	}

	@Override
	public Optional<String> getMinimumNeoLoadVersion() {
		return Optional.of("5.1");
	}
}
