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
package com.axelor.apps.talent.service;

import java.util.HashSet;
import java.util.Set;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.talent.db.JobApplication;
import com.axelor.apps.talent.db.TagSkill;
import com.axelor.apps.talent.db.repo.JobApplicationRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class JobApplicationServiceImpl implements JobApplicationService {
	
	@Inject
	private JobApplicationRepository jobApplicationRepo;
	
	@Inject
	private AppBaseService appBaseService;
	
	@Inject
	private PartnerService partnerSerivce;

	@Transactional
	@Override
	public Employee hire(JobApplication jobApplication) {
		
		Employee employee = createEmployee(jobApplication);
		
		jobApplication.setStatusSelect(3);
		jobApplication.setEmployee(employee);
		if (jobApplication.getJobPosition() != null) {
			int nbPeopleHired = jobApplication.getJobPosition().getNbPeopleHired();
			nbPeopleHired += 1;
			jobApplication.getJobPosition().setNbPeopleHired(nbPeopleHired);
		}
		
		jobApplicationRepo.save(jobApplication);
		
		return employee;
	}
	
	private Employee createEmployee(JobApplication jobApplication) {
		
		Employee employee = new Employee();
		employee.setDateOfHire(appBaseService.getTodayDate());
		employee.setContactPartner(createContact(jobApplication));
		Set<TagSkill> tagSkillSet = new HashSet<TagSkill>();
		tagSkillSet.addAll(jobApplication.getTagSkillSet());
		employee.setTagSkillSet(tagSkillSet);
		employee.setDepartment(jobApplication.getJobPosition().getCompanyDepartment());
		employee.setName(employee.getContactPartner().getName());
		
		return employee;
	}
	
	private Partner createContact(JobApplication jobApplication) {
		
		Partner contact = new Partner();
		contact.setPartnerTypeSelect(2);
		contact.setFirstName(jobApplication.getFirstName());
		contact.setName(jobApplication.getLastName());
		contact.setIsContact(true);
		contact.setIsEmployee(true);
		contact.setFixedPhone(jobApplication.getFixedPhone());
		contact.setMobilePhone(jobApplication.getMobilePhone());
		contact.setSource(jobApplication.getJobSource());
		contact.setEmailAddress(jobApplication.getEmailAddress());
		contact.setFullName(partnerSerivce.computeFullName(contact));
		
		return contact;
	}
}
