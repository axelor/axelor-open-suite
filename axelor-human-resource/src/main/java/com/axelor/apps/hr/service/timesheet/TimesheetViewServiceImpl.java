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
package com.axelor.apps.hr.service.timesheet;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.db.repo.TimesheetRepository;
import com.axelor.auth.db.User;
import com.axelor.db.Query;
import com.axelor.i18n.I18n;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.utils.db.Wizard;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TimesheetViewServiceImpl implements TimesheetViewService {

  protected TimesheetRepository timesheetRepository;

  @Inject
  public TimesheetViewServiceImpl(TimesheetRepository timesheetRepository) {
    this.timesheetRepository = timesheetRepository;
  }

  @Override
  public Map<String, Object> buildEditTimesheetView(User user) {
    List<Timesheet> timesheetList =
        timesheetRepository
            .all()
            .filter(
                "self.employee.user.id = ?1 AND self.company = ?2 AND self.statusSelect = ?3",
                user.getId(),
                Optional.ofNullable(user).map(User::getActiveCompany).orElse(null),
                TimesheetRepository.STATUS_DRAFT)
            .fetch();
    if (timesheetList.isEmpty()) {
      return ActionView.define(I18n.get("Timesheet"))
          .model(Timesheet.class.getName())
          .add("form", "complete-my-timesheet-form")
          .context("_isEmployeeReadOnly", true)
          .map();
    }
    if (timesheetList.size() == 1) {
      return ActionView.define(I18n.get("Timesheet"))
          .model(Timesheet.class.getName())
          .add("form", "complete-my-timesheet-form")
          .param("forceEdit", "true")
          .context("_showRecord", String.valueOf(timesheetList.get(0).getId()))
          .context("_isEmployeeReadOnly", true)
          .map();
    }
    return ActionView.define(I18n.get("Timesheet"))
        .model(Wizard.class.getName())
        .add("form", "popup-timesheet-form")
        .param("forceEdit", "true")
        .param("popup", "true")
        .param("show-toolbar", "false")
        .param("show-confirm", "false")
        .param("forceEdit", "true")
        .param("popup-save", "false")
        .map();
  }

  @Override
  public Map<String, Object> buildAllTimesheetView(User user, Employee employee) {
    ActionViewBuilder actionView =
        ActionView.define(I18n.get("Timesheets"))
            .model(Timesheet.class.getName())
            .add("grid", "all-timesheet-grid")
            .add("form", "timesheet-form")
            .param("search-filters", "timesheet-filters");
    if (employee == null || !employee.getHrManager()) {
      if (employee == null || employee.getManagerUser() == null) {
        actionView
            .domain("self.employee.user = :_user OR self.employee.managerUser = :_user")
            .context("_user", user);
      } else {
        actionView.domain("self.employee.managerUser = :_user").context("_user", user);
      }
    }
    return actionView.map();
  }

  @Override
  public Map<String, Object> buildEditSelectedTimesheetView(Long timesheetId) {
    return ActionView.define(I18n.get("Timesheet"))
        .model(Timesheet.class.getName())
        .add("form", "complete-my-timesheet-form")
        .param("forceEdit", "true")
        .domain("self.id = " + timesheetId)
        .context("_showRecord", timesheetId)
        .context("_isEmployeeReadOnly", true)
        .map();
  }

  @Override
  public Map<String, Object> buildHistoricTimesheetView(User user, Employee employee) {
    ActionViewBuilder actionView =
        ActionView.define(I18n.get("Historic colleague Timesheets"))
            .model(Timesheet.class.getName())
            .add("grid", "timesheet-grid")
            .add("form", "timesheet-form")
            .param("search-filters", "timesheet-filters")
            .domain(
                "(self.statusSelect = :_statusValidated OR self.statusSelect = :_statusRefused)")
            .context("_statusValidated", TimesheetRepository.STATUS_VALIDATED)
            .context("_statusRefused", TimesheetRepository.STATUS_REFUSED);
    if (employee == null || !employee.getHrManager()) {
      actionView
          .domain(actionView.get().getDomain() + " AND self.employee.managerUser = :_user")
          .context("_user", user);
    }
    return actionView.map();
  }

  @Override
  public Map<String, Object> buildHistoricTimesheetLineView(User user, Employee employee) {
    ActionViewBuilder actionView =
        ActionView.define(I18n.get("See timesheet lines"))
            .model(TimesheetLine.class.getName())
            .add("grid", "timesheet-line-grid")
            .add("form", "timesheet-line-form")
            .domain(
                "self.timesheet.company = :_activeCompany AND (self.timesheet.statusSelect = :_statusValidated OR self.timesheet.statusSelect = :_statusRefused)")
            .context("_activeCompany", user.getActiveCompany())
            .context("_statusValidated", TimesheetRepository.STATUS_VALIDATED)
            .context("_statusRefused", TimesheetRepository.STATUS_REFUSED);
    if (employee == null || !employee.getHrManager()) {
      actionView
          .domain(
              actionView.get().getDomain() + " AND self.timesheet.employee.managerUser = :_user")
          .context("_user", user);
    }
    return actionView.map();
  }

  @Override
  public Map<String, Object> buildSubordinateTimesheetsView(User user) {
    Company company = user.getActiveCompany();
    String domain =
        "self.employee.managerUser.employee.managerUser = :_user AND self.company = :_activeCompany AND self.statusSelect = :_status";
    long nbTimesheets =
        Query.of(Timesheet.class)
            .filter(domain)
            .bind("_user", user)
            .bind("_activeCompany", company)
            .bind("_status", TimesheetRepository.STATUS_CONFIRMED)
            .count();
    if (nbTimesheets == 0) {
      return null;
    }
    return ActionView.define(I18n.get("Timesheets to be Validated by your subordinates"))
        .model(Timesheet.class.getName())
        .add("grid", "timesheet-grid")
        .add("form", "timesheet-form")
        .param("search-filters", "timesheet-filters")
        .domain(domain)
        .context("_user", user)
        .context("_activeCompany", company)
        .context("_status", TimesheetRepository.STATUS_CONFIRMED)
        .map();
  }
}
