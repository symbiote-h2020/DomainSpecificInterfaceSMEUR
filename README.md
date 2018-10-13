[![Build Status](https://api.travis-ci.org/symbiote-h2020/DomainSpecificInterfaceSMEUR.svg?branch=staging)](https://api.travis-ci.org/symbiote-h2020/DomainSpecificInterfaceSMEUR)
[![codecov.io](https://codecov.io/github/symbiote-h2020/DomainSpecificInterfaceSMEUR/branch/master/graph/badge.svg)](https://codecov.io/github/symbiote-h2020/DomainSpecificInterfaceSMEUR)

# Domain Specific Interface SMEUR

Examples of using DSI to reach SMEUR services:

## PoI search service

- HTTP GET request
- URL:
  - http://smeur.tel.fer.hr:8823/smeur/poi?lat=45.8014&lon=15.9711&r=2&amenity=bicycle_rental
- Parameters:
  - lat – current user location latitude
  - lon – current user location longitude
  - r – radius of distance from user where pois are searched
  - amenity – amenity type (available [amenities](https://wiki.openstreetmap.org/wiki/Key:amenity))
- Response:
  - json list of queried amenities containing latitude, longitude and name of result locations

## GRC service

- HTTP GET request
- URL:
  - http://smeur.tel.fer.hr:8823/smeur/grc?fromLon=16.3657665&fromLat=48.2114620&toLon=16.18465&toLat=48.216799&transport=foot&optimisation=something
- Parameters:
  - fromLon – starting position longitude
  - fromLat – starting position latitude
  - toLon – finish position longitude
  - toLat – finish position latitude
  - transport – transportation type
  - optimisation – optimisation criteria
- Response:
  - json route
  
## Interpolation service

- HTTP POST request
- URL:
  - http://smeur.tel.fer.hr:8823/smeur/interpolation
- Header:
  - Content-Type:application/json
- Body:
  - List of locations where interpolated data is queried
  - Example:
```json
[{
		"latitude": "45.8092991",
		"longitude": "15.9878854"
	},
	{
		"latitude": "45.8052317",
		"longitude": "15.9747292"
}]
```
- Response:
  - Interpolated data for queried locations
