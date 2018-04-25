package eu.h2020.symbiote.smeur.dsi.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.h2020.symbiote.cloud.model.data.Result;
import eu.h2020.symbiote.enabler.messaging.model.rap.access.ResourceAccessSetMessage;
import eu.h2020.symbiote.enabler.messaging.model.rap.db.ResourceInfo;
import eu.h2020.symbiote.model.cim.ObservationValue;
import eu.h2020.symbiote.model.cim.WGS84Location;
import eu.h2020.symbiote.rapplugin.messaging.RapPluginOkResponse;
import eu.h2020.symbiote.smeur.dsi.messaging.RabbitManager;
import eu.h2020.symbiote.smeur.messages.DomainSpecificInterfaceResponse;
import eu.h2020.symbiote.smeur.messages.GrcRequest;
import eu.h2020.symbiote.smeur.messages.PoiSearchRequest;
import eu.h2020.symbiote.smeur.messages.QueryPoiInterpolatedValues;
import eu.h2020.symbiote.smeur.messages.QueryPoiInterpolatedValuesResponse;

/**
 * DomainSpecificInterface-SMEUR rest interface.
 * 
 * @author petarkrivic
 *
 */
@RestController
public class DomainSpecificInterfaceRestController {

	private static final Logger log = LoggerFactory.getLogger(DomainSpecificInterfaceRestController.class);

	private MessageConverter messageConverter = new Jackson2JsonMessageConverter();

	@Value("${rabbit.exchange.enablerLogic.plugin.name}")
	private String poiExchangeName;

	@Value("${rabbit.routingKey.enablerLogic.poiSearch}")
	private String poiRoutingKey;

	@Value("${rabbit.exchange.enablerLogic.name}")
	private String enablerLogicExchange;

	@Value("${rabbit.routingKey.enablerLogic.grc}")
	private String grcRoutingKey;

	@Value("${rabbit.routingKey.enablerLogic.interpolator}")
	private String interpolatorRoutingKey;

	@Autowired
	@Qualifier("RabbitManagerDSI")
	RabbitManager rabbitManager;

	/**
	 * Method parses and forwards received request to EL-PoI component, and
	 * returns the received result back to user.
	 * 
	 * @param lat
	 * @param lon
	 * @param r(in
	 *            km)
	 * @param amenity
	 * @return searched amenities in specified area
	 * @throws JsonProcessingException
	 */
	@RequestMapping(value = "/smeur/poi", method = RequestMethod.GET)
	public ResponseEntity<?> poiLatLon(@RequestParam(value = "lat") double lat, @RequestParam(value = "lon") double lon,
			@RequestParam(value = "r") double r, @RequestParam(value = "amenity") String amenity)
			throws JsonProcessingException {

		ObjectMapper om = new ObjectMapper();

		// prepare resourceInfo list for ResourceAccessSetMessage
		ResourceInfo resourceInfo = new ResourceInfo();
		resourceInfo.setInternalId("23");
		List<ResourceInfo> resourceInfoList = new ArrayList<ResourceInfo>();
		resourceInfoList.add(resourceInfo);

		// prepare received InputParameters for PoI request
		PoiSearchRequest poiReq = new PoiSearchRequest(lat, lon, r, amenity);

		RapPluginOkResponse receivedOK = (RapPluginOkResponse) rabbitManager.sendRpcMessage(poiExchangeName,
				poiRoutingKey, om.writeValueAsString(
						new ResourceAccessSetMessage(resourceInfoList, om.writeValueAsString(Arrays.asList(poiReq)))));

		log.info("Received response from PoI service: " + receivedOK.getContent());

		try {
			// fetch Result object from received RapPluginOkResponse
			Result<?> result = om.readValue(receivedOK.getContent(), Result.class);

			// Return response to user..
			HttpHeaders headers = new HttpHeaders();
			headers.add(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");

			ResponseEntity<?> re = ResponseEntity.ok().headers(headers).body(result.getValue());

			return re;
		} catch (IOException e1) {
			log.info("Error reading value from received PoI response!");
			e1.printStackTrace();
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (NullPointerException e) {
			log.info("Interpolator returned null!");
			return new ResponseEntity<String>("Interpolator returned null!", HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	/**
	 * Method parses and forwards received request to EL-GRC component, and
	 * returns the received result back to user.
	 * 
	 * @param fromLat
	 * @param fromLon
	 * @param toLat
	 * @param toLon
	 * @param transport
	 * @param optimisation
	 * @return
	 * @throws JsonProcessingException
	 */
	@RequestMapping(value = "/smeur/grc", method = RequestMethod.GET)
	public ResponseEntity<?> grcRequest(@RequestParam(value = "fromLat") double fromLat,
			@RequestParam(value = "fromLon") double fromLon, @RequestParam(value = "toLat") double toLat,
			@RequestParam(value = "toLon") double toLon, @RequestParam(value = "transport") String transport,
			@RequestParam(value = "optimisation") String optimisation) throws JsonProcessingException {

		// create locations
		WGS84Location from = new WGS84Location(fromLon, fromLat, 0, null, null);
		WGS84Location to = new WGS84Location(toLon, toLat, 0, null, null);
		// create request
		GrcRequest request = new GrcRequest(from, to, transport, optimisation);

		// send RMQ-rpc message to el-grc and return response
		Object k = rabbitManager.sendRpcMessage(enablerLogicExchange, grcRoutingKey,
				messageConverter.toMessage(request, null));

		try {
			log.info(new String((byte[]) k, StandardCharsets.UTF_8));
			return new ResponseEntity<String>(new String((byte[]) k, StandardCharsets.UTF_8), HttpStatus.OK);
		} catch (NullPointerException e) {
			log.info("Interpolator returned null!");
			return new ResponseEntity<String>("Interpolator returned null!", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * Method forwards received request to interpolator component, and returns
	 * received response back to user.
	 * 
	 * @param jsonString
	 * @return
	 * @throws JsonProcessingException
	 */
	@RequestMapping(value = "/smeur/interpolation", method = RequestMethod.POST, headers = "Accept=application/json")
	public ResponseEntity<?> interpolatorRequest(@RequestBody String jsonString) throws JsonProcessingException {
		log.info(jsonString);
		Map<String, WGS84Location> locations = new HashMap<String, WGS84Location>();
		try {
			JSONArray array = new JSONArray(jsonString);
			for (int i = 0; i < array.length(); i++) {
				JSONObject entry = array.getJSONObject(i);
				log.info("latitude is " + entry.getDouble("latitude") + " and longitude is "
						+ entry.getDouble("longitude"));
				// for every object create new wgs84location and put it to a map
				WGS84Location location = new WGS84Location(entry.getDouble("longitude"), entry.getDouble("latitude"), 0,
						"dummy", Arrays.asList("dummy"));
				locations.put(UUID.randomUUID().toString(), location);
			}

			QueryPoiInterpolatedValues qiv = new QueryPoiInterpolatedValues(locations);
			// send to interpolator and return response to user
			Object response = rabbitManager.sendRpcMessageJSON(enablerLogicExchange, interpolatorRoutingKey, qiv);
			try {
				log.info("Received from interpolator: " + response.toString());

				return new ResponseEntity<>(formatResponse(qiv, (QueryPoiInterpolatedValuesResponse) response),
						HttpStatus.OK);
			} catch (NullPointerException ex) {
				log.info("Interpolator returned null!");
				return new ResponseEntity<>("Interpolator returned null!", HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (JSONException e) {
			log.info("Bad JSON received!");
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

	/**
	 * Formatting received interpolator response to user response.
	 * 
	 * @param interpolatorQuery
	 * @param interpolatorResponse
	 * @return
	 */
	public List<DomainSpecificInterfaceResponse> formatResponse(QueryPoiInterpolatedValues interpolatorQuery,
			QueryPoiInterpolatedValuesResponse interpolatorResponse) {

		List<DomainSpecificInterfaceResponse> formatedResponse = new LinkedList<DomainSpecificInterfaceResponse>();

		for (Entry<String, WGS84Location> entry : interpolatorQuery.thePoints.entrySet()) {
			DomainSpecificInterfaceResponse place = new DomainSpecificInterfaceResponse();
			place.setName(entry.getValue().getName());
			place.setLatitude(String.valueOf(entry.getValue().getLatitude()));
			place.setLongitude(String.valueOf(entry.getValue().getLongitude()));
			List<ObservationValue> observations = new LinkedList<ObservationValue>();

			try {
				for (Map.Entry<String, ObservationValue> e : interpolatorResponse.theData
						.get(entry.getKey()).interpolatedValues.entrySet()) {
					observations.add(e.getValue());
				}
			} catch (NullPointerException e) {
				log.info("Error occurred! Interpolator doesn't have any data for requested POIs.");
			}

			place.setObservation(observations);
			formatedResponse.add(place);
		}
		return formatedResponse;
	}
}
