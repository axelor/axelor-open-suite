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
package com.axelor.apps.stock.web;

import com.axelor.apps.stock.db.LogisticalForm;
import com.axelor.apps.stock.db.LogisticalFormLine;
import com.axelor.apps.stock.service.LogisticalFormLineService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;
import java.math.BigDecimal;

@Singleton
public class LogisticalFormLineController {

  public void setQty(ActionRequest request, ActionResponse response) {
    try {
      LogisticalFormLine logisticalFormLine = getLogisticalFormLine(request);
      if (logisticalFormLine.getQty() == null) {
        BigDecimal qty =
            Beans.get(LogisticalFormLineService.class).getUnspreadQty(logisticalFormLine);
        response.setValue("qty", qty);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setStockMoveLineDomain(ActionRequest request, ActionResponse response) {
    try {
      LogisticalFormLine logisticalFormLine = getLogisticalFormLine(request);
      String domain =
          Beans.get(LogisticalFormLineService.class).getStockMoveLineDomain(logisticalFormLine);
      response.setAttr("stockMoveLine", "domain", domain);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void initParcelPallet(ActionRequest request, ActionResponse response) {
    try {
      LogisticalFormLine logisticalFormLine = getLogisticalFormLine(request);
      Beans.get(LogisticalFormLineService.class).initParcelPallet(logisticalFormLine);
      response.setValue("parcelPalletNumber", logisticalFormLine.getParcelPalletNumber());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  private LogisticalFormLine getLogisticalFormLine(ActionRequest request) {
    LogisticalFormLine logisticalFormLine = request.getContext().asType(LogisticalFormLine.class);

    if (logisticalFormLine.getLogisticalForm() == null) {
      logisticalFormLine.setLogisticalForm(
          request.getContext().getParent().asType(LogisticalForm.class));
    }

    return logisticalFormLine;
  }
}
