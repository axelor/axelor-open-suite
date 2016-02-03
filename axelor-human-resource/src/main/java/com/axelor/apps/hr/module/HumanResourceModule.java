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
package com.axelor.apps.hr.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.service.batch.MailBatchService;
import com.axelor.apps.hr.service.batch.MailBatchServiceHR;
import com.axelor.apps.hr.service.config.AccountConfigHRService;
import com.axelor.apps.hr.service.employee.EmployeeService;
import com.axelor.apps.hr.service.employee.EmployeeServiceImp;
import com.axelor.apps.hr.service.timesheet.TimesheetService;
import com.axelor.apps.hr.service.timesheet.TimesheetServiceImp;


public class HumanResourceModule extends AxelorModule {

	@Override
	protected void configure() {
		
		bind(EmployeeService.class).to(EmployeeServiceImp.class);
		bind(TimesheetService.class).to(TimesheetServiceImp.class);
		bind(MailBatchService.class).to(MailBatchServiceHR.class);
		bind(AccountConfigService.class).to(AccountConfigHRService.class);
	}

}
