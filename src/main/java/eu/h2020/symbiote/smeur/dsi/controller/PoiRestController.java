package eu.h2020.symbiote.smeur.dsi.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PoiRestController {
	
	@RequestMapping(value="/smeur/poi", method=RequestMethod.GET)
    public String greeting(@RequestParam(value="city") String city,
    		@RequestParam(value="amenity") String amenity) {
		
		//send RMQ-rpc message to el-poi and return response
		
        return "result";
    }

}
