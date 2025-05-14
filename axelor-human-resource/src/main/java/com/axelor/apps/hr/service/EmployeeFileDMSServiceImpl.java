/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.db.File;
import com.axelor.apps.base.db.repo.FileRepository;
import com.axelor.apps.base.service.DMSService;
import com.axelor.apps.base.service.FileService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.dms.db.DMSFile;
import com.axelor.dms.db.repo.DMSFileRepository;
import com.axelor.meta.MetaFiles;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class EmployeeFileDMSServiceImpl implements EmployeeFileDMSService {
  protected FileRepository employeeFileRepository;
  protected DMSFileRepository dmsFileRepository;
  protected DMSService dmsService;
  protected MetaFiles metaFiles;
  protected AppBaseService appBaseService;
  protected FileService fileService;

  @Inject
  public EmployeeFileDMSServiceImpl(
      FileRepository employeeFileRepository,
      DMSFileRepository dmsFileRepository,
      DMSService dmsService,
      MetaFiles metaFiles,
      AppBaseService appBaseService,
      FileService fileService) {
    this.employeeFileRepository = employeeFileRepository;
    this.dmsFileRepository = dmsFileRepository;
    this.dmsService = dmsService;
    this.metaFiles = metaFiles;
    this.appBaseService = appBaseService;
    this.fileService = fileService;
  }

  @Override
  @Transactional
  public File createEmployeeFile(DMSFile dmsFile, Employee employee) {
    File employeeFile = new File();
    employeeFile.setMetaFile(dmsFile.getMetaFile());
    employeeFile.setEmployee(employee);
    employeeFile.setFileDescription(dmsFile.getFileName());
    employeeFile.setRecordDate(appBaseService.getTodayDate(null));
    employeeFile.setFileTypeSelect(FileRepository.EMPLOYEE_FILE_TYPE);
    employeeFileRepository.save(employeeFile);
    fileService.setDMSFile(employeeFile);
    return employeeFile;
  }
}
