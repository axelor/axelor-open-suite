/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2016 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.base.service;

import groovy.util.XmlSlurper;
import groovy.util.slurpersupport.GPathResult;
import groovy.util.slurpersupport.Node;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import wslite.json.JSONArray;
import wslite.json.JSONException;
import wslite.json.JSONObject;
import wslite.rest.ContentType;
import wslite.rest.RESTClient;
import wslite.rest.Response;

import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;


public class MapService {

	@Inject
	protected GeneralService generalService;

	private static final Logger LOG = LoggerFactory.getLogger(MapService.class);

	public JSONObject geocodeGoogle(String qString) {
		if(qString == null){
			return null;
		}
		Map<String,Object> response = new HashMap<String,Object>();
		//http://maps.googleapis.com/maps/api/geocode/json?address=1600+Amphitheatre+Parkway,+Mountain+View,+CA&sensor=true_or_false

		// TODO inject the rest client, or better, run it in the browser
		RESTClient restClient = new RESTClient("https://maps.googleapis.com");
		Map<String,Object> responseQuery = new HashMap<String,Object>();
		responseQuery.put("address", qString.trim());
		responseQuery.put("sensor", "false");
		Map<String,Object> responseMap = new HashMap<String,Object>();
		responseMap.put("path", "/maps/api/geocode/json");
		responseMap.put("accept", ContentType.JSON);
		responseMap.put("query", responseQuery);

		responseMap.put("connectTimeout", 5000);
		responseMap.put("readTimeout", 10000);
		responseMap.put("followRedirects", false);
		responseMap.put("useCaches", false);
		responseMap.put("sslTrustAllCerts", true);
		JSONObject restResponse = null;
		try {
			restResponse = new JSONObject(restClient.get(responseMap).getContentAsString());
			LOG.debug("Gmap response: {}",restResponse);
			if(restResponse != null && restResponse.containsKey("results")){
				JSONObject result = (JSONObject)((JSONArray)restResponse.get("results")).get(0);
				if(result != null && result.containsKey("geometry"))
					restResponse = (JSONObject)((JSONObject) result.get("geometry")).get("location");
				else restResponse = null;
			}
			else restResponse = null;

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/*
		log.debug("restResponse = {}", restResponse)
		log.debug("restResponse.parsedResponseContent.text = {}", restResponse.parsedResponseContent.text)
		 */
		//def searchresults = new JsonSlurper().parseText(restResponse.parsedResponseContent.text);
		/*
		LOG.debug("searchresults.status = {}", searchresults.status);
		if (searchresults.status == "OK") {
			/*
			log.debug("searchresults.results.size() = {}", searchresults.results.size())
			log.debug("searchresults.results[0] = {}", searchresults.results[0])
			log.debug("searchresults.results[0].address_components = {}", searchresults.results[0].address_components)
			log.debug("searchresults.results[0].geometry.location = {}", searchresults.results[0].geometry.location)
		 */
		/*
		def results = searchresults.results;

		if (results.size() > 1) {
			response.put("multiple", true);
		}
		def firstPlaceFound = results[0];

		if (firstPlaceFound) {
			BigDecimal lat = new BigDecimal(firstPlaceFound.geometry.location.lat);
			BigDecimal lng = new BigDecimal(firstPlaceFound.geometry.location.lng);

			response.put("lat", lat.setScale(10, RoundingMode.HALF_EVEN));
			response.put("lng", lng.setScale(10, RoundingMode.HALF_EVEN));
		}
	*/
	//}

		return restResponse;
	}

	public HashMap<String,Object> getMapGoogle(String qString){
		LOG.debug("Query string: {}",qString);
		try {
				JSONObject googleResponse = geocodeGoogle(qString);
				LOG.debug("Google response: {}",googleResponse);
				if(googleResponse != null){
					HashMap<String,Object> result = new HashMap<String,Object>();
					BigDecimal latitude = new BigDecimal(googleResponse.get("lat").toString());
					BigDecimal longitude = new BigDecimal(googleResponse.get("lng").toString());
					LOG.debug("URL:"+"map/gmaps.html?x="+latitude+"&y="+longitude+"&z=18");
					result.put("url","map/gmaps.html?x="+latitude+"&y="+longitude+"&z=18");
					result.put("latitude", latitude);
					result.put("longitude",longitude);
					return result;
				}

		}catch(Exception e){
			TraceBackService.trace(e);
		}
		return null;
	}


	public HashMap<String,Object> getMapOsm(String qString){
		HashMap<String,Object> result = new HashMap<String,Object>();
		try {
			BigDecimal latitude = BigDecimal.ZERO;
			BigDecimal longitude = BigDecimal.ZERO;
			RESTClient restClient = new RESTClient("http://nominatim.openstreetmap.org/");
			Map<String,Object> mapQuery = new HashMap<String,Object>();
			mapQuery.put("q", qString);
			mapQuery.put("format", "xml");
			mapQuery.put("polygon", true);
			mapQuery.put("addressdetails", true);
			Map<String,Object> mapHeaders = new HashMap<String,Object>();
			mapHeaders.put("HTTP referrer", "axelor");
			Map<String,Object> mapResponse = new HashMap<String,Object>();
			mapResponse.put("path", "/search");
			mapResponse.put("accept", ContentType.JSON);
			mapResponse.put("query", mapQuery);
			mapResponse.put("headers", mapHeaders);
			mapResponse.put("connectTimeout", 5000);
			mapResponse.put("readTimeout", 10000);
			mapResponse.put("followRedirects", false);
			mapResponse.put("useCaches", false);
			mapResponse.put("sslTrustAllCerts", true);
			Response restResponse = restClient.get(mapResponse);
			GPathResult searchresults = new XmlSlurper().parseText(restResponse.getContentAsString());
			Iterator<Node> iterator = searchresults.childNodes();
			if(iterator.hasNext()){
				Node node = iterator.next();
				Map attributes = node.attributes();
				if(attributes.containsKey("lat") && attributes.containsKey("lon")){
					if(BigDecimal.ZERO.compareTo(latitude) == 0)
						latitude = new BigDecimal(node.attributes().get("lat").toString());
					if(BigDecimal.ZERO.compareTo(longitude) == 0)
						longitude = new BigDecimal(node.attributes().get("lon").toString());
				}
			}
			
			LOG.debug("OSMap qString: {}, latitude: {}, longitude: {}", qString, latitude, longitude);
			
			if(BigDecimal.ZERO.compareTo(latitude) != 0 && BigDecimal.ZERO.compareTo(longitude) != 0){
				result.put("url","map/oneMarker.html?x="+latitude+"&y="+longitude+"&z=18");
				result.put("latitude", latitude);
				result.put("longitude",longitude);
				return result;
			}

		}catch(Exception e)  {
			TraceBackService.trace(e);
		}
		return null;
	}

	public HashMap<String,Object> getMap(String qString){
		LOG.debug("qString = {}", qString);
		if (generalService.getGeneral().getMapApiSelect() == IAdministration.MAP_API_GOOGLE)
			return getMapGoogle(qString);
		else
			return getMapOsm(qString);
	}

	public String getMapUrl(BigDecimal latitude, BigDecimal longitude){
		if (generalService.getGeneral().getMapApiSelect() == IAdministration.MAP_API_GOOGLE)
			return "map/gmaps.html?x="+latitude+"&y="+longitude+"&z=18";
		else
			return "map/oneMarker.html?x="+latitude+"&y="+longitude+"&z=18";
	}

	public String getDirectionUrl(BigDecimal dLat, BigDecimal dLon, BigDecimal aLat, BigDecimal aLon){
			return "map/directions.html?dx="+dLat+"&dy="+dLon+"&ax="+aLat+"&ay="+aLon;
	}

	public HashMap<String,Object> getDirectionMapGoogle(String dString, BigDecimal dLat, BigDecimal dLon, String aString, BigDecimal aLat, BigDecimal aLon){
		LOG.debug("departureString = {}", dString);
		LOG.debug("arrivalString = {}", aString);
		HashMap<String,Object> result = new HashMap<String,Object>();
		try {
			if (BigDecimal.ZERO.compareTo(dLat) == 0 || BigDecimal.ZERO.compareTo(dLon) == 0) {
				Map<String,Object> googleResponse = geocodeGoogle(dString);
				if(googleResponse != null){
					dLat = new BigDecimal(googleResponse.get("lat").toString());
					dLon = new BigDecimal(googleResponse.get("lng").toString());
				}
			}
			LOG.debug("departureLat = {}, departureLng={}", dLat,dLon);
			if (BigDecimal.ZERO.compareTo(aLat) == 0  || BigDecimal.ZERO.compareTo(aLon) == 0 ) {
				Map<String,Object> googleResponse = geocodeGoogle(aString);
				if(googleResponse != null){
					aLat = new BigDecimal(googleResponse.get("lat").toString());
					aLon = new BigDecimal(googleResponse.get("lng").toString());
				}
			}
			LOG.debug("arrivalLat = {}, arrivalLng={}", aLat,aLon);
			if(BigDecimal.ZERO.compareTo(dLat) != 0  && BigDecimal.ZERO.compareTo(dLon) != 0){
				if(BigDecimal.ZERO.compareTo(aLat) != 0 && BigDecimal.ZERO.compareTo(aLon) != 0){
					result.put("url","map/directions.html?dx="+dLat+"&dy="+dLon+"&ax="+aLat+"&ay="+aLon);
					result.put("aLat", aLat);
					result.put("dLat", dLat);
					return result;
				}
			}
		}catch(Exception e)  {
			TraceBackService.trace(e);
		}

		return null;
	}

	public boolean isInternetAvailable() {
		return testInternet("google.com") || testInternet("facebook.com") || testInternet("yahoo.com");
	}

	private boolean testInternet(String site) {
	    Socket socket = new Socket();
	    InetSocketAddress address = new InetSocketAddress(site, 80);
	    try {
	    	socket.connect(address, 3000);
	        return true;
	    } catch (IOException e) {
	        return false;
	    } finally {
	        try {socket.close();}
	        catch (IOException e) {}
	    }
	}

	public String makeAddressString(Address address, ObjectNode objectNode) {

		address = Beans.get(AddressService.class).checkLatLang(address,false);
		BigDecimal latit = address.getLatit();
		BigDecimal longit = address.getLongit();
		if(BigDecimal.ZERO.compareTo(latit) == 0 || BigDecimal.ZERO.compareTo(longit) == 0){
			return null;
		}
		objectNode.put("latit",latit);
		objectNode.put("longit",longit);

		StringBuilder addressString = new StringBuilder();

		if (address.getAddressL2() != null) {
			addressString.append(address.getAddressL2() + "</br>");
		}
		if (address.getAddressL3() != null) {
			addressString.append(address.getAddressL3() + "</br>");
		}
		if (address.getAddressL4() != null) {
			addressString.append(address.getAddressL4() + "</br>");
		}
		if (address.getAddressL5() != null) {
			addressString.append(address.getAddressL5() + "</br>");
		}
		if (address.getAddressL6() != null) {
			addressString.append(address.getAddressL6());
		}
		if (address.getAddressL7Country() != null) {
			addressString = addressString.append("</br>" + address.getAddressL7Country().getName());
		}

		return addressString.toString();
	}
	
	public boolean testGMapService(){
		
		RESTClient restClient = new RESTClient("https://maps.googleapis.com");
		
		Map<String,Object> responseMap = new HashMap<String,Object>();
		responseMap.put("path", "/maps/api/geocode/json");
		responseMap.put("accept", ContentType.JSON);

		responseMap.put("connectTimeout", 5000);
		responseMap.put("readTimeout", 10000);
		responseMap.put("followRedirects", false);
		responseMap.put("useCaches", false);
		responseMap.put("sslTrustAllCerts", true);
		
		Response response = restClient.get(responseMap);
		
		LOG.debug("Gmap connection status code: {}, message: {}", response.getStatusCode(), response.getStatusMessage());
		
		if(response.getStatusCode() == 200){
			return true;
		}
		
		return false;
	}
	
}
