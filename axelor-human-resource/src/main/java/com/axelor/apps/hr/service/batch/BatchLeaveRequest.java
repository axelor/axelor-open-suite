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
package com.axelor.apps.hr.service.batch;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.ExceptionOriginRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.HrBatch;
import com.axelor.apps.hr.db.LeaveRequest;
import com.axelor.apps.hr.db.repo.EmployeeHRRepository;
import com.axelor.apps.hr.db.repo.LeaveRequestRepository;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.apps.hr.service.employee.EmployeeFetchService;
import com.axelor.apps.hr.service.employee.EmployeeService;
import com.axelor.apps.hr.service.leave.LeaveRequestCreateService;
import com.axelor.apps.hr.service.leave.LeaveRequestMailService;
import com.axelor.apps.hr.service.leave.LeaveRequestSendService;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import java.util.List;

public class BatchLeaveRequest extends BatchStrategy {

  protected EmployeeService employeeService;
  protected EmployeeFetchService employeeFetchService;
  protected LeaveRequestCreateService leaveRequestCreateService;
  protected LeaveRequestRepository leaveRequestRepository;
  protected LeaveRequestSendService leaveRequestSendService;
  protected LeaveRequestMailService leaveRequestMailService;

  @Inject
  public BatchLeaveRequest(
      EmployeeService employeeService,
      EmployeeFetchService employeeFetchService,
      LeaveRequestCreateService leaveRequestCreateService,
      LeaveRequestRepository leaveRequestRepository,
      LeaveRequestSendService leaveRequestSendService,
      LeaveRequestMailService leaveRequestMailService) {
    this.employeeService = employeeService;
    this.employeeFetchService = employeeFetchService;
    this.leaveRequestCreateService = leaveRequestCreateService;
    this.leaveRequestRepository = leaveRequestRepository;
    this.leaveRequestSendService = leaveRequestSendService;
    this.leaveRequestMailService = leaveRequestMailService;
  }

  @Override
  protected void start() throws IllegalAccessException {

    super.start();

    HrBatch hrBatch = batch.getHrBatch();

    if (hrBatch.getLeaveReason() == null) {
      TraceBackService.trace(
          new AxelorException(
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(HumanResourceExceptionMessage.BATCH_MISSING_FIELD)),
          ExceptionOriginRepository.LEAVE_REQUEST,
          batch.getId());
    }
    checkPoint();
  }

  @Override
  protected void process() {
    batch.getHrBatch().setPlanningSet(null);
    List<Employee> employeeList = employeeFetchService.getEmployees(batch.getHrBatch());
    generateLeaveRequest(employeeList);
  }

  public void generateLeaveRequest(List<Employee> employeeList) {

    for (Employee employee : employeeList) {
      employee = employeeRepository.find(employee.getId());

      try {
        createLeaveRequest(employee);
      } catch (AxelorException e) {
        TraceBackService.trace(e, ExceptionOriginRepository.LEAVE_REQUEST, batch.getId());
        incrementAnomaly();
      } finally {
        JPA.clear();
      }
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  public void createLeaveRequest(Employee employee) throws AxelorException {
    if (employee == null || EmployeeHRRepository.isEmployeeFormerNewOrArchived(employee)) {
      return;
    }

    batch = batchRepo.find(batch.getId());
    HrBatch hrBatch = batch.getHrBatch();

    LeaveRequest leaveRequest =
        leaveRequestCreateService.createLeaveRequest(
            hrBatch.getStartDate().atTime(0, 0, 0),
            hrBatch.getEndDate().atTime(0, 0, 0),
            hrBatch.getStartOnSelect(),
            hrBatch.getEndOnSelect(),
            null,
            hrBatch.getLeaveReason(),
            employee.getUser(),
            employee);

    if (leaveRequest.getDuration().signum() == 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(HumanResourceExceptionMessage.LEAVE_REQUEST_WRONG_DURATION));
    }

    leaveRequest.addBatchSetItem(batch);
    leaveRequestRepository.save(leaveRequest);
    leaveRequestSendService.send(leaveRequest);
    leaveRequestMailService.sendCancellationEmail(leaveRequest);
    incrementDone();
  }

  @Override
  protected void stop() {
    String comment =
        String.format(
                I18n.get(HumanResourceExceptionMessage.BATCH_LEAVE_REQUEST_ENDING_1),
                batch.getDone())
            + '\n';

    comment +=
        String.format(
                I18n.get(HumanResourceExceptionMessage.BATCH_LEAVE_REQUEST_ENDING_2),
                batch.getAnomaly())
            + '\n';

    addComment(comment);
    super.stop();
  }
}
