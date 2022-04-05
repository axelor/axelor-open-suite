/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.apps.hr.service.leave;

import com.axelor.apps.base.db.WeeklyPlanning;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.LeaveLine;
import com.axelor.apps.hr.db.LeaveReason;
import com.axelor.apps.hr.db.LeaveRequest;
import com.axelor.apps.message.db.Message;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import javax.mail.MessagingException;
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

  /**
   * Set the leave request status to canceled.
   *
   * @param leaveRequest
   * @throws AxelorException if the leave request was already canceled.
   */
  public void cancel(LeaveRequest leaveRequest) throws AxelorException;

  /**
   * Set the leave request status to draft.
   *
   * @param leaveRequest
   * @throws AxelorException if the leave request wasn't refused nor canceled.
   */
  void backToDraft(LeaveRequest leaveRequest) throws AxelorException;

  public Message sendCancellationEmail(LeaveRequest leaveRequest)
      throws AxelorException, ClassNotFoundException, InstantiationException,
          IllegalAccessException, MessagingException, IOException, JSONException;

  /**
   * Set the leave request status to confirmed.
   *
   * @param leaveRequest
   * @throws AxelorException if the leave request wasn't drafted.
   */
  public void confirm(LeaveRequest leaveRequest) throws AxelorException;

  public Message sendConfirmationEmail(LeaveRequest leaveRequest)
      throws AxelorException, ClassNotFoundException, InstantiationException,
          IllegalAccessException, MessagingException, IOException, JSONException;

  /**
   * Set the leave request status to validated.
   *
   * @param leaveRequest
   * @throws AxelorException if the leave request wasn't awaiting for a validation.
   */
  public void validate(LeaveRequest leaveRequest) throws AxelorException;

  public Message sendValidationEmail(LeaveRequest leaveRequest)
      throws AxelorException, ClassNotFoundException, InstantiationException,
          IllegalAccessException, MessagingException, IOException, JSONException;

  /**
   * Set the leave request refusal date, the user who refused and change the status to refused.
   *
   * @param leaveRequest
   * @throws AxelorException if the leave request wasn't awaiting for a validation.
   */
  public void refuse(LeaveRequest leaveRequest) throws AxelorException;

  public Message sendRefusalEmail(LeaveRequest leaveRequest)
      throws AxelorException, ClassNotFoundException, InstantiationException,
          IllegalAccessException, MessagingException, IOException, JSONException;

  public boolean willHaveEnoughDays(LeaveRequest leaveRequest);

  public LeaveLine addLeaveReasonOrCreateIt(Employee employee, LeaveReason leaveReason)
      throws AxelorException;

  /**
   * Checks if the given day is a leave day.
   *
   * @param user
   * @param date
   * @return
   */
  public boolean isLeaveDay(User user, LocalDate date);

  /**
   * Gets the leaves for the given user for the given date.
   *
   * @param user
   * @param date
   * @return
   */
  public List<LeaveRequest> getLeaves(User user, LocalDate date);
}
