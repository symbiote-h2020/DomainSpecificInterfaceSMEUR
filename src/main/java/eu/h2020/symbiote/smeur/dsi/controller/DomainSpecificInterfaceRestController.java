package eu.h2020.symbiote.smeur.dsi.controller;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.h2020.symbiote.cloud.model.data.InputParameter;
import eu.h2020.symbiote.enabler.messaging.model.rap.access.ResourceAccessSetMessage;
import eu.h2020.symbiote.enabler.messaging.model.rap.db.ResourceInfo;
import eu.h2020.symbiote.model.cim.Location;
import eu.h2020.symbiote.model.cim.WGS84Location;
import eu.h2020.symbiote.smeur.dsi.messaging.RabbitManager;
import eu.h2020.symbiote.smeur.messages.GrcRequest;
import eu.h2020.symbiote.smeur.messages.QueryPoiInterpolatedValues;
import eu.h2020.symbiote.smeur.messages.QueryPoiInterpolatedValuesResponse;

/**
 * DomainSpecificInterface-SMEUR rest interface. Created by Petar Krivic on
 * 28/08/2017.
 */
/**
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
	RabbitManager rabbitManager;

	/**
	 * Method parses and forwards received request to EL-PoI component, and
	 * returns the received result back to user.
	 * 
	 * @param lat
	 * @param lon
	 * @param r(in km)
	 * @param amenity
	 * @return searched amenities in specified area
	 * @throws JsonProcessingException
	 */
	@RequestMapping(value = "/smeur/poi", method = RequestMethod.GET)
	public String poiLatLon(@RequestParam(value = "lat") double lat, @RequestParam(value = "lon") double lon,
			@RequestParam(value = "r") double r, @RequestParam(value = "amenity") String amenity)
			throws JsonProcessingException {

		ObjectMapper om = new ObjectMapper();

		ResourceInfo ri = new ResourceInfo();
		ri.setInternalId("23");

		// prepare received InputParameters for PoI request
		InputParameter latitude = new InputParameter("latitude");
		latitude.setValue(String.valueOf(lat));
		InputParameter longitude = new InputParameter("longitude");
		longitude.setValue(String.valueOf(lon));
		InputParameter radius = new InputParameter("radius");
		radius.setValue(String.valueOf(r));
		InputParameter amenit = new InputParameter("amenity");
		amenit.setValue(String.valueOf(amenity));

		List<ResourceInfo> l = new ArrayList<ResourceInfo>();
		l.add(ri);
		List<InputParameter> l1 = new ArrayList<InputParameter>();
		l1.add(latitude);
		l1.add(longitude);
		l1.add(radius);
		l1.add(amenit);
		ResourceAccessSetMessage rasm = new ResourceAccessSetMessage(l, om.writeValueAsString(l1));

		Object k = rabbitManager.sendRpcMessage(poiExchangeName, poiRoutingKey, om.writeValueAsString(rasm));

		// TODO testing phase (implementation of receiving answer)
		try {
			log.info(new String((byte[]) k, StandardCharsets.UTF_8));
			return new String((byte[]) k, StandardCharsets.UTF_8);
		} catch (NullPointerException e) {
			log.info("Interpolator returned null!");
			return null;
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
	public String grcRequest(@RequestParam(value = "fromLat") double fromLat,
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
			return new String((byte[]) k, StandardCharsets.UTF_8);
		} catch (NullPointerException e) {
			log.info("Interpolator returned null!");
			return null;
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
	public ResponseEntity interpolatorRequest(@RequestBody String jsonString) throws JsonProcessingException {
		log.info(jsonString);
		Map<String, WGS84Location> locations = new HashMap<String, WGS84Location>();
		try {
			JSONArray array = new JSONArray(jsonString);
			for (int i = 0; i < array.length(); i++) {
				JSONObject entry = array.getJSONObject(i);
				log.info("latitude is " + entry.getDouble("latitude") + " and longitude is "
						+ entry.getDouble("longitude"));
				// for every object create new wgs84location and put it to a map
				WGS84Location location = new WGS84Location(entry.getDouble("latitude"), entry.getDouble("longitude"), 0,
						null, null);
				locations.put(UUID.randomUUID().toString(), location);
			}

			QueryPoiInterpolatedValues qiv = new QueryPoiInterpolatedValues(locations);
			// send to interpolator and return response to user
			Object response = rabbitManager.sendRpcMessageJSON(enablerLogicExchange, interpolatorRoutingKey, qiv);
			log.info("Received from interpolator: " + response.toString());

			// TODO format received interpolated data to location
			return new ResponseEntity<>(response.toString(), HttpStatus.OK);
		} catch (JSONException e) {
			log.info("Bad JSON received!");
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
	}

}
