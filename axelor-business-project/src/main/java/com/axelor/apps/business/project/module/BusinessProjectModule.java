/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2015 Axelor (<http://axelor.com>).
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
package com.axelor.apps.business.project.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.business.project.service.ExpenseProjectService;
import com.axelor.apps.business.project.service.ProjectTaskBusinessService;
import com.axelor.apps.business.project.service.PurchaseOrderInvoiceProjectServiceImpl;
import com.axelor.apps.business.project.service.SaleOrderInvoiceProjectServiceImpl;
import com.axelor.apps.business.project.service.TimesheetProjectServiceImp;
import com.axelor.apps.hr.service.expense.ExpenseService;
import com.axelor.apps.hr.service.timesheet.TimesheetServiceImp;
import com.axelor.apps.project.service.ProjectTaskService;
import com.axelor.apps.supplychain.service.PurchaseOrderInvoiceServiceImpl;
import com.axelor.apps.supplychain.service.SaleOrderInvoiceServiceImpl;

public class BusinessProjectModule extends AxelorModule{

	    @Override
	    protected void configure() {
	    	 bind(SaleOrderInvoiceServiceImpl.class).to(SaleOrderInvoiceProjectServiceImpl.class);
	    	 bind(PurchaseOrderInvoiceServiceImpl.class).to(PurchaseOrderInvoiceProjectServiceImpl.class);
	    	 bind(TimesheetServiceImp.class).to(TimesheetProjectServiceImp.class);
	    	 bind(ExpenseService.class).to(ExpenseProjectService.class);
	    	 bind(ProjectTaskService.class).to(ProjectTaskBusinessService.class);
	    }
}
