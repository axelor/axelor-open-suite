/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2017 Axelor (<http://axelor.com>).
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

package com.axelor.apps.prestashop.imports.service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;

import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.AppPrestashop;
import com.axelor.apps.base.db.City;
import com.axelor.apps.base.db.Country;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PartnerAddress;
import com.axelor.apps.base.db.repo.AddressRepository;
import com.axelor.apps.base.db.repo.AppPrestashopRepository;
import com.axelor.apps.base.db.repo.CityRepository;
import com.axelor.apps.base.db.repo.CountryRepository;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.prestashop.exception.IExceptionMessage;
import com.axelor.apps.prestashop.service.library.PSWebServiceClient;
import com.axelor.apps.prestashop.service.library.PrestaShopWebserviceException;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import wslite.json.JSONException;
import wslite.json.JSONObject;

public class ImportAddressServiceImpl implements ImportAddressService {

	PSWebServiceClient ws;
    HashMap<String,Object> opt;
    JSONObject schema;
    private final String shopUrl;
	private final String key;
	
	@Inject
	private CityRepository cityRepo;
	
	@Inject
	private PartnerRepository partnerRepo;
	
	/**
	 * initialization
	 */
	public ImportAddressServiceImpl() {
		AppPrestashop prestaShopObj = Beans.get(AppPrestashopRepository.class).all().fetchOne();
		shopUrl = prestaShopObj.getPrestaShopUrl();
		key = prestaShopObj.getPrestaShopKey();
	}
	
	@SuppressWarnings("deprecation")
	@Override
	@Transactional
	public BufferedWriter importAddress(BufferedWriter bwImport)
			throws IOException, PrestaShopWebserviceException, TransformerException, JAXBException, JSONException {
		
		Integer done = 0;
		Integer anomaly = 0;
		bwImport.newLine();
		bwImport.write("-----------------------------------------------");
		bwImport.newLine();
		bwImport.write("Address");
		
		String partnerId = null;
		String deletedId = null;
		String addressId = null;
		String addressL4 = null;
		String addressL5 = null;
		String postcode = null;
		String cityName = null;
		String countryId = null;
		Partner partner = null;
		Address address = null;
		PartnerAddress partnerAddress = null;
		City city = null;
		
		ws = new PSWebServiceClient(shopUrl,key);
		List<Integer> addressIds = ws.fetchApiIds("addresses");
		
		for (Integer id : addressIds) {
			ws = new PSWebServiceClient(shopUrl,key);
			opt = new HashMap<String, Object>();
			opt.put("resource", "addresses");
			opt.put("id", id);
			schema = ws.getJson(opt);


			deletedId = schema.getJSONObject("address").getString("deleted");
			partnerId = schema.getJSONObject("address").getString("id_customer");
					
			if(deletedId.equals("1"))
				continue;
			
			try {
				if(partnerId == null || partnerId.equals("0"))
					throw new AxelorException(I18n.get(IExceptionMessage.INVALID_ADDRESS), IException.NO_VALUE);
				
				addressId = String.valueOf(schema.getJSONObject("address").getInt("id"));
				addressL4 = schema.getJSONObject("address").getString("address1");
				addressL5 = schema.getJSONObject("address").getString("address2");
				cityName = schema.getJSONObject("address").getString("city");
				postcode = schema.getJSONObject("address").getString("postcode");
				countryId = schema.getJSONObject("address").getString("id_country");
				partner = Beans.get(PartnerRepository.class).all().filter("self.prestaShopId = ?", partnerId).fetchOne();
				address = Beans.get(AddressRepository.class).all().filter("self.prestaShopId = ?", id).fetchOne(); 
				city = cityRepo.findByName(cityName);

				if(city == null) {
					city = new City();
				}
				
				Country country = Beans.get(CountryRepository.class).all().filter("self.prestaShopId = ?", countryId).fetchOne();
				if(country == null)
					throw new AxelorException(I18n.get(IExceptionMessage.INVALID_COUNTRY), IException.NO_VALUE);
				
				if(address == null) {
					address = new Address();
					address.setAddressL4(addressL4);
					address.setAddressL5(addressL5);
					city.setName(cityName);
					city.setHasZipOnRight(false);
					address.setAddressL6(cityName + " " + postcode);
					address.setFullName(address.getAddressL4().toString() + " " + address.getAddressL6().toString());
					address.setCity(city);
					address.setAddressL7Country(country);
					partnerAddress = new PartnerAddress();
					partnerAddress.setIsDeliveryAddr(true);
					partnerAddress.setIsInvoicingAddr(true);
					partnerAddress.setIsDefaultAddr(true);
					partnerAddress.setAddress(address);
					partnerAddress.setPartner(partner);
					address.setPrestaShopId(addressId);
					partner.addPartnerAddressListItem(partnerAddress);
					
				} else {
					address.setAddressL4(addressL4);
					address.setAddressL5(addressL5);
					city.setName(cityName);
					city.setHasZipOnRight(false);
					address.setAddressL6(cityName + " " + postcode);
					address.setFullName(address.getAddressL4().toString() + " " + address.getAddressL6().toString());
					address.setAddressL7Country(country);
					address.setPrestaShopId(addressId);
					address.setCity(city);
				}
				partnerRepo.save(partner);
				done++;
				
			} catch (AxelorException e) {
				
				bwImport.newLine();
				bwImport.newLine();
				bwImport.write("Id - " + id + " " + e.getMessage());
				anomaly++;
				continue;
			} catch (Exception e) {
				
				bwImport.newLine();
				bwImport.newLine();
				bwImport.write("Id - " + id + " " + e.getMessage());
				anomaly++;
				continue;
			}
		}
		
		bwImport.newLine();
		bwImport.newLine();
		bwImport.write("Succeed : " + done + " " + "Anomaly : " + anomaly);
		return bwImport;
	}
}	
