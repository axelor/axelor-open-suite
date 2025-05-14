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
package com.axelor.apps.hr.service.leavereason;

import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.LeaveLine;
import com.axelor.apps.hr.db.LeaveReason;
import com.axelor.apps.hr.db.repo.LeaveReasonRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.utils.helpers.StringHelper;
import com.google.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class LeaveReasonDomainServiceImpl implements LeaveReasonDomainService {

  protected final LeaveReasonRepository leaveReasonRepository;

  @Inject
  public LeaveReasonDomainServiceImpl(LeaveReasonRepository leaveReasonRepository) {
    this.leaveReasonRepository = leaveReasonRepository;
  }

  @Override
  public String getLeaveReasonDomain(Employee employee) {
    StringBuilder filter =
        new StringBuilder(
            String.format(
                "self.leaveReasonTypeSelect = %s",
                LeaveReasonRepository.TYPE_SELECT_EXCEPTIONAL_DAYS));

    if (employee == null) {
      return filter.toString();
    }

    Employee userEmployee =
        Optional.ofNullable(AuthUtils.getUser()).map(User::getEmployee).orElse(null);

    List<LeaveReason> leaveLineLeaveReasonList = getLeaveLineLeaveReasons(employee, userEmployee);

    if (userEmployee != null && !userEmployee.getHrManager()) {
      filter.append(" AND self.selectedByMgtOnly IS FALSE");
      filter
          .append(" OR self.id IN (")
          .append(StringHelper.getIdListString(leaveLineLeaveReasonList))
          .append(")");
    } else {
      filter
          .append(" OR self.id IN (")
          .append(StringHelper.getIdListString(leaveLineLeaveReasonList))
          .append(")");
    }
    return filter.toString();
  }

  protected List<LeaveReason> getLeaveLineLeaveReasons(Employee employee, Employee userEmployee) {
    List<LeaveReason> leaveLineLeaveReasonList;

    if (userEmployee != null && !userEmployee.getHrManager()) {
      leaveLineLeaveReasonList =
          employee.getLeaveLineList().stream()
              .map(LeaveLine::getLeaveReason)
              .filter(leaveReason -> !leaveReason.getSelectedByMgtOnly())
              .collect(Collectors.toList());
    } else {
      leaveLineLeaveReasonList =
          employee.getLeaveLineList().stream()
              .map(LeaveLine::getLeaveReason)
              .collect(Collectors.toList());
    }
    return leaveLineLeaveReasonList;
  }
}
