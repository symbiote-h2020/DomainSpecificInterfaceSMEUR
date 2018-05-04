package eu.h2020.symbiote.smeur.dsi;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.core.JsonProcessingException;

import eu.h2020.symbiote.rapplugin.messaging.RapPluginOkResponse;
import eu.h2020.symbiote.smeur.dsi.controller.DomainSpecificInterfaceRestController;
import eu.h2020.symbiote.smeur.dsi.messaging.RabbitManager;
import eu.h2020.symbiote.smeur.messages.GrcResponse;
import eu.h2020.symbiote.smeur.messages.QueryPoiInterpolatedValuesResponse;

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
	public void testPoiSearchLatLon_interpolatorError() throws JsonProcessingException {

		when(rm.sendRpcMessage(any(String.class), any(String.class), any(String.class))).thenReturn(new RapPluginOkResponse());
		assertEquals(new ResponseEntity<String>("Interpolator returned null!",HttpStatus.INTERNAL_SERVER_ERROR), poiRest.poiLatLon(15.2133, 40.32131, 23, "hospital"));
	}

	@Test
	public void testPoiSearchLatLon_responseReceived() throws JsonProcessingException {

		when(rm.sendRpcMessage(any(String.class), any(String.class), any(String.class)))
				.thenReturn(new RapPluginOkResponse());
		assertNotNull(poiRest.poiLatLon(15.2133, 40.32131, 23, "hospital"));
	}

	@Test
	public void testGrc_responseReceived() throws JsonProcessingException {

		when(rm.sendRpcMessage(any(String.class), any(String.class), any(String.class)))
				.thenReturn(new GrcResponse());
		assertNotNull(poiRest.grcRequest(15.212, 30.2121, 16.2123, 32.212, "car", "airQuality"));
	}
	
	@Test
	public void testInterpolator_nullReceived() throws JsonProcessingException {

		when(rm.sendRpcMessageJSON(any(String.class), any(String.class), any(String.class)))
				.thenReturn(null);
		assertEquals(new ResponseEntity<>("Interpolator returned null!",HttpStatus.INTERNAL_SERVER_ERROR),poiRest.interpolatorRequest("[\r\n\t{\r\n\t\t\"latitude\": \"45.8092991\",\r\n\t\t\"longitude\": \"15.9878854\"\r\n\t},\r\n\t{\r\n\t\t\"latitude\": \"45.8052317\",\r\n\t\t\"longitude\": \"15.9747292\"\r\n\t}\r\n]"));
	}
	
	@Test
	public void testInterpolator_responseReceived() throws JsonProcessingException {

		when(rm.sendRpcMessageJSON(any(String.class), any(String.class), any(String.class)))
				.thenReturn(new QueryPoiInterpolatedValuesResponse());
		assertNotNull(poiRest.interpolatorRequest("[\r\n\t{\r\n\t\t\"latitude\": \"45.8092991\",\r\n\t\t\"longitude\": \"15.9878854\"\r\n\t},\r\n\t{\r\n\t\t\"latitude\": \"45.8052317\",\r\n\t\t\"longitude\": \"15.9747292\"\r\n\t}\r\n]"));
	}
	
	@Test
	public void grcRequest_properJson() throws JsonProcessingException{
		assertEquals(new ResponseEntity<>(HttpStatus.OK), poiRest.grcRequest("{\"location\":{\"@c\":\".WGS84Location\",\"longitude\":43.22,\"latitude\":15.21,\"altitude\":40.0,\"name\":\"location\",\"description\":[\"description\"]},\"routeId\":1234,\"timestamp\":15252667}"));
	}
	
	@Test
	public void grcRequest_badJson() throws JsonProcessingException{
		assertEquals(new ResponseEntity<>("Bad JSON!", HttpStatus.BAD_REQUEST), poiRest.grcRequest("{\"location\":{\"@c\":\".WGS84Location\",\"longitude\":43.22,\"latitude\":15.21,\"altitude\":40.0,\"name\":\"location\",\"description\":[\"description\"]},\"routeId\":1234}"));
	}
}
