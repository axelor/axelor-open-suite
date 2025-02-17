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
package com.axelor.apps.businessproduction.web;

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.businessproduction.exception.BusinessProductionExceptionMessage;
import com.axelor.apps.businessproduction.service.ManufOrderServiceBusinessImpl;
import com.axelor.apps.businessproduction.service.ManufOrderValidateBusinessService;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ManufOrderBusinessController {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public void propagateIsToInvoice(ActionRequest request, ActionResponse response) {

    ManufOrderServiceBusinessImpl manufOrderService =
        Beans.get(ManufOrderServiceBusinessImpl.class);
    ManufOrder manufOrder = request.getContext().asType(ManufOrder.class);

    manufOrderService.propagateIsToInvoice(
        Beans.get(ManufOrderRepository.class).find(manufOrder.getId()));

    response.setReload(true);
  }

  /**
   * Called from operation order view before finish. Alert the user if we will use timesheet waiting
   * validation for the real duration of the operation order.
   *
   * @param request
   * @param response
   */
  public void alertNonValidatedTimesheet(ActionRequest request, ActionResponse response) {
    try {
      ManufOrder manufOrder = request.getContext().asType(ManufOrder.class);
      if (Beans.get(AppProductionService.class).getAppProduction().getEnableTimesheetOnManufOrder()
          && Beans.get(ManufOrderValidateBusinessService.class).checkTimesheet(manufOrder) > 0) {
        response.setAlert(
            I18n.get(BusinessProductionExceptionMessage.MANUF_ORDER_TIMESHEET_WAITING_VALIDATION));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
