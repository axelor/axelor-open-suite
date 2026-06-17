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
package com.axelor.apps.hr.web.leave;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.PeriodService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.base.service.message.MessageServiceBaseImpl;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.LeaveLine;
import com.axelor.apps.hr.db.LeaveRequest;
import com.axelor.apps.hr.db.repo.LeaveRequestRepository;
import com.axelor.apps.hr.service.EmployeeComputeAvailableLeaveService;
import com.axelor.apps.hr.service.HRMenuTagService;
import com.axelor.apps.hr.service.HRMenuValidateService;
import com.axelor.apps.hr.service.leave.LeaveBusinessService;
import com.axelor.apps.hr.service.leave.LeaveExportService;
import com.axelor.apps.hr.service.leave.LeaveRequestCancelService;
import com.axelor.apps.hr.service.leave.LeaveRequestMailService;
import com.axelor.apps.hr.service.leave.LeaveRequestRefuseService;
import com.axelor.apps.hr.service.leave.LeaveRequestSendService;
import com.axelor.apps.hr.service.leave.LeaveRequestService;
import com.axelor.apps.hr.service.leave.LeaveRequestValidateService;
import com.axelor.apps.hr.service.leave.LeaveViewService;
import com.axelor.apps.hr.service.leave.compute.LeaveRequestComputeDurationService;
import com.axelor.apps.hr.service.leavereason.LeaveReasonDomainService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.common.StringUtils;
import com.axelor.db.JpaSecurity;
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
import jakarta.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
public class LeaveController {

  public void editLeave(ActionRequest request, ActionResponse response) {
    User user = AuthUtils.getUser();
    response.setView(Beans.get(LeaveViewService.class).buildEditLeaveView(user));
  }

  @SuppressWarnings("unchecked")
  public void editLeaveSelected(ActionRequest request, ActionResponse response) {
    Map<String, Object> leaveMap = (Map<String, Object>) request.getContext().get("leaveSelect");
    if (leaveMap == null) {
      response.setError(I18n.get("Select the leave request you want to edit"));
    } else {
      Long leaveId = Long.valueOf(leaveMap.get("id").toString());
      response.setView(Beans.get(LeaveViewService.class).buildEditSelectedLeaveView(leaveId));
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
    User user = AuthUtils.getUser();
    Employee employee = user.getEmployee();
    response.setView(Beans.get(LeaveViewService.class).buildHistoricLeaveView(user, employee));
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
    User user = AuthUtils.getUser();
    Map<String, Object> actionView =
        Beans.get(LeaveViewService.class).buildSubordinateLeavesView(user);
    if (actionView == null) {
      response.setNotify(I18n.get("No Leave Request to be validated by your subordinates"));
    } else {
      response.setView(actionView);
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

  public void leaveReasonToJustify(ActionRequest request, ActionResponse response) {
    try {
      LeaveRequest leave = request.getContext().asType(LeaveRequest.class);
      LeaveLine leaveLine =
          Beans.get(LeaveBusinessService.class).processLeaveReasonToJustify(leave);
      if (leaveLine != null) {
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

  public void exportLeaveRequest(ActionRequest request, ActionResponse response) {
    try {
      List<Long> ids = getLeaveRequestIds(request);
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

  @SuppressWarnings("unchecked")
  public List<Long> getLeaveRequestIds(ActionRequest request) {
    Context context = request.getContext();
    if (context.get("_ids") == null) {
      JpaSecurity security = Beans.get(JpaSecurity.class);
      return Criteria.parse(request)
          .createQuery(
              LeaveRequest.class, security.getFilter(JpaSecurity.CAN_READ, LeaveRequest.class))
          .select("id")
          .fetch(0, 0)
          .stream()
          .map(m -> (Long) m.get("id"))
          .collect(Collectors.toList());
    }
    List<Integer> idIntList = (List<Integer>) context.get("_ids");
    return idIntList.stream().map(Long::valueOf).collect(Collectors.toList());
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
