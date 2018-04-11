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
package com.axelor.apps.purchase.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.ShippingCoef;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.purchase.db.SupplierCatalog;
import com.axelor.inject.Beans;

public class PurchaseProductServiceImpl implements PurchaseProductService  {

	@Override
	public Map<String, Object> getDiscountsFromCatalog(SupplierCatalog supplierCatalog,BigDecimal price){
		Map<String, Object> discounts = new HashMap<String, Object>();

		discounts.put("discountAmount", supplierCatalog.getPrice().subtract(price));
		discounts.put("discountTypeSelect", 2);

		return discounts;
	}

	@Override
	public Optional<BigDecimal> getShippingCoefFromPartners(Product product) {
		if (product.getSupplierCatalogList() == null) {
			return Optional.empty();
		}
		Company userActiveCompany = Beans.get(UserService.class).getUserActiveCompany();
		return product.getSupplierCatalogList().stream()
				.filter(supplierCatalog ->
						supplierCatalog.getShippingCoefList() != null
								&& supplierCatalog.getSupplierPartner() != null
								&& supplierCatalog.getSupplierPartner()
								.equals(product.getDefaultSupplierPartner()))
				.flatMap(supplierCatalog -> supplierCatalog.getShippingCoefList().stream())
				.filter(shippingCoef ->
						shippingCoef.getCompany() != null
								&& shippingCoef.getCompany().equals(userActiveCompany))
				.map(ShippingCoef::getShippingCoef)
				.findAny();
	}

}
