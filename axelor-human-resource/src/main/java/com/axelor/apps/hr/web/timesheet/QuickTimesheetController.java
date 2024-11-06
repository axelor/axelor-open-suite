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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.service.project.ProjectPlanningTimeService;
import com.axelor.apps.hr.service.timesheet.QuickTimesheetService;
import com.axelor.apps.hr.service.timesheet.TimesheetFetchService;
import com.axelor.apps.project.db.ProjectTask;
import com.axelor.auth.db.User;
import com.axelor.auth.db.repo.UserRepository;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.LinkedHashMap;

public class QuickTimesheetController {

  @SuppressWarnings("unchecked")
  public void computeTimesheet(ActionRequest request, ActionResponse response)
      throws AxelorException {

    Object userContext = request.getContext().get("user");

    if (userContext != null) {

      User user =
          userContext instanceof User
              ? (User) userContext
              : Beans.get(UserRepository.class)
                  .find(
                      Long.valueOf(
                          ((LinkedHashMap<String, Object>) userContext).get("id").toString()));

      if (user.getEmployee() != null) {
        LocalDate todayDate = Beans.get(AppBaseService.class).getTodayDate(user.getActiveCompany());
        LocalDate fromDate = todayDate.with(DayOfWeek.MONDAY);
        LocalDate toDate = todayDate.with(DayOfWeek.SUNDAY);

        Timesheet timesheet =
            Beans.get(TimesheetFetchService.class)
                .getOrCreateOpenTimesheet(user.getEmployee(), fromDate, toDate);

        response.setValue("timesheet", timesheet);
        response.setValue(
            "fromDate",
            timesheet.getFromDate().compareTo(fromDate) < 0 ? timesheet.getFromDate() : fromDate);
        response.setValue(
            "toDate",
            timesheet.getToDate() != null && toDate.compareTo(timesheet.getToDate()) > 0
                ? timesheet.getToDate()
                : toDate);
        response.setValue(
            "timeLoggingPreferenceSelect",
            !StringUtils.isEmpty(timesheet.getTimeLoggingPreferenceSelect())
                ? timesheet.getTimeLoggingPreferenceSelect()
                : Beans.get(AppBaseService.class).getAppBase().getTimeLoggingPreferenceSelect());
      }
    }
  }

  @SuppressWarnings("unchecked")
  public void viewLines(ActionRequest request, ActionResponse response) {

    Object userContext = request.getContext().get("user");
    Object fromDateContext = request.getContext().get("fromDate");
    Object toDateContext = request.getContext().get("toDate");

    if (userContext != null && fromDateContext != null && toDateContext != null) {

      User user =
          userContext instanceof User
              ? (User) userContext
              : Beans.get(UserRepository.class)
                  .find(
                      Long.valueOf(
                          ((LinkedHashMap<String, Object>) userContext).get("id").toString()));

      ActionView.ActionViewBuilder actionViewBuilder =
          ActionView.define(I18n.get("Timesheet lines"))
              .model(TimesheetLine.class.getName())
              .add("grid", "quick-timesheet-line-grid")
              .add("form", "quick-timesheet-line-form")
              .param("popup", "true")
              .param("popup-save", "true")
              .param("forceEdit", "true")
              .domain("self.employee = :employee AND self.date between :fromDate and :toDate")
              .context("employee", user.getEmployee())
              .context("fromDate", LocalDate.parse(fromDateContext.toString()))
              .context("toDate", LocalDate.parse(toDateContext.toString()));

      response.setView(actionViewBuilder.map());
    }
  }

  @SuppressWarnings("unchecked")
  public void computeTotals(ActionRequest request, ActionResponse response) throws AxelorException {

    Object userContext = request.getContext().get("user");
    Object fromDateContext = request.getContext().get("fromDate");
    Object toDateContext = request.getContext().get("toDate");
    Object timeUnitContext = request.getContext().get("timeLoggingPreferenceSelect");

    if (userContext != null
        && fromDateContext != null
        && toDateContext != null
        && timeUnitContext != null) {

      User user =
          userContext instanceof User
              ? (User) userContext
              : Beans.get(UserRepository.class)
                  .find(
                      Long.valueOf(
                          ((LinkedHashMap<String, Object>) userContext).get("id").toString()));

      Employee employee = user.getEmployee();

      if (employee != null) {
        LocalDate fromDate = LocalDate.parse(fromDateContext.toString());
        LocalDate toDate = LocalDate.parse(toDateContext.toString());
        String timeUnit = (String) timeUnitContext;

        QuickTimesheetService quickTimesheetService = Beans.get(QuickTimesheetService.class);

        BigDecimal totalTimeEntriesForPeriod =
            quickTimesheetService.computeTotalTimeEntriesForPeriod(employee, fromDate, toDate);
        BigDecimal totalLeavesAndHolidaysForPeriod =
            quickTimesheetService.computeTotalLeavesAndHolidaysForPeriod(
                employee, fromDate, toDate, timeUnit);
        BigDecimal totalWorkDurtionForPeriod =
            quickTimesheetService.computeTotalWorkDurtionForPeriod(
                employee, fromDate, toDate, timeUnit);

        response.setValue("totalTimeEntriesForPeriod", totalTimeEntriesForPeriod);
        response.setValue("totalLeavesAndHolidaysForPeriod", totalLeavesAndHolidaysForPeriod);
        response.setValue(
            "totalDueTimeEntriesForPeriod",
            totalWorkDurtionForPeriod.subtract(totalTimeEntriesForPeriod));
      }
    }
  }

  @SuppressWarnings("unchecked")
  public void viewTasks(ActionRequest request, ActionResponse response) {

    Object userContext = request.getContext().get("user");
    Object fromDateContext = request.getContext().get("fromDate");
    Object toDateContext = request.getContext().get("toDate");

    if (userContext != null && fromDateContext != null && toDateContext != null) {

      User user =
          userContext instanceof User
              ? (User) userContext
              : Beans.get(UserRepository.class)
                  .find(
                      Long.valueOf(
                          ((LinkedHashMap<String, Object>) userContext).get("id").toString()));

      if (user.getEmployee() != null) {
        LocalDate fromDate = LocalDate.parse(fromDateContext.toString());
        LocalDate toDate = LocalDate.parse(toDateContext.toString());

        ActionView.ActionViewBuilder actionViewBuilder =
            ActionView.define(I18n.get("Tasks"))
                .model(ProjectTask.class.getName())
                .add("grid", "project-task-quick-timesheet-grid")
                .add("form", "project-task-form")
                .domain("self.id in (:idList)")
                .context(
                    "idList",
                    Beans.get(ProjectPlanningTimeService.class)
                        .getOpenProjectTaskIdList(user.getEmployee(), fromDate, toDate));

        response.setView(actionViewBuilder.map());
      }
    }
  }
}
