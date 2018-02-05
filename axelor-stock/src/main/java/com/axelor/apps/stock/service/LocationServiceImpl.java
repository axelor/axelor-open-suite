/**
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

import java.math.BigDecimal;
import java.util.List;

import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.stock.db.Location;
import com.axelor.apps.stock.db.LocationLine;
import com.axelor.apps.stock.db.repo.LocationRepository;
import com.google.inject.Inject;

public class LocationServiceImpl implements LocationService{
	
	@Inject
	LocationRepository locationRepo;
	
	@Inject
	LocationLineService locationLineService;
	
	@Inject
	ProductRepository productRepo;
	
	@Override
	public Location getDefaultLocation() {
		return locationRepo.all().filter("self.isDefaultLocation = true AND self.typeSelect = ?1", LocationRepository.TYPE_INTERNAL).fetchOne();
	}
	
	public List<Location> getInternalLocations() {
		return locationRepo.all().filter("self.typeSelect = ?1", LocationRepository.TYPE_INTERNAL).fetch();
	}
	
	@Override
	public BigDecimal getQty(Long productId, Long locationId, String qtyType) {
		if (productId != null) {
			if (locationId == null) {
				List<Location> locations = getInternalLocations();
				if (!locations.isEmpty()) {
					BigDecimal qty = BigDecimal.ZERO;
					for (Location location : locations) {
						LocationLine locationLine = locationLineService.getLocationLine(locationRepo.find(location.getId()), productRepo.find(productId));
						
						if (locationLine != null) {
							qty = qty.add(qtyType == "real" ? locationLine.getCurrentQty() : locationLine.getFutureQty());
						}
					}
					return qty;
				}
			} else {
				LocationLine locationLine = locationLineService.getLocationLine(locationRepo.find(locationId), productRepo.find(productId));
				
				if (locationLine != null) {
					return qtyType == "real" ? locationLine.getCurrentQty() : locationLine.getFutureQty();
				}
			}
		}
		
		return null;
	}

	@Override
	public BigDecimal getRealQty(Long productId, Long locationId) {
		return getQty(productId, locationId, "real");
	}

	@Override
	public BigDecimal getFutureQty(Long productId, Long locationId) {
		return getQty(productId, locationId, "future");
	}

	
}
