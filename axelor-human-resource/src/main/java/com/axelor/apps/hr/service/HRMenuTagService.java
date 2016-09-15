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
