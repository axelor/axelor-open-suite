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
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.LeaveReason;
import com.axelor.apps.hr.db.LeaveRequest;
import com.axelor.auth.db.User;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface LeaveRequestService {
  List<LeaveRequest> getLeaves(Employee employee, LocalDate date);

  boolean willHaveEnoughDays(LeaveRequest leaveRequest) throws AxelorException;

  String getLeaveCalendarDomain(User user);

  boolean isLeaveDay(Employee employee, LocalDate date);

  BigDecimal getLeaveDaysToDate(LeaveRequest leaveRequest) throws AxelorException;

  BigDecimal getLeaveDaysToDate(LocalDateTime toDateT, Employee employee, LeaveReason leaveReason)
      throws AxelorException;
}
