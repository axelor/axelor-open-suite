/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
import com.axelor.apps.base.db.repo.BatchRepository;
import com.axelor.apps.base.service.administration.AbstractBatch;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.LeaveReason;
import com.axelor.apps.hr.db.repo.EmployeeRepository;
import com.axelor.apps.hr.db.repo.LeaveReasonRepository;
import com.axelor.apps.hr.service.leave.IncrementLeaveService;
import com.axelor.apps.hr.translation.ITranslation;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.i18n.I18n;
import com.axelor.utils.helpers.StringHelper;
import com.google.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class BatchIncrementLeave extends AbstractBatch {

  protected LeaveReasonRepository leaveReasonRepository;
  protected EmployeeRepository employeeRepository;
  protected IncrementLeaveService incrementLeaveService;

  @Inject
  public BatchIncrementLeave(
      LeaveReasonRepository leaveReasonRepository,
      EmployeeRepository employeeRepository,
      IncrementLeaveService incrementLeaveService) {
    this.leaveReasonRepository = leaveReasonRepository;
    this.employeeRepository = employeeRepository;
    this.incrementLeaveService = incrementLeaveService;
  }

  @Override
  protected void process() {
    List<Long> leaveReasonList =
        leaveReasonRepository.all()
            .filter(
                "self.isAutoIncrement = true AND self.leaveReasonTypeSelect = :typeSelect AND self.leaveReasonTypeSelect != :exceptionalSelect")
            .bind("typeSelect", batch.getHrBatch().getLeaveReasonTypeSelect())
            .bind("exceptionalSelect", LeaveReasonRepository.TYPE_SELECT_EXCEPTIONAL_DAYS).fetch()
            .stream()
            .map(LeaveReason::getId)
            .collect(Collectors.toList());

    for (Long id : leaveReasonList) {
      try {
        incrementLeaveForEmployees(id);
        incrementDone();
      } catch (Exception e) {
        incrementAnomaly();
        TraceBackService.trace(e, "Increment leave batch", batch.getId());
      }
    }
  }

  protected void incrementLeaveForEmployees(Long id) throws AxelorException {
    List<Employee> employeeList;
    int offset = 0;
    LeaveReason leaveReason = leaveReasonRepository.find(id);
    Query<Employee> query = getEmployeeQuery(leaveReason);
    while (!(employeeList = query.fetch(FETCH_LIMIT, offset)).isEmpty()) {
      for (Employee employee : employeeList) {
        ++offset;
        employee = employeeRepository.find(employee.getId());
        incrementLeaveService.updateEmployeeLeaveLines(leaveReason, employee);
      }
    }
    JPA.clear();
  }

  protected Query<Employee> getEmployeeQuery(LeaveReason leaveReason) {
    Query<Employee> query = employeeRepository.all();
    if (CollectionUtils.isEmpty(leaveReason.getPlanningSet())) {
      return query;
    }

    String filter =
        "self.weeklyPlanning in ("
            + StringHelper.getIdListString(leaveReason.getPlanningSet())
            + ")";
    query.filter(filter);

    return query;
  }

  @Override
  protected void setBatchTypeSelect() {
    this.batch.setBatchTypeSelect(BatchRepository.BATCH_TYPE_HR_BATCH);
  }

  @Override
  protected void stop() {
    super.stop();
    addComment(
        String.format(
            I18n.get(ITranslation.INCREMENT_LEAVE_REASON_BATCH_EXECUTION_RESULT),
            batch.getDone(),
            batch.getAnomaly()));
  }
}
