package eu.h2020.symbiote.smeur.dsi;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.core.JsonProcessingException;

import eu.h2020.symbiote.smeur.dsi.controller.DomainSpecificInterfaceRestController;
import eu.h2020.symbiote.smeur.dsi.messaging.RabbitManager;

public class DomainSpecificInterfaceControllerTest {

	@Mock
	RabbitManager rm;

	@InjectMocks
	DomainSpecificInterfaceRestController poiRest;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testPoiSearchLatLon_nullReceived() throws JsonProcessingException {

		when(rm.sendRpcMessage(any(String.class), any(String.class), any(String.class))).thenReturn(null);
		assertNull(poiRest.poiLatLon(15.2133, 40.32131, 23, "hospital"));
	}

	@Test
	public void testPoiSearchLatLon_responseReceived() throws JsonProcessingException {

		when(rm.sendRpcMessage(any(String.class), any(String.class), any(String.class)))
				.thenReturn("dummyStringResponse".getBytes());
		assertNotNull(poiRest.poiLatLon(15.2133, 40.32131, 23, "hospital"));
	}

	@Test
	public void testGrc_nullReceived() throws JsonProcessingException {

		when(rm.sendRpcMessage(any(String.class), any(String.class), any(String.class))).thenReturn(null);
		assertNull(poiRest.grcRequest(15.212, 30.2121, 16.2123, 32.212, "car", "airQuality"));
	}

	@Test
	public void testGrc_responseReceived() throws JsonProcessingException {

		when(rm.sendRpcMessage(any(String.class), any(String.class), any(String.class)))
				.thenReturn("dummyStringResponse".getBytes());
		assertNotNull(poiRest.grcRequest(15.212, 30.2121, 16.2123, 32.212, "car", "airQuality"));
	}
}