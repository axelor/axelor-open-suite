/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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

import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.LeaveRequest;
import com.axelor.apps.hr.db.repo.LeaveRequestRepository;
import com.axelor.auth.db.User;
import com.axelor.db.Query;
import com.axelor.i18n.I18n;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.utils.db.Wizard;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Map;

public class LeaveViewServiceImpl implements LeaveViewService {

  protected LeaveRequestRepository leaveRequestRepository;

  @Inject
  public LeaveViewServiceImpl(LeaveRequestRepository leaveRequestRepository) {
    this.leaveRequestRepository = leaveRequestRepository;
  }

  @Override
  public Map<String, Object> buildEditLeaveView(User user) {
    List<LeaveRequest> leaveList =
        leaveRequestRepository
            .all()
            .filter(
                "self.employee.user.id = ?1 AND self.company = ?2 AND self.statusSelect = ?3",
                user.getId(),
                user.getActiveCompany(),
                LeaveRequestRepository.STATUS_DRAFT)
            .fetch();
    if (leaveList.isEmpty()) {
      return ActionView.define(I18n.get("LeaveRequest"))
          .model(LeaveRequest.class.getName())
          .add("form", "complete-my-leave-request-form")
          .context("_isEmployeeReadOnly", true)
          .map();
    }
    if (leaveList.size() == 1) {
      return ActionView.define(I18n.get("LeaveRequest"))
          .model(LeaveRequest.class.getName())
          .add("form", "complete-my-leave-request-form")
          .param("forceEdit", "true")
          .context("_showRecord", String.valueOf(leaveList.get(0).getId()))
          .context("_isEmployeeReadOnly", true)
          .map();
    }
    return ActionView.define(I18n.get("LeaveRequest"))
        .model(Wizard.class.getName())
        .add("form", "popup-leave-request-form")
        .param("forceEdit", "true")
        .param("popup", "true")
        .param("show-toolbar", "false")
        .param("show-confirm", "false")
        .param("popup-save", "false")
        .map();
  }

  @Override
  public Map<String, Object> buildEditSelectedLeaveView(Long leaveId) {
    return ActionView.define(I18n.get("LeaveRequest"))
        .model(LeaveRequest.class.getName())
        .add("form", "complete-my-leave-request-form")
        .param("forceEdit", "true")
        .domain("self.id = " + leaveId)
        .context("_showRecord", leaveId)
        .context("_isEmployeeReadOnly", true)
        .map();
  }

  @Override
  public Map<String, Object> buildHistoricLeaveView(User user, Employee employee) {
    ActionViewBuilder actionView =
        ActionView.define(I18n.get("Colleague Leave Requests"))
            .model(LeaveRequest.class.getName())
            .add("grid", "leave-request-grid")
            .add("form", "leave-request-form")
            .param("search-filters", "leave-request-filters")
            .domain("(self.statusSelect IN :statusSelectList)")
            .context(
                "statusSelectList",
                List.of(
                    LeaveRequestRepository.STATUS_VALIDATED,
                    LeaveRequestRepository.STATUS_REFUSED,
                    LeaveRequestRepository.STATUS_CANCELED));
    if (employee == null || !employee.getHrManager()) {
      actionView
          .domain(actionView.get().getDomain() + " AND self.employee.managerUser = :_user")
          .context("_user", user);
    }
    return actionView.map();
  }

  @Override
  public Map<String, Object> buildSubordinateLeavesView(User user) {
    String domain =
        "self.employee.managerUser.employee.managerUser = :_user AND self.statusSelect = :_status";
    long nbLeaveRequests =
        Query.of(LeaveRequest.class)
            .filter(domain)
            .bind("_user", user)
            .bind("_status", LeaveRequestRepository.STATUS_AWAITING_VALIDATION)
            .count();
    if (nbLeaveRequests == 0) {
      return null;
    }
    return ActionView.define(I18n.get("Leaves to be Validated by your subordinates"))
        .model(LeaveRequest.class.getName())
        .add("grid", "leave-request-grid")
        .add("form", "leave-request-form")
        .param("search-filters", "leave-request-filters")
        .domain(domain)
        .context("_user", user)
        .context("_status", LeaveRequestRepository.STATUS_AWAITING_VALIDATION)
        .map();
  }
}
