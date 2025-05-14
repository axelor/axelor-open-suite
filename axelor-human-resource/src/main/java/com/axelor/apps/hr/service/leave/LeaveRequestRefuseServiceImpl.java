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
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.hr.db.LeaveReason;
import com.axelor.apps.hr.db.LeaveRequest;
import com.axelor.apps.hr.db.repo.LeaveRequestRepository;
import com.axelor.apps.hr.service.leavereason.LeaveReasonService;
import com.axelor.auth.AuthUtils;
import com.axelor.common.StringUtils;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class LeaveRequestRefuseServiceImpl implements LeaveRequestRefuseService {

  protected LeaveRequestManagementService leaveRequestManagementService;
  protected LeaveLineService leaveLineService;
  protected LeaveReasonService leaveReasonService;
  protected LeaveRequestRepository leaveRequestRepository;
  protected AppBaseService appBaseService;
  protected LeaveRequestCheckService leaveRequestCheckService;

  @Inject
  public LeaveRequestRefuseServiceImpl(
      LeaveRequestManagementService leaveRequestManagementService,
      LeaveLineService leaveLineService,
      LeaveReasonService leaveReasonService,
      LeaveRequestRepository leaveRequestRepository,
      AppBaseService appBaseService,
      LeaveRequestCheckService leaveRequestCheckService) {
    this.leaveRequestManagementService = leaveRequestManagementService;
    this.leaveLineService = leaveLineService;
    this.leaveReasonService = leaveReasonService;
    this.leaveRequestRepository = leaveRequestRepository;
    this.appBaseService = appBaseService;
    this.leaveRequestCheckService = leaveRequestCheckService;
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public void refuse(LeaveRequest leaveRequest, String groundForRefusal) throws AxelorException {
    LeaveReason leaveReason = leaveRequest.getLeaveReason();

    leaveRequestCheckService.checkCompany(leaveRequest);
    if (!leaveReasonService.isExceptionalDaysReason(leaveReason)) {
      leaveRequestManagementService.manageRefuseLeaves(leaveRequest);
    }

    leaveRequest.setStatusSelect(LeaveRequestRepository.STATUS_REFUSED);
    leaveRequest.setRefusedBy(AuthUtils.getUser());
    leaveRequest.setRefusalDateTime(
        appBaseService.getTodayDateTime(leaveRequest.getCompany()).toLocalDateTime());

    if (StringUtils.notEmpty(groundForRefusal)) {
      leaveRequest.setGroundForRefusal(groundForRefusal);
    }

    leaveRequestRepository.save(leaveRequest);
    if (!leaveReasonService.isExceptionalDaysReason(leaveReason)) {
      leaveLineService.updateDaysToValidate(leaveLineService.getLeaveLine(leaveRequest));
    }
  }
}
