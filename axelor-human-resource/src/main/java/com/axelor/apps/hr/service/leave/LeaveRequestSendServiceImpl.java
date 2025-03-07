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
package com.axelor.apps.hr.service.leave;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.hr.db.LeaveLine;
import com.axelor.apps.hr.db.LeaveReason;
import com.axelor.apps.hr.db.LeaveRequest;
import com.axelor.apps.hr.db.repo.LeaveRequestRepository;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.apps.hr.service.leavereason.LeaveReasonService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class LeaveRequestSendServiceImpl implements LeaveRequestSendService {

  protected final LeaveRequestCheckService leaveRequestCheckService;
  protected final LeaveRequestRepository leaveRequestRepository;
  protected final LeaveLineService leaveLineService;
  protected final LeaveRequestService leaveRequestService;
  protected final LeaveReasonService leaveReasonService;
  protected final LeaveRequestManagementService leaveRequestManagementService;
  protected final AppBaseService appBaseService;

  @Inject
  public LeaveRequestSendServiceImpl(
      LeaveRequestCheckService leaveRequestCheckService,
      LeaveRequestRepository leaveRequestRepository,
      LeaveLineService leaveLineService,
      LeaveRequestService leaveRequestService,
      LeaveReasonService leaveReasonService,
      LeaveRequestManagementService leaveRequestManagementService,
      AppBaseService appBaseService) {
    this.leaveRequestCheckService = leaveRequestCheckService;
    this.leaveRequestRepository = leaveRequestRepository;
    this.leaveLineService = leaveLineService;
    this.leaveRequestService = leaveRequestService;
    this.leaveReasonService = leaveReasonService;
    this.leaveRequestManagementService = leaveRequestManagementService;
    this.appBaseService = appBaseService;
  }

  @Override
  public String send(LeaveRequest leaveRequest) throws AxelorException {
    if (leaveRequest.getEmployee().getWeeklyPlanning() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          String.format(
              I18n.get(HumanResourceExceptionMessage.EMPLOYEE_PLANNING),
              leaveRequest.getEmployee().getName()));
    }

    LeaveLine leaveLine = leaveLineService.getLeaveLine(leaveRequest);
    if (leaveLine != null
        && leaveLine.getQuantity().subtract(leaveRequest.getDuration()).signum() < 0) {
      if (!leaveRequest.getLeaveReason().getAllowNegativeValue()
          && !leaveRequestService.willHaveEnoughDays(leaveRequest)) {
        String instruction = leaveRequest.getLeaveReason().getInstruction();
        if (instruction == null) {
          instruction = "";
        }
        throw new AxelorException(
            TraceBackRepository.CATEGORY_INCONSISTENCY,
            String.format(
                    I18n.get(HumanResourceExceptionMessage.LEAVE_ALLOW_NEGATIVE_VALUE_REASON),
                    leaveRequest.getLeaveReason().getName())
                + " "
                + instruction);
      } else {
        return String.format(
            I18n.get(HumanResourceExceptionMessage.LEAVE_ALLOW_NEGATIVE_ALERT),
            leaveRequest.getLeaveReason().getName());
      }
    }

    confirm(leaveRequest);

    return "";
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void confirm(LeaveRequest leaveRequest) throws AxelorException {
    LeaveReason leaveReason = leaveRequest.getLeaveReason();

    leaveRequestCheckService.checkDates(leaveRequest);
    leaveRequestCheckService.checkCompany(leaveRequest);
    if (!leaveReasonService.isExceptionalDaysReason(leaveReason)) {
      leaveRequestManagementService.manageSentLeaves(leaveRequest);
    }

    leaveRequest.setStatusSelect(LeaveRequestRepository.STATUS_AWAITING_VALIDATION);
    leaveRequest.setRequestDate(appBaseService.getTodayDate(leaveRequest.getCompany()));

    leaveRequestRepository.save(leaveRequest);

    if (!leaveReasonService.isExceptionalDaysReason(leaveReason)) {
      leaveLineService.updateDaysToValidate(leaveLineService.getLeaveLine(leaveRequest));
    }
  }
}
