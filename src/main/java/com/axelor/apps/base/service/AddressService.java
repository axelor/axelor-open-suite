/**
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the “License”); you may not use
 * this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://license.axelor.com/.
 *
 * The License is based on the Mozilla Public License Version 1.1 but
 * Sections 14 and 15 have been added to cover use of software over a
 * computer network and provide for limited attribution for the
 * Original Developer. In addition, Exhibit A has been modified to be
 * consistent with Exhibit B.
 *
 * Software distributed under the License is distributed on an “AS IS”
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is part of "Axelor Business Suite", developed by
 * Axelor exclusively.
 *
 * The Original Developer is the Initial Developer. The Initial Developer of
 * the Original Code is Axelor.
 *
 * All portions of the code written by Axelor are
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 */
package com.axelor.apps.base.service;

import groovy.util.XmlSlurper;
import groovy.util.slurpersupport.GPathResult;
import groovy.util.slurpersupport.Node;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import wslite.json.JSONArray;
import wslite.json.JSONException;
import wslite.json.JSONObject;
import wslite.rest.ContentType;
import wslite.rest.RESTClient;
import wslite.rest.Response;

import au.com.bytecode.opencsv.CSVWriter;

import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.Country;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.exception.service.TraceBackService;
import com.google.inject.Inject;


public class AddressService {
	
	
	@Inject
	private com.axelor.apps.tool.address.AddressTool ads;
	
	private static final Logger LOG = LoggerFactory.getLogger(AddressService.class);
	
	public boolean check(String wsdlUrl) {
		return ads.doCanSearch(wsdlUrl);
	}
	
	public Map<String,Object> validate(String wsdlUrl, String search) {
		return (Map<String, Object>) ads.doSearch(wsdlUrl, search);
	}
	
	public com.qas.web_2005_02.Address select(String wsdlUrl, String moniker) {
		return ads.doGetAddress(wsdlUrl, moniker);
	}
	
	public int export(String path) throws IOException {
		List<Address> addresses = (List<Address>) Address.all().filter("self.certifiedOk IS FALSE").fetch();
		
		CSVWriter csv = new CSVWriter(new java.io.FileWriter(path), "|".charAt(0), CSVWriter.NO_QUOTE_CHARACTER);
		List<String> header = new ArrayList<String>();
		header.add("Id");
		header.add("AddressL1");
		header.add("AddressL2");
		header.add("AddressL3");
		header.add("AddressL4");
		header.add("AddressL5");
		header.add("AddressL6");
		header.add("CodeINSEE");
		
		csv.writeNext(header.toArray(new String[header.size()]));
		List<String> items = new ArrayList<String>();
		for (Address a : addresses) {
			
			items.add(a.getId() != null ? a.getId().toString(): "");
			items.add(a.getAddressL2() != null ? a.getAddressL2(): "");
			items.add(a.getAddressL3() != null ? a.getAddressL3(): "");
			items.add(a.getAddressL4() != null ? a.getAddressL4(): "");
			items.add(a.getAddressL5() != null ? a.getAddressL5(): "");
			items.add(a.getAddressL6() != null ? a.getAddressL6(): "");
			items.add(a.getInseeCode() != null ? a.getInseeCode(): "");
			
			csv.writeNext(items.toArray(new String[items.size()]));
			items.clear();
		}
		csv.close();
		LOG.info("{} exported", path);
		
		return addresses.size();
	}
	
	
	public Address createAddress(String addressL2, String addressL3, String addressL4, String addressL5, String addressL6, Country addressL7Country)  {
		
		Address address = new Address();
		address.setAddressL2(addressL2);
		address.setAddressL3(addressL3);
		address.setAddressL4(addressL4);
		address.setAddressL5(addressL5);
		address.setAddressL6(addressL6);
		address.setAddressL7Country(addressL7Country);
		
		return address;
	}
	
	
	public Address getAddress(String addressL2, String addressL3, String addressL4, String addressL5, String addressL6, Country addressL7Country)  {
		
		return Address.all().filter("self.addressL2 = ?1 AND self.addressL3 = ?2 AND self.addressL4 = ?3 " +
				"AND self.addressL5 = ?4 AND self.addressL6 = ?5 AND self.addressL7Country = ?6",
				addressL2,
				addressL3,
				addressL4,
				addressL5,
				addressL6,
				addressL7Country).fetchOne();
	}
	
	public JSONObject geocodeGoogle(String qString) {
		Map<String,Object> response = new HashMap<String,Object>();
		//http://maps.googleapis.com/maps/api/geocode/json?address=1600+Amphitheatre+Parkway,+Mountain+View,+CA&sensor=true_or_false

		// TODO inject the rest client, or better, run it in the browser
		RESTClient restClient = new RESTClient("https://maps.googleapis.com");
		Map<String,Object> responseQuery = new HashMap<String,Object>();
		responseQuery.put("address", qString);
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
	
	public HashMap<String,Object> getMapGoogle(String qString, BigDecimal latitude, BigDecimal longitude){
		HashMap<String,Object> result = new HashMap<String,Object>();
		try {
			if(BigDecimal.ZERO.compareTo(latitude) == 0 || BigDecimal.ZERO.compareTo(longitude) == 0){
				JSONObject googleResponse = geocodeGoogle(qString);
				if(googleResponse != null){
					latitude = new BigDecimal(googleResponse.get("lat").toString());
					longitude = new BigDecimal(googleResponse.get("lng").toString());
				}
			}
			if(BigDecimal.ZERO.compareTo(latitude) != 0 && BigDecimal.ZERO.compareTo(longitude) != 0){
				result.put("url",String.format("map/gmaps.html?x=%f&y=%f&z=18",latitude,longitude));
				result.put("latitude", latitude);
				result.put("longitude",longitude);
				return result;
			}
			
		}catch(Exception e){
			TraceBackService.trace(e);
		}
		return null;
	}
	
	
	public HashMap<String,Object> getMapOsm(String qString, BigDecimal latitude, BigDecimal longitude){
		HashMap<String,Object> result = new HashMap<String,Object>();
		try {
			if(BigDecimal.ZERO.compareTo(latitude) == 0 ||  BigDecimal.ZERO.compareTo(longitude) == 0 ){
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
			}
			if(BigDecimal.ZERO.compareTo(latitude) != 0 && BigDecimal.ZERO.compareTo(longitude) != 0){
				result.put("url",String.format("map/oneMarker.html?x=%f&y=%f&z=18",latitude,longitude));
				result.put("latitude", latitude);
				result.put("longitude",longitude);
				return result;
			}
			
		}catch(Exception e)  {
			TraceBackService.trace(e); 
		}
		return null;
	}
	
	public HashMap<String,Object> getMap(String qString, BigDecimal latitude, BigDecimal longitude){
		LOG.debug("qString = {}", qString);
		if (GeneralService.getGeneral().getMapApiSelect().equals("1")) 
			return getMapGoogle(qString,latitude,longitude);
		else
			return getMapOsm(qString,latitude,longitude);
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
					result.put("url",String.format("map/directions.html?dx=%f&dy=%f&ax=%f&ay=%f",dLat,dLon,aLat,aLon));
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
}
