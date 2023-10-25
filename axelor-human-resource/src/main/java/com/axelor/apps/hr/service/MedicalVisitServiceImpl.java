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
package com.axelor.apps.hr.service;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.EmployeeFile;
import com.axelor.apps.hr.db.MedicalVisit;
import com.axelor.apps.hr.db.repo.EmployeeFileRepository;
import com.axelor.apps.hr.translation.ITranslation;
import com.axelor.i18n.I18n;
import com.axelor.meta.db.MetaFile;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.Collections;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class MedicalVisitServiceImpl implements MedicalVisitService {

  protected AppBaseService appBaseService;
  protected EmployeeFileRepository employeeFileRepository;

  @Inject
  public MedicalVisitServiceImpl(
      AppBaseService appBaseService, EmployeeFileRepository employeeFileRepository) {
    this.appBaseService = appBaseService;
    this.employeeFileRepository = employeeFileRepository;
  }

  @Transactional
  @Override
  public List<EmployeeFile> addToEmployeeFiles(Employee employee) {
    List<EmployeeFile> employeeFileList = employee.getEmployeeFileList();
    List<MedicalVisit> medicalVisitList = employee.getMedicalVisitList();
    if (CollectionUtils.isEmpty(medicalVisitList)) {
      return Collections.emptyList();
    }
    MedicalVisit medicalVisit = Iterables.getLast(medicalVisitList);
    MetaFile metaFile = medicalVisit.getMedicalVisitFile();
    if (metaFile != null) {
      if (employee.getEmployeeFileList().stream()
          .anyMatch(
              employeeFile ->
                  employeeFile.getMetaFile().getFileName().equals(metaFile.getFileName()))) {
        return employeeFileList;
      }
      employee.addEmployeeFileListItem(createEmployeeFile(medicalVisit, metaFile));
    }
    return employee.getEmployeeFileList();
  }

  @Override
  public String getMedicalVisitSubject(MedicalVisit medicalVisit) {
    return I18n.get(ITranslation.MEDICAL_VISIT) + " - " + medicalVisit.getVisitReason().getName();
  }

  protected EmployeeFile createEmployeeFile(MedicalVisit medicalVisit, MetaFile metaFile) {
    EmployeeFile employeeFile = new EmployeeFile();
    employeeFile.setMetaFile(metaFile);
    employeeFile.setFileDescription(getMedicalVisitSubject(medicalVisit));
    employeeFile.setRecordDate(appBaseService.getTodayDate(null));
    return employeeFile;
  }
}
