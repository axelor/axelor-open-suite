/**
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the “License”); you may not use
 * this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://license.axelor.com/.
 *
 * The License is based on the Mozilla Public License Version 1.1 but
 * Sections 14 and 15 have been added to cover use of software over a
 * computer network and provide for limited attribution for the
 * Original Developer. In addition, Exhibit A has been modified to be
 * consistent with Exhibit B.
 *
 * Software distributed under the License is distributed on an “AS IS”
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is part of "Axelor Business Suite", developed by
 * Axelor exclusively.
 *
 * The Original Developer is the Initial Developer. The Initial Developer of
 * the Original Code is Axelor.
 *
 * All portions of the code written by Axelor are
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 */
package com.axelor.apps.organisation.service;

import java.util.HashSet;

import com.axelor.apps.base.db.Keyword;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.organisation.db.Candidate;
import com.axelor.apps.organisation.db.Employee;
import com.google.inject.persist.Transactional;

public class EmployeeService {

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
		
		return employee.save();
		
		
	}
	
	
	
	
	
	
	
	
	
}
