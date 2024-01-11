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
package com.axelor.apps.hr.service.leave;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.WeeklyPlanning;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.LeaveLine;
import com.axelor.apps.hr.db.LeaveReason;
import com.axelor.apps.hr.db.LeaveRequest;
import com.axelor.auth.db.User;
import com.axelor.message.db.Message;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import wslite.json.JSONException;

public interface LeaveService {

  public BigDecimal computeDuration(LeaveRequest leave) throws AxelorException;

  public BigDecimal computeDuration(LeaveRequest leave, LocalDate fromDate, LocalDate toDate)
      throws AxelorException;

  public BigDecimal computeDuration(
      LeaveRequest leave, LocalDateTime from, LocalDateTime to, int startOn, int endOn)
      throws AxelorException;

  public void manageSentLeaves(LeaveRequest leave) throws AxelorException;

  public void manageValidateLeaves(LeaveRequest leave) throws AxelorException;

  public void manageRefuseLeaves(LeaveRequest leave) throws AxelorException;

  public void manageCancelLeaves(LeaveRequest leave) throws AxelorException;

  public double computeStartDateWithSelect(
      LocalDate date, int select, WeeklyPlanning weeklyPlanning);

  public double computeEndDateWithSelect(LocalDate date, int select, WeeklyPlanning weeklyPlanning);

  public LeaveRequest createEvents(LeaveRequest leave) throws AxelorException;

  public BigDecimal computeLeaveDaysByLeaveRequest(
      LocalDate fromDate, LocalDate toDate, LeaveRequest leaveRequest, Employee employee)
      throws AxelorException;

  public void cancel(LeaveRequest leaveRequest) throws AxelorException;

  public Message sendCancellationEmail(LeaveRequest leaveRequest)
      throws AxelorException, ClassNotFoundException, IOException, JSONException;

  public void confirm(LeaveRequest leaveRequest) throws AxelorException;

  public Message sendConfirmationEmail(LeaveRequest leaveRequest)
      throws AxelorException, ClassNotFoundException, IOException, JSONException;

  public void validate(LeaveRequest leaveRequest) throws AxelorException;

  public Message sendValidationEmail(LeaveRequest leaveRequest)
      throws AxelorException, ClassNotFoundException, IOException, JSONException;

  public void refuse(LeaveRequest leaveRequest) throws AxelorException;

  public Message sendRefusalEmail(LeaveRequest leaveRequest)
      throws AxelorException, ClassNotFoundException, IOException, JSONException;

  public boolean willHaveEnoughDays(LeaveRequest leaveRequest);

  public LeaveLine addLeaveReasonOrCreateIt(Employee employee, LeaveReason leaveReason);

  /**
   * Checks if the given day is a leave day.
   *
   * @param user
   * @param date
   * @return
   */
  public boolean isLeaveDay(Employee employee, LocalDate date);

  /**
   * Gets the leaves for the given user for the given date.
   *
   * @param user
   * @param date
   * @return
   */
  public List<LeaveRequest> getLeaves(Employee employee, LocalDate date);

  /**
   * Get the LeaveLine associated with the leaveRequest
   *
   * @param leaveRequest
   * @return
   */
  LeaveLine getLeaveLine(LeaveRequest leaveRequest);

  /**
   * Update daysToValidate field of leaveLine
   *
   * @param leaveLine
   */
  void updateDaysToValidate(LeaveLine leaveLine);

  String getLeaveCalendarDomain(User user);
}
