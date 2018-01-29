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
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.axelor.apps.base.db.AppPrestashop;
import com.axelor.apps.base.db.PartnerAddress;
import com.axelor.apps.base.db.repo.AppPrestashopRepository;
import com.axelor.apps.base.db.repo.PartnerAddressRepository;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.prestashop.db.Addresses;
import com.axelor.apps.prestashop.db.Prestashop;
import com.axelor.apps.prestashop.exception.IExceptionMessage;
import com.axelor.apps.prestashop.service.library.PSWebServiceClient;
import com.axelor.apps.prestashop.service.library.PrestaShopWebserviceException;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class ExportAddressServiceImpl implements ExportAddressService {

	Integer done = 0;
	Integer anomaly = 0;
	private final String shopUrl;
	private final String key;

	@Inject
	private PartnerRepository partnerRepo;

	/**
	 * Initialization
	 */
	public ExportAddressServiceImpl() {
		AppPrestashop prestaShopObj = Beans.get(AppPrestashopRepository.class).all().fetchOne();
		shopUrl = prestaShopObj.getPrestaShopUrl();
		key = prestaShopObj.getPrestaShopKey();
	}

	@SuppressWarnings("deprecation")
	@Override
	@Transactional
	public void exportAddress(ZonedDateTime endDate, BufferedWriter bwExport) throws IOException,
			PrestaShopWebserviceException, ParserConfigurationException, SAXException, TransformerException {

		bwExport.newLine();
		bwExport.write("-----------------------------------------------");
		bwExport.newLine();
		bwExport.write("Address");
		List<PartnerAddress> partnerAddresses = null;
		String schema = null;
		Document document = null;

		if(endDate == null) {
			partnerAddresses = Beans.get(PartnerAddressRepository.class).all().filter("self.partner.prestaShopId != null").fetch();
		} else {
			partnerAddresses = Beans.get(PartnerAddressRepository.class).all().filter("(self.createdOn > ?1 OR self.updatedOn > ?2 OR self.address.updatedOn > ?3 OR self.address.prestaShopId = null) AND self.partner.prestaShopId != null", endDate, endDate, endDate).fetch();
		}
		for (PartnerAddress partnerAddress : partnerAddresses) {

			try {
				Addresses address = new Addresses();
				address.setId(partnerAddress.getAddress().getPrestaShopId());
				address.setId_customer(partnerAddress.getPartner().getPrestaShopId());

				if(partnerAddress.getPartner().getPartnerTypeSelect() == 1) {

					if(!partnerAddress.getPartner().getContactPartnerSet().isEmpty()) {

						if (partnerAddress.getPartner().getContactPartnerSet().iterator().next().getName() != null &&
								partnerAddress.getPartner().getContactPartnerSet().iterator().next().getFirstName() != null) {

							address.setCompany(partnerAddress.getPartner().getName());
							address.setFirstname(partnerAddress.getPartner().getContactPartnerSet().iterator().next().getFirstName());
							address.setLastname(partnerAddress.getPartner().getContactPartnerSet().iterator().next().getName());

						} else {
							throw new AxelorException(I18n.get(IExceptionMessage.INVALID_COMPANY), IException.NO_VALUE);
						}
					} else {
						throw new AxelorException(I18n.get(IExceptionMessage.INVALID_CONTACT), IException.NO_VALUE);
					}

				} else {
					if (partnerAddress.getPartner().getName() != null && partnerAddress.getPartner().getFirstName() != null) {
						address.setFirstname(partnerAddress.getPartner().getFirstName());
						address.setLastname(partnerAddress.getPartner().getName());
					} else {
						throw new AxelorException(I18n.get(IExceptionMessage.INVALID_COMPANY), IException.NO_VALUE);
					}
				}

				address.setId_country(partnerAddress.getAddress().getAddressL7Country().getPrestaShopId());
				address.setAlias("Main Addresses");

				if (partnerAddress.getAddress().getCity() != null) {

					String postCode = null;
					String addString = partnerAddress.getAddress().getAddressL6();
					String[] words = addString.split("\\s");

					if(partnerAddress.getAddress().getCity().getHasZipOnRight()) {
						postCode = words[1];
					} else {
						postCode = words[0];
					}

					address.setAddress1(partnerAddress.getAddress().getAddressL4());
					address.setAddress2(partnerAddress.getAddress().getAddressL5());
					address.setPostcode(postCode);
					address.setCity(partnerAddress.getAddress().getCity().getName());
				} else {
					throw new AxelorException(I18n.get(IExceptionMessage.INVALID_CITY), IException.NO_VALUE);
				}

				Prestashop prestaShop = new Prestashop();
				prestaShop.setPrestashop(address);

				StringWriter sw = new StringWriter();
				JAXBContext contextObj = JAXBContext.newInstance(Prestashop.class);
				Marshaller marshallerObj = contextObj.createMarshaller();
				marshallerObj.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
				marshallerObj.marshal(prestaShop, sw);
				schema = sw.toString();

				PSWebServiceClient ws = new PSWebServiceClient(shopUrl + "/api/" + "addresses" + "?schema=synopsis", key);
				HashMap<String, Object> opt = new HashMap<String, Object>();
				opt.put("resource", "addresses");
				opt.put("postXml", schema);

				if (partnerAddress.getAddress().getPrestaShopId() == null) {
					document = ws.add(opt);
				} else {
					opt.put("id", partnerAddress.getAddress().getPrestaShopId());
					ws = new PSWebServiceClient(shopUrl, key);
					document = ws.edit(opt);
				}

				partnerAddress.getAddress().setPrestaShopId(document.getElementsByTagName("id").item(0).getTextContent());
				partnerRepo.save(partnerAddress.getPartner());
				done++;

			} catch (AxelorException e) {
				bwExport.newLine();
				bwExport.newLine();
				bwExport.write("Id - " + partnerAddress.getAddress().getId().toString() + " " + e.getMessage());
				anomaly++;
				continue;

			} catch (Exception e) {
				bwExport.newLine();
				bwExport.newLine();
				bwExport.write("Id - " + partnerAddress.getAddress().getId().toString() + " " + e.getMessage());
				anomaly++;
				continue;
			}
		}

		bwExport.newLine();
		bwExport.newLine();
		bwExport.write("Succeed : " + done + " " + "Anomaly : " + anomaly);
	}
}
