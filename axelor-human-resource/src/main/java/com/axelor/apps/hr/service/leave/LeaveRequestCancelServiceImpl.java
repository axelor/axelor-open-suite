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
import com.axelor.apps.base.db.ICalendarEvent;
import com.axelor.apps.base.db.repo.ICalendarEventRepository;
import com.axelor.apps.hr.db.LeaveReason;
import com.axelor.apps.hr.db.LeaveRequest;
import com.axelor.apps.hr.db.repo.LeaveRequestRepository;
import com.axelor.apps.hr.service.leavereason.LeaveReasonService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class LeaveRequestCancelServiceImpl implements LeaveRequestCancelService {

  protected LeaveRequestManagementService leaveRequestManagementService;
  protected LeaveLineService leaveLineService;
  protected ICalendarEventRepository iCalendarEventRepository;
  protected LeaveReasonService leaveReasonService;
  protected LeaveRequestRepository leaveRequestRepository;
  protected LeaveRequestCheckService leaveRequestCheckService;

  @Inject
  public LeaveRequestCancelServiceImpl(
      LeaveRequestManagementService leaveRequestManagementService,
      LeaveLineService leaveLineService,
      ICalendarEventRepository iCalendarEventRepository,
      LeaveReasonService leaveReasonService,
      LeaveRequestRepository leaveRequestRepository,
      LeaveRequestCheckService leaveRequestCheckService) {
    this.leaveRequestManagementService = leaveRequestManagementService;
    this.leaveLineService = leaveLineService;
    this.iCalendarEventRepository = iCalendarEventRepository;
    this.leaveReasonService = leaveReasonService;
    this.leaveRequestRepository = leaveRequestRepository;
    this.leaveRequestCheckService = leaveRequestCheckService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void cancel(LeaveRequest leaveRequest) throws AxelorException {
    LeaveReason leaveReason = leaveRequest.getLeaveReason();

    leaveRequestCheckService.checkCompany(leaveRequest);
    if (!leaveReasonService.isExceptionalDaysReason(leaveReason)) {
      leaveRequestManagementService.manageCancelLeaves(leaveRequest);
    }

    if (leaveRequest.getIcalendarEvent() != null) {
      ICalendarEvent event = leaveRequest.getIcalendarEvent();
      leaveRequest.setIcalendarEvent(null);
      iCalendarEventRepository.remove(iCalendarEventRepository.find(event.getId()));
    }
    leaveRequest.setStatusSelect(LeaveRequestRepository.STATUS_CANCELED);
    leaveRequestRepository.save(leaveRequest);

    if (!leaveReasonService.isExceptionalDaysReason(leaveReason)) {
      leaveLineService.updateDaysToValidate(leaveLineService.getLeaveLine(leaveRequest));
    }
  }
}
