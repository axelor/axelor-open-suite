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
package com.axelor.apps.stock.service;

import java.util.List;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.stock.db.PartnerDefaultStockLocation;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.db.repo.PartnerDefaultStockLocationRepository;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.persist.Transactional;

public class StockLocationSaveService {

	/**
	 * Remove default stock locations in partner that are not linked with this location anymore.
	 * @param defaultStockLocation
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public void removeForbiddenDefaultStockLocation(StockLocation defaultStockLocation) {
	    Partner currentPartner = defaultStockLocation.getPartner();
		Company currentCompany = defaultStockLocation.getCompany();
	    Long partnerId = currentPartner != null ? currentPartner.getId() : 0L;
		Long companyId = currentCompany != null ? currentCompany.getId() : 0L;
	    PartnerDefaultStockLocationRepository partnerDefaultRepo = Beans.get(PartnerDefaultStockLocationRepository.class);
		List<PartnerDefaultStockLocation> partnerDefaultStockLocations = partnerDefaultRepo.all()
				.filter("(self.partner.id != :partnerId OR self.company.id != :companyId)"
						+ " AND (self.defaultStockLocation.id = :stockLocationId)")
				.bind("partnerId", partnerId)
				.bind("companyId", companyId)
				.bind("stockLocationId", stockLocation.getId())
				.fetch();
		for (PartnerDefaultStockLocation partnerDefaultStockLocation : partnerDefaultStockLocations) {
			Partner partnerToClean = partnerDefaultStockLocation.getPartner();
			partnerToClean.removePartnerDefaultStockLocationListItem(partnerDefaultStockLocation);
		}

	}
}
