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

import java.io.IOException;
import java.io.Writer;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import com.axelor.apps.base.db.AppPrestashop;
import com.axelor.apps.base.db.IAdministration;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.message.db.EmailAddress;
import com.axelor.apps.prestashop.entities.PrestashopCustomer;
import com.axelor.apps.prestashop.entities.PrestashopResourceType;
import com.axelor.apps.prestashop.service.library.PSWebServiceClient;
import com.axelor.apps.prestashop.service.library.PrestaShopWebserviceException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;

@Singleton
public class ImportCustomerServiceImpl implements ImportCustomerService {
	private PartnerRepository partnerRepo;
	private AppBaseService appBaseService;
	private PartnerService partnerService;

	@Inject
	public ImportCustomerServiceImpl(PartnerRepository partnerRepo, final AppBaseService appBaseService, final PartnerService partnerService) {
		this.partnerRepo = partnerRepo;
		this.appBaseService = appBaseService;
		this.partnerService = partnerService;
	}

	@Override
	@Transactional
	public void importCustomer(AppPrestashop appConfig, ZonedDateTime endDate, Writer logBuffer) throws IOException, PrestaShopWebserviceException {
		int done = 0;
		int errors = 0;

		logBuffer.write(String.format("%n====== CUSTOMERS ======%n"));

		final PSWebServiceClient ws = new PSWebServiceClient(appConfig.getPrestaShopUrl(), appConfig.getPrestaShopKey());
		final List<PrestashopCustomer> remoteCustomers = ws.fetchAll(PrestashopResourceType.CUSTOMERS);

		for(PrestashopCustomer remoteCustomer : remoteCustomers) {
			logBuffer.write(String.format("Importing customer #%d (%s) - ", remoteCustomer.getId(), remoteCustomer.getFullname()));

			Partner localCustomer = partnerRepo.findByPrestaShopId(remoteCustomer.getId());
			if(localCustomer == null) {
				logBuffer.write("not found by ID, creating, ");
				localCustomer = new Partner();
				localCustomer.setPrestaShopId(remoteCustomer.getId());
				localCustomer.setIsCustomer(Boolean.TRUE);
				localCustomer.setContactPartnerSet(new HashSet<>());
				if(appBaseService.getAppBase().getGeneratePartnerSequence() == Boolean.TRUE) {
					localCustomer.setPartnerSeq(Beans.get(SequenceService.class).getSequenceNumber(IAdministration.PARTNER));
					if(localCustomer.getPartnerSeq() == null) {
						++errors;
						logBuffer.write(String.format("No sequence configured for partners, unable to create customer, skipping [ERROR]%n"));
						continue;
					}
				}
			}

			if(localCustomer.getId() == null || appConfig.getPrestaShopMasterForCustomers()) {
				if(StringUtils.isNotBlank(remoteCustomer.getCompany())) {
					localCustomer.setPartnerTypeSelect(PartnerRepository.PARTNER_TYPE_COMPANY);
					localCustomer.setName(remoteCustomer.getCompany());
					localCustomer.setRegistrationCode(remoteCustomer.getSiret());
					if(StringUtils.isNotBlank(remoteCustomer.getLastname())) {
						boolean found = false;
						for(Partner contact : localCustomer.getContactPartnerSet()) {
							if(Objects.equals(contact.getName(), remoteCustomer.getLastname())) {
								found = true;
								break;
							}
						}
						if(found == false) {
							logBuffer.write("local customer has no contact with the same lastname, adding a new one –");
							Partner mainContact = new Partner();
							mainContact.setIsContact(true);
							mainContact.setTitleSelect(remoteCustomer.getGenderId() == PrestashopCustomer.GENDER_FEMALE ? PartnerRepository.PARTNER_TITLE_MS : PartnerRepository.PARTNER_TITLE_M);
							mainContact.setFirstName(remoteCustomer.getFirstname());
							mainContact.setName(remoteCustomer.getLastname());
							mainContact.setFullName(partnerService.computeFullName(mainContact));
							mainContact.setMainPartner(localCustomer);
							if(appBaseService.getAppBase().getGeneratePartnerSequence() == Boolean.TRUE) {
								mainContact.setPartnerSeq(Beans.get(SequenceService.class).getSequenceNumber(IAdministration.PARTNER));
								if(mainContact.getPartnerSeq() == null) {
									++errors;
									logBuffer.write(String.format("No sequence configured for partners, unable to import main contact, skipping [ERROR]%n"));
									continue;
								}
							}
							localCustomer.addContactPartnerSetItem(mainContact);
						}
					}
				} else {
					localCustomer.setPartnerTypeSelect(PartnerRepository.PARTNER_TYPE_INDIVIDUAL);
					localCustomer.setName(remoteCustomer.getLastname());
					localCustomer.setFirstName(remoteCustomer.getFirstname());
					localCustomer.setTitleSelect(remoteCustomer.getGenderId() == PrestashopCustomer.GENDER_FEMALE ? PartnerRepository.PARTNER_TITLE_MS : PartnerRepository.PARTNER_TITLE_M);

				}

				localCustomer.setFullName(partnerService.computeFullName(localCustomer));
				localCustomer.setWebSite(remoteCustomer.getWebsite());
				if(localCustomer.getEmailAddress() == null || Objects.equals(remoteCustomer.getEmail(), localCustomer.getEmailAddress().getAddress()) == false) {
					EmailAddress email = new EmailAddress();
					email.setPartner(localCustomer);
					email.setAddress(remoteCustomer.getEmail());
					localCustomer.setEmailAddress(email);
				}
				partnerRepo.save(localCustomer);
			} else {
				logBuffer.write("local customer exists and PrestaShop isn't master for customers, leaving untouched");
			}

			logBuffer.write(String.format(" [SUCCESS]%n"));
			++done;
		}

		logBuffer.write(String.format("%n=== END OF CUSTOMERS IMPORT, done: %d, errors: %d ===%n", done, errors));
	}

}
