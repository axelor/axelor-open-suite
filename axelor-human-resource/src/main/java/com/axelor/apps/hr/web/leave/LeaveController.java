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
package com.axelor.apps.hr.web.leave;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.PeriodService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.base.service.message.MessageServiceBaseImpl;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.LeaveLine;
import com.axelor.apps.hr.db.LeaveReason;
import com.axelor.apps.hr.db.LeaveRequest;
import com.axelor.apps.hr.db.repo.EmployeeRepository;
import com.axelor.apps.hr.db.repo.LeaveReasonRepository;
import com.axelor.apps.hr.db.repo.LeaveRequestRepository;
import com.axelor.apps.hr.service.EmployeeComputeAvailableLeaveService;
import com.axelor.apps.hr.service.HRMenuTagService;
import com.axelor.apps.hr.service.HRMenuValidateService;
import com.axelor.apps.hr.service.config.HRConfigService;
import com.axelor.apps.hr.service.leave.LeaveExportService;
import com.axelor.apps.hr.service.leave.LeaveLineService;
import com.axelor.apps.hr.service.leave.LeaveRequestCancelService;
import com.axelor.apps.hr.service.leave.LeaveRequestMailService;
import com.axelor.apps.hr.service.leave.LeaveRequestRefuseService;
import com.axelor.apps.hr.service.leave.LeaveRequestSendService;
import com.axelor.apps.hr.service.leave.LeaveRequestService;
import com.axelor.apps.hr.service.leave.LeaveRequestValidateService;
import com.axelor.apps.hr.service.leave.compute.LeaveRequestComputeDurationService;
import com.axelor.apps.hr.service.leavereason.LeaveReasonDomainService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.common.StringUtils;
import com.axelor.db.JpaSecurity;
import com.axelor.db.Query;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.message.db.Message;
import com.axelor.message.db.repo.MessageRepository;
import com.axelor.meta.CallMethod;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.axelor.rpc.Criteria;
import com.axelor.utils.db.Wizard;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
public class LeaveController {

  public void editLeave(ActionRequest request, ActionResponse response) {
    try {

      User user = AuthUtils.getUser();

      List<LeaveRequest> leaveList =
          Beans.get(LeaveRequestRepository.class)
              .all()
              .filter(
                  "self.employee.user.id = ?1 AND self.company = ?2 AND self.statusSelect = 1",
                  user.getId(),
                  user.getActiveCompany())
              .fetch();
      if (leaveList.isEmpty()) {
        response.setView(
            ActionView.define(I18n.get("LeaveRequest"))
                .model(LeaveRequest.class.getName())
                .add("form", "complete-my-leave-request-form")
                .context("_isEmployeeReadOnly", true)
                .map());
      } else if (leaveList.size() == 1) {
        response.setView(
            ActionView.define(I18n.get("LeaveRequest"))
                .model(LeaveRequest.class.getName())
                .add("form", "complete-my-leave-request-form")
                .param("forceEdit", "true")
                .context("_showRecord", String.valueOf(leaveList.get(0).getId()))
                .context("_isEmployeeReadOnly", true)
                .map());
      } else {
        response.setView(
            ActionView.define(I18n.get("LeaveRequest"))
                .model(Wizard.class.getName())
                .add("form", "popup-leave-request-form")
                .param("forceEdit", "true")
                .param("popup", "true")
                .param("show-toolbar", "false")
                .param("show-confirm", "false")
                .param("forceEdit", "true")
                .param("popup-save", "false")
                .map());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  @SuppressWarnings("unchecked")
  public void editLeaveSelected(ActionRequest request, ActionResponse response) {
    try {
      Map<String, Object> leaveMap = (Map<String, Object>) request.getContext().get("leaveSelect");
      if (leaveMap == null) {
        response.setError(I18n.get("Select the leave request you want to edit"));
      } else {
        Long leaveId = Long.valueOf(leaveMap.get("id").toString());

        response.setView(
            ActionView.define(I18n.get("LeaveRequest"))
                .model(LeaveRequest.class.getName())
                .add("form", "complete-my-leave-request-form")
                .param("forceEdit", "true")
                .domain("self.id = " + leaveId)
                .context("_showRecord", leaveId)
                .context("_isEmployeeReadOnly", true)
                .map());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void validateLeave(ActionRequest request, ActionResponse response) {
    try {
      User user = AuthUtils.getUser();
      Employee employee = user.getEmployee();

      ActionViewBuilder actionView =
          ActionView.define(I18n.get("Leave Requests to Validate"))
              .model(LeaveRequest.class.getName())
              .add("grid", "leave-request-validate-grid")
              .add("form", "leave-request-form")
              .param("search-filters", "leave-request-filters");

      Beans.get(HRMenuValidateService.class).createValidateDomain(user, employee, actionView);

      response.setView(actionView.map());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void historicLeave(ActionRequest request, ActionResponse response) {
    try {

      User user = AuthUtils.getUser();
      Employee employee = user.getEmployee();

      ActionViewBuilder actionView =
          ActionView.define(I18n.get("Colleague Leave Requests"))
              .model(LeaveRequest.class.getName())
              .add("grid", "leave-request-grid")
              .add("form", "leave-request-form")
              .param("search-filters", "leave-request-filters");

      actionView
          .domain("(self.statusSelect IN :statusSelectList)")
          .context(
              "statusSelectList",
              List.of(
                  LeaveRequestRepository.STATUS_VALIDATED, LeaveRequestRepository.STATUS_REFUSED));

      if (employee == null || !employee.getHrManager()) {
        actionView
            .domain(actionView.get().getDomain() + " AND self.employee.managerUser = :_user")
            .context("_user", user);
      }

      response.setView(actionView.map());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void leaveCalendar(ActionRequest request, ActionResponse response) {
    try {
      User user = AuthUtils.getUser();
      ActionViewBuilder actionView =
          ActionView.define(I18n.get("Leaves calendar"))
              .model(LeaveRequest.class.getName())
              .add("calendar", "calendar-event-leave-request")
              .add("grid", "leave-request-grid")
              .add("form", "leave-request-form");

      actionView.domain(Beans.get(LeaveRequestService.class).getLeaveCalendarDomain(user));
      actionView.context("userId", user.getId());
      response.setView(actionView.map());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void showSubordinateLeaves(ActionRequest request, ActionResponse response) {
    try {

      User user = AuthUtils.getUser();

      String domain =
          "self.employee.managerUser.employee.managerUser = :_user AND self.statusSelect = 2";
      long nbLeaveRequests =
          Query.of(LeaveRequest.class).filter(domain).bind("_user", user).count();

      if (nbLeaveRequests == 0) {
        response.setNotify(I18n.get("No Leave Request to be validated by your subordinates"));
      } else {
        ActionViewBuilder actionView =
            ActionView.define(I18n.get("Leaves to be Validated by your subordinates"))
                .model(LeaveRequest.class.getName())
                .add("grid", "leave-request-grid")
                .add("form", "leave-request-form")
                .param("search-filters", "leave-request-filters");
        response.setView(actionView.domain(domain).context("_user", user).map());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void testDuration(ActionRequest request, ActionResponse response) {
    try {
      LeaveRequest leave = request.getContext().asType(LeaveRequest.class);
      double duration = leave.getDuration().doubleValue();
      if (duration % 0.5 != 0) {
        response.setError(I18n.get("Invalid duration (must be a 0.5's multiple)"));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void computeDuration(ActionRequest request, ActionResponse response) {
    try {
      LeaveRequest leave = request.getContext().asType(LeaveRequest.class);
      Employee employee = leave.getEmployee();
      if (employee == null) {
        return;
      }
      response.setValue(
          "duration", Beans.get(LeaveRequestComputeDurationService.class).computeDuration(leave));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  // sending leave request and an email to the manager
  public void send(ActionRequest request, ActionResponse response) throws AxelorException {

    LeaveRequest leaveRequest = request.getContext().asType(LeaveRequest.class);
    leaveRequest = Beans.get(LeaveRequestRepository.class).find(leaveRequest.getId());

    String notifyMessage = Beans.get(LeaveRequestSendService.class).send(leaveRequest);
    if (StringUtils.notEmpty(notifyMessage)) {
      response.setNotify(notifyMessage);
    }

    Message message = Beans.get(LeaveRequestMailService.class).sendConfirmationEmail(leaveRequest);
    if (message != null && message.getStatusSelect() == MessageRepository.STATUS_SENT) {
      response.setInfo(
          String.format(
              I18n.get("Email sent to %s"),
              Beans.get(MessageServiceBaseImpl.class).getToRecipients(message)));
    }
    response.setReload(true);
  }

  /**
   * Validates leave request and sends an email to the applicant.
   *
   * @param request
   * @param response
   * @throws AxelorException
   */
  public void validate(ActionRequest request, ActionResponse response) throws AxelorException {

    LeaveRequest leaveRequest = request.getContext().asType(LeaveRequest.class);
    leaveRequest = Beans.get(LeaveRequestRepository.class).find(leaveRequest.getId());

    Beans.get(LeaveRequestValidateService.class).validate(leaveRequest);

    Message message = Beans.get(LeaveRequestMailService.class).sendValidationEmail(leaveRequest);
    if (message != null && message.getStatusSelect() == MessageRepository.STATUS_SENT) {
      response.setInfo(
          String.format(
              I18n.get("Email sent to %s"),
              Beans.get(MessageServiceBaseImpl.class).getToRecipients(message)));
    }
    Beans.get(PeriodService.class)
        .checkPeriod(
            leaveRequest.getCompany(),
            leaveRequest.getToDateT().toLocalDate(),
            leaveRequest.getFromDateT().toLocalDate());
    response.setReload(true);
  }

  /**
   * Refuses leave request and sends an email to the applicant.
   *
   * @param request
   * @param response
   * @throws AxelorException
   */
  public void refuse(ActionRequest request, ActionResponse response) {

    try {
      LeaveRequest leaveRequest = request.getContext().asType(LeaveRequest.class);
      leaveRequest = Beans.get(LeaveRequestRepository.class).find(leaveRequest.getId());

      Beans.get(LeaveRequestRefuseService.class).refuse(leaveRequest, null);

      Message message = Beans.get(LeaveRequestMailService.class).sendRefusalEmail(leaveRequest);
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

  public void cancel(ActionRequest request, ActionResponse response) {
    try {
      LeaveRequest leave = request.getContext().asType(LeaveRequest.class);
      leave = Beans.get(LeaveRequestRepository.class).find(leave.getId());
      Beans.get(LeaveRequestCancelService.class).cancel(leave);

      Message message = Beans.get(LeaveRequestMailService.class).sendCancellationEmail(leave);
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

  @Transactional
  public void leaveReasonToJustify(ActionRequest request, ActionResponse response) {
    try {
      LeaveRequest leave = request.getContext().asType(LeaveRequest.class);
      Boolean leaveToJustify = leave.getToJustifyLeaveReason();
      LeaveLine leaveLine = null;

      if (!leaveToJustify) {
        return;
      }
      Company company = leave.getCompany();
      if (leave.getEmployee() == null) {
        return;
      }
      if (company == null && leave.getEmployee().getUser() != null) {
        company = leave.getEmployee().getUser().getActiveCompany();
      }
      if (company == null) {
        return;
      }

      Beans.get(HRConfigService.class).getLeaveReason(company.getHrConfig());

      Employee employee = leave.getEmployee();

      LeaveReason leaveReason =
          Beans.get(LeaveReasonRepository.class)
              .find(company.getHrConfig().getToJustifyLeaveReason().getId());

      if (employee != null) {
        employee = Beans.get(EmployeeRepository.class).find(employee.getId());
        leaveLine =
            Beans.get(LeaveLineService.class).addLeaveReasonOrCreateIt(employee, leaveReason);
        response.setValue("leaveLine", leaveLine);
      }
    } catch (AxelorException e) {
      TraceBackService.trace(response, e);
    }
  }

  @CallMethod
  public String leaveValidateMenuTag() {

    return Beans.get(HRMenuTagService.class)
        .countRecordsTag(LeaveRequest.class, LeaveRequestRepository.STATUS_AWAITING_VALIDATION);
  }

  @SuppressWarnings("unchecked")
  public void exportLeaveRequest(ActionRequest request, ActionResponse response) {
    try {

      Context context = request.getContext();
      List<Long> ids = null;

      if (context.get("_ids") == null) {
        JpaSecurity security = Beans.get(JpaSecurity.class);
        ids =
            Criteria.parse(request)
                .createQuery(
                    LeaveRequest.class,
                    security.getFilter(JpaSecurity.CAN_READ, LeaveRequest.class))
                .select("id")
                .fetch(0, 0)
                .stream()
                .map(m -> (Long) m.get("id"))
                .collect(Collectors.toList());
      } else {
        List<Integer> idIntList = (List<Integer>) context.get("_ids");
        ids = idIntList.stream().map(Long::valueOf).collect(Collectors.toList());
      }

      MetaFile metaFile = Beans.get(LeaveExportService.class).export(ids, request.getUser());

      if (metaFile != null) {
        response.setView(
            ActionView.define(I18n.get("Export file"))
                .model(LeaveRequest.class.getName())
                .add(
                    "html",
                    "ws/rest/com.axelor.meta.db.MetaFile/"
                        + metaFile.getId()
                        + "/content/download?v="
                        + metaFile.getVersion())
                .param("download", "true")
                .map());
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void getLeaveReasonDomain(ActionRequest request, ActionResponse response) {
    LeaveRequest leave = request.getContext().asType(LeaveRequest.class);
    response.setAttr(
        "leaveReason",
        "domain",
        Beans.get(LeaveReasonDomainService.class).getLeaveReasonDomain(leave.getEmployee()));
  }

  public void computeLeaveToDate(ActionRequest request, ActionResponse response)
      throws AxelorException {
    LeaveRequest leave = request.getContext().asType(LeaveRequest.class);
    response.setValue(
        "leaveDaysToDate", Beans.get(LeaveRequestService.class).getLeaveDaysToDate(leave));
  }

  public void computeLeaveQuantity(ActionRequest request, ActionResponse response) {
    LeaveRequest leave = request.getContext().asType(LeaveRequest.class);
    response.setValue(
        "$leavequantity",
        Beans.get(EmployeeComputeAvailableLeaveService.class)
            .computeAvailableLeaveQuantityForActiveUser(
                leave.getEmployee(), leave.getLeaveReason()));
  }
}
