/*
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
package com.axelor.apps.stock.service;

import java.util.List;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.stock.db.PartnerDefaultLocation;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.repo.PartnerDefaultLocationRepository;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.persist.Transactional;

public class LocationSaveService {

	/**
	 * Remove default locations in partner that are not linked with this location anymore.
	 * @param location
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void removeForbiddenDefaultLocation(StockLocation location) {
	    Partner currentPartner = location.getPartner();
		Company currentCompany = location.getCompany();
	    Long partnerId = currentPartner != null ? currentPartner.getId() : 0L;
		Long companyId = currentCompany != null ? currentCompany.getId() : 0L;
	    PartnerDefaultLocationRepository partnerDefaultRepo = Beans.get(PartnerDefaultLocationRepository.class);
		List<PartnerDefaultLocation> partnerDefaultLocations = partnerDefaultRepo.all()
				.filter("(self.partner.id != :partnerId OR self.company.id != :companyId)"
						+ " AND (self.location = :location)")
				.bind("partnerId", partnerId)
				.bind("companyId", companyId)
				.bind("location", location)
				.fetch();
		for (PartnerDefaultLocation partnerDefaultLocation : partnerDefaultLocations) {
			Partner partnerToClean = partnerDefaultLocation.getPartner();
			partnerToClean.removePartnerDefaultLocationListItem(partnerDefaultLocation);
		}

	}
}
