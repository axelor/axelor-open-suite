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

import java.io.IOException;
import java.io.Writer;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.AppPrestashop;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PartnerAddress;
import com.axelor.apps.base.db.repo.PartnerAddressRepository;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.prestashop.entities.PrestashopAddress;
import com.axelor.apps.prestashop.entities.PrestashopResourceType;
import com.axelor.apps.prestashop.service.library.PSWebServiceClient;
import com.axelor.apps.prestashop.service.library.PrestaShopWebserviceException;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;

@Singleton
public class ExportAddressServiceImpl implements ExportAddressService {
	private Logger log = LoggerFactory.getLogger(getClass());

	private PartnerAddressRepository partnerAddressRepo;

	@Inject
	public ExportAddressServiceImpl(PartnerAddressRepository partnerAddressRepo) {
		this.partnerAddressRepo = partnerAddressRepo;
	}

	@Override
	@Transactional
	public void exportAddress(AppPrestashop appConfig, ZonedDateTime endDate, Writer logBuffer) throws IOException, PrestaShopWebserviceException {
		int done = 0;
		int errors = 0;

		logBuffer.write(String.format("%n====== ADDRESSES ======%n"));

		List<PartnerAddress> addresses = null;
		if(endDate == null) {
			addresses = partnerAddressRepo.all().filter("self.partner.prestaShopId != null").fetch();
		} else {
			addresses = partnerAddressRepo.all().filter("(self.createdOn > ?1 OR self.updatedOn > ?2 OR self.address.updatedOn > ?3 OR self.address.prestaShopId = null) AND self.partner.prestaShopId != null", endDate, endDate, endDate).fetch();
		}

		final PSWebServiceClient ws = new PSWebServiceClient(appConfig.getPrestaShopUrl(), appConfig.getPrestaShopKey());

		final List<PrestashopAddress> remoteAddresses = ws.fetchAll(PrestashopResourceType.ADDRESSES);
		final Map<Integer, PrestashopAddress> addressesById = new HashMap<>();
		for(PrestashopAddress a : remoteAddresses) {
			addressesById.put(a.getId(), a);
		}

		for (PartnerAddress partnerAddress : addresses) {
			final Address localAddress = partnerAddress.getAddress();

			logBuffer.write(String.format("Exporting partner address #%d (%s) – ", partnerAddress.getId(), localAddress.getFullName()));

			try {
				PrestashopAddress remoteAddress;
				if(localAddress.getPrestaShopId() != null) {
					logBuffer.write("prestashop id=" + localAddress.getPrestaShopId());
					remoteAddress = addressesById.get(localAddress.getPrestaShopId());
					if(remoteAddress == null) {
						logBuffer.write(String.format(" [ERROR] Not found remotely%n"));
						log.error("Unable to fetch remote address #{} ({}), something's probably very wrong, skipping",
								localAddress.getPrestaShopId(), localAddress.getFullName());
						++errors;
						continue;
					}
				} else {
					if(partnerAddress.getPartner().getPrestaShopId() == null) {
						logBuffer.write(String.format(" [WARNING] Address belongs to a not-yet synced customer, skipping%n"));
						continue;
					}
					remoteAddress = new PrestashopAddress();
					remoteAddress.setCustomerId(partnerAddress.getPartner().getPrestaShopId());
					remoteAddress.setAlias(I18n.getBundle(new Locale(partnerAddress.getPartner().getLanguageSelect())).getString("Main address"));

					// Do this on creation, it seems hazardous to update data since user can update them on its
					// side too… I guess import job should trigger new address creation when too much data
					// differs
					if(partnerAddress.getPartner().getPartnerTypeSelect() == PartnerRepository.PARTNER_TYPE_INDIVIDUAL) {
						remoteAddress.setFirstname(partnerAddress.getPartner().getFirstName());
						remoteAddress.setLastname(partnerAddress.getPartner().getName());
					} else {
						remoteAddress.setCompany(partnerAddress.getPartner().getName());
						if(partnerAddress.getPartner().getContactPartnerSet().isEmpty() == false) {
							Partner localContact = partnerAddress.getPartner().getContactPartnerSet().iterator().next();
							remoteAddress.setFirstname(localContact.getFirstName());
							remoteAddress.setLastname(localContact.getName());
						} else {
							logBuffer.write(String.format(" [WARNING] No contact filled, required for Pretashop, skipping%n"));
							continue;
						}
					}

					if(localAddress.getCity() == null) {
						if(StringUtils.isEmpty(localAddress.getAddressL6())) {
							logBuffer.write(String.format(" [WARNING] No city filled, it is required for Prestashop, skipping%n"));
							continue;
						} else {
							// Don't try to split city/zipcode since this can cause more issues than it solves
							remoteAddress.setCity(localAddress.getAddressL6());
						}
					} else {
						remoteAddress.setCity(localAddress.getCity().getName());
						remoteAddress.setZipcode(localAddress.getCity().getZip());
					}
					remoteAddress.setAddress1(localAddress.getAddressL4());
					remoteAddress.setAddress2(localAddress.getAddressL5());

					if(localAddress.getAddressL7Country() == null) {
						logBuffer.write(String.format(" [WARNING] No country filled, it is required for Prestashop, skipping%n"));
						continue;
					}
					if(localAddress.getAddressL7Country().getPrestaShopId() == null) {
						logBuffer.write(String.format(" [WARNING] Bound country has not be synced yet, skipping%n"));
						continue;
					}
					remoteAddress.setCountryId(localAddress.getAddressL7Country().getPrestaShopId());
				}

				// Don't know if we should actually synchronize something on update…

				remoteAddress.setUpdateDate(LocalDateTime.now());
				remoteAddress = ws.save(PrestashopResourceType.ADDRESSES, remoteAddress);
				logBuffer.write(String.format(" [SUCCESS]%n"));
				localAddress.setPrestaShopId(remoteAddress.getId());
				++done;
			} catch (PrestaShopWebserviceException e) {
				logBuffer.write(String.format(" [ERROR] %s (full trace is in application logs)%n", e.getLocalizedMessage()));
				log.error(String.format("Exception while synchronizing address #%d (%s)", localAddress.getId(), localAddress.getFullName()), e);
				++errors;
			}
		}

		logBuffer.write(String.format("%n=== END OF ADDRESSES EXPORT, done: %d, errors: %d ===%n", done, errors));

	}
}
