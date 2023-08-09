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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Period;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.EmploymentContractType;
import com.axelor.apps.talent.db.HiringStage;
import com.axelor.apps.talent.db.JobApplication;
import com.axelor.apps.talent.db.JobPosition;
import com.axelor.apps.talent.db.TrainingRegister;
import com.axelor.apps.talent.db.TrainingSession;
import com.axelor.apps.talent.db.repo.JobApplicationRepository;
import com.axelor.apps.talent.db.repo.TrainingRegisterRepository;
import com.axelor.common.ObjectUtils;
import com.axelor.message.db.EmailAddress;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.MetaStore;
import com.axelor.meta.schema.views.Selection;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TalentDashboardServiceImpl implements TalentDashboardService {

  protected static final String JOB_POSITION_SELECT = "job.position.position.status";

  protected TrainingRegisterRepository trainingRegisterRepo;
  protected JobApplicationRepository jobApplicationRepository;
  protected MetaFiles metaFiles;
  protected PartnerService partnerService;

  @Inject
  public TalentDashboardServiceImpl(
      TrainingRegisterRepository trainingRegisterRepo,
      JobApplicationRepository jobApplicationRepository,
      MetaFiles metaFiles,
      PartnerService partnerService) {
    this.trainingRegisterRepo = trainingRegisterRepo;
    this.jobApplicationRepository = jobApplicationRepository;
    this.metaFiles = metaFiles;
    this.partnerService = partnerService;
  }

  @Override
  public List<Map<String, Object>> getTrainingData(Employee employee, Period period)
      throws AxelorException {
    List<TrainingRegister> trainingList = getTrainingList(employee, period);
    return getTrainingData(trainingList);
  }

  protected List<TrainingRegister> getTrainingList(Employee employee, Period period) {
    StringBuilder filter = new StringBuilder();
    Map<String, Object> params = new HashMap<>();

    if (employee != null) {
      filter.append("self.employee = :employee");
      params.put("employee", employee);
    }
    if (period != null) {
      filter.append(ObjectUtils.isEmpty(filter) ? "" : " AND ");
      filter.append(
          "((self.fromDate >= :fromDate AND self.toDate <= :endDate) OR (self.fromDate <= :fromDate AND self.toDate >= :fromDate AND self.toDate <= :endDate) OR (self.fromDate >= :fromDate AND self.fromDate <= :endDate AND self.toDate >= :endDate))");
      params.put("fromDate", period.getFromDate());
      params.put("endDate", period.getToDate());
    }
    if (ObjectUtils.isEmpty(filter)) {
      return trainingRegisterRepo.all().fetch();
    }
    return trainingRegisterRepo.all().filter(filter.toString()).bind(params).fetch();
  }

  protected List<Map<String, Object>> getTrainingData(List<TrainingRegister> trainingList) {
    List<Map<String, Object>> trainingData = new ArrayList<>();

    for (TrainingRegister trainingRegister : trainingList) {
      Map<String, Object> map = new HashMap<>();
      map.put("fromDate", trainingRegister.getFromDate());
      map.put("toDate", trainingRegister.getToDate());
      map.put(
          "duration",
          Optional.ofNullable(trainingRegister.getTrainingSession())
              .map(TrainingSession::getDuration)
              .orElse(BigDecimal.ZERO));
      map.put("training", trainingRegister.getTraining().getName());
      map.put("id", trainingRegister.getId());
      map.put(
          "fullName",
          Optional.ofNullable(trainingRegister.getEmployee())
              .map(Employee::getContactPartner)
              .map(partner -> partnerService.computeSimpleFullName(partner))
              .orElse(null));
      trainingData.add(map);
    }
    return trainingData;
  }

  @Override
  public List<Map<String, Object>> getRecruitmentData(Employee employee, Period period)
      throws AxelorException {
    List<JobApplication> jobApplicationList = getRecruitmentList(employee, period);
    return getRecruitmentData(jobApplicationList);
  }

  protected List<JobApplication> getRecruitmentList(Employee employee, Period period) {
    StringBuilder filter = new StringBuilder();
    Map<String, Object> params = new HashMap<>();

    if (employee != null) {
      filter.append("self.responsible = :employee");
      params.put("employee", employee);
    }
    if (period != null) {
      filter.append(ObjectUtils.isEmpty(filter) ? "" : " AND ");
      filter.append("self.creationDate >= :fromDate AND self.creationDate <= :endDate");
      params.put("fromDate", period.getFromDate());
      params.put("endDate", period.getToDate());
    }
    if (ObjectUtils.isEmpty(filter)) {
      return jobApplicationRepository.all().fetch();
    }
    return jobApplicationRepository.all().filter(filter.toString()).bind(params).fetch();
  }

  protected List<Map<String, Object>> getRecruitmentData(List<JobApplication> jobApplicationList) {
    List<Map<String, Object>> recruitmentData = new ArrayList<>();

    for (JobApplication jobApplication : jobApplicationList) {
      Map<String, Object> map = new HashMap<>();
      map.put("id", jobApplication.getId());
      if (jobApplication.getPicture() != null) {
        String picture = metaFiles.getDownloadLink(jobApplication.getPicture(), jobApplication);
        map.put("picture", picture);
      }
      map.put("fullName", jobApplication.getFullName());
      map.put("mobilePhone", jobApplication.getMobilePhone());
      map.put(
          "emailAddress",
          Optional.ofNullable(jobApplication.getEmailAddress())
              .map(EmailAddress::getAddress)
              .orElse(null));
      map.put(
          "hiringStage",
          Optional.ofNullable(jobApplication.getHiringStage())
              .map(HiringStage::getName)
              .orElse(null));
      map.put(
          "jobPosition",
          Optional.ofNullable(jobApplication.getJobPosition())
              .map(JobPosition::getPositionStatusSelect)
              .map(select -> MetaStore.getSelectionItem(JOB_POSITION_SELECT, select))
              .map(Selection.Option::getLocalizedTitle)
              .orElse(null));
      map.put(
          "contractType",
          Optional.ofNullable(jobApplication.getJobPosition())
              .map(JobPosition::getContractType)
              .map(EmploymentContractType::getName)
              .orElse(null));
      recruitmentData.add(map);
    }
    return recruitmentData;
  }
}
