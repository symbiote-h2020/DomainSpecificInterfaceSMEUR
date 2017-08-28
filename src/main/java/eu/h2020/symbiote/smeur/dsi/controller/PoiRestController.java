package eu.h2020.symbiote.smeur.dsi.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * DomainSpecificInterface-SMEUR rest interface
 * Created by Petar Krivic on 28/08/2017.
 */
@RestController
public class PoiRestController {
	
	/**
	 * Method parses and forwards received request to EL-PoI component,
	 * and returns the received result back to user.
	 * @param city
	 * @param amenity
	 * @return searched amenities in the queried city
	 */
	@RequestMapping(value="/smeur/poi/city", method=RequestMethod.GET)
    public String poiCity(@RequestParam(value="city") String city,
    		@RequestParam(value="amenity") String amenity) {
		
		//send RMQ-rpc message to el-poi and return response
		
        return "result";
    }
	
	/**
	 * Method parses and forwards received request to EL-PoI component,
	 * and returns the received result back to user.
	 * @param lat
	 * @param lon
	 * @param r (in km)
	 * @param amenity
	 * @return searched amenities in specified area
	 */
	@RequestMapping(value="/smeur/poi", method=RequestMethod.GET)
    public String poiLatLon(@RequestParam(value="lat") double lat,
    		@RequestParam(value="lon") double lon,
    		@RequestParam(value="r") double r,
    		@RequestParam(value="amenity") String amenity) {
		
		double northBound = lat + ((1/111)*r);
		double southBound = lat - ((1/111)*r);
		
		double eastBound = lon + ((1/(111*(Math.cos(Math.toRadians(lat)))))*r);
		double westBound = lon - ((1/(111*Math.cos(Math.toRadians(lat))))*r);
		
		//send RMQ-rpc message to el-poi and return response
		
        return "result";
    }

}
