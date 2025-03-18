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
package com.axelor.apps.hr.rest;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.hr.db.repo.LeaveReasonRepository;
import com.axelor.apps.hr.db.repo.LeaveRequestRepository;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.apps.hr.rest.dto.LeaveRequestCreatePostRequest;
import com.axelor.apps.hr.rest.dto.LeaveRequestCreateResponse;
import com.axelor.apps.hr.rest.dto.LeaveRequestReasonRequest;
import com.axelor.apps.hr.rest.dto.LeaveRequestResponse;
import com.axelor.apps.hr.service.leave.LeaveRequestCreateHelperService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class LeaveRequestCreateRestServiceImpl implements LeaveRequestCreateRestService {

  protected final LeaveRequestCreateHelperService leaveRequestCreateHelperService;
  protected final LeaveRequestRepository leaveRequestRepository;
  protected final LeaveReasonRepository leaveReasonRepository;

  @Inject
  public LeaveRequestCreateRestServiceImpl(
      LeaveRequestCreateHelperService leaveRequestCreateHelperService,
      LeaveRequestRepository leaveRequestRepository,
      LeaveReasonRepository leaveReasonRepository) {
    this.leaveRequestCreateHelperService = leaveRequestCreateHelperService;
    this.leaveRequestRepository = leaveRequestRepository;
    this.leaveReasonRepository = leaveReasonRepository;
  }

  @Override
  public List<Long> createLeaveRequests(
      LocalDate fromDate, int startOnSelect, List<LeaveRequestReasonRequest> leaveRequestReasonList)
      throws AxelorException {

    if (CollectionUtils.isEmpty(leaveRequestReasonList)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(HumanResourceExceptionMessage.API_LEAVE_REQUEST_NONE_CREATED));
    }

    List<HashMap<String, Object>> leaveReasonRequestMap = new ArrayList<>();
    for (LeaveRequestReasonRequest leaveReason : leaveRequestReasonList) {
      if (skipRequest(leaveReason)) {
        continue;
      }

      HashMap<String, Object> leaveReasonMap = new HashMap<>();
      leaveReasonMap.put("duration", leaveReason.getDuration());
      leaveReasonMap.put("comment", leaveReason.getComment());

      HashMap<String, Object> leaveReasonIdMap = new HashMap<>();
      leaveReasonIdMap.put("id", leaveReason.fetchLeaveReason().getId());
      leaveReasonMap.put("leaveReason", leaveReasonIdMap);

      leaveReasonRequestMap.add(leaveReasonMap);
    }

    return leaveRequestCreateHelperService.createLeaveRequests(
        fromDate, startOnSelect, leaveReasonRequestMap);
  }

  protected boolean skipRequest(LeaveRequestReasonRequest leaveReason) {
    return leaveReason.getDuration().compareTo(BigDecimal.ZERO) == 0
        || leaveReasonRepository
                .all()
                .filter("self.id = :leaveReasonId")
                .bind("leaveReasonId", leaveReason.getLeaveReasonId())
                .count()
            == 0;
  }

  @Override
  public LeaveRequestCreateResponse createLeaveRequestResponse(List<Long> leaveRequestIdList) {
    List<LeaveRequestResponse> leaveRequestResponseList = new ArrayList<>();
    for (Long leaveRequestId : leaveRequestIdList) {
      leaveRequestResponseList.add(
          new LeaveRequestResponse(leaveRequestRepository.find(leaveRequestId)));
    }
    return new LeaveRequestCreateResponse(leaveRequestResponseList);
  }

  @Override
  public void checkLeaveRequestCreatePostRequest(
      LeaveRequestCreatePostRequest leaveRequestCreatePostRequest) throws AxelorException {
    int startOnSelect = leaveRequestCreatePostRequest.getStartOnSelect();
    List<Integer> authorizedInt = new ArrayList<>();
    authorizedInt.add(LeaveRequestRepository.SELECT_MORNING);
    authorizedInt.add(LeaveRequestRepository.SELECT_AFTERNOON);
    if (!authorizedInt.contains(startOnSelect)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(HumanResourceExceptionMessage.API_LEAVE_REQUEST_WRONG_START_ON_SELECT));
    }
  }
}
