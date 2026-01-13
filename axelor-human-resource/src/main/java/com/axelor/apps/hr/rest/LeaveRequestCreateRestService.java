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
import com.axelor.apps.hr.rest.dto.LeaveRequestCreatePostRequest;
import com.axelor.apps.hr.rest.dto.LeaveRequestCreateResponse;
import com.axelor.apps.hr.rest.dto.LeaveRequestReasonRequest;
import java.time.LocalDate;
import java.util.List;

public interface LeaveRequestCreateRestService {

  List<Long> createLeaveRequests(
      LocalDate fromDate, int startOnSelect, List<LeaveRequestReasonRequest> leaveRequestReasonList)
      throws AxelorException;

  LeaveRequestCreateResponse createLeaveRequestResponse(List<Long> leaveRequestIdList);

  void checkLeaveRequestCreatePostRequest(
      LeaveRequestCreatePostRequest leaveRequestCreatePostRequest) throws AxelorException;
}
