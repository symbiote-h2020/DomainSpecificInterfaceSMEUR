package eu.h2020.symbiote.smeur.dsi;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;

import eu.h2020.symbiote.smeur.dsi.messaging.RabbitManager;

public class RabbitManagerTest {

	@Mock
	private RabbitTemplate rabbitTemplate;

	@InjectMocks
	private RabbitManager rabbitManager;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void sendRpcMessageWithObject_shouldCallRabbitTemplate() throws Exception {
		// given
		String exchange = "e";
		String key = "k";
		Object obj = new Object();

		// when
		rabbitManager.sendRpcMessage(exchange, key, obj);

		// then
		verify(rabbitTemplate).convertSendAndReceive(eq(exchange), eq(key), eq(obj), any(CorrelationData.class));
	}

	@Test
	public void sendRpcMessageWithObjectNullReceived() throws Exception {
		// given
		String exchange = "e";
		String key = "k";
		Object obj = new Object();

		// when
		when(rabbitTemplate.convertSendAndReceive(eq(exchange), eq(key), eq(obj), any(CorrelationData.class)))
				.thenReturn(null);

		assertNull(rabbitManager.sendRpcMessage(exchange, key, obj));
	}

	@Test
	public void sendRpcMessageWithObjectNResponseReceived() throws Exception {
		// given
		String exchange = "e";
		String key = "k";
		Object obj = new Object();

		// when
		when(rabbitTemplate.convertSendAndReceive(eq(exchange), eq(key), eq(obj), any(CorrelationData.class)))
				.thenReturn("dummyResponse");

		assertEquals("dummyResponse", rabbitManager.sendRpcMessage(exchange, key, obj));
	}
}
