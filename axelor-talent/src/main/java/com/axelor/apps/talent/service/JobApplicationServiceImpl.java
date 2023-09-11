/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
import com.axelor.apps.base.service.DMSService;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.talent.db.JobApplication;
import com.axelor.apps.talent.db.repo.JobApplicationRepository;
import com.axelor.common.ObjectUtils;
import com.axelor.dms.db.repo.DMSFileRepository;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.File;
import java.io.IOException;

public class JobApplicationServiceImpl implements JobApplicationService {

  protected JobApplicationRepository jobApplicationRepo;

  protected AppBaseService appBaseService;

  protected MetaFiles metaFiles;

  protected DMSFileRepository dmsFileRepo;
  protected AppTalentService appTalentService;
  protected DMSService dmsService;

  @Inject
  public JobApplicationServiceImpl(
      JobApplicationRepository jobApplicationRepo,
      AppBaseService appBaseService,
      MetaFiles metaFiles,
      DMSFileRepository dmsFileRepo,
      AppTalentService appTalentService,
      DMSService dmsService) {
    this.jobApplicationRepo = jobApplicationRepo;
    this.appBaseService = appBaseService;
    this.metaFiles = metaFiles;
    this.dmsFileRepo = dmsFileRepo;
    this.appTalentService = appTalentService;
    this.dmsService = dmsService;
  }

  @Transactional
  @Override
  public Employee createEmployeeFromJobApplication(JobApplication jobApplication) {
    Employee employee = createEmployee(jobApplication);
    jobApplication.setEmployee(employee);
    jobApplicationRepo.save(jobApplication);
    hireCandidate(jobApplication);
    return employee;
  }

  protected void hireCandidate(JobApplication jobApplication) {
    if (jobApplication.getJobPosition() != null
        && jobApplication.getHiringStage()
            == appTalentService.getAppRecruitment().getHiringStatus()) {
      int nbPeopleHired = jobApplication.getJobPosition().getNbPeopleHired();
      nbPeopleHired += 1;
      jobApplication.getJobPosition().setNbPeopleHired(nbPeopleHired);
    }
  }

  protected Employee createEmployee(JobApplication jobApplication) {

    Employee employee = new Employee();
    employee.setHireDate(appBaseService.getTodayDate(jobApplication.getJobPosition().getCompany()));
    employee.setContactPartner(createContact(jobApplication));
    if (employee.getMainEmploymentContract() != null)
      employee
          .getMainEmploymentContract()
          .setCompanyDepartment(jobApplication.getJobPosition().getCompanyDepartment());
    setEmployeeFileList(jobApplication, employee);
    employee.setName(employee.getContactPartner().getName());

    return employee;
  }

  @Override
  public void setEmployeeFileList(JobApplication jobApplication, Employee employee) {
    if (ObjectUtils.isEmpty(employee)) {
      employee = jobApplication.getEmployee();
      if (ObjectUtils.isEmpty(employee)) return;
    }
    if (ObjectUtils.notEmpty(jobApplication.getFileList())) {
      jobApplication.getFileList().forEach(employee::addEmployeeFileListItem);
    }
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
    MetaFile metaFile = jobApplication.getResume();
    dmsService.setDmsFile(metaFile, jobApplication);
    jobApplicationRepo.save(jobApplication);
  }

  public String getInlineUrl(JobApplication jobApplication) {
    return dmsService.getInlineUrl(jobApplication.getDmsFile());
  }
}
