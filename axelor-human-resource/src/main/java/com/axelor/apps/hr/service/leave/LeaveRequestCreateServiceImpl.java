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
import com.axelor.apps.hr.db.LeaveReason;
import com.axelor.apps.hr.db.LeaveRequest;
import com.axelor.apps.hr.db.repo.LeaveRequestRepository;
import com.axelor.apps.hr.service.leave.compute.LeaveRequestComputeDurationService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.LocalDateTime;

public class LeaveRequestCreateServiceImpl implements LeaveRequestCreateService {

  protected final LeaveRequestInitValueService leaveRequestInitValueService;
  protected final LeaveRequestRepository leaveRequestRepository;
  protected final LeaveRequestComputeDurationService leaveRequestComputeDurationService;

  @Inject
  public LeaveRequestCreateServiceImpl(
      LeaveRequestInitValueService leaveRequestInitValueService,
      LeaveRequestRepository leaveRequestRepository,
      LeaveRequestComputeDurationService leaveRequestComputeDurationService) {
    this.leaveRequestInitValueService = leaveRequestInitValueService;
    this.leaveRequestRepository = leaveRequestRepository;
    this.leaveRequestComputeDurationService = leaveRequestComputeDurationService;
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public LeaveRequest createLeaveRequest(
      LocalDateTime fromDateTime,
      LocalDateTime toDateTime,
      int startOnSelect,
      int endOnSelect,
      String comment,
      LeaveReason leaveReason)
      throws AxelorException {
    LeaveRequest leaveRequest = new LeaveRequest();
    leaveRequestInitValueService.initLeaveRequest(leaveRequest);

    leaveRequest.setFromDateT(fromDateTime);
    leaveRequest.setToDateT(toDateTime);
    leaveRequest.setStartOnSelect(startOnSelect);
    leaveRequest.setEndOnSelect(endOnSelect);
    leaveRequest.setLeaveReason(leaveReason);
    leaveRequest.setComments(comment);

    leaveRequest.setDuration(leaveRequestComputeDurationService.computeDuration(leaveRequest));
    return leaveRequestRepository.save(leaveRequest);
  }
}
