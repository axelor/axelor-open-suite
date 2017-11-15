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
package com.axelor.apps.purchase.web;

import java.math.BigDecimal;

import com.axelor.apps.base.db.ShippingCoef;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.purchase.db.SupplierCatalog;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class PurchaseProductController {

	@Inject
	private ProductRepository productRepo;

public void fillShippingCoeff(ActionRequest request, ActionResponse response) throws AxelorException {
	    Product product = request.getContext().asType(Product.class);
	    if (!product.getDefShipCoefByPartner()) {
	    	return;
		}
	    product = productRepo.find(product.getId());
		BigDecimal productShippingCoef = null;
	    for (SupplierCatalog supplierCatalog : product.getSupplierCatalogList()) {
	    	if (!supplierCatalog.getSupplierPartner().equals(product.getDefaultSupplierPartner()) ||
					supplierCatalog.getShippingCoefList() == null) {
	    		continue;
			}
	    	for(ShippingCoef shippingCoef : supplierCatalog.getShippingCoefList()) {
	    	    if (shippingCoef.getCompany() == Beans.get(UserService.class).getUserActiveCompany()) {
	    	        productShippingCoef = shippingCoef.getShippingCoef();
	    	        break;
				}
			}
		}
	    response.setValue("$shippingCoef", productShippingCoef);
    }
}
