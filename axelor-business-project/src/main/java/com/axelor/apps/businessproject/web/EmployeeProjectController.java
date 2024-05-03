package com.axelor.apps.businessproject.web;

import com.axelor.apps.account.service.analytic.AnalyticAttrsService;
import com.axelor.apps.account.service.analytic.AnalyticGroupService;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.businessproject.model.AnalyticLineEmployeeModel;
import com.axelor.apps.hr.db.Employee;
import com.axelor.apps.supplychain.service.AnalyticLineModelService;
import com.axelor.auth.db.User;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class EmployeeProjectController {

  public void createAnalyticDistributionWithTemplate(
      ActionRequest request, ActionResponse response) {
    try {
      Employee employee = request.getContext().asType(Employee.class);
      AnalyticLineEmployeeModel analyticLineEmployeeModel = new AnalyticLineEmployeeModel(employee);

      Beans.get(AnalyticLineModelService.class)
          .createAnalyticDistributionWithTemplate(analyticLineEmployeeModel);

      response.setValue(
          "analyticMoveLineList", analyticLineEmployeeModel.getAnalyticMoveLineList());
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void printAnalyticAccounts(ActionRequest request, ActionResponse response) {
    try {
      Employee employee = request.getContext().asType(Employee.class);

      Company activeCompany =
          Optional.ofNullable(employee)
              .map(Employee::getUser)
              .map(User::getActiveCompany)
              .orElse(null);
      if (activeCompany == null) {
        return;
      }

      AnalyticLineEmployeeModel analyticLineEmployeeModel = new AnalyticLineEmployeeModel(employee);
      response.setValues(
          Beans.get(AnalyticGroupService.class)
              .getAnalyticAccountValueMap(analyticLineEmployeeModel, activeCompany));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setAxisDomains(ActionRequest request, ActionResponse response) {
    try {
      Employee employee = request.getContext().asType(Employee.class);

      if (employee == null) {
        return;
      }

      AnalyticLineEmployeeModel analyticLineEmployeeModel = new AnalyticLineEmployeeModel(employee);
      response.setAttrs(
          Beans.get(AnalyticGroupService.class)
              .getAnalyticAxisDomainAttrsMap(
                  analyticLineEmployeeModel,
                  Optional.ofNullable(employee.getUser())
                      .map(User::getActiveCompany)
                      .orElse(null)));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void createAnalyticAccountLines(ActionRequest request, ActionResponse response) {
    try {
      Employee employee = request.getContext().asType(Employee.class);

      if (employee == null) {
        return;
      }

      AnalyticLineEmployeeModel analyticLineEmployeeModel = new AnalyticLineEmployeeModel(employee);

      if (Beans.get(AnalyticLineModelService.class)
          .analyzeAnalyticLineModel(
              analyticLineEmployeeModel,
              Optional.ofNullable(employee.getUser()).map(User::getActiveCompany).orElse(null))) {
        response.setValue(
            "analyticMoveLineList", analyticLineEmployeeModel.getAnalyticMoveLineList());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void manageAxis(ActionRequest request, ActionResponse response) {
    try {
      Employee employee = request.getContext().asType(Employee.class);
      Company company =
          Optional.ofNullable(employee)
              .map(Employee::getUser)
              .map(User::getActiveCompany)
              .orElse(null);

      if (company == null) {
        return;
      }

      Map<String, Map<String, Object>> attrsMap = new HashMap<>();
      Beans.get(AnalyticAttrsService.class).addAnalyticAxisAttrs(company, null, attrsMap);

      response.setAttrs(attrsMap);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
