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
package com.axelor.apps.maintenance.web;

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.maintenance.exception.MaintenanceExceptionMessage;
import com.axelor.apps.maintenance.service.ManufOrderPrintService;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.birt.core.exception.BirtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ManufOrderController {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  /**
   * Method that generate a Pdf file for an manufacturing order
   *
   * @param request
   * @param response
   * @return
   * @throws BirtException
   * @throws IOException
   */
  public void print(ActionRequest request, ActionResponse response) {

    try {
      ManufOrder manufOrder = request.getContext().asType(ManufOrder.class);
      ManufOrderPrintService manufOrderPrintService = Beans.get(ManufOrderPrintService.class);
      @SuppressWarnings("unchecked")
      List<Integer> selectedManufOrderList = (List<Integer>) request.getContext().get("_ids");

      if (selectedManufOrderList != null) {
        String name = manufOrderPrintService.getManufOrdersFilename();
        String fileLink =
            manufOrderPrintService.printManufOrders(
                selectedManufOrderList.stream()
                    .map(Integer::longValue)
                    .collect(Collectors.toList()));
        LOG.debug("Printing {}", name);
        response.setView(ActionView.define(name).add("html", fileLink).map());
      } else if (manufOrder.getId() != null) {
        String name = manufOrderPrintService.getFileName(manufOrder);
        String fileLink = manufOrderPrintService.printManufOrder(manufOrder);
        LOG.debug("Printing {}", name);
        response.setView(ActionView.define(name).add("html", fileLink).map());
      } else {
        response.setInfo(I18n.get(MaintenanceExceptionMessage.MANUF_ORDER_1));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
