/**
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

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.ProductService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.stock.db.Location;
import com.axelor.apps.stock.db.LocationLine;
import com.axelor.apps.stock.db.StockConfig;
import com.axelor.apps.stock.db.StockRules;
import com.axelor.apps.stock.db.repo.LocationLineRepository;
import com.axelor.apps.stock.db.repo.LocationRepository;
import com.axelor.apps.stock.db.repo.StockRulesRepository;
import com.axelor.apps.stock.service.config.StockConfigService;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class LocationServiceImpl implements LocationService{
	
	@Inject
	LocationRepository locationRepo;
	
	@Inject
	LocationLineService locationLineService;
	
	@Inject
	ProductRepository productRepo;

	public Location getLocation(Company company) {
		try {
			StockConfigService stockConfigService = Beans.get(StockConfigService.class);
			StockConfig stockConfig = stockConfigService.getStockConfig(company);
			return stockConfig.getDefaultLocation();
		} catch (AxelorException e) {
			return null;
		}
	}

	public List<Location> getNonVirtualLocations() {
		return locationRepo.all().filter("self.typeSelect != ?1", LocationRepository.TYPE_VIRTUAL).fetch();
	}
	
	@Override
	public BigDecimal getQty(Long productId, Long locationId, String qtyType) {
		if (productId != null) {
			if (locationId == null) {
				List<Location> locations = getNonVirtualLocations();
				if (!locations.isEmpty()) {
					BigDecimal qty = BigDecimal.ZERO;
					for (Location location : locations) {
						LocationLine locationLine = locationLineService.getLocationLine(locationRepo.find(location.getId()), productRepo.find(productId));
						
						if (locationLine != null) {
							qty = qty.add(qtyType.equals("real") ? locationLine.getCurrentQty() : locationLine.getFutureQty());
						}
					}
					return qty;
				}
			} else {
				LocationLine locationLine = locationLineService.getLocationLine(locationRepo.find(locationId), productRepo.find(productId));
				
				if (locationLine != null) {
					return qtyType.equals("real") ? locationLine.getCurrentQty() : locationLine.getFutureQty();
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

	@Override
	public void computeAvgPriceForProduct(Product product) {
		Long productId = product.getId();
		String query = "SELECT new list(self.id, self.avgPrice, self.currentQty) FROM LocationLine as self "
				+ "WHERE self.product.id = " + productId + " AND self.location.typeSelect != "
				+ LocationRepository.TYPE_VIRTUAL;
		int scale = Beans.get(AppBaseService.class).getNbDecimalDigitForUnitPrice();
		BigDecimal productAvgPrice = BigDecimal.ZERO;
		BigDecimal qtyTot = BigDecimal.ZERO;
		List<List<Object>> results = JPA.em().createQuery(query).getResultList();
		if (results.isEmpty()) {
			return;
		}
		for (List<Object> result : results) {
			BigDecimal avgPrice = (BigDecimal) result.get(1);
			BigDecimal qty = (BigDecimal) result.get(2);
			productAvgPrice = productAvgPrice.add(avgPrice.multiply(qty));
			qtyTot = qtyTot.add(qty);
		}
		if (qtyTot.compareTo(BigDecimal.ZERO) == 0) {
			return;
		}
		productAvgPrice = productAvgPrice.divide(qtyTot, scale, BigDecimal.ROUND_HALF_UP);
		product.setAvgPrice(productAvgPrice);
		if (product.getCostTypeSelect() == ProductRepository.COST_TYPE_AVERAGE_PRICE) {
		    product.setCostPrice(productAvgPrice);
			if (product.getAutoUpdateSalePrice()) {
				Beans.get(ProductService.class).updateSalePrice(product);
			}
		}
		productRepo.save(product);
	}

	public List<Long> getBadLocationLineId() {

		List<LocationLine> locationLineList = Beans.get(LocationLineRepository.class)
				.all().filter("self.location.typeSelect = 1 OR self.location.typeSelect = 2").fetch();

		List<Long> idList = new ArrayList<>();

		for (LocationLine locationLine : locationLineList) {
			StockRules stockRules = Beans.get(StockRulesRepository.class).all()
					.filter("self.location = ?1 AND self.product = ?2", locationLine.getLocation(), locationLine.getProduct()).fetchOne();
			if (stockRules != null)
			if (locationLine.getFutureQty().compareTo(stockRules.getMinQty()) < 0) {
				idList.add(locationLine.getId());
			}
		}

		if (idList.isEmpty()) {
			idList.add(0L);
		}

		return idList;
	}
}
