package com.axelor.apps.hr.service;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.EmployeeFile;
import com.axelor.apps.hr.db.MedicalVisit;
import com.axelor.apps.hr.db.repo.EmployeeFileRepository;
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
      employee.addEmployeeFileListItem(getEmployeeFile(medicalVisit, metaFile));
    }
    return employee.getEmployeeFileList();
  }

  protected EmployeeFile getEmployeeFile(MedicalVisit medicalVisit, MetaFile metaFile) {
    EmployeeFile employeeFile = new EmployeeFile();
    employeeFile.setMetaFile(metaFile);
    employeeFile.setFileDescription(medicalVisit.getVisitReason().getName());
    employeeFile.setRecordDate(appBaseService.getTodayDate(null));
    return employeeFile;
  }
}
