package com.axelor.apps.hr.service;

import com.axelor.apps.base.service.DMSService;
import com.axelor.apps.hr.db.EmployeeFile;
import com.axelor.apps.hr.db.repo.EmployeeFileRepository;
import com.axelor.dms.db.DMSFile;
import com.axelor.meta.db.MetaFile;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class EmployeeFileDMSServiceImpl implements EmployeeFileDMSService {
  protected EmployeeFileRepository employeeFileRepository;
  protected DMSService dmsService;

  @Inject
  public EmployeeFileDMSServiceImpl(
      EmployeeFileRepository employeeFileRepository, DMSService dmsService) {
    this.employeeFileRepository = employeeFileRepository;
    this.dmsService = dmsService;
  }

  @Override
  @Transactional
  public void setDMSFile(EmployeeFile employeeFile) {
    MetaFile metaFile = employeeFile.getMetaFile();
    DMSFile dmsFile = dmsService.setDmsFile(metaFile, employeeFile.getDmsFile(), employeeFile);

    if (metaFile == null) {
      employeeFile.setDmsFile(null);
    }
    if (dmsFile != null) {
      employeeFile.setDmsFile(dmsFile);
    }
    employeeFileRepository.save(employeeFile);
  }

  @Override
  public String getInlineUrl(EmployeeFile employeeFile) {
    MetaFile metaFile = employeeFile.getMetaFile();
    if (metaFile == null || !"application/pdf".equals(metaFile.getFileType())) {
      return "";
    }
    return dmsService.getInlineUrl(employeeFile.getDmsFile());
  }
}
