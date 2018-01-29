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

import com.axelor.apps.base.db.Company;
import com.axelor.apps.hr.db.Employee;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.db.JPA;
import com.axelor.db.Model;

public class HRMenuTagService {

	/**
	 * 
	 * @param modelConcerned
	 *
	 * @param status
	 * 			1 : Draft
	 * 			2 : Confirmed
	 * 			3 : Validated
	 * 			4 : Refused
	 * 			5 : Canceled
	 * @return
	 * 		The number of records
	 */
	public <T extends Model> String countRecordsTag(Class<T> modelConcerned, int status) {
		
		User user = AuthUtils.getUser();
		Employee employee = user.getEmployee();
		Company activeCompany = user.getActiveCompany();
		
		if(employee != null && employee.getHrManager())  {
			
			return Long.toString(JPA.all(modelConcerned).filter("self.company = ?1 AND  self.statusSelect = ?2", activeCompany, status).count());

		}
		else  {
			
			return Long.toString(JPA.all(modelConcerned).filter("self.user.employee.manager = ?1 AND self.company = ?2 AND  self.statusSelect = ?3", user, activeCompany, status).count());

		}
	}
	

}
