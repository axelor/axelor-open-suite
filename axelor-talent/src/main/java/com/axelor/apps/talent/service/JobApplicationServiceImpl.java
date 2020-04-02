/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.talent.db.JobApplication;
import com.axelor.apps.talent.db.Skill;
import com.axelor.apps.talent.db.repo.JobApplicationRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.HashSet;
import java.util.Set;

public class JobApplicationServiceImpl implements JobApplicationService {

  @Inject private JobApplicationRepository jobApplicationRepo;

  @Inject private AppBaseService appBaseService;

  @Inject private PartnerService partnerSerivce;

  @Transactional
  @Override
  public Employee hire(JobApplication jobApplication) {

    Employee employee = createEmployee(jobApplication);

    jobApplication.setStatusSelect(JobApplicationRepository.STATUS_HIRED);
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
    Set<Skill> tagSkillSet = new HashSet<Skill>();
    tagSkillSet.addAll(jobApplication.getSkillSet());
    employee.setSkillSet(tagSkillSet);
    if (employee.getMainEmploymentContract() != null)
      employee
          .getMainEmploymentContract()
          .setCompanyDepartment(jobApplication.getJobPosition().getCompanyDepartment());
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
    contact.setEmailAddress(jobApplication.getEmailAddress());
    contact.setFullName(partnerSerivce.computeFullName(contact));

    return contact;
  }

  @Override
  public String computeFullName(JobApplication jobApplication) {

    String fullName = null;

    if (jobApplication.getFirstName() != null) {
      fullName = jobApplication.getFirstName();
    }
    if (fullName == null) {
      fullName = jobApplication.getLastName();
    } else {
      fullName += " " + jobApplication.getLastName();
    }

    return fullName;
  }
}
