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
