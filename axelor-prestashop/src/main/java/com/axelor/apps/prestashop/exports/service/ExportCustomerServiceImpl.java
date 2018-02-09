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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.AppPrestashop;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.prestashop.entities.PrestashopCustomer;
import com.axelor.apps.prestashop.entities.PrestashopResourceType;
import com.axelor.apps.prestashop.service.library.PSWebServiceClient;
import com.axelor.apps.prestashop.service.library.PrestaShopWebserviceException;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;

@Singleton
public class ExportCustomerServiceImpl implements ExportCustomerService {
	private Logger log = LoggerFactory.getLogger(getClass());

	private PartnerRepository partnerRepo;

	@Inject
	public ExportCustomerServiceImpl(PartnerRepository partnerRepo) {
		this.partnerRepo = partnerRepo;
	}

	@Override
	@Transactional
	public void exportCustomer(AppPrestashop appConfig, ZonedDateTime endDate, BufferedWriter logBuffer) throws IOException, PrestaShopWebserviceException {
		int done = 0;
		int errors = 0;

		logBuffer.write(String.format("%n====== CUSTOMERS ======%n"));
		log.debug("Starting customers export to PrestaShop");

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

		final PSWebServiceClient ws = new PSWebServiceClient(appConfig.getPrestaShopUrl(), appConfig.getPrestaShopKey());

		final List<PrestashopCustomer> remoteCustomers = ws.fetchAll(PrestashopResourceType.CUSTOMERS);
		final Map<Integer, PrestashopCustomer> customersById = new HashMap<>();
		final Map<String, PrestashopCustomer> customersBySiret = new HashMap<>();
		final Map<String, PrestashopCustomer> customersByCompany = new HashMap<>();
		for(PrestashopCustomer c : remoteCustomers) {
			customersById.put(c.getId(), c);
			customersBySiret.put(c.getSiret(), c);
			customersByCompany.put(c.getCompany(), c);
		}

		final LocalDateTime now = LocalDateTime.now();

		for (Partner localCustomer : partnerRepo.all().filter(filter.toString(), params.toArray(new Object[0])).fetch()) {
			logBuffer.write(String.format("Exporting customer #%d (%s) - ", localCustomer.getId(), localCustomer.getName()));
			try {
				PrestashopCustomer remoteCustomer;
				if(localCustomer.getPrestaShopId() != null) {
					logBuffer.write("prestashop id=" + localCustomer.getPrestaShopId());
					remoteCustomer = customersById.get(localCustomer.getPrestaShopId());
					if(remoteCustomer == null) {
						logBuffer.write(String.format(" [ERROR] Not found remotely%n"));
						log.error("Unable to fetch remote customer #{} ({}), something's probably very wrong, skipping",
								localCustomer.getPrestaShopId(), localCustomer.getName());
						++errors;
						continue;
					} // Note: contrary to currencies and products, we don't check that various fields match since customer can edit them
				} else {
					remoteCustomer = null;

					if(StringUtils.isNotBlank(localCustomer.getRegistrationCode())) {
						remoteCustomer = customersBySiret.get(localCustomer.getRegistrationCode());
						if(remoteCustomer != null) {
							logBuffer.write(String.format("remotely found by registration code (%s), remote id: %d", localCustomer.getRegistrationCode(), remoteCustomer.getId()));
						}
					}
					if(remoteCustomer == null && localCustomer.getPartnerTypeSelect() == PartnerRepository.PARTNER_TYPE_COMPANY) {
						remoteCustomer = customersByCompany.get(localCustomer.getName());
						if(remoteCustomer != null) {
							logBuffer.write(String.format("remotely found by company name, remote id: %d", remoteCustomer.getId()));
						}
					}

					if(remoteCustomer == null) {
						logBuffer.write("failed to find by registration code or company name, creating");
						remoteCustomer = new PrestashopCustomer();
						remoteCustomer.setNote(I18n.get("Imported from Axelor"));
						remoteCustomer.setPassword(RandomStringUtils.randomGraph(16));
						if(localCustomer.getEmailAddress() != null) {
							remoteCustomer.setEmail(localCustomer.getEmailAddress().getAddress());
						}

						if(localCustomer.getPartnerTypeSelect() == PartnerRepository.PARTNER_TYPE_INDIVIDUAL) {
							remoteCustomer.setFirstname(localCustomer.getFirstName());
							remoteCustomer.setLastname(localCustomer.getName());
						} else {
							remoteCustomer.setCompany(localCustomer.getName());
							if(localCustomer.getContactPartnerSet().isEmpty() == false) {
								Partner localContact = localCustomer.getContactPartnerSet().iterator().next();
								remoteCustomer.setFirstname(localContact.getFirstName());
								remoteCustomer.setLastname(localContact.getName());
							} else {
								logBuffer.write(String.format(" [WARNING] No contact filled, required for Pretashop, skipping%n"));
								continue;
							}
						}
					}
				}

				// Only push elements that cannot be edited by user
				remoteCustomer.setSiret(localCustomer.getRegistrationCode());
				remoteCustomer.setWebsite(localCustomer.getWebSite());
				if(localCustomer.getPaymentCondition() != null) {
					remoteCustomer.setMaxPaymentDays(localCustomer.getPaymentCondition().getPaymentTime());
				}
				if(localCustomer.getAccountingSituationList().isEmpty() == false) {
					// FIXME We should have a per company configuration…
					remoteCustomer.setAllowedOutstandingAmount(localCustomer.getAccountingSituationList().get(0).getAcceptedCredit().setScale(appConfig.getExportPriceScale(), BigDecimal.ROUND_HALF_UP));
				}

				remoteCustomer.setUpdateDate(now);
				remoteCustomer = ws.save(PrestashopResourceType.CUSTOMERS, remoteCustomer);
				localCustomer.setPrestaShopId(remoteCustomer.getId());
				logBuffer.write(String.format(" [SUCCESS]%n"));
				++done;
			} catch(PrestaShopWebserviceException | IOException e) {
				logBuffer.write(String.format(" [ERROR] %s (full trace is in application logs)%n", e.getLocalizedMessage()));
				log.error(String.format("Exception while synchronizing customer #%d (%s)", localCustomer.getId(), localCustomer.getName()), e);
				++errors;
			}
		}

		logBuffer.write(String.format("%n=== END OF CUSTOMERS IMPORT, done: %d, errors: %d ===%n", done, errors));
	}
}
