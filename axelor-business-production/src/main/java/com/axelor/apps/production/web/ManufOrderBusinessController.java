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
package com.axelor.apps.production.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.service.ManufOrderServiceBusinessImpl;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class ManufOrderBusinessController {

	
	private static final Logger LOG = LoggerFactory.getLogger(ManufOrderController.class);
	
	public void propagateIsToInvoice (ActionRequest request, ActionResponse response) {
		
		ManufOrderServiceBusinessImpl manufOrderService = Beans.get(ManufOrderServiceBusinessImpl.class);
		ManufOrder manufOrder = request.getContext().asType( ManufOrder.class );

		manufOrderService.propagateIsToInvoice(manufOrderService.find(manufOrder.getId()));
		
		response.setReload(true);
		
	}
	
	
	
}
