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
package com.axelor.apps.businessproduction.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.businessproduction.service.ManufOrderServiceBusinessImpl;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.web.ManufOrderController;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class ManufOrderBusinessController {
	
	private static final Logger LOG = LoggerFactory.getLogger(ManufOrderController.class);
	
	@Inject
	private ManufOrderRepository manufOrderRepo;
	
	public void propagateIsToInvoice (ActionRequest request, ActionResponse response) {
		
		ManufOrderServiceBusinessImpl manufOrderService = Beans.get(ManufOrderServiceBusinessImpl.class);
		ManufOrder manufOrder = request.getContext().asType( ManufOrder.class );

		manufOrderService.propagateIsToInvoice(manufOrderRepo.find(manufOrder.getId()));
		
		response.setReload(true);
		
	}
	
	
	
}
