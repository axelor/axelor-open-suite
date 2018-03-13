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
import java.util.List;

import org.apache.shiro.util.CollectionUtils;

import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.AppPrestashop;
import com.axelor.apps.base.db.City;
import com.axelor.apps.base.db.Country;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.PartnerAddress;
import com.axelor.apps.base.db.repo.AddressRepository;
import com.axelor.apps.base.db.repo.CityRepository;
import com.axelor.apps.base.db.repo.CountryRepository;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.service.AddressService;
import com.axelor.apps.db.IPrestaShopBatch;
import com.axelor.apps.prestashop.entities.PrestashopAddress;
import com.axelor.apps.prestashop.entities.PrestashopResourceType;
import com.axelor.apps.prestashop.service.library.PSWebServiceClient;
import com.axelor.apps.prestashop.service.library.PrestaShopWebserviceException;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;

@Singleton
public class ImportAddressServiceImpl implements ImportAddressService {
	private AddressRepository addressRepo;
	private CityRepository cityRepo;
	private CountryRepository countryRepo;
	private PartnerRepository partnerRepo;
	private AddressService addressService;

	@Inject
	public ImportAddressServiceImpl(AddressRepository addressRepo, CityRepository cityRepo, CountryRepository countryRepo, PartnerRepository partnerRepo, AddressService addressService) {
		this.addressRepo = addressRepo;
		this.cityRepo = cityRepo;
		this.countryRepo = countryRepo;
		this.partnerRepo = partnerRepo;
		this.addressService = addressService;
	}

	@Override
	@Transactional
	public void importAddress(AppPrestashop appConfig, ZonedDateTime endDate, Writer logBuffer) throws IOException, PrestaShopWebserviceException {
		Integer done = 0;
		Integer errors = 0;

		logBuffer.write(String.format("%n====== ADDRESSES ======%n"));

		final PSWebServiceClient ws = new PSWebServiceClient(appConfig.getPrestaShopUrl(), appConfig.getPrestaShopKey());
		final List<PrestashopAddress> remoteAddresses = ws.fetchAll(PrestashopResourceType.ADDRESSES);

		for(PrestashopAddress remoteAddress : remoteAddresses) {
			logBuffer.write(String.format("Importing PrestaShop address #%d (%s %s) – ", remoteAddress.getId(), remoteAddress.getAddress1(), remoteAddress.getCity()));

			Address localAddress = addressRepo.findByPrestaShopId(remoteAddress.getId());

			if(remoteAddress.isDeleted()) {
				if(localAddress != null) localAddress.setArchived(Boolean.TRUE);
				logBuffer.write(String.format("[WARNING] Tagged as deleted, skipping%n"));
				continue;
			}

			if(remoteAddress.getCustomerId() == null) {
				logBuffer.write(String.format("[WARNING] Address is not bound to a customer, skipping%n"));
				continue;
			}

			Country country = countryRepo.findByPrestaShopId(remoteAddress.getCountryId());
			if(country == null) {
				logBuffer.write(String.format(" [WARNING] Address belongs to a not-yet synced country, skipping%n"));
				continue;
			}

			if(localAddress == null) {
				localAddress = new Address();
				localAddress.setImportOrigin(IPrestaShopBatch.IMPORT_ORIGIN_PRESTASHOP);
				localAddress.setPrestaShopId(remoteAddress.getId());
				Partner customer = partnerRepo.findByPrestaShopId(remoteAddress.getCustomerId());
				if(customer == null) {
					logBuffer.write(String.format(" [WARNING] Address belongs to a not-yet synced customer, skipping%n"));
					continue;
				}
				PartnerAddress partnerAddress = new PartnerAddress();
				partnerAddress.setAddress(localAddress);
				partnerAddress.setPartner(customer);
				if(CollectionUtils.size(customer.getPartnerAddressList()) == 0) {
					partnerAddress.setIsDeliveryAddr(Boolean.TRUE);
					partnerAddress.setIsInvoicingAddr(Boolean.TRUE);
					partnerAddress.setIsDefaultAddr(Boolean.TRUE);
					customer.addPartnerAddressListItem(partnerAddress);
				}
			}

			if(localAddress == null || IPrestaShopBatch.IMPORT_ORIGIN_PRESTASHOP.equals(localAddress.getImportOrigin())) {
				localAddress.setAddressL4(remoteAddress.getAddress1());
				localAddress.setAddressL5(remoteAddress.getAddress2());
				City city = cityRepo.findByName(remoteAddress.getCity());
				if(city == null) {
					city = new City();
					city.setName(remoteAddress.getCity());
				}
				localAddress.setZip(remoteAddress.getZipcode());
				localAddress.setCity(city);
				localAddress.setAddressL7Country(country);

				localAddress.setAddressL6(localAddress.getZip() + " " + localAddress.getCity().getName());
				localAddress.setFullName(addressService.computeFullName(localAddress));

				addressRepo.save(localAddress);
			} else {
				logBuffer.write("local address exists and wasn't created on PrestaShop, leaving untouched");
			}

			logBuffer.write(String.format(" [SUCCESS]%n"));
			++done;
		}

		logBuffer.write(String.format("%n=== END OF ADDRESSES IMPORT, done: %d, errors: %d ===%n", done, errors));
	}
}
