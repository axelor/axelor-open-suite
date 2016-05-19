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
package com.axelor.apps.businessproject.service;

import java.util.ArrayList;
import java.util.List;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.administration.GeneralService;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class SaleOrderProjectService {

	@Inject
	protected GeneralService generalService;

	@Inject
	protected ProjectTaskBusinessService projectTaskBusinessService;
	
	@Inject
	protected SaleOrderRepository saleOrderRepo;

	@Transactional
	public ProjectTask generateProject(SaleOrder saleOrder){
		ProjectTask project = projectTaskBusinessService.generateProject(saleOrder);
		saleOrderRepo.save(saleOrder);
		return project;
	}

	@Transactional
	public List<Long> generateTasks(SaleOrder saleOrder){
		List<Long> listId = new ArrayList<Long>();
		List<SaleOrderLine> saleOrderLineList = saleOrder.getSaleOrderLineList();
		for (SaleOrderLine saleOrderLine : saleOrderLineList) {
			Product product = saleOrderLine.getProduct();
			if(ProductRepository.PRODUCT_TYPE_SERVICE.equals(product.getProductTypeSelect()) && saleOrderLine.getSaleSupplySelect() == SaleOrderLineRepository.SALE_SUPPLY_PRODUCE){
				ProjectTask task = projectTaskBusinessService.generateTask(saleOrderLine, saleOrder.getProject());
				Beans.get(SaleOrderLineRepository.class).save(saleOrderLine);
				listId.add(task.getId());
			}
		}
		return listId;
	}

}
