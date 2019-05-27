/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.hr.service.batch;

import com.axelor.apps.base.db.WeeklyPlanning;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.HrBatch;
import com.axelor.apps.hr.db.LeaveLine;
import com.axelor.apps.hr.db.LeaveManagement;
import com.axelor.apps.hr.db.LeaveReason;
import com.axelor.apps.hr.db.repo.LeaveLineRepository;
import com.axelor.apps.hr.db.repo.LeaveManagementRepository;
import com.axelor.apps.hr.exception.IExceptionMessage;
import com.axelor.apps.hr.service.leave.LeaveService;
import com.axelor.apps.hr.service.leave.management.LeaveManagementService;
import com.axelor.auth.AuthUtils;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.IException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.beust.jcommander.internal.Lists;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import javax.validation.constraints.Digits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatchLeaveManagement extends BatchStrategy {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  int total;
  int noValueAnomaly;
  int confAnomaly;

  protected LeaveLineRepository leaveLineRepository;
  protected LeaveManagementRepository leaveManagementRepository;

  @Inject private Provider<LeaveService> leaveServiceProvider;

  @Inject
  public BatchLeaveManagement(
      LeaveManagementService leaveManagementService,
      LeaveLineRepository leaveLineRepository,
      LeaveManagementRepository leaveManagementRepository) {

    super(leaveManagementService);
    this.leaveLineRepository = leaveLineRepository;
    this.leaveManagementRepository = leaveManagementRepository;
  }

  @Override
  protected void start() throws IllegalAccessException, AxelorException {

    super.start();

    if (batch.getHrBatch().getDayNumber() == null
        || batch.getHrBatch().getDayNumber().compareTo(BigDecimal.ZERO) == 0
        || batch.getHrBatch().getLeaveReason() == null) {
      TraceBackService.trace(
          new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(IExceptionMessage.BATCH_MISSING_FIELD)),
          IException.LEAVE_MANAGEMENT,
          batch.getId());
    }
    total = 0;
    noValueAnomaly = 0;
    confAnomaly = 0;
    checkPoint();
  }

  @Override
  protected void process() {

    List<Employee> employeeList = this.getEmployees(batch.getHrBatch());
    generateLeaveManagementLines(employeeList);
  }

  public List<Employee> getEmployees(HrBatch hrBatch) {

    List<String> query = Lists.newArrayList();

    if (hrBatch.getEmployeeSet() != null && !hrBatch.getEmployeeSet().isEmpty()) {
      String employeeIds =
          Joiner.on(',')
              .join(
                  Iterables.transform(
                      hrBatch.getEmployeeSet(),
                      new Function<Employee, String>() {
                        @Override
                        public String apply(Employee obj) {
                          return obj.getId().toString();
                        }
                      }));
      query.add("self.id IN (" + employeeIds + ")");
    }
    if (hrBatch.getEmployeeSet() != null && !hrBatch.getPlanningSet().isEmpty()) {
      String planningIds =
          Joiner.on(',')
              .join(
                  Iterables.transform(
                      hrBatch.getPlanningSet(),
                      new Function<WeeklyPlanning, String>() {
                        @Override
                        public String apply(WeeklyPlanning obj) {
                          return obj.getId().toString();
                        }
                      }));

      query.add("self.weeklyPlanning.id IN (" + planningIds + ")");
    }

    List<Employee> employeeList;
    String liaison = query.isEmpty() ? "" : " AND";
    if (hrBatch.getCompany() != null) {
      employeeList =
          JPA.all(Employee.class)
              .filter(
                  Joiner.on(" AND ").join(query)
                      + liaison
                      + " self.mainEmploymentContract.payCompany = :company")
              .bind("company", hrBatch.getCompany())
              .fetch();
    } else {
      employeeList = JPA.all(Employee.class).filter(Joiner.on(" AND ").join(query)).fetch();
    }

    return employeeList;
  }

  public void generateLeaveManagementLines(List<Employee> employeeList) {

    for (Employee employee : employeeList) {

      try {
        createLeaveManagement(employeeRepository.find(employee.getId()));
      } catch (AxelorException e) {
        TraceBackService.trace(e, IException.LEAVE_MANAGEMENT, batch.getId());
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

  @Transactional
  public void createLeaveManagement(Employee employee) throws AxelorException {

    batch = batchRepo.find(batch.getId());
    LeaveLine leaveLine = null;
    LeaveReason leaveReason = batch.getHrBatch().getLeaveReason();

    if (employee != null) {
      leaveLine = leaveServiceProvider.get().addLeaveReasonOrCreateIt(employee, leaveReason);

      BigDecimal dayNumber =
          batch.getHrBatch().getUseWeeklyPlanningCoef()
              ? batch
                  .getHrBatch()
                  .getDayNumber()
                  .multiply(employee.getWeeklyPlanning().getLeaveCoef())
              : batch.getHrBatch().getDayNumber();
      dayNumber =
          dayNumber.subtract(
              new BigDecimal(
                  publicHolidayService.getImposedDayNumber(
                      employee,
                      batch.getHrBatch().getStartDate(),
                      batch.getHrBatch().getEndDate())));
      LeaveManagement leaveManagement =
          leaveManagementService.createLeaveManagement(
              leaveLine,
              AuthUtils.getUser(),
              batch.getHrBatch().getComments(),
              null,
              batch.getHrBatch().getStartDate(),
              batch.getHrBatch().getEndDate(),
              dayNumber);
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
              I18n.get(IExceptionMessage.BATCH_LEAVE_MANAGEMENT_QTY_OUT_OF_BOUNDS),
              limit.longValue());
        }

      } catch (NoSuchFieldException | SecurityException e) {
        throw new AxelorException(e, TraceBackRepository.CATEGORY_CONFIGURATION_ERROR);
      }

      leaveLine.setQuantity(qty.setScale(4, RoundingMode.HALF_EVEN));
      leaveLine.setTotalQuantity(totalQty.setScale(4, RoundingMode.HALF_EVEN));

      leaveManagementRepository.save(leaveManagement);
      leaveLineRepository.save(leaveLine);
      updateEmployee(employee);
    }
  }

  @Override
  protected void stop() {

    String comment =
        String.format(I18n.get(IExceptionMessage.BATCH_LEAVE_MANAGEMENT_ENDING_0), total) + '\n';

    comment +=
        String.format(I18n.get(IExceptionMessage.BATCH_LEAVE_MANAGEMENT_ENDING_1), batch.getDone())
            + '\n';

    if (confAnomaly > 0) {
      comment +=
          String.format(I18n.get(IExceptionMessage.BATCH_LEAVE_MANAGEMENT_ENDING_2), confAnomaly)
              + '\n';
    }
    if (noValueAnomaly > 0) {
      comment +=
          String.format(I18n.get(IExceptionMessage.BATCH_LEAVE_MANAGEMENT_ENDING_3), noValueAnomaly)
              + '\n';
    }

    addComment(comment);
    super.stop();
  }
}
