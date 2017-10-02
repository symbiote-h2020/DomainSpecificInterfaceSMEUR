package eu.h2020.symbiote.smeur.dsi.controller;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.h2020.symbiote.cloud.model.data.observation.Location;
import eu.h2020.symbiote.cloud.model.data.parameter.InputParameter;
import eu.h2020.symbiote.enabler.messaging.model.rap.access.ResourceAccessSetMessage;
import eu.h2020.symbiote.enabler.messaging.model.rap.db.ResourceInfo;
import eu.h2020.symbiote.smeur.dsi.messaging.RabbitManager;
import eu.h2020.symbiote.smeur.messages.GrcRequest;

/**
 * DomainSpecificInterface-SMEUR rest interface.
 * Created by Petar Krivic on
 * 28/08/2017.
 */
@RestController
public class PoiRestController {

	String poiExchangeName = "symbIoTe.enablerLogicPoi";
	String poiRoutingKey = "symbiote.enablerLogic.poiSearch";

	@Autowired
	RabbitManager rabbitManager;

	/**
	 * Method parses and forwards received request to EL-PoI component, and
	 * returns the received result back to user.
	 * 
	 * @param city
	 * @param amenity
	 * @return searched amenities in the queried city
	 */
	@RequestMapping(value = "/smeur/poi/city", method = RequestMethod.GET)
	public String poiCity(@RequestParam(value = "city") String city, @RequestParam(value = "amenity") String amenity) {

		// send RMQ-rpc message to el-poi and return response

		return "result";
	}

	/**
	 * Method parses and forwards received request to EL-PoI component, and
	 * returns the received result back to user.
	 * @param lat
	 * @param lon
	 * @param r (in km)
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

		// prepare InputParameters for PoI request
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

		Object k = rabbitManager.sendRpcMessage("plugin-exchange", "EnablerLogicPoISearch.set",
				om.writeValueAsString(rasm));
		// TODO testing phase (implementation of receiving answer)
		System.out.println(new String((byte[]) k, StandardCharsets.UTF_8));
		return new String((byte[]) k, StandardCharsets.UTF_8);
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
		Location from = new Location();
		from.setLatitude(fromLat);
		from.setLongitude(fromLon);
		Location to = new Location();
		to.setLatitude(toLat);
		to.setLongitude(toLon);
		// create request
		GrcRequest request = new GrcRequest(from, to, transport, optimisation);
		// send RMQ-rpc message to el-grc and return response
		ObjectMapper om = new ObjectMapper();
		// TODO exchange name, routing key
		Object k = rabbitManager.sendRpcMessage("?", "?", om.writeValueAsString(request));

		System.out.println(new String((byte[]) k, StandardCharsets.UTF_8));
		return new String((byte[]) k, StandardCharsets.UTF_8);
	}

}
