package com.axelor.apps.hr.service;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.EmployeeFile;
import com.axelor.apps.hr.db.MedicalLeave;
import com.axelor.apps.hr.db.repo.EmployeeFileRepository;
import com.axelor.apps.hr.db.repo.MedicalLeaveRepository;
import com.axelor.meta.db.MetaFile;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.apache.commons.collections.CollectionUtils;

public class MedicalLeaveServiceImpl implements MedicalLeaveService {

  protected AppBaseService appBaseService;
  protected EmployeeFileRepository employeeFileRepository;
  protected MedicalLeaveRepository medicalLeaveRepository;

  @Inject
  public MedicalLeaveServiceImpl(
      AppBaseService appBaseService,
      EmployeeFileRepository employeeFileRepository,
      MedicalLeaveRepository medicalLeaveRepository) {
    this.appBaseService = appBaseService;
    this.employeeFileRepository = employeeFileRepository;
    this.medicalLeaveRepository = medicalLeaveRepository;
  }

  @Override
  public LocalDate getLastMedicalLeaveDate(Employee employee) {
    List<MedicalLeave> medicalLeaveList = employee.getMedicalLeaveList();
    if (CollectionUtils.isEmpty(medicalLeaveList)) {
      return null;
    }

    Optional<MedicalLeave> optionalMedicalLeave =
        medicalLeaveList.stream()
            .filter(
                medicalLeave ->
                    medicalLeave.getStatusSelect() == MedicalLeaveRepository.STATUS_REALIZED)
            .max(Comparator.comparing(MedicalLeave::getMedicalLeaveDate));

    return optionalMedicalLeave.map(MedicalLeave::getMedicalLeaveDate).orElse(null);
  }

  @Override
  public LocalDate getNextMedicalLeaveDate(Employee employee) {
    LocalDate todayDate = appBaseService.getTodayDate(null);
    List<MedicalLeave> medicalLeaveList = employee.getMedicalLeaveList();
    if (CollectionUtils.isEmpty(medicalLeaveList)) {
      return null;
    }

    Optional<MedicalLeave> optionalMedicalLeave =
        medicalLeaveList.stream()
            .filter(
                medicalLeave ->
                    medicalLeave.getStatusSelect() == MedicalLeaveRepository.STATUS_PLANNED
                        && medicalLeave.getMedicalLeaveDate().isAfter(todayDate))
            .min(Comparator.comparing(MedicalLeave::getMedicalLeaveDate));

    return optionalMedicalLeave.map(MedicalLeave::getMedicalLeaveDate).orElse(null);
  }

  @Transactional
  @Override
  public List<EmployeeFile> addToEmployeeFiles(Employee employee) {
    List<EmployeeFile> employeeFileList = employee.getEmployeeFileList();
    MedicalLeave medicalLeave = Iterables.getLast(employee.getMedicalLeaveList());
    MetaFile metaFile = medicalLeave.getMedicalLeaveFile();
    if (metaFile != null) {
      if (employee.getEmployeeFileList().stream()
          .anyMatch(
              employeeFile ->
                  employeeFile.getMetaFile().getFileName().equals(metaFile.getFileName()))) {
        return employeeFileList;
      }
      employee.addEmployeeFileListItem(getEmployeeFile(medicalLeave, metaFile));
    }
    return employee.getEmployeeFileList();
  }

  protected EmployeeFile getEmployeeFile(MedicalLeave medicalLeave, MetaFile metaFile) {
    EmployeeFile employeeFile = new EmployeeFile();
    employeeFile.setMetaFile(metaFile);
    employeeFile.setFileDescription(medicalLeave.getLeaveReason());
    employeeFile.setRecordDate(appBaseService.getTodayDate(null));
    return employeeFile;
  }
}
