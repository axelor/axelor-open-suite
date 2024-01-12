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
package com.axelor.apps.hr.service.batch;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.ExceptionOriginRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.HrBatch;
import com.axelor.apps.hr.db.LeaveLine;
import com.axelor.apps.hr.db.LeaveManagement;
import com.axelor.apps.hr.db.LeaveReason;
import com.axelor.apps.hr.db.repo.EmployeeHRRepository;
import com.axelor.apps.hr.db.repo.LeaveLineRepository;
import com.axelor.apps.hr.db.repo.LeaveManagementRepository;
import com.axelor.apps.hr.db.repo.LeaveReasonRepository;
import com.axelor.apps.hr.service.employee.EmployeeFetchService;
import com.axelor.apps.hr.service.employee.EmployeeService;
import com.axelor.apps.hr.service.leave.LeaveLineService;
import com.axelor.apps.hr.service.leave.management.LeaveManagementService;
import com.axelor.db.JPA;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class BatchLeaveManagementReset extends BatchLeaveManagement {

  @Inject
  public BatchLeaveManagementReset(
      LeaveManagementService leaveManagementService,
      LeaveLineRepository leaveLineRepository,
      LeaveManagementRepository leaveManagementRepository,
      EmployeeService employeeService,
      LeaveLineService leaveLineService,
      EmployeeFetchService employeeFetchService,
      LeaveReasonRepository leaveReasonRepository) {
    super(
        leaveManagementService,
        leaveLineRepository,
        leaveManagementRepository,
        employeeService,
        leaveLineService,
        employeeFetchService,
        leaveReasonRepository);
  }

  @Override
  protected void process() {
    List<Employee> employeeList = employeeFetchService.getEmployees(batch.getHrBatch());
    resetLeaveManagementLines(employeeList);
  }

  public void resetLeaveManagementLines(List<Employee> employeeList) {
    HrBatch hrBatch = batch.getHrBatch();
    List<LeaveReason> selectedLeaveReasons = new ArrayList<>(hrBatch.getLeaveReasonSet());
    List<LeaveReason> noRecoveryleaveReasonList =
        getLeaveReasonWithNoRecovery(selectedLeaveReasons);
    List<LeaveReason> recoveryleaveReasonList = getLeaveReasonWithRecovery(selectedLeaveReasons);

    for (Employee employee :
        employeeList.stream().filter(Objects::nonNull).collect(Collectors.toList())) {
      employee = employeeRepository.find(employee.getId());
      if (EmployeeHRRepository.isEmployeeFormerNewOrArchived(employee)) {
        continue;
      }
      try {
        resetLeaveManagement(employee, noRecoveryleaveReasonList, recoveryleaveReasonList);
      } catch (AxelorException e) {
        TraceBackService.trace(e, ExceptionOriginRepository.LEAVE_MANAGEMENT, batch.getId());
        incrementAnomaly();
        if (e.getCategory() == TraceBackRepository.CATEGORY_NO_VALUE) {
          noValueAnomaly++;
        }
        if (e.getCategory() == TraceBackRepository.CATEGORY_CONFIGURATION_ERROR) {
          confAnomaly++;
        }
      } finally {
        total++;
        JPA.clear();
      }
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  public void resetLeaveManagement(
      Employee employee,
      List<LeaveReason> leaveReasonWithNoRecoveryList,
      List<LeaveReason> leaveReasonWithRecoveryList)
      throws AxelorException {
    HrBatch hrBatch = batch.getHrBatch();
    createRecoveryLeaveLines(employee, leaveReasonWithNoRecoveryList, hrBatch);
    createRecoveryLeaveLines(employee, leaveReasonWithRecoveryList, hrBatch);
  }

  protected void createRecoveryLeaveLines(
      Employee employee, List<LeaveReason> leaveReasonList, HrBatch hrBatch)
      throws AxelorException {
    for (LeaveReason leaveReason : leaveReasonList) {
      for (LeaveLine leaveLine : employee.getLeaveLineList()) {
        if (leaveReason.equals(leaveLine.getLeaveReason())) {
          createNextLeaveManagement(employee, leaveLine, leaveReason, hrBatch.getComments());
          leaveManagementService.reset(
              leaveLine,
              employeeService.getUser(employee),
              hrBatch.getComments(),
              null,
              hrBatch.getStartDate(),
              hrBatch.getEndDate());
        }
      }
    }
  }

  protected List<LeaveReason> getLeaveReasonWithNoRecovery(
      List<LeaveReason> selectedLeaveReasonList) {
    if (CollectionUtils.isNotEmpty(selectedLeaveReasonList)) {
      return selectedLeaveReasonList.stream()
          .filter(reason -> reason.getRecoveryLeaveReason() == null)
          .collect(Collectors.toList());
    }

    return leaveReasonRepository
        .all()
        .filter(
            "self.leaveReasonTypeSelect != 2 AND self.isToBeResetYearly = true AND self.recoveryLeaveReason IS NULL")
        .fetch();
  }

  protected List<LeaveReason> getLeaveReasonWithRecovery(
      List<LeaveReason> selectedLeaveReasonList) {
    if (CollectionUtils.isNotEmpty(selectedLeaveReasonList)) {
      return selectedLeaveReasonList.stream()
          .filter(reason -> reason.getRecoveryLeaveReason() != null)
          .collect(Collectors.toList());
    }

    return leaveReasonRepository
        .all()
        .filter(
            "self.leaveReasonTypeSelect != 2 AND self.isToBeResetYearly = true AND self.recoveryLeaveReason IS NOT NULL")
        .fetch();
  }

  protected void createNextLeaveManagement(
      Employee employee, LeaveLine leaveLine, LeaveReason leaveReason, String comment)
      throws AxelorException {
    LeaveReason recoveryLeaveReason = leaveReason.getRecoveryLeaveReason();
    recoveryLeaveReason = leaveReasonRepository.find(recoveryLeaveReason.getId());
    BigDecimal qty = leaveLine.getQuantity();
    if (recoveryLeaveReason != null) {
      LeaveLine nextLeaveLine =
          leaveLineService.addLeaveReasonOrCreateIt(employee, recoveryLeaveReason);
      if (qty.signum() != 0) {
        LeaveManagement nextLeaveManagement =
            leaveManagementService.createLeaveManagement(
                nextLeaveLine, employeeService.getUser(employee), comment, null, null, null, qty);
        nextLeaveLine.addLeaveManagementListItem(nextLeaveManagement);
        leaveManagementService.computeQuantityAvailable(nextLeaveLine);
      }
    }
  }
}
