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
import java.util.List;
import java.util.Objects;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.axelor.apps.base.db.AppPrestashop;
import com.axelor.apps.base.db.PartnerAddress;
import com.axelor.apps.base.db.repo.PartnerAddressRepository;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.prestashop.db.Addresses;
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
public class ExportAddressServiceImpl implements ExportAddressService {

	private PartnerRepository partnerRepo;
	private PartnerAddressRepository partnerAddressRepo;

	@Inject
	public ExportAddressServiceImpl(PartnerRepository partnerRepo, PartnerAddressRepository partnerAddressRepo) {
		this.partnerRepo = partnerRepo;
		this.partnerAddressRepo = partnerAddressRepo;
	}

	@Override
	@Transactional
	public void exportAddress(AppPrestashop appConfig, ZonedDateTime endDate, BufferedWriter bwExport) throws IOException,
			PrestaShopWebserviceException, ParserConfigurationException, SAXException, TransformerException {
		int done = 0;
		int anomaly = 0;

		bwExport.newLine();
		bwExport.write("-----------------------------------------------");
		bwExport.newLine();
		bwExport.write("Address");
		List<PartnerAddress> partnerAddresses = null;
		String schema = null;
		Document document = null;

		if(endDate == null) {
			partnerAddresses = partnerAddressRepo.all().filter("self.partner.prestaShopId != null").fetch();
		} else {
			partnerAddresses = partnerAddressRepo.all().filter("(self.createdOn > ?1 OR self.updatedOn > ?2 OR self.address.updatedOn > ?3 OR self.address.prestaShopId = null) AND self.partner.prestaShopId != null", endDate, endDate, endDate).fetch();
		}
		for (PartnerAddress partnerAddress : partnerAddresses) {

			try {
				Addresses address = new Addresses();
				address.setId(Objects.toString(partnerAddress.getAddress().getPrestaShopId(), null));
				address.setId_customer(Objects.toString(partnerAddress.getPartner().getPrestaShopId(), null));

				if(partnerAddress.getPartner().getPartnerTypeSelect() == 1) {

					if(!partnerAddress.getPartner().getContactPartnerSet().isEmpty()) {

						if (partnerAddress.getPartner().getContactPartnerSet().iterator().next().getName() != null &&
								partnerAddress.getPartner().getContactPartnerSet().iterator().next().getFirstName() != null) {

							address.setCompany(partnerAddress.getPartner().getName());
							address.setFirstname(partnerAddress.getPartner().getContactPartnerSet().iterator().next().getFirstName());
							address.setLastname(partnerAddress.getPartner().getContactPartnerSet().iterator().next().getName());

						} else {
							throw new AxelorException(IException.NO_VALUE, I18n.get(IExceptionMessage.INVALID_COMPANY));
						}
					} else {
						throw new AxelorException(IException.NO_VALUE, I18n.get(IExceptionMessage.INVALID_CONTACT));
					}

				} else {
					if (partnerAddress.getPartner().getName() != null && partnerAddress.getPartner().getFirstName() != null) {
						address.setFirstname(partnerAddress.getPartner().getFirstName());
						address.setLastname(partnerAddress.getPartner().getName());
					} else {
						throw new AxelorException(IException.NO_VALUE, I18n.get(IExceptionMessage.INVALID_COMPANY));
					}
				}

				address.setId_country(Objects.toString(partnerAddress.getAddress().getAddressL7Country().getPrestaShopId(), null));
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
					throw new AxelorException(IException.NO_VALUE, I18n.get(IExceptionMessage.INVALID_CITY));
				}

				Prestashop prestaShop = new Prestashop();
				prestaShop.setPrestashop(address);

				StringWriter sw = new StringWriter();
				JAXBContext contextObj = JAXBContext.newInstance(Prestashop.class);
				Marshaller marshallerObj = contextObj.createMarshaller();
				marshallerObj.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
				marshallerObj.marshal(prestaShop, sw);
				schema = sw.toString();

				PSWebServiceClient ws = new PSWebServiceClient(appConfig.getPrestaShopUrl() + "/api/addresses?schema=synopsis", appConfig.getPrestaShopKey());
				Options options = new Options();
				options.setResourceType(PrestashopResourceType.ADDRESSES);
				options.setXmlPayload(schema);

				if (partnerAddress.getAddress().getPrestaShopId() == null) {
					document = ws.add(options);
				} else {
					options.setRequestedId(partnerAddress.getAddress().getPrestaShopId());
					ws = new PSWebServiceClient(appConfig.getPrestaShopUrl(), appConfig.getPrestaShopKey());
					document = ws.edit(options);
				}

				partnerAddress.getAddress().setPrestaShopId(Integer.valueOf(document.getElementsByTagName("id").item(0).getTextContent()));
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
