/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2014 Axelor (<http://axelor.com>).
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
package com.axelor.apps.production.service;

import java.math.BigDecimal;

import com.axelor.apps.base.db.Product;
//import com.axelor.apps.organisation.db.Project;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.ProductionOrder;
import com.axelor.db.Repository;
import com.axelor.exception.AxelorException;
import com.google.inject.persist.Transactional;

public interface ProductionOrderService extends Repository<ProductionOrder> {

	
	public ProductionOrder createProductionOrder() throws AxelorException;
	
	public String getProductionOrderSeq() throws AxelorException;

	/**
	 * Generate a Production Order
	 * @param product
	 * 		Product must be passed in param because product can be different of bill of material product (Product variant)
	 * @param billOfMaterial
	 * @param qtyRequested
	 * @param businessProject
	 * @return
	 * @throws AxelorException
	 */
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public ProductionOrder generateProductionOrder(Product product, BillOfMaterial billOfMaterial, BigDecimal qtyRequested) throws AxelorException;
	
	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public ProductionOrder addManufOrder(ProductionOrder productionOrder, Product product, BillOfMaterial billOfMaterial, BigDecimal qtyRequested) throws AxelorException;
	
}
