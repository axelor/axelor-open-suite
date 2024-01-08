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
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.apps.hr.service.employee.EmployeeFetchService;
import com.axelor.apps.hr.service.employee.EmployeeService;
import com.axelor.apps.hr.service.leave.LeaveLineService;
import com.axelor.apps.hr.service.leave.management.LeaveManagementService;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.validation.constraints.Digits;
import org.apache.commons.collections.CollectionUtils;

public class BatchLeaveManagement extends BatchStrategy {

  int total;
  int noValueAnomaly;
  int confAnomaly;

  protected LeaveLineRepository leaveLineRepository;
  protected LeaveManagementRepository leaveManagementRepository;
  protected EmployeeService employeeService;
  protected LeaveLineService leaveLineService;
  protected EmployeeFetchService employeeFetchService;
  protected LeaveReasonRepository leaveReasonRepository;

  @Inject
  public BatchLeaveManagement(
      LeaveManagementService leaveManagementService,
      LeaveLineRepository leaveLineRepository,
      LeaveManagementRepository leaveManagementRepository,
      EmployeeService employeeService,
      LeaveLineService leaveLineService,
      EmployeeFetchService employeeFetchService,
      LeaveReasonRepository leaveReasonRepository) {

    super(leaveManagementService);
    this.leaveLineRepository = leaveLineRepository;
    this.leaveManagementRepository = leaveManagementRepository;
    this.employeeService = employeeService;
    this.leaveLineService = leaveLineService;
    this.employeeFetchService = employeeFetchService;
    this.leaveReasonRepository = leaveReasonRepository;
  }

  @Override
  protected void start() throws IllegalAccessException {

    super.start();

    HrBatch hrBatch = batch.getHrBatch();

    if (hrBatch.getDayNumber() == null
        || hrBatch.getDayNumber().signum() == 0
        || (hrBatch.getLeaveReasonTypeSelect() == null
            && CollectionUtils.isEmpty(hrBatch.getLeaveReasonSet()))) {
      TraceBackService.trace(
          new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(HumanResourceExceptionMessage.BATCH_MISSING_FIELD)),
          ExceptionOriginRepository.LEAVE_MANAGEMENT,
          batch.getId());
    }
    total = 0;
    noValueAnomaly = 0;
    confAnomaly = 0;
    checkPoint();
  }

  @Override
  protected void process() {

    List<Employee> employeeList = employeeFetchService.getEmployees(batch.getHrBatch());
    generateLeaveManagementLines(employeeList);
  }

  public void generateLeaveManagementLines(List<Employee> employeeList) {

    for (Employee employee : employeeList) {
      employee = employeeRepository.find(employee.getId());

      try {
        createLeaveManagement(employee);
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
  public void createLeaveManagement(Employee employee) throws AxelorException {
    if (employee == null || EmployeeHRRepository.isEmployeeFormerNewOrArchived(employee)) {
      return;
    }

    batch = batchRepo.find(batch.getId());
    HrBatch hrBatch = batch.getHrBatch();

    for (LeaveReason leaveReason : getLeaveReasons(hrBatch)) {
      LeaveLine leaveLine = leaveLineService.addLeaveReasonOrCreateIt(employee, leaveReason);

      BigDecimal dayNumber = getDayNumber(employee, hrBatch);
      LeaveManagement leaveManagement =
          leaveManagementService.createLeaveManagement(
              leaveLine,
              employeeService.getUser(employee),
              hrBatch.getComments(),
              null,
              hrBatch.getStartDate(),
              hrBatch.getEndDate(),
              dayNumber);

      updateLeaveLine(employee, leaveLine, dayNumber);
      leaveManagementRepository.save(leaveManagement);
      leaveLineRepository.save(leaveLine);
      updateEmployee(employee);
    }
  }

  protected List<LeaveReason> getLeaveReasons(HrBatch hrBatch) {
    List<LeaveReason> leaveReasonList = new ArrayList<>();
    Set<LeaveReason> leaveReasonSet = hrBatch.getLeaveReasonSet();
    if (CollectionUtils.isNotEmpty(leaveReasonSet)) {
      leaveReasonList.addAll(leaveReasonSet);
    } else if (hrBatch.getLeaveReasonTypeSelect() != null) {
      leaveReasonList.addAll(
          leaveReasonRepository
              .all()
              .filter("self.leaveReasonTypeSelect = :typeSelect AND self.isActive = true")
              .bind("typeSelect", hrBatch.getLeaveReasonTypeSelect())
              .fetch());
    }
    return leaveReasonList;
  }

  protected void updateLeaveLine(Employee employee, LeaveLine leaveLine, BigDecimal dayNumber)
      throws AxelorException {
    BigDecimal qty = leaveLine.getQuantity().add(dayNumber);
    BigDecimal totalQty = leaveLine.getTotalQuantity().add(dayNumber);

    try {
      int integer =
          LeaveLine.class.getDeclaredField("quantity").getAnnotation(Digits.class).integer();
      BigDecimal limit = new BigDecimal((long) Math.pow(10, integer));
      if (qty.compareTo(limit) >= 0 || totalQty.compareTo(limit) >= 0) {
        throw new AxelorException(
            employee,
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(HumanResourceExceptionMessage.BATCH_LEAVE_MANAGEMENT_QTY_OUT_OF_BOUNDS),
            limit.longValue());
      }

    } catch (NoSuchFieldException | SecurityException e) {
      throw new AxelorException(e, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR);
    }

    leaveLine.setQuantity(qty.setScale(4, RoundingMode.HALF_UP));
    leaveLine.setTotalQuantity(totalQty.setScale(4, RoundingMode.HALF_UP));
  }

  protected BigDecimal getDayNumber(Employee employee, HrBatch hrBatch) {
    BigDecimal dayNumberToAdd = hrBatch.getDayNumber();
    BigDecimal dayNumber =
        hrBatch.getUseWeeklyPlanningCoef()
            ? dayNumberToAdd.multiply(employee.getWeeklyPlanning().getLeaveCoef())
            : dayNumberToAdd;
    dayNumber =
        dayNumber.subtract(
            new BigDecimal(
                publicHolidayService.getImposedDayNumber(
                    employee, hrBatch.getStartDate(), hrBatch.getEndDate())));
    return dayNumber;
  }

  @Override
  protected void stop() {

    String comment =
        String.format(
                I18n.get(HumanResourceExceptionMessage.BATCH_LEAVE_MANAGEMENT_ENDING_0), total)
            + '\n';

    comment +=
        String.format(
                I18n.get(HumanResourceExceptionMessage.BATCH_LEAVE_MANAGEMENT_ENDING_1),
                batch.getDone())
            + '\n';

    if (confAnomaly > 0) {
      comment +=
          String.format(
                  I18n.get(HumanResourceExceptionMessage.BATCH_LEAVE_MANAGEMENT_ENDING_2),
                  confAnomaly)
              + '\n';
    }
    if (noValueAnomaly > 0) {
      comment +=
          String.format(
                  I18n.get(HumanResourceExceptionMessage.BATCH_LEAVE_MANAGEMENT_ENDING_3),
                  noValueAnomaly)
              + '\n';
    }

    addComment(comment);
    super.stop();
  }
}
