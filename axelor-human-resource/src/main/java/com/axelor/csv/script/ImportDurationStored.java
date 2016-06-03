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
package com.axelor.csv.script;

import java.math.BigDecimal;

import com.axelor.apps.hr.service.employee.EmployeeService;
import com.axelor.auth.db.User;
import com.axelor.auth.db.repo.UserRepository;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;

public class ImportDurationStored {

	@Inject
	protected EmployeeService employeeService;

	public String getDurationHoursImport(String duration, String userImp) throws AxelorException{
		BigDecimal visibleDuration = new BigDecimal(duration);
		long userId = Long.parseLong(userImp);
		User user = Beans.get(UserRepository.class).find(userId);
		BigDecimal durationStored = employeeService.getUserDuration(visibleDuration, user.getEmployee().getDailyWorkHours(), true);
		return durationStored.toString();
	}
}
