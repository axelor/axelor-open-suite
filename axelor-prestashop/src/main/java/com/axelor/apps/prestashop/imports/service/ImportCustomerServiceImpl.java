/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;

import com.axelor.apps.base.db.AppPrestashop;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.AppPrestashopRepository;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.message.db.EmailAddress;
import com.axelor.apps.prestashop.entities.PrestashopResourceType;
import com.axelor.apps.prestashop.exception.IExceptionMessage;
import com.axelor.apps.prestashop.service.library.PSWebServiceClient;
import com.axelor.apps.prestashop.service.library.PSWebServiceClient.Options;
import com.axelor.apps.prestashop.service.library.PrestaShopWebserviceException;
import com.axelor.auth.AuthUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import wslite.json.JSONException;
import wslite.json.JSONObject;

public class ImportCustomerServiceImpl implements ImportCustomerService {
    private final String shopUrl;
	private final String key;

	@Inject
	private PartnerRepository partnerRepo;

	/**
	 * Initialization
	 */
	public ImportCustomerServiceImpl() {
		AppPrestashop prestaShopObj = Beans.get(AppPrestashopRepository.class).all().fetchOne();
		shopUrl = prestaShopObj.getPrestaShopUrl();
		key = prestaShopObj.getPrestaShopKey();
	}

	@SuppressWarnings("deprecation")
	@Override
	@Transactional
	public BufferedWriter importCustomer(BufferedWriter bwImport)
			throws IOException, PrestaShopWebserviceException, TransformerException, JAXBException, JSONException {

		Integer done = 0;
		Integer anomaly = 0;
		bwImport.newLine();
		bwImport.write("-----------------------------------------------");
		bwImport.newLine();
		bwImport.write("Customer");

		PSWebServiceClient ws = new PSWebServiceClient(shopUrl, key);
		List<Integer> customerIds = ws.fetchApiIds(PrestashopResourceType.CUSTOMERS);

		for (Integer id : customerIds) {
			Options options = new Options();
			// FIXME use dispkay options and fetch everything at once
			options.setResourceType(PrestashopResourceType.CUSTOMERS);
			options.setRequestedId(id);
			JSONObject schema = ws.getJson(options);

			String emailName = null;
			String firstName = null;
			String name = null;
			String company = null;
			boolean newPartner = false;
			Partner partner = null;
			Partner contactPartner = null;

			try {
				Integer prestashopId = schema.getJSONObject("customer").getInt("id");
				partner = partnerRepo.all().filter("self.prestaShopId = ?", prestashopId).fetchOne();

				if(partner == null) {
					newPartner = true;
					partner = new Partner();
					partner.setPrestaShopId(prestashopId);
				}

				if (!schema.getJSONObject("customer").getString("firstname").isEmpty() &&
						!schema.getJSONObject("customer").getString("lastname").isEmpty()) {

					firstName = schema.getJSONObject("customer").getString("firstname");
					name = schema.getJSONObject("customer").getString("lastname");
					company =  schema.getJSONObject("customer").get("company").toString();

					if(!company.isEmpty() && company != null && company != "null" && !company.equals("")) {

						partner.setPartnerTypeSelect(1);
						partner.setName(company);

						if(newPartner) {
							contactPartner = new Partner();
						} else {
							contactPartner = partner.getContactPartnerSet().iterator().next();
						}

						partner.setFullName(company);
						contactPartner.setName(name);
						contactPartner.setFirstName(firstName);
						contactPartner.setIsContact(true);
						contactPartner.setMainPartner(partner);

						if(name != null && firstName != null) {
							contactPartner.setFullName(name + " " + firstName);
						} else if (name != null && firstName == null) {
							contactPartner.setFullName(name);
						} else if (name == null && firstName != null) {
							contactPartner.setFullName(firstName);
						}

						if(newPartner) {
							partner.addContactPartnerSetItem(contactPartner);
						}

					} else {

						partner.setPartnerTypeSelect(2);
						partner.setFirstName(firstName);
						partner.setName(name);

						if(name != null && firstName != null) {
							partner.setFullName(name + " " + firstName);
						} else if (name != null && firstName == null) {
							partner.setFullName(name);
						} else if (name == null && firstName != null) {
							partner.setFullName(firstName);
						}
					}
				} else {
					throw new AxelorException(I18n.get(IExceptionMessage.INVALID_COMPANY), IException.NO_VALUE);
				}

						partner.setTitleSelect(Integer.parseInt(schema.getJSONObject("customer").getString("id_gender")));
						EmailAddress emailAddress = new EmailAddress();
						emailAddress.setAddress(schema.getJSONObject("customer").getString("email"));

						if (partner != null)
							emailName = partner.getFullName();

						if (emailAddress.getAddress() != null)
							emailName = emailAddress.getAddress();

						if(newPartner) {
							partner.addCompanySetItem(AuthUtils.getUser().getActiveCompany());
							newPartner = false;
						}

						emailAddress.setName(emailName);
						partner.setEmailAddress(emailAddress);
						partner.setWebSite(schema.getJSONObject("customer").get("website").toString());
						partner.setSecureKey(schema.getJSONObject("customer").getString("secure_key"));
						partner.setIsCustomer(true);
						partnerRepo.persist(partner);
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
