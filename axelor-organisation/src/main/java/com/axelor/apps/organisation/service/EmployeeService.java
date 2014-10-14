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
package com.axelor.apps.organisation.service;

import java.util.HashSet;

import com.axelor.apps.base.db.Keyword;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.organisation.db.Candidate;
import com.axelor.apps.organisation.db.Employee;
import com.axelor.apps.organisation.db.repo.EmployeeRepository;
import com.google.inject.persist.Transactional;

public class EmployeeService extends EmployeeRepository{

	@Transactional
	public Employee createEmployee(Candidate candidate)  {
		
		Employee employee = new Employee();
		
		employee.setCandidate(candidate);
		employee.setCategorySelect(candidate.getCategorySelect());
		employee.setCompetence(candidate.getCompetence());
		
		employee.setFieldKeywordSet(new HashSet<Keyword>());
		employee.getFieldKeywordSet().addAll(candidate.getFieldKeywordSet());
		employee.setFirstName(candidate.getFirstName());
		employee.setJobKeywordSet(new HashSet<Keyword>());
		employee.getJobKeywordSet().addAll(candidate.getJobKeywordSet());
		
		employee.setMacroCategorySelect(candidate.getMacroCategorySelect());
		employee.setName(candidate.getName());
		employee.setPartnerSet(new HashSet<Partner>());
		employee.getPartnerSet().addAll(candidate.getPartnerSet());
		employee.setPersonalInfo(candidate.getPersonalInfo());
		employee.setPicture(candidate.getPicture());
		if(candidate.getPositionList()!=null && !candidate.getPositionList().isEmpty())  {
			employee.getPositionList().addAll(candidate.getPositionList());
		}
		employee.setTitleSelect(candidate.getTitleSelect());
		employee.setToolKeywordSet(new HashSet<Keyword>());
		employee.getToolKeywordSet().addAll(candidate.getToolKeywordSet());
		
		return save(employee);
		
		
	}
	
	
	
	
	
	
	
	
	
}
