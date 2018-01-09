/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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
package com.axelor.apps.hr.service;

import com.axelor.apps.hr.db.Expense;
import com.axelor.apps.hr.db.ExtraHours;
import com.axelor.apps.hr.db.LeaveRequest;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.auth.AuthUtils;
import com.axelor.db.JPA;

public class HRMenuTagService {

	public String CountRecordsTag(Object object) {
		Long total = null;
		if(AuthUtils.getUser().getEmployee() != null && AuthUtils.getUser().getEmployee().getHrManager()){
			if(object instanceof Timesheet)
				total= JPA.all(Timesheet.class).filter("self.company = ?1 AND  self.statusSelect = 2",AuthUtils.getUser().getActiveCompany()).count();
			if(object instanceof Expense)
				total= JPA.all(Expense.class).filter("self.company = ?1 AND  self.statusSelect = 2",AuthUtils.getUser().getActiveCompany()).count();
			if(object instanceof LeaveRequest)
				total= JPA.all(LeaveRequest.class).filter("self.company = ?1 AND  self.statusSelect = 2",AuthUtils.getUser().getActiveCompany()).count();
			if(object instanceof ExtraHours)
				total= JPA.all(ExtraHours.class).filter("self.company = ?1 AND  self.statusSelect = 2",AuthUtils.getUser().getActiveCompany()).count();
			
		}else{
			if(object instanceof Timesheet)
				total = JPA.all(Timesheet.class).filter("self.user.employee.manager = ?1 AND self.company = ?2 AND  self.statusSelect = 2",AuthUtils.getUser(),AuthUtils.getUser().getActiveCompany()).count();
			if(object instanceof Expense)
				total = JPA.all(Expense.class).filter("self.user.employee.manager = ?1 AND self.company = ?2 AND  self.statusSelect = 2",AuthUtils.getUser(),AuthUtils.getUser().getActiveCompany()).count();
			if(object instanceof LeaveRequest)
				total = JPA.all(LeaveRequest.class).filter("self.user.employee.manager = ?1 AND self.company = ?2 AND  self.statusSelect = 2",AuthUtils.getUser(),AuthUtils.getUser().getActiveCompany()).count();
			if(object instanceof ExtraHours)
				total = JPA.all(ExtraHours.class).filter("self.user.employee.manager = ?1 AND self.company = ?2 AND  self.statusSelect = 2",AuthUtils.getUser(),AuthUtils.getUser().getActiveCompany()).count();
		}
		return String.format("%s", total);
	}
	

}
