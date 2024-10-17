package com.axelor.apps.hr.rest;

import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.rest.dto.TimesheetLinePostRequest;
import com.axelor.apps.hr.service.timesheet.TimesheetCreateService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.inject.Beans;
import java.util.Optional;

public class TimesheetLinePostRequestHelper {

  private TimesheetLinePostRequestHelper() {}

  public static Timesheet fetchOrCreateTimesheet(TimesheetLinePostRequest requestBody) {
    if (requestBody.getTimesheetId() != null) {
      return requestBody.fetchTimesheet();
    } else {
      return Beans.get(TimesheetCreateService.class)
          .getOrCreateTimesheet(
              Optional.ofNullable(AuthUtils.getUser()).map(User::getEmployee).orElse(null),
              requestBody.fetchProject(),
              requestBody.getDate());
    }
  }
}
