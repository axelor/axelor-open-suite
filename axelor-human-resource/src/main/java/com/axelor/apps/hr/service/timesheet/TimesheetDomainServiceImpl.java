/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
import com.axelor.apps.project.db.Project;
import com.axelor.auth.db.User;
import com.axelor.db.Query;
import com.axelor.meta.schema.actions.ActionView;
import com.google.inject.Inject;
import java.time.LocalDate;
import java.util.Optional;

public class TimesheetDomainServiceImpl implements TimesheetDomainService {

  protected TimesheetRepository timesheetRepository;

  @Inject
  public TimesheetDomainServiceImpl(TimesheetRepository timesheetRepository) {
    this.timesheetRepository = timesheetRepository;
  }

  @Override
  public void createDomainAllTimesheetLine(
      User user, Employee employee, ActionView.ActionViewBuilder actionView) {

    actionView
        .domain("self.timesheet.company = :_activeCompany")
        .context("_activeCompany", user.getActiveCompany());

    if (employee == null || !employee.getHrManager()) {
      if (employee == null || employee.getManagerUser() == null) {
        actionView
            .domain(
                actionView.get().getDomain()
                    + " AND (self.timesheet.employee.user.id = :_user_id OR self.timesheet.employee.managerUser = :_user)")
            .context("_user_id", user.getId())
            .context("_user", user);
      } else {
        actionView
            .domain(
                actionView.get().getDomain() + " AND self.timesheet.employee.managerUser = :_user")
            .context("_user", user);
      }
    }
  }

  @Override
  public void createValidateDomainTimesheetLine(
      User user, Employee employee, ActionView.ActionViewBuilder actionView) {

    actionView
        .domain("self.timesheet.company = :_activeCompany AND  self.timesheet.statusSelect = 2")
        .context("_activeCompany", user.getActiveCompany());

    if (employee == null || !employee.getHrManager()) {
      if (employee == null || employee.getManagerUser() == null) {
        actionView
            .domain(
                actionView.get().getDomain()
                    + " AND (self.timesheet.employee.user = :_user OR self.timesheet.employee.managerUser = :_user)")
            .context("_user", user);
      } else {
        actionView
            .domain(
                actionView.get().getDomain() + " AND self.timesheet.employee.managerUser = :_user")
            .context("_user", user);
      }
    }
  }

  @Override
  public Query<Timesheet> getTimesheetQuery(TimesheetLine timesheetLine) {
    return getTimesheetQuery(
        timesheetLine.getEmployee(),
        Optional.of(timesheetLine)
            .map(TimesheetLine::getProject)
            .map(Project::getCompany)
            .orElse(null),
        timesheetLine.getDate());
  }

  protected Query<Timesheet> getTimesheetQuery(Employee employee, Company company, LocalDate date) {
    return timesheetRepository
        .all()
        .filter(
            "self.employee = ?1 AND self.company = ?2 AND (self.statusSelect = 1 OR self.statusSelect = 2) AND ((?3 BETWEEN self.fromDate AND self.toDate) OR (self.toDate = null))",
            employee,
            company,
            date);
  }
}
