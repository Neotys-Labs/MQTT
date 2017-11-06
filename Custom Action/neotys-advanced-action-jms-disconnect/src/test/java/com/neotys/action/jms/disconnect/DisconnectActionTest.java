package com.neotys.action.jms.disconnect;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class DisconnectActionTest {
	@Test
	public void shouldReturnType() {
		final DisconnectAction action = new DisconnectAction();
		assertEquals("Disconnect", action.getType());
	}

	@Test
	public void shouldReturnDefaultParameters() {
		final DisconnectAction action = new DisconnectAction();
		assertNotNull(action.getDefaultActionParameters());
		assertEquals(1, action.getDefaultActionParameters().size());
	}

	@Test
	public void shouldReturnIcon() {
		final DisconnectAction action = new DisconnectAction();
		assertNotNull(action.getIcon());
	}

	@Test
	public void shouldReturnEngineClass() {
		final DisconnectAction action = new DisconnectAction();
		assertEquals(DisconnectActionEngine.class, action.getEngineClass());
	}

	@Test
	public void shouldReturnDescription() {
		final DisconnectAction action = new DisconnectAction();
		assertNotNull(action.getDescription());
	}

	@Test
	public void shouldReturnDisplayName() {
		final DisconnectAction action = new DisconnectAction();
		assertNotNull(action.getDisplayName());
	}

	@Test
	public void shouldReturnDisplayPath() {
		final DisconnectAction action = new DisconnectAction();
		assertNotNull(action.getDisplayPath());
	}
}
