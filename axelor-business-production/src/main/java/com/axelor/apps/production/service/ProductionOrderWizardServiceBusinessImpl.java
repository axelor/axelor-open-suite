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
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.base.db.Product;
//import com.axelor.apps.organisation.db.Project;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.ProductionOrder;
import com.axelor.apps.production.exceptions.IExceptionMessage;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.apps.project.db.repo.ProjectTaskRepository;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.Context;
import com.google.inject.Inject;

public class ProductionOrderWizardServiceBusinessImpl extends ProductionOrderWizardServiceImpl {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Inject
	private ProductionOrderServiceBusinessImpl productionOrderServiceBusinessImpl;

	@Override
	public String validate(Context context) throws AxelorException  {

		Map<String, Object> bomContext = (Map<String, Object>) context.get("billOfMaterial");
		BillOfMaterial billOfMaterial = billOfMaterialService.find(((Integer) bomContext.get("id")).longValue());

		BigDecimal qty = new BigDecimal((String)context.get("qty"));

		Product product = null;

		if(context.get("product") != null)  {
			Map<String, Object> productContext = (Map<String, Object>) context.get("product");
			product = productRepo.find(((Integer) productContext.get("id")).longValue());
		}
		else  {
			product = billOfMaterial.getProduct();
		}

		ProjectTask projectTask = null;
		if(context.get("business_id") != null)  {
			projectTask = Beans.get(ProjectTaskRepository.class).find(((Integer) context.get("business_id")).longValue());
		}

		ProductionOrder productionOrder = productionOrderServiceBusinessImpl.generateProductionOrder(product, billOfMaterial, qty, projectTask);

		if(productionOrder != null)  {
			return I18n.get(IExceptionMessage.PRODUCTION_ORDER_1)+" ("+productionOrder.getProductionOrderSeq()+")";
		}
		else  {
			return I18n.get(IExceptionMessage.PRODUCTION_ORDER_2);
		}
	}

}
