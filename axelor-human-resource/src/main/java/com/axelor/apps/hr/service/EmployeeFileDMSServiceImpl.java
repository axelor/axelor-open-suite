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

import com.axelor.apps.base.service.DMSService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.EmployeeFile;
import com.axelor.apps.hr.db.repo.EmployeeFileRepository;
import com.axelor.dms.db.DMSFile;
import com.axelor.dms.db.repo.DMSFileRepository;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class EmployeeFileDMSServiceImpl implements EmployeeFileDMSService {
  protected EmployeeFileRepository employeeFileRepository;
  protected DMSFileRepository dmsFileRepository;
  protected DMSService dmsService;
  protected MetaFiles metaFiles;
  protected AppBaseService appBaseService;

  @Inject
  public EmployeeFileDMSServiceImpl(
      EmployeeFileRepository employeeFileRepository,
      DMSFileRepository dmsFileRepository,
      DMSService dmsService,
      MetaFiles metaFiles,
      AppBaseService appBaseService) {
    this.employeeFileRepository = employeeFileRepository;
    this.dmsFileRepository = dmsFileRepository;
    this.dmsService = dmsService;
    this.metaFiles = metaFiles;
    this.appBaseService = appBaseService;
  }

  @Override
  @Transactional
  public void setDMSFile(EmployeeFile employeeFile) {
    MetaFile metaFile = employeeFile.getMetaFile();
    dmsService.setDmsFile(metaFile, employeeFile);
    employeeFileRepository.save(employeeFile);
  }

  @Override
  public String getInlineUrl(EmployeeFile employeeFile) {
    MetaFile metaFile = employeeFile.getMetaFile();
    DMSFile dmsFile = employeeFile.getDmsFile();
    if (metaFile == null || dmsFile == null || !"application/pdf".equals(metaFile.getFileType())) {
      return "";
    }
    return dmsService.getInlineUrl(dmsFile);
  }

  @Override
  @Transactional
  public EmployeeFile createEmployeeFile(DMSFile dmsFile, Employee employee) {
    EmployeeFile employeeFile = new EmployeeFile();
    employeeFile.setMetaFile(dmsFile.getMetaFile());
    employeeFile.setEmployee(employee);
    employeeFile.setFileDescription(dmsFile.getFileName());
    employeeFile.setRecordDate(appBaseService.getTodayDate(null));
    employeeFileRepository.save(employeeFile);
    setDMSFile(employeeFile);
    return employeeFile;
  }
}
