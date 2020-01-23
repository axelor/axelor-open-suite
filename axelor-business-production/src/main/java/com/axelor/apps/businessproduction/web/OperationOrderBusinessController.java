/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.businessproduction.web;

import com.axelor.apps.businessproduction.exception.IExceptionMessage;
import com.axelor.apps.businessproduction.service.OperationOrderValidateBusinessService;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.exception.service.TraceBackService;
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
        response.setAlert(I18n.get(IExceptionMessage.OPERATION_ORDER_TIMESHEET_WAITING_VALIDATION));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
