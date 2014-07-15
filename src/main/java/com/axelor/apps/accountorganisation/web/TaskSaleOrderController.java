/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2012-2014 Axelor (<http://axelor.com>).
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
package com.axelor.apps.accountorganisation.web;

import com.axelor.apps.accountorganisation.service.TaskSaleOrderService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class TaskSaleOrderController {

	@Inject
	private TaskSaleOrderService taskSaleOrderService;
	
	
	public void createTasks(ActionRequest request, ActionResponse response) throws AxelorException {
		
		SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);
		
		taskSaleOrderService.createTasks(SaleOrder.find(saleOrder.getId()));
	
	}
}
