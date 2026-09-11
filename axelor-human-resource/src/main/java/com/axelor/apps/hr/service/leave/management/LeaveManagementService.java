/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.hr.service.leave.management;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.hr.db.LeaveLine;
import com.axelor.apps.hr.db.LeaveManagement;
import com.axelor.auth.db.User;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class LeaveManagementService {

  @Inject protected AppBaseService appBaseService;

  public LeaveLine computeQuantityAvailable(LeaveLine leaveLine) {

    leaveLine.setTotalQuantity(BigDecimal.ZERO);
    leaveLine.setQuantity(BigDecimal.ZERO);

    List<LeaveManagement> leaveManagementList = leaveLine.getLeaveManagementList();
    if (CollectionUtils.isEmpty(leaveManagementList)) {
      return leaveLine;
    }

    leaveLine.setTotalQuantity(
        leaveManagementList.stream()
            .map(LeaveManagement::getValue)
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO));
    leaveLine.setQuantity(leaveLine.getTotalQuantity().subtract(leaveLine.getDaysValidated()));
    return leaveLine;
  }

  @Transactional
  public LeaveManagement createLeaveManagement(
      LeaveLine leaveLine,
      User user,
      String comments,
      LocalDate date,
      LocalDate fromDate,
      LocalDate toDate,
      BigDecimal value) {

    LeaveManagement leaveManagement =
        createLeaveManagement(user, comments, date, fromDate, toDate, value);
    leaveManagement.setLeaveLine(leaveLine);

    return leaveManagement;
  }

  @Transactional
  public LeaveManagement createLeaveManagement(
      User user,
      String comments,
      LocalDate date,
      LocalDate fromDate,
      LocalDate toDate,
      BigDecimal value) {

    LeaveManagement leaveManagement = new LeaveManagement();

    leaveManagement.setComments(comments);
    if (date == null) {
      leaveManagement.setDate(
          appBaseService.getTodayDate(
              Optional.ofNullable(user).map(User::getActiveCompany).orElse(null)));
    } else {
      leaveManagement.setDate(date);
    }
    leaveManagement.setFromDate(fromDate);
    leaveManagement.setToDate(toDate);
    leaveManagement.setValue(value.setScale(4, RoundingMode.HALF_UP));

    return leaveManagement;
  }

  /**
   * Reset leave management list by adding a new leave management line with negative quantity.
   *
   * @param leaveLine
   * @param comments
   * @param date
   * @param fromDate
   * @param toDate
   */
  @Transactional
  public void reset(
      LeaveLine leaveLine,
      User user,
      String comments,
      LocalDate date,
      LocalDate fromDate,
      LocalDate toDate) {
    LeaveManagement leaveManagement =
        createLeaveManagement(
            leaveLine, user, comments, date, fromDate, toDate, leaveLine.getQuantity().negate());
    leaveLine.addLeaveManagementListItem(leaveManagement);
    leaveLine.setQuantity(BigDecimal.ZERO);
    leaveLine.setTotalQuantity(BigDecimal.ZERO);
  }

  /**
   * Compute the quantity available on the leave line at the given date. Leave managements whose
   * validity period ("To (included)") has already ended at that date are considered expired and do
   * not contribute. Validated days and negative leave managements are deducted from the oldest
   * grants first.
   *
   * @param leaveLine
   * @param date the date at which the balance is evaluated
   * @return the quantity available at the given date
   */
  public BigDecimal computeQuantityAvailable(LeaveLine leaveLine, LocalDate date) {
    List<LeaveManagement> leaveManagementList = leaveLine.getLeaveManagementList();
    if (CollectionUtils.isEmpty(leaveManagementList)) {
      return BigDecimal.ZERO;
    }

    BigDecimal quantityToDeduct =
        leaveManagementList.stream()
            .map(LeaveManagement::getValue)
            .filter(value -> value.signum() < 0)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .negate()
            .add(leaveLine.getDaysValidated());

    List<LeaveManagement> grantList =
        leaveManagementList.stream()
            .filter(leaveManagement -> leaveManagement.getValue().signum() > 0)
            .sorted(
                Comparator.comparing(
                        LeaveManagement::getToDate, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(
                        LeaveManagement::getDate, Comparator.nullsFirst(Comparator.naturalOrder())))
            .collect(Collectors.toList());

    BigDecimal quantityAvailable = BigDecimal.ZERO;
    for (LeaveManagement grant : grantList) {
      BigDecimal remainingValue = grant.getValue();
      if (quantityToDeduct.signum() > 0) {
        BigDecimal deductedValue = remainingValue.min(quantityToDeduct);
        remainingValue = remainingValue.subtract(deductedValue);
        quantityToDeduct = quantityToDeduct.subtract(deductedValue);
      }
      if (isActive(grant, date)) {
        quantityAvailable = quantityAvailable.add(remainingValue);
      }
    }
    return quantityAvailable;
  }

  protected boolean isActive(LeaveManagement leaveManagement, LocalDate date) {
    return leaveManagement.getToDate() == null
        || date == null
        || !leaveManagement.getToDate().isBefore(date);
  }

  /**
   * Check whether the leave line holds at least one grant still active at the given date.
   *
   * @param leaveLine
   * @param date the date at which the balance is evaluated
   * @return true if at least one positive leave management is active at the given date
   */
  public boolean hasActiveLeaveManagement(LeaveLine leaveLine, LocalDate date) {
    List<LeaveManagement> leaveManagementList = leaveLine.getLeaveManagementList();
    return CollectionUtils.isNotEmpty(leaveManagementList)
        && leaveManagementList.stream()
            .filter(leaveManagement -> leaveManagement.getValue().signum() > 0)
            .anyMatch(leaveManagement -> isActive(leaveManagement, date));
  }
}
