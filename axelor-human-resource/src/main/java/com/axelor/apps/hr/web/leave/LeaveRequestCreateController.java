package com.axelor.apps.hr.web.leave;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.hr.db.LeaveRequest;
import com.axelor.apps.hr.service.leave.LeaveRequestCreateHelperDurationService;
import com.axelor.apps.hr.service.leave.LeaveRequestCreateHelperService;
import com.axelor.apps.hr.service.leave.compute.LeaveRequestComputeDayDurationService;
import com.axelor.apps.hr.service.leavereason.LeaveReasonDomainService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class LeaveRequestCreateController {

  public void create(ActionRequest request, ActionResponse response) throws AxelorException {
    LocalDate fromDate =
        request.getContext().get("fromDate") != null
            ? LocalDate.parse((String) request.getContext().get("fromDate"))
            : null;
    int startOnSelect =
        request.getContext().get("startOnSelect") != null
            ? (int) request.getContext().get("startOnSelect")
            : 0;

    List<HashMap<String, Object>> leaveReasonList =
        (List<HashMap<String, Object>>) request.getContext().get("leaveReasonList");

    List<Long> idList =
        Beans.get(LeaveRequestCreateHelperService.class)
            .createLeaveRequests(fromDate, startOnSelect, leaveReasonList);

    response.setCanClose(true);

    if (!CollectionUtils.isEmpty(idList)) {
      response.setView(
          ActionView.define(I18n.get("Leave requests"))
              .model(LeaveRequest.class.getName())
              .add("grid", "leave-request-grid")
              .add("form", "leave-request-form")
              .domain(
                  "self.id in ("
                      + idList.stream().map(Object::toString).collect(Collectors.joining(","))
                      + ")")
              .map());
    }
  }

  public void checkDuration(ActionRequest request, ActionResponse response) throws AxelorException {
    LeaveRequestCreateHelperDurationService leaveRequestCreateHelperDurationService =
        Beans.get(LeaveRequestCreateHelperDurationService.class);
    List<HashMap<String, Object>> leaveReasonList =
        (List<HashMap<String, Object>>) request.getContext().get("leaveReasonList");
    BigDecimal duration =
        request.getContext().get("duration") != null
            ? new BigDecimal((String) request.getContext().get("duration"))
            : BigDecimal.ZERO;
    BigDecimal totalDuration =
        leaveRequestCreateHelperDurationService.getTotalDuration(leaveReasonList);

    leaveRequestCreateHelperDurationService.checkDuration(duration, totalDuration);
  }

  public void computeDuration(ActionRequest request, ActionResponse response)
      throws AxelorException {
    LocalDate fromDate =
        request.getContext().get("fromDate") != null
            ? LocalDate.parse((String) request.getContext().get("fromDate"))
            : null;
    LocalDate toDate =
        request.getContext().get("toDate") != null
            ? LocalDate.parse((String) request.getContext().get("toDate"))
            : null;
    int startOnSelect =
        request.getContext().get("startOnSelect") != null
            ? (int) request.getContext().get("startOnSelect")
            : 0;
    int endOnSelect =
        request.getContext().get("endOnSelect") != null
            ? (int) request.getContext().get("endOnSelect")
            : 0;

    User user = AuthUtils.getUser();
    Company company = Optional.ofNullable(user).map(User::getActiveCompany).orElse(null);
    Employee employee = Optional.ofNullable(user).map(User::getEmployee).orElse(null);

    response.setValue(
        "$duration",
        Beans.get(LeaveRequestComputeDayDurationService.class)
            .computeDurationInDays(
                company, employee, fromDate, toDate, startOnSelect, endOnSelect));
  }

  public void getLeaveReasonDomain(ActionRequest request, ActionResponse response) {
    Employee employee =
        Optional.ofNullable(AuthUtils.getUser()).map(User::getEmployee).orElse(null);
    response.setAttr(
        "leaveReason",
        "domain",
        Beans.get(LeaveReasonDomainService.class).getLeaveReasonDomain(employee));
  }
}
