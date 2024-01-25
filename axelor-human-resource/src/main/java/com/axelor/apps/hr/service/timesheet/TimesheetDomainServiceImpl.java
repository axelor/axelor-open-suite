package com.axelor.apps.hr.service.timesheet;

import com.axelor.apps.hr.db.Employee;
import com.axelor.auth.db.User;
import com.axelor.meta.schema.actions.ActionView;

public class TimesheetDomainServiceImpl implements TimesheetDomainService {

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
}
