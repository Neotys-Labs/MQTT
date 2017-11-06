package com.neotys.action.jms.unsubscribe;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class UnsubscribeFromTopicActionTest {
	@Test
	public void shouldReturnType() {
		final UnsubscribeFromTopicAction action = new UnsubscribeFromTopicAction();
		assertEquals("Unsubscribe From Topic", action.getType());
	}

	@Test
	public void shouldReturnDefaultParameters() {
		final UnsubscribeFromTopicAction action = new UnsubscribeFromTopicAction();
		assertNotNull(action.getDefaultActionParameters());
		assertEquals(1, action.getDefaultActionParameters().size());
	}

	@Test
	public void shouldReturnIcon() {
		final UnsubscribeFromTopicAction action = new UnsubscribeFromTopicAction();
		assertNotNull(action.getIcon());
	}

	@Test
	public void shouldReturnEngineClass() {
		final UnsubscribeFromTopicAction action = new UnsubscribeFromTopicAction();
		assertEquals(UnsubscribeFromTopicActionEngine.class, action.getEngineClass());
	}

	@Test
	public void shouldReturnDescription() {
		final UnsubscribeFromTopicAction action = new UnsubscribeFromTopicAction();
		assertNotNull(action.getDescription());
	}

	@Test
	public void shouldReturnDisplayName() {
		final UnsubscribeFromTopicAction action = new UnsubscribeFromTopicAction();
		assertNotNull(action.getDisplayName());
	}

	@Test
	public void shouldReturnDisplayPath() {
		final UnsubscribeFromTopicAction action = new UnsubscribeFromTopicAction();
		assertNotNull(action.getDisplayPath());
	}
}
