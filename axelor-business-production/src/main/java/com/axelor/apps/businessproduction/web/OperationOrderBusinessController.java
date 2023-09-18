/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.businessproduction.web;

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.businessproduction.exception.BusinessProductionExceptionMessage;
import com.axelor.apps.businessproduction.service.OperationOrderBusinessProductionCheckService;
import com.axelor.apps.businessproduction.service.OperationOrderValidateBusinessService;
import com.axelor.apps.hr.db.repo.EmployeeRepository;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class OperationOrderBusinessController {

  /**
   * Called from operation order view before finish. Alert the user if we will use timesheet waiting
   * validation for the real duration of the operation order.
   *
   * @param request
   * @param response
   */
  public void alertNonValidatedTimesheet(ActionRequest request, ActionResponse response) {
    try {
      OperationOrder operationOrder = request.getContext().asType(OperationOrder.class);
      if (Beans.get(AppProductionService.class).getAppProduction().getEnableTimesheetOnManufOrder()
          && Beans.get(OperationOrderValidateBusinessService.class).checkTimesheet(operationOrder)
              > 0) {
        response.setAlert(
            I18n.get(
                BusinessProductionExceptionMessage.OPERATION_ORDER_TIMESHEET_WAITING_VALIDATION));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void checkUsersHaveEmployee(ActionRequest request, ActionResponse response) {
    try {

      OperationOrderBusinessProductionCheckService operationOrderCheckService =
          Beans.get(OperationOrderBusinessProductionCheckService.class);
      OperationOrder operationOrder = request.getContext().asType(OperationOrder.class);

      AppProductionService appProductionService = Beans.get(AppProductionService.class);

      if (appProductionService.isApp("production")
          && appProductionService.getAppProduction().getManageBusinessProduction()
          && appProductionService.getAppProduction().getAutoGenerateTimesheetLine()) {
        if (!operationOrderCheckService.workingUsersHaveEmployee(operationOrder)) {
          response.setAlert(
              I18n.get(BusinessProductionExceptionMessage.WORKING_USERS_HAVE_NO_EMPLOYEE));
        }
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void checkUsersTSImputation(ActionRequest request, ActionResponse response) {
    try {

      OperationOrderBusinessProductionCheckService operationOrderCheckService =
          Beans.get(OperationOrderBusinessProductionCheckService.class);
      OperationOrder operationOrder = request.getContext().asType(OperationOrder.class);

      AppProductionService appProductionService = Beans.get(AppProductionService.class);

      if (appProductionService.isApp("production")
          && appProductionService.getAppProduction().getManageBusinessProduction()
          && appProductionService.getAppProduction().getAutoGenerateTimesheetLine()) {
        if (!operationOrderCheckService.workingUsersHaveTSImputationSelect(
            operationOrder, EmployeeRepository.TIMESHEET_MANUF_ORDER)) {
          response.setAlert(
              I18n.get(
                  BusinessProductionExceptionMessage
                      .WORKING_USERS_EMPLOYEE_NOT_CORRECT_TIMESHEET_IMPUTATION));
        }
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void checkUsersTimeLoggingPref(ActionRequest request, ActionResponse response) {
    try {

      OperationOrderBusinessProductionCheckService operationOrderCheckService =
          Beans.get(OperationOrderBusinessProductionCheckService.class);
      OperationOrder operationOrder = request.getContext().asType(OperationOrder.class);

      AppProductionService appProductionService = Beans.get(AppProductionService.class);

      if (appProductionService.isApp("production")
          && appProductionService.getAppProduction().getManageBusinessProduction()
          && appProductionService.getAppProduction().getAutoGenerateTimesheetLine()) {
        if (!operationOrderCheckService.workingUsersHaveCorrectTimeLoggingPref(operationOrder)) {
          response.setAlert(
              I18n.get(
                  BusinessProductionExceptionMessage
                      .WORKING_USERS_EMPLOYEE_NOT_CORRECT_TIME_LOGGING_PREF));
        }
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void checkUserTimeLoggingMatch(ActionRequest request, ActionResponse response) {
    try {

      OperationOrderBusinessProductionCheckService operationOrderCheckService =
          Beans.get(OperationOrderBusinessProductionCheckService.class);
      OperationOrder operationOrder = request.getContext().asType(OperationOrder.class);

      AppProductionService appProductionService = Beans.get(AppProductionService.class);

      if (appProductionService.isApp("production")
          && appProductionService.getAppProduction().getManageBusinessProduction()
          && appProductionService.getAppProduction().getAutoGenerateTimesheetLine()) {
        if (!operationOrderCheckService.workingUsersHaveTSTimeLoggingPrefMatching(operationOrder)) {
          response.setAlert(
              I18n.get(
                  BusinessProductionExceptionMessage
                      .WORKING_USERS_EMPLOYEE_NOT_MATCHING_TIME_LOGGING_WITH_TIMESHEET));
        }
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
