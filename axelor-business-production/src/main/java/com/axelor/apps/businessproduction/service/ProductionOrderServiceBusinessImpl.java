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
package com.axelor.apps.businessproduction.service;

import java.math.BigDecimal;

import org.joda.time.LocalDateTime;

import com.axelor.apps.base.db.Product;
//import com.axelor.apps.organisation.db.Project;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.ProductionOrder;
import com.axelor.apps.production.service.ProductionOrderServiceImpl;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.exception.AxelorException;
import com.google.inject.persist.Transactional;

public class ProductionOrderServiceBusinessImpl extends ProductionOrderServiceImpl  {
	

	public ProductionOrder createProductionOrder(ProjectTask projectTask, boolean isToInvoice) throws AxelorException  {

		ProductionOrder productionOrder = new ProductionOrder(this.getProductionOrderSeq());
		productionOrder.setProjectTask(projectTask);

		return productionOrder;

	}


	@Transactional(rollbackOn = {AxelorException.class, Exception.class})
	public ProductionOrder generateProductionOrder(Product product, BillOfMaterial billOfMaterial, BigDecimal qtyRequested, ProjectTask projectTask, LocalDateTime startDate) throws AxelorException  {

		ProductionOrder productionOrder = this.createProductionOrder(projectTask, false);

		this.addManufOrder(productionOrder, product, billOfMaterial, qtyRequested, startDate);

		return productionOrderRepo.save(productionOrder);

	}



}
