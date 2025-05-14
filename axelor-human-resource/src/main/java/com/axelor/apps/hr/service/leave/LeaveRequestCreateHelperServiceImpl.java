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
import com.axelor.apps.hr.db.repo.LeaveReasonRepository;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LeaveRequestCreateHelperServiceImpl implements LeaveRequestCreateHelperService {

  protected final LeaveRequestCreateService leaveRequestCreateService;
  protected final LeaveReasonRepository leaveReasonRepository;
  protected final LeaveRequestCreateHelperDateService leaveRequestCreateHelperDateService;

  @Inject
  public LeaveRequestCreateHelperServiceImpl(
      LeaveRequestCreateService leaveRequestCreateService,
      LeaveReasonRepository leaveReasonRepository,
      LeaveRequestCreateHelperDateService leaveRequestCreateHelperDateService) {
    this.leaveRequestCreateService = leaveRequestCreateService;
    this.leaveReasonRepository = leaveReasonRepository;
    this.leaveRequestCreateHelperDateService = leaveRequestCreateHelperDateService;
  }

  @Override
  public List<Long> createLeaveRequests(
      LocalDate fromDate, int startOnSelect, List<HashMap<String, Object>> leaveReasonList)
      throws AxelorException {
    List<Long> idList = new ArrayList<>();
    LocalDate initialDate = fromDate;
    int initalStartOnSelect = startOnSelect;

    for (HashMap<String, Object> object : leaveReasonList) {
      BigDecimal duration = new BigDecimal(object.get("duration").toString());
      Long leaveReasonId =
          Long.valueOf(((Map<String, Object>) object.get("leaveReason")).get("id").toString());
      LeaveReason leaveReason = leaveReasonRepository.find(leaveReasonId);
      String comment = (String) object.get("comment");

      int currentEndOfSelect =
          leaveRequestCreateHelperDateService.computeEndOnSelect(duration, initalStartOnSelect);

      LocalDate toDate =
          leaveRequestCreateHelperDateService.computeNextToDate(
              initialDate, duration, initalStartOnSelect);
      LocalDateTime toDateTime = toDate.atTime(0, 0, 0);
      LeaveRequest leaveRequest =
          leaveRequestCreateService.createLeaveRequest(
              initialDate.atTime(0, 0, 0),
              toDateTime,
              initalStartOnSelect,
              currentEndOfSelect,
              comment,
              leaveReason);
      idList.add(leaveRequest.getId());

      initalStartOnSelect =
          leaveRequestCreateHelperDateService.computeNextStartOnSelect(currentEndOfSelect);
      initialDate =
          leaveRequestCreateHelperDateService.computeNextStartDate(
              toDate, currentEndOfSelect, initalStartOnSelect);
    }

    return idList;
  }
}
