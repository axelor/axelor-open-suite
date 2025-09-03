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
package com.axelor.apps.hr.web.timesheet;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
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
import com.axelor.apps.hr.service.app.AppHumanResourceService;
import com.axelor.apps.hr.service.project.ProjectPlanningTimeService;
import com.axelor.apps.hr.service.timesheet.TimesheetAttrsService;
import com.axelor.apps.hr.service.timesheet.TimesheetDomainService;
import com.axelor.apps.hr.service.timesheet.TimesheetLeaveService;
import com.axelor.apps.hr.service.timesheet.TimesheetLineCreateService;
import com.axelor.apps.hr.service.timesheet.TimesheetLineGenerationService;
import com.axelor.apps.hr.service.timesheet.TimesheetProjectPlanningTimeService;
import com.axelor.apps.hr.service.timesheet.TimesheetRemoveService;
import com.axelor.apps.hr.service.timesheet.TimesheetWorkflowService;
import com.axelor.apps.hr.service.user.UserHrService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectPlanningTime;
import com.axelor.apps.project.db.repo.ProjectPlanningTimeRepository;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.axelor.db.Query;
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
import com.axelor.utils.db.Wizard;
import com.axelor.utils.helpers.date.LocalDateHelper;
import com.google.inject.Singleton;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

  @SuppressWarnings("unchecked")
  public void generateLines(ActionRequest request, ActionResponse response) throws AxelorException {
    try {
      Timesheet timesheet = request.getContext().asType(Timesheet.class);
      if (timesheet.getEmployee() == null) {
        throw new AxelorException(
            timesheet,
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(HumanResourceExceptionMessage.LEAVE_USER_EMPLOYEE),
            AuthUtils.getUser().getName());
      }

      Context context = request.getContext();

      LocalDate fromGenerationDate = null;
      if (context.get("fromGenerationDate") != null)
        fromGenerationDate =
            LocalDate.parse(
                context.get("fromGenerationDate").toString(), DateTimeFormatter.ISO_DATE);
      LocalDate toGenerationDate = null;
      if (context.get("toGenerationDate") != null)
        toGenerationDate =
            LocalDate.parse(context.get("toGenerationDate").toString(), DateTimeFormatter.ISO_DATE);
      BigDecimal logTime = BigDecimal.ZERO;
      if (context.get("logTime") != null)
        logTime = new BigDecimal(context.get("logTime").toString());

      Map<String, Object> projectContext = (Map<String, Object>) context.get("project");
      Project project = null;
      if (projectContext != null) {
        project =
            Beans.get(ProjectRepository.class)
                .find(((Integer) projectContext.get("id")).longValue());
      }

      Map<String, Object> productContext = (Map<String, Object>) context.get("product");
      Product product = null;
      if (productContext != null) {
        product =
            Beans.get(ProductRepository.class)
                .find(((Integer) productContext.get("id")).longValue());
      }
      if (context.get("showActivity") == null || !(Boolean) context.get("showActivity")) {
        product = Beans.get(UserHrService.class).getTimesheetProduct(timesheet.getEmployee(), null);
      }

      timesheet =
          Beans.get(TimesheetLineGenerationService.class)
              .generateLines(
                  timesheet, fromGenerationDate, toGenerationDate, logTime, project, product);
      response.setValue("timesheetLineList", timesheet.getTimesheetLineList());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void editTimesheet(ActionRequest request, ActionResponse response) {
    List<Timesheet> timesheetList =
        Beans.get(TimesheetRepository.class)
            .all()
            .filter(
                "self.employee.user.id = ?1 AND self.company = ?2 AND self.statusSelect = 1",
                AuthUtils.getUser().getId(),
                Optional.ofNullable(AuthUtils.getUser()).map(User::getActiveCompany).orElse(null))
            .fetch();
    if (timesheetList.isEmpty()) {
      response.setView(
          ActionView.define(I18n.get("Timesheet"))
              .model(Timesheet.class.getName())
              .add("form", "complete-my-timesheet-form")
              .context("_isEmployeeReadOnly", true)
              .map());
    } else if (timesheetList.size() == 1) {
      response.setView(
          ActionView.define(I18n.get("Timesheet"))
              .model(Timesheet.class.getName())
              .add("form", "complete-my-timesheet-form")
              .param("forceEdit", "true")
              .context("_showRecord", String.valueOf(timesheetList.get(0).getId()))
              .context("_isEmployeeReadOnly", true)
              .map());
    } else {
      response.setView(
          ActionView.define(I18n.get("Timesheet"))
              .model(Wizard.class.getName())
              .add("form", "popup-timesheet-form")
              .param("forceEdit", "true")
              .param("popup", "true")
              .param("show-toolbar", "false")
              .param("show-confirm", "false")
              .param("forceEdit", "true")
              .param("popup-save", "false")
              .map());
    }
  }

  public void allTimesheet(ActionRequest request, ActionResponse response) {

    User user = AuthUtils.getUser();
    Employee employee = user.getEmployee();

    ActionViewBuilder actionView =
        ActionView.define(I18n.get("Timesheets"))
            .model(Timesheet.class.getName())
            .add("grid", "all-timesheet-grid")
            .add("form", "timesheet-form")
            .param("search-filters", "timesheet-filters");

    if (employee == null || !employee.getHrManager()) {
      if (employee == null || employee.getManagerUser() == null) {
        actionView
            .domain("self.employee.user.id = :_userId OR self.employee.managerUser = :_user")
            .context("_userId", user.getId());
      } else {
        actionView.domain("self.employee.managerUser = :_user").context("_user", user);
      }
    }

    response.setView(actionView.map());
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
    Timesheet timesheet =
        Beans.get(TimesheetRepository.class).find(Long.valueOf((Integer) timesheetMap.get("id")));
    response.setView(
        ActionView.define(I18n.get("Timesheet"))
            .model(Timesheet.class.getName())
            .add("form", "complete-my-timesheet-form")
            .param("forceEdit", "true")
            .domain("self.id = " + timesheetMap.get("id"))
            .context("_showRecord", String.valueOf(timesheet.getId()))
            .context("_isEmployeeReadOnly", true)
            .map());
  }

  public void historicTimesheet(ActionRequest request, ActionResponse response) {

    User user = AuthUtils.getUser();
    Employee employee = user.getEmployee();

    ActionViewBuilder actionView =
        ActionView.define(I18n.get("Historic colleague Timesheets"))
            .model(Timesheet.class.getName())
            .add("grid", "timesheet-grid")
            .add("form", "timesheet-form")
            .param("search-filters", "timesheet-filters");

    actionView.domain("(self.statusSelect = 3 OR self.statusSelect = 4)");

    if (employee == null || !employee.getHrManager()) {
      actionView
          .domain(actionView.get().getDomain() + " AND self.employee.managerUser = :_user")
          .context("_user", user);
    }

    response.setView(actionView.map());
  }

  public void historicTimesheetLine(ActionRequest request, ActionResponse response) {

    User user = AuthUtils.getUser();
    Employee employee = user.getEmployee();

    ActionViewBuilder actionView =
        ActionView.define(I18n.get("See timesheet lines"))
            .model(TimesheetLine.class.getName())
            .add("grid", "timesheet-line-grid")
            .add("form", "timesheet-line-form");

    actionView
        .domain(
            "self.timesheet.company = :_activeCompany AND (self.timesheet.statusSelect = 3 OR self.timesheet.statusSelect = 4)")
        .context("_activeCompany", user.getActiveCompany());

    if (employee == null || !employee.getHrManager()) {
      actionView
          .domain(
              actionView.get().getDomain() + " AND self.timesheet.employee.managerUser = :_user")
          .context("_user", user);
    }

    response.setView(actionView.map());
  }

  public void showSubordinateTimesheets(ActionRequest request, ActionResponse response) {

    User user = AuthUtils.getUser();
    Company activeCompany = user.getActiveCompany();

    ActionViewBuilder actionView =
        ActionView.define(I18n.get("Timesheets to be Validated by your subordinates"))
            .model(Timesheet.class.getName())
            .add("grid", "timesheet-grid")
            .add("form", "timesheet-form")
            .param("search-filters", "timesheet-filters");

    String domain =
        "self.employee.managerUser.employee.managerUser = :_user AND self.company = :_activeCompany AND self.statusSelect = 2";

    long nbTimesheets =
        Query.of(Timesheet.class)
            .filter(domain)
            .bind("_user", user)
            .bind("_activeCompany", activeCompany)
            .count();

    if (nbTimesheets == 0) {
      response.setNotify(I18n.get("No timesheet to be validated by your subordinates"));
    } else {
      response.setView(
          actionView
              .domain(domain)
              .context("_user", user)
              .context("_activeCompany", activeCompany)
              .map());
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

    boolean showActivity = true;

    if (timesheet.getEmployee() != null) {
      User user = timesheet.getEmployee().getUser();
      if (user != null) {
        Company company = user.getActiveCompany();
        if (company != null && company.getHrConfig() != null) {
          showActivity =
              !company.getHrConfig().getUseUniqueProductForTimesheet()
                  && Beans.get(AppHumanResourceService.class).getAppTimesheet().getEnableActivity();
        }
      }
    }

    Integer dailyLimit = Beans.get(AppHumanResourceService.class).getAppTimesheet().getDailyLimit();

    response.setValue("$dailyLimit", dailyLimit);
    response.setValue("$showActivity", showActivity);
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
