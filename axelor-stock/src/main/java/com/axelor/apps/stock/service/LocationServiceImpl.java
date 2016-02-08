/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2016 Axelor (<http://axelor.com>).
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

		return locationRepo.all().filter("self.isDefaultLocation = true AND self.typeSelect = 1").fetchOne();
	}
	
	@Override
	public	BigDecimal getQty(Long productId, Long locationId, String qtyType) {
		
		if(productId != null){
			
			if(locationId == null){
				Location location = getDefaultLocation();
				if(location != null){
					locationId = location.getId();
				}
			}

			if(locationId != null){
				LocationLine locationLine = locationLineService.getLocationLine(locationRepo.find(locationId), productRepo.find(productId));
				
				if(locationLine != null){
					return qtyType == "current" ? locationLine.getCurrentQty() : locationLine.getFutureQty();
				}
			}
		}
		
		return null;
	}

	@Override
	public BigDecimal getRealQty(Long productId, Long locationId) {
		return getQty(productId, locationId, "current");
	}

	@Override
	public BigDecimal getFutureQty(Long productId, Long locationId) {
		return getQty(productId, locationId, "real");
	}

	
}
