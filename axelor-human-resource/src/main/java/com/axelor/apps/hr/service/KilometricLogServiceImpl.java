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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Year;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.db.repo.YearRepository;
import com.axelor.apps.base.service.YearService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.ExpenseLine;
import com.axelor.apps.hr.db.KilometricLog;
import com.axelor.apps.hr.db.repo.ExpenseRepository;
import com.axelor.apps.hr.db.repo.KilometricLogRepository;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.i18n.I18n;
import com.axelor.utils.helpers.date.LocalDateHelper;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class KilometricLogServiceImpl implements KilometricLogService {

  protected final KilometricLogRepository kilometricLogRepository;
  protected final YearService yearService;

  @Inject
  public KilometricLogServiceImpl(
      KilometricLogRepository kilometricLogRepository, YearService yearService) {
    this.kilometricLogRepository = kilometricLogRepository;
    this.yearService = yearService;
  }

  @Transactional(rollbackOn = {Exception.class})
  public void updateKilometricLog(ExpenseLine expenseLine, Employee employee)
      throws AxelorException {
    KilometricLog log = getOrCreateKilometricLog(employee, expenseLine.getExpenseDate());
    if (log.getExpenseLineList() == null || !log.getExpenseLineList().contains(expenseLine)) {
      log.addExpenseLineListItem(expenseLine);
    }
    computeKilometricLogDistance(log);
    kilometricLogRepository.save(log);
  }

  protected void computeKilometricLogDistance(KilometricLog kilometricLog) {
    List<ExpenseLine> expenseLineList = kilometricLog.getExpenseLineList();
    kilometricLog.setDistanceTravelled(
        expenseLineList.stream()
            .filter(
                line -> line.getExpense().getStatusSelect() == ExpenseRepository.STATUS_VALIDATED)
            .map(ExpenseLine::getDistance)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
  }

  protected KilometricLog getOrCreateKilometricLog(Employee employee, LocalDate date)
      throws AxelorException {

    KilometricLog log = getKilometricLog(employee, date);

    if (log != null) {
      return log;
    }
    if (employee.getMainEmploymentContract() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(HumanResourceExceptionMessage.EMPLOYEE_CONTRACT_OF_EMPLOYMENT),
          employee.getName());
    }

    Year year =
        yearService.getYear(
            date, employee.getMainEmploymentContract().getPayCompany(), YearRepository.TYPE_CIVIL);

    if (year == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(HumanResourceExceptionMessage.KILOMETRIC_LOG_NO_CIVIL_YEAR),
          employee.getMainEmploymentContract().getPayCompany(),
          date);
    }

    return createKilometricLog(employee, new BigDecimal("0.00"), year);
  }

  protected KilometricLog createKilometricLog(Employee employee, BigDecimal distance, Year year) {

    KilometricLog log = new KilometricLog();
    log.setDistanceTravelled(distance);
    log.setYear(year);
    employee.addKilometricLogListItem(log);
    return log;
  }

  @Override
  public KilometricLog getKilometricLog(Employee employee, LocalDate refDate) {
    return employee.getKilometricLogList().stream()
        .filter(
            log ->
                LocalDateHelper.isBetween(
                    log.getYear().getFromDate(), log.getYear().getToDate(), refDate))
        .findFirst()
        .orElse(null);
  }

  @Override
  public List<Long> getExpenseLineIdList(KilometricLog kilometricLog) {
    return kilometricLog.getExpenseLineList().stream()
        .filter(line -> line.getExpense().getStatusSelect() != ExpenseRepository.STATUS_CANCELED)
        .map(ExpenseLine::getId)
        .collect(Collectors.toList());
  }
}
