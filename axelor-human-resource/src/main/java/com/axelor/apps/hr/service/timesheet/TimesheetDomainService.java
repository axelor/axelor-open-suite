package com.axelor.apps.hr.service.timesheet;

import com.axelor.apps.hr.db.Employee;
import com.axelor.auth.db.User;
import com.axelor.meta.schema.actions.ActionView;

public interface TimesheetDomainService {

  void createDomainAllTimesheetLine(
      User user, Employee employee, ActionView.ActionViewBuilder actionView);

  void createValidateDomainTimesheetLine(
      User user, Employee employee, ActionView.ActionViewBuilder actionView);
}
