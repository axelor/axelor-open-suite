/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.talent.service;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.talent.db.JobApplication;
import com.axelor.apps.talent.db.Skill;
import com.axelor.apps.talent.db.repo.JobApplicationRepository;
import com.axelor.dms.db.DMSFile;
import com.axelor.dms.db.repo.DMSFileRepository;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class JobApplicationServiceImpl implements JobApplicationService {

  protected JobApplicationRepository jobApplicationRepo;

  protected AppBaseService appBaseService;

  protected MetaFiles metaFiles;

  protected DMSFileRepository dmsFileRepo;

  @Inject
  public JobApplicationServiceImpl(
      JobApplicationRepository jobApplicationRepo,
      AppBaseService appBaseService,
      MetaFiles metaFiles,
      DMSFileRepository dmsFileRepo) {
    this.jobApplicationRepo = jobApplicationRepo;
    this.appBaseService = appBaseService;
    this.metaFiles = metaFiles;
    this.dmsFileRepo = dmsFileRepo;
  }

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

  protected Employee createEmployee(JobApplication jobApplication) {

    Employee employee = new Employee();
    employee.setHireDate(appBaseService.getTodayDate(jobApplication.getJobPosition().getCompany()));
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

  protected Partner createContact(JobApplication jobApplication) {

    Partner contact = new Partner();
    contact.setPartnerTypeSelect(2);
    contact.setFirstName(jobApplication.getFirstName());
    contact.setName(jobApplication.getLastName());
    contact.setIsContact(true);
    contact.setIsEmployee(true);
    contact.setFixedPhone(jobApplication.getFixedPhone());
    contact.setMobilePhone(jobApplication.getMobilePhone());
    contact.setEmailAddress(jobApplication.getEmailAddress());
    if (jobApplication.getPicture() != null) {
      File file = MetaFiles.getPath(jobApplication.getPicture()).toFile();
      try {
        contact.setPicture(metaFiles.upload(file));
      } catch (IOException e) {
        TraceBackService.trace(e);
      }
    }
    Beans.get(PartnerService.class).setPartnerFullName(contact);

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

  @Override
  @Transactional
  public void setDMSFile(JobApplication jobApplication) {
    if (jobApplication.getResume() == null) {
      DMSFile toDelete = dmsFileRepo.find(jobApplication.getResumeId());
      if (toDelete != null) {
        metaFiles.delete(toDelete);
      }
      jobApplication.setResumeId(null);
    } else {
      MetaFile resume = jobApplication.getResume();
      DMSFile resumeFile = metaFiles.attach(resume, resume.getFileName(), jobApplication);
      jobApplication.setResumeId(resumeFile.getId());
    }
    jobApplicationRepo.save(jobApplication);
  }
}
