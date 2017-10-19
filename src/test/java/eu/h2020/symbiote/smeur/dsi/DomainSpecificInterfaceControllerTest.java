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

import eu.h2020.symbiote.smeur.dsi.controller.PoiRestController;
import eu.h2020.symbiote.smeur.dsi.messaging.RabbitManager;

public class DomainSpecificInterfaceControllerTest {

	@Mock
	RabbitManager rm;

	@InjectMocks
	PoiRestController poiRest;

	@Before
	public void setUp() throws Exception {

		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testPoiSearchLatLon() throws JsonProcessingException {

		when(rm.sendRpcMessage(any(String.class), any(String.class), any(String.class))).thenReturn(null);
		assertNull(poiRest.poiLatLon(15.2133, 40.32131, 23, "hospital"));
	}

	@Test
	public void testPoiSearchLatLonNotNull() throws JsonProcessingException {

		when(rm.sendRpcMessage(any(String.class), any(String.class), any(String.class)))
				.thenReturn("dummyStringResponse".getBytes());
		assertNotNull(poiRest.poiLatLon(15.2133, 40.32131, 23, "hospital"));
	}
}
