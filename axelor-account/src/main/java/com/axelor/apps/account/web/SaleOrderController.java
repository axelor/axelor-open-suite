/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.web;

import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class SaleOrderController {

  private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public void emptyFiscalPositionIfNotCompatible(ActionRequest request, ActionResponse response) {
    try {
      SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);
      FiscalPosition soFiscalPosition = saleOrder.getFiscalPosition();
      if (saleOrder.getTaxNumber() == null || soFiscalPosition == null) {
        return;
      }
      for (FiscalPosition fiscalPosition : saleOrder.getTaxNumber().getFiscalPositionSet()) {
        if (fiscalPosition.getId().equals(soFiscalPosition.getId())) {
          return;
        }
      }
      response.setValue("fiscalPosition", null);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
