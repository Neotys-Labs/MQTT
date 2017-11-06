package com.neotys.action.jms.subscribe;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class SubscribeToTopicActionTest {
	@Test
	public void shouldReturnType() {
		final SubscribeToTopicAction action = new SubscribeToTopicAction();
		assertEquals("Subscribe To Topic", action.getType());
	}

	@Test
	public void shouldReturnDefaultParameters() {
		final SubscribeToTopicAction action = new SubscribeToTopicAction();
		assertNotNull(action.getDefaultActionParameters());
		assertEquals(1, action.getDefaultActionParameters().size());
	}

	@Test
	public void shouldReturnIcon() {
		final SubscribeToTopicAction action = new SubscribeToTopicAction();
		assertNotNull(action.getIcon());
	}

	@Test
	public void shouldReturnEngineClass() {
		final SubscribeToTopicAction action = new SubscribeToTopicAction();
		assertEquals(SubscribeToTopicActionEngine.class, action.getEngineClass());
	}

	@Test
	public void shouldReturnDescription() {
		final SubscribeToTopicAction action = new SubscribeToTopicAction();
		assertNotNull(action.getDescription());
	}

	@Test
	public void shouldReturnDisplayName() {
		final SubscribeToTopicAction action = new SubscribeToTopicAction();
		assertNotNull(action.getDisplayName());
	}

	@Test
	public void shouldReturnDisplayPath() {
		final SubscribeToTopicAction action = new SubscribeToTopicAction();
		assertNotNull(action.getDisplayPath());
	}
}
