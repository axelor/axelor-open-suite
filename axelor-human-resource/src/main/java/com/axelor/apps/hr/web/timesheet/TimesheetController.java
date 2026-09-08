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
package com.axelor.apps.hr.web.timesheet;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.base.service.message.MessageServiceBaseImpl;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.Timesheet;
import com.axelor.apps.hr.db.TimesheetLine;
import com.axelor.apps.hr.db.repo.TimesheetRepository;
import com.axelor.apps.hr.exception.HumanResourceExceptionMessage;
import com.axelor.apps.hr.service.HRMenuTagService;
import com.axelor.apps.hr.service.HRMenuValidateService;
import com.axelor.apps.hr.service.project.ProjectPlanningTimeService;
import com.axelor.apps.hr.service.timesheet.TimesheetAttrsService;
import com.axelor.apps.hr.service.timesheet.TimesheetBusinessService;
import com.axelor.apps.hr.service.timesheet.TimesheetDomainService;
import com.axelor.apps.hr.service.timesheet.TimesheetLeaveService;
import com.axelor.apps.hr.service.timesheet.TimesheetLineCreateService;
import com.axelor.apps.hr.service.timesheet.TimesheetLineGenerationService;
import com.axelor.apps.hr.service.timesheet.TimesheetProjectPlanningTimeService;
import com.axelor.apps.hr.service.timesheet.TimesheetRemoveService;
import com.axelor.apps.hr.service.timesheet.TimesheetViewService;
import com.axelor.apps.hr.service.timesheet.TimesheetWorkflowService;
import com.axelor.apps.project.db.ProjectPlanningTime;
import com.axelor.apps.project.db.repo.ProjectPlanningTimeRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.message.db.Message;
import com.axelor.message.db.repo.MessageRepository;
import com.axelor.meta.CallMethod;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.axelor.utils.helpers.date.LocalDateHelper;
import jakarta.inject.Singleton;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class TimesheetController {

  private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public void prefillLines(ActionRequest request, ActionResponse response) {
    try {
      Timesheet timesheet = request.getContext().asType(Timesheet.class);
      Beans.get(TimesheetLeaveService.class).prefillLines(timesheet);
      response.setValues(timesheet);
    } catch (AxelorException e) {
      TraceBackService.trace(response, e);
    }
  }

  public void generateLines(ActionRequest request, ActionResponse response) throws AxelorException {
    try {
      Context context = request.getContext();
      Timesheet timesheet = context.asType(Timesheet.class);
      timesheet = Beans.get(TimesheetLineGenerationService.class).generateLines(context, timesheet);
      response.setValue("timesheetLineList", timesheet.getTimesheetLineList());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void editTimesheet(ActionRequest request, ActionResponse response) {
    User user = AuthUtils.getUser();
    response.setView(Beans.get(TimesheetViewService.class).buildEditTimesheetView(user));
  }

  public void allTimesheet(ActionRequest request, ActionResponse response) {
    User user = AuthUtils.getUser();
    Employee employee = user.getEmployee();
    response.setView(Beans.get(TimesheetViewService.class).buildAllTimesheetView(user, employee));
  }

  public void allTimesheetLine(ActionRequest request, ActionResponse response) {

    User user = AuthUtils.getUser();
    Employee employee = user.getEmployee();

    ActionViewBuilder actionView =
        ActionView.define(I18n.get("See timesheet lines"))
            .model(TimesheetLine.class.getName())
            .add("grid", "timesheet-line-grid")
            .add("form", "timesheet-line-form");

    Beans.get(TimesheetDomainService.class)
        .createDomainAllTimesheetLine(user, employee, actionView);

    response.setView(actionView.map());
  }

  public void validateTimesheet(ActionRequest request, ActionResponse response) {

    User user = AuthUtils.getUser();
    Employee employee = user.getEmployee();

    ActionViewBuilder actionView =
        ActionView.define(I18n.get("Timesheets to Validate"))
            .model(Timesheet.class.getName())
            .add("grid", "timesheet-validate-grid")
            .add("form", "timesheet-form")
            .param("search-filters", "timesheet-filters")
            .context(
                "todayDate", Beans.get(AppBaseService.class).getTodayDate(user.getActiveCompany()));

    Beans.get(HRMenuValidateService.class).createValidateDomain(user, employee, actionView);

    response.setView(actionView.map());
  }

  public void validateTimesheetLine(ActionRequest request, ActionResponse response) {

    User user = AuthUtils.getUser();
    Employee employee = user.getEmployee();

    ActionViewBuilder actionView =
        ActionView.define(I18n.get("See timesheet lines"))
            .model(TimesheetLine.class.getName())
            .add("grid", "timesheet-line-grid")
            .add("form", "timesheet-line-form")
            .context(
                "todayDate", Beans.get(AppBaseService.class).getTodayDate(user.getActiveCompany()));

    Beans.get(TimesheetDomainService.class)
        .createValidateDomainTimesheetLine(user, employee, actionView);

    response.setView(actionView.map());
  }

  public void editTimesheetSelected(ActionRequest request, ActionResponse response) {
    Map<?, ?> timesheetMap = (Map<?, ?>) request.getContext().get("timesheetSelect");
    Long timesheetId = Long.valueOf(timesheetMap.get("id").toString());
    response.setView(
        Beans.get(TimesheetViewService.class).buildEditSelectedTimesheetView(timesheetId));
  }

  public void historicTimesheet(ActionRequest request, ActionResponse response) {
    User user = AuthUtils.getUser();
    Employee employee = user.getEmployee();
    response.setView(
        Beans.get(TimesheetViewService.class).buildHistoricTimesheetView(user, employee));
  }

  public void historicTimesheetLine(ActionRequest request, ActionResponse response) {
    User user = AuthUtils.getUser();
    Employee employee = user.getEmployee();
    response.setView(
        Beans.get(TimesheetViewService.class).buildHistoricTimesheetLineView(user, employee));
  }

  public void showSubordinateTimesheets(ActionRequest request, ActionResponse response) {
    User user = AuthUtils.getUser();
    Map<String, Object> actionView =
        Beans.get(TimesheetViewService.class).buildSubordinateTimesheetsView(user);
    if (actionView == null) {
      response.setNotify(I18n.get("No timesheet to be validated by your subordinates"));
    } else {
      response.setView(actionView);
    }
  }

  public void cancel(ActionRequest request, ActionResponse response) {
    try {
      Timesheet timesheet = request.getContext().asType(Timesheet.class);
      timesheet = Beans.get(TimesheetRepository.class).find(timesheet.getId());

      Message message =
          Beans.get(TimesheetWorkflowService.class).cancelAndSendCancellationEmail(timesheet);
      if (message != null && message.getStatusSelect() == MessageRepository.STATUS_SENT) {
        response.setInfo(
            String.format(
                I18n.get("Email sent to %s"),
                Beans.get(MessageServiceBaseImpl.class).getToRecipients(message)));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    } finally {
      response.setReload(true);
    }
  }

  /**
   * Called from timesheet form view, on clicking "return to draft" button. <br>
   * Call {@link TimesheetWorkflowService#draft(Timesheet)}
   *
   * @param request
   * @param response
   */
  public void draft(ActionRequest request, ActionResponse response) {
    try {
      Timesheet timesheet = request.getContext().asType(Timesheet.class);
      timesheet = Beans.get(TimesheetRepository.class).find(timesheet.getId());

      Beans.get(TimesheetWorkflowService.class).draft(timesheet);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Action called when confirming a timesheet. Changing status + Sending mail to Manager
   *
   * @param request
   * @param response
   */
  public void confirm(ActionRequest request, ActionResponse response) {

    try {
      Timesheet timesheet = request.getContext().asType(Timesheet.class);
      timesheet = Beans.get(TimesheetRepository.class).find(timesheet.getId());

      Message message =
          Beans.get(TimesheetWorkflowService.class).confirmAndSendConfirmationEmail(timesheet);

      if (message != null && message.getStatusSelect() == MessageRepository.STATUS_SENT) {
        response.setInfo(
            String.format(
                I18n.get("Email sent to %s"),
                Beans.get(MessageServiceBaseImpl.class).getToRecipients(message)));
      }

      response.setReload(true);

    } catch (Exception e) {
      TraceBackService.trace(e);
      response.setError(e.getMessage());
    }
  }

  // Continue button
  public void continueBtn(ActionRequest request, ActionResponse response) {
    response.setView(
        ActionView.define(I18n.get("Timesheet"))
            .model(Timesheet.class.getName())
            .add("form", "timesheet-form")
            .add("grid", "timesheet-grid")
            .param("search-filters", "timesheet-filters")
            .domain("self.employee.user = :_user")
            .context("_user", AuthUtils.getUser())
            .map());
  }

  // Confirm and continue button
  public void confirmContinue(ActionRequest request, ActionResponse response) {
    this.confirm(request, response);
    this.continueBtn(request, response);
  }

  /**
   * Action called when validating a timesheet. Changing status + Sending mail to Applicant
   *
   * @param request
   * @param response
   * @throws AxelorException
   */
  public void valid(ActionRequest request, ActionResponse response) {

    try {
      Timesheet timesheet = request.getContext().asType(Timesheet.class);
      timesheet = Beans.get(TimesheetRepository.class).find(timesheet.getId());

      Message message =
          Beans.get(TimesheetWorkflowService.class).validateAndSendValidationEmail(timesheet);
      if (message != null && message.getStatusSelect() == MessageRepository.STATUS_SENT) {
        response.setInfo(
            String.format(
                I18n.get("Email sent to %s"),
                Beans.get(MessageServiceBaseImpl.class).getToRecipients(message)));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    } finally {
      response.setReload(true);
    }
  }

  public void complete(ActionRequest request, ActionResponse response) {
    try {
      Timesheet timesheet = request.getContext().asType(Timesheet.class);
      timesheet = Beans.get(TimesheetRepository.class).find(timesheet.getId());
      // confirm
      Message message = Beans.get(TimesheetWorkflowService.class).complete(timesheet);
      if (message != null && message.getStatusSelect() == MessageRepository.STATUS_SENT) {
        response.setInfo(
            String.format(
                I18n.get("Email sent to %s"),
                Beans.get(MessageServiceBaseImpl.class).getToRecipients(message)));
      }
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  // action called when refusing a timesheet. Changing status + Sending mail to Applicant
  public void refuse(ActionRequest request, ActionResponse response) {

    try {
      Timesheet timesheet = request.getContext().asType(Timesheet.class);
      timesheet = Beans.get(TimesheetRepository.class).find(timesheet.getId());

      Message message =
          Beans.get(TimesheetWorkflowService.class).refuseAndSendRefusalEmail(timesheet);
      if (message != null && message.getStatusSelect() == MessageRepository.STATUS_SENT) {
        response.setInfo(
            String.format(
                I18n.get("Email sent to %s"),
                Beans.get(MessageServiceBaseImpl.class).getToRecipients(message)));
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    } finally {
      response.setReload(true);
    }
  }

  /* Count Tags displayed on the menu items */
  @CallMethod
  public String timesheetValidateMenuTag() {

    return Beans.get(HRMenuTagService.class)
        .countRecordsTag(Timesheet.class, TimesheetRepository.STATUS_CONFIRMED);
  }

  public void setShowActivity(ActionRequest request, ActionResponse response) {
    Timesheet timesheet = request.getContext().asType(Timesheet.class);
    Map<String, Object> values =
        Beans.get(TimesheetBusinessService.class).computeShowActivity(timesheet);
    response.setValues(values);
  }

  public void openTimesheetEditor(ActionRequest request, ActionResponse response) {

    Context context = request.getContext();

    String url =
        "hr/timesheet/?timesheetId="
            + context.get("id")
            + "&showActivity="
            + context.get("showActivity")
            + "&dailyLimit="
            + context.get("dailyLimit");

    response.setView(
        ActionView.define(I18n.get("Timesheet lines"))
            .add("html", url)
            .param("popup", "reload")
            .param("popup-save", "false")
            .map());
  }

  public void timesheetPeriodTotalController(ActionRequest request, ActionResponse response) {

    try {
      Timesheet timesheet = request.getContext().asType(Timesheet.class);
      response.setAttrs(Beans.get(TimesheetAttrsService.class).getPeriodTotalsAttrsMap(timesheet));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void generateLinesFromExpectedPlanning(ActionRequest request, ActionResponse response) {
    try {
      Timesheet timesheet = request.getContext().asType(Timesheet.class);
      timesheet = Beans.get(TimesheetRepository.class).find(timesheet.getId());
      Beans.get(TimesheetProjectPlanningTimeService.class)
          .generateLinesFromExpectedProjectPlanning(timesheet);
      response.setReload(true);
    } catch (AxelorException e) {
      TraceBackService.trace(response, e);
    }
  }

  public void removeAfterToDateTimesheetLines(ActionRequest request, ActionResponse response) {

    Timesheet timesheet = request.getContext().asType(Timesheet.class);
    if (timesheet.getTimesheetLineList() != null && !timesheet.getTimesheetLineList().isEmpty()) {
      Beans.get(TimesheetRemoveService.class).removeAfterToDateTimesheetLines(timesheet);
    }
    response.setValues(timesheet);
  }

  public void initProjectPlanningTime(ActionRequest request, ActionResponse response) {
    Timesheet timesheet = request.getContext().asType(Timesheet.class);

    Employee employee = timesheet.getEmployee();
    LocalDate fromDate = timesheet.getFromDate();
    LocalDate toDate = timesheet.getToDate();

    List<ProjectPlanningTime> projectPlanningTimeList =
        Beans.get(ProjectPlanningTimeService.class)
            .getProjectPlanningTimeIdList(employee, fromDate, toDate);

    if (!ObjectUtils.isEmpty(projectPlanningTimeList)
        && TimesheetRepository.STATUS_DRAFT == timesheet.getStatusSelect()) {
      response.setValue("$projectPlanningTimeList", projectPlanningTimeList);
      LocalDate todayDate = Beans.get(AppBaseService.class).getTodayDate(timesheet.getCompany());
      response.setValue(
          "$generationDate",
          LocalDateHelper.isBetween(fromDate, toDate, todayDate) ? todayDate : null);

      response.setAttr("projectPlanningTimePanel", "hidden", false);
    } else {
      response.setAttr("projectPlanningTimePanel", "hidden", true);
    }
  }

  public void generateTimesheetLine(ActionRequest request, ActionResponse response)
      throws AxelorException {
    Timesheet timesheet = request.getContext().asType(Timesheet.class);
    Object generationDateObject = request.getContext().get("generationDate");

    if (generationDateObject == null) {
      response.setError(I18n.get(HumanResourceExceptionMessage.NO_TIMESHEET_GENERATED_DATE));
      return;
    }
    LocalDate generationDate = LocalDate.parse(generationDateObject.toString());
    if (!LocalDateHelper.isBetween(
        timesheet.getFromDate(), timesheet.getToDate(), generationDate)) {
      response.setError(I18n.get(HumanResourceExceptionMessage.DATE_NOT_IN_TIMESHEET_PERIOD));
      return;
    }

    ProjectPlanningTimeRepository projectPlanningTimeRepository =
        Beans.get(ProjectPlanningTimeRepository.class);

    List<Pair<ProjectPlanningTime, BigDecimal>> projectPlanningTimeListWithDuration =
        ((List<Map<String, Object>>) request.getContext().get("projectPlanningTimeList"))
            .stream()
                .filter(
                    it ->
                        Objects.nonNull(it.get("duration"))
                            && (new BigDecimal(String.valueOf(it.get("duration")))).signum() > 0)
                .map(
                    ppt ->
                        Pair.of(
                            projectPlanningTimeRepository.find(
                                Optional.of(ppt)
                                    .map(it -> it.get("id"))
                                    .map(String::valueOf)
                                    .map(Long::valueOf)
                                    .orElse(0L)),
                            new BigDecimal(
                                Optional.of(ppt)
                                    .map(it -> it.get("duration"))
                                    .map(String::valueOf)
                                    .orElse("0"))))
                .collect(Collectors.toList());
    if (!ObjectUtils.isEmpty(projectPlanningTimeListWithDuration)) {
      Beans.get(TimesheetLineCreateService.class)
          .createTimesheetLinesUsingPlanning(
              projectPlanningTimeListWithDuration, generationDate, timesheet);
      response.setValues(timesheet);
    } else {
      response.setError(I18n.get(HumanResourceExceptionMessage.NO_TIMESHEET_LINE_GENERATED));
    }
  }

  public void validateGenerationDate(ActionRequest request, ActionResponse response)
      throws AxelorException {
    Timesheet timesheet = request.getContext().asType(Timesheet.class);
    LocalDate generationDate =
        LocalDate.parse(request.getContext().get("generationDate").toString());

    if (!LocalDateHelper.isBetween(
        timesheet.getFromDate(), timesheet.getToDate(), generationDate)) {
      response.setValue("$generationDate", null);
      response.setNotify(I18n.get(HumanResourceExceptionMessage.INVALID_DATES));
    }
  }

  @SuppressWarnings("unchecked")
  public void clearProjectPlanningTimesDuration(ActionRequest request, ActionResponse response) {
    if (request.getContext().get("projectPlanningTimeList") == null) {
      return;
    }
    List<Map<String, Object>> projectPlanningTimeList =
        (List<Map<String, Object>>) request.getContext().get("projectPlanningTimeList");
    projectPlanningTimeList.forEach(
        it -> {
          it.put("$duration", BigDecimal.ZERO);
        });
    response.setValue("$projectPlanningTimeList", projectPlanningTimeList);
  }
}
