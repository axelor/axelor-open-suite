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
package com.axelor.apps.prestashop.exports.service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.axelor.apps.base.db.AppPrestashop;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.prestashop.db.Customers;
import com.axelor.apps.prestashop.db.Prestashop;
import com.axelor.apps.prestashop.entities.PrestashopResourceType;
import com.axelor.apps.prestashop.exception.IExceptionMessage;
import com.axelor.apps.prestashop.service.library.PSWebServiceClient;
import com.axelor.apps.prestashop.service.library.PSWebServiceClient.Options;
import com.axelor.apps.prestashop.service.library.PrestaShopWebserviceException;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;

@Singleton
public class ExportCustomerServiceImpl implements ExportCustomerService {
	private PartnerRepository partnerRepo;

	@Inject
	public ExportCustomerServiceImpl(PartnerRepository partnerRepo) {
		this.partnerRepo = partnerRepo;
	}

	@Override
	@Transactional
	public void exportCustomer(AppPrestashop appConfig, ZonedDateTime endDate, BufferedWriter bwExport)
			throws IOException, TransformerConfigurationException, TransformerException, ParserConfigurationException,
			SAXException, PrestaShopWebserviceException, JAXBException, TransformerFactoryConfigurationError {
		int done = 0;
		int anomaly = 0;

		String prestaShopId = null;
		String schema = null;
		Document document = null;

		bwExport.newLine();
		bwExport.write("-----------------------------------------------");
		bwExport.newLine();
		bwExport.write("Customer");

		final StringBuilder filter = new StringBuilder(128);
		final List<Object> params = new ArrayList<>(4);

		filter.append("(self.isCustomer = true)");

		if(endDate != null) {
			filter.append(" AND (self.createdOn > ?1 OR self.updatedOn > ?2 OR self.emailAddress.createdOn > ?3 OR self.emailAddress.updatedOn > ?4 OR self.prestaShopId is null)");
			params.add(endDate);
			params.add(endDate);
			params.add(endDate);
			params.add(endDate);
		}

		if(appConfig.getExportNonPrestashopCustomers() == Boolean.FALSE) {
			filter.append(" AND (self.prestaShopId IS NOT NULL)");
		}

		for (Partner partner : partnerRepo.all().filter(filter.toString(), params.toArray(new Object[0])).fetch()) {
			try {

				Customers customer = new Customers();
				customer.setId(Objects.toString(partner.getPrestaShopId(), null));

				if (partner.getPartnerTypeSelect() == 1) {
					if (partner.getContactPartnerSet().size() != 0) {
						customer.setCompany(partner.getName());
						if(!partner.getContactPartnerSet().iterator().next().getFirstName().isEmpty() && !partner.getContactPartnerSet().iterator().next().getName().isEmpty()) {
							customer.setFirstname(partner.getContactPartnerSet().iterator().next().getFirstName());
							customer.setLastname(partner.getContactPartnerSet().iterator().next().getName());
						} else {
							throw new AxelorException(IException.NO_VALUE, I18n.get(IExceptionMessage.INVALID_CONTACT));
						}
					} else {
						throw new AxelorException(IException.NO_VALUE, I18n.get(IExceptionMessage.INVALID_CONTACT));
					}
				} else {

					if (!partner.getName().isEmpty() && !partner.getFirstName().isEmpty()) {
						customer.setFirstname(partner.getFirstName());
						customer.setLastname(partner.getName());
					} else {
						throw new AxelorException(IException.NO_VALUE, I18n.get(IExceptionMessage.INVALID_INDIVIDUAL));
					}
				}

				if (partner.getPaymentCondition() != null) {
					customer.setMax_payment_days(partner.getPaymentCondition().getPaymentTime().toString());
				}

				if (partner.getEmailAddress() != null) {
					customer.setEmail(partner.getEmailAddress().getAddress());
				} else {
					throw new AxelorException(IException.NO_VALUE, I18n.get(IExceptionMessage.INVALID_EMAIL));
				}

				customer.setId_default_group("3");
				customer.setWebsite(partner.getWebSite());
				customer.setSecure_key(partner.getSecureKey());
				customer.setActive("1");
				customer.setId_shop("1");
				customer.setId_shop_group("1");
				customer.setPasswd("NULL"); // required

				Prestashop prestaShop = new Prestashop();
				prestaShop.setPrestashop(customer);

				StringWriter sw = new StringWriter();
				JAXBContext contextObj = JAXBContext.newInstance(Prestashop.class);
				Marshaller marshallerObj = contextObj.createMarshaller();
				marshallerObj.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
				marshallerObj.marshal(prestaShop, sw);
				schema = sw.toString();

				PSWebServiceClient ws;
				Options options = new Options();
				options.setResourceType(PrestashopResourceType.CUSTOMERS);
				options.setXmlPayload(schema);

				if (partner.getPrestaShopId() == null) {
					ws = new PSWebServiceClient(appConfig.getPrestaShopUrl() + "/api/customers?schema=synopsis", appConfig.getPrestaShopKey());
					document = ws.add(options);
				} else {
					options.setRequestedId(partner.getPrestaShopId());
					ws = new PSWebServiceClient(appConfig.getPrestaShopUrl(), appConfig.getPrestaShopKey());
					document = ws.edit(options);
				}

				prestaShopId = document.getElementsByTagName("id").item(0).getTextContent();
				partner.setSecureKey(document.getElementsByTagName("secure_key").item(0).getTextContent());
				partner.setPrestaShopId(Integer.valueOf(prestaShopId));
				partnerRepo.save(partner);
				done++;

			} catch (AxelorException e) {
				bwExport.newLine();
				bwExport.newLine();
				bwExport.write("Id - " + partner.getId().toString() + " " + e.getMessage());
				anomaly++;
				continue;

			} catch (Exception e) {
				bwExport.newLine();
				bwExport.newLine();
				bwExport.write("Id - " + partner.getId().toString() + " " + e.getMessage());
				anomaly++;
				continue;
			}
		}

		bwExport.newLine();
		bwExport.newLine();
		bwExport.write("Succeed : " + done + " " + "Anomaly : " + anomaly);
	}
}
