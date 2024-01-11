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
package com.axelor.apps.hr.web.timesheet;

import com.axelor.apps.ReportFactory;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.TimesheetReport;
import com.axelor.apps.hr.report.IReport;
import com.axelor.apps.hr.service.timesheet.TimesheetReportService;
import com.axelor.apps.hr.translation.ITranslation;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.message.db.Message;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.lang.invoke.MethodHandles;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimesheetReportController {

  private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public void printEmployeeTimesheetReport(ActionRequest request, ActionResponse response)
      throws AxelorException {
    TimesheetReport timesheetReport = request.getContext().asType(TimesheetReport.class);
    String name = I18n.get(ITranslation.TS_REPORT_TITLE);

    String fileLink =
        ReportFactory.createReport(IReport.EMPLOYEE_TIMESHEET, name)
            .addParam("TimesheetReportId", timesheetReport.getId().toString())
            .addParam("Timezone", null)
            .addParam("Locale", ReportSettings.getPrintingLocale(null))
            .addParam(
                "FromDate",
                timesheetReport.getFromDate().format(DateTimeFormatter.ofPattern("dd-MM-yyy")))
            .addParam(
                "ToDate",
                timesheetReport.getToDate().format(DateTimeFormatter.ofPattern("dd-MM-yyy")))
            .toAttach(timesheetReport)
            .generate()
            .getFileLink();
    logger.debug("Printing {}", name);
    response.setView(ActionView.define(name).add("html", fileLink).map());
  }

  public void fillTimesheetReportReminderEmployees(ActionRequest request, ActionResponse response) {
    TimesheetReport timesheetReport = request.getContext().asType(TimesheetReport.class);
    Set<Employee> reminderEmployeeSet =
        Beans.get(TimesheetReportService.class).getEmployeeToBeReminded(timesheetReport);
    if (!ObjectUtils.isEmpty(reminderEmployeeSet)) {
      response.setValue("reminderEmployeeSet", reminderEmployeeSet);
    } else {
      response.setValue("reminderEmployeeSet", null);
      response.setNotify(I18n.get(ITranslation.TS_REPORT_FILL_NO_EMPLOYEE));
    }
  }

  public void sendTimesheetReminder(ActionRequest request, ActionResponse response)
      throws AxelorException {
    TimesheetReport timesheetReport = request.getContext().asType(TimesheetReport.class);

    if (ObjectUtils.isEmpty(timesheetReport.getReminderEmployeeSet())) {
      return;
    }

    List<Message> messages = Beans.get(TimesheetReportService.class).sendReminders(timesheetReport);
    List<Long> messageIds = new ArrayList<Long>();
    messageIds.add(0L);
    for (Message message : messages) {
      messageIds.add(message.getId());
    }

    response.setView(
        ActionView.define(I18n.get("Messages"))
            .model(Message.class.getName())
            .add("grid", "message-timesheet-reminder-grid")
            .add("form", "message-form")
            .domain("self.id in (:messageIds)")
            .context("messageIds", messageIds)
            .map());
  }
}
