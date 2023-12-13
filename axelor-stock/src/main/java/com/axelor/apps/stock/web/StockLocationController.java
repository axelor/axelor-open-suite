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
package com.axelor.apps.stock.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.apps.stock.exception.StockExceptionMessage;
import com.axelor.apps.stock.service.StockLocationPrintService;
import com.axelor.apps.stock.service.StockLocationService;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.axelor.utils.db.Wizard;
import com.google.inject.Singleton;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.birt.core.exception.BirtException;

@Singleton
public class StockLocationController {

  /**
   * Method that generate inventory as a pdf
   *
   * @param request
   * @param response
   * @return
   * @throws BirtException
   * @throws IOException
   */
  public void print(ActionRequest request, ActionResponse response) throws AxelorException {
    try {
      Context context = request.getContext();

      // print from form
      LinkedHashMap<String, Object> stockLocationMap =
          (LinkedHashMap<String, Object>) context.get("_stockLocation");
      Integer stockLocationId = (Integer) stockLocationMap.get("id");

      // print from grid selection
      List<Integer> selectedStockLocationIds = (List<Integer>) context.get("_ids");

      boolean withoutDetailsByStockLocation =
          context.get("withoutDetailsByStockLocation") != null
              && (boolean) context.get("withoutDetailsByStockLocation");

      Long[] idsArray =
          ObjectUtils.notEmpty(selectedStockLocationIds)
              ? ArrayUtils.toObject(
                  selectedStockLocationIds.stream().mapToLong(Long::valueOf).toArray())
              : new Long[] {Long.valueOf(stockLocationId)};

      String printTypeStr = (String) context.get("printingType");
      String exportType = (String) context.get("exportTypeSelect");

      Integer printType = Integer.parseInt(printTypeStr);
      String financialDataDateTimeString = (String) context.get("financialDataDateTime");

      ReportSettings reportSettings =
          Beans.get(StockLocationPrintService.class)
              .print(
                  printType,
                  exportType,
                  financialDataDateTimeString,
                  withoutDetailsByStockLocation,
                  idsArray);

      String fileLink = reportSettings.getFileLink();
      String title = reportSettings.getOutputName();

      response.setView(ActionView.define(title).add("html", fileLink).map());
      response.setCanClose(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setStocklocationValue(ActionRequest request, ActionResponse response) {

    StockLocation stockLocation = request.getContext().asType(StockLocation.class);
    if (stockLocation.getIsValued()) {
      response.setValue(
          "stockLocationValue",
          Beans.get(StockLocationService.class).getStockLocationValue(stockLocation));
    }
  }

  public void openPrintWizard(ActionRequest request, ActionResponse response) {
    StockLocation stockLocation = request.getContext().asType(StockLocation.class);

    @SuppressWarnings("unchecked")
    List<Integer> lstSelectedLocations = (List<Integer>) request.getContext().get("_ids");

    response.setView(
        ActionView.define(I18n.get(StockExceptionMessage.STOCK_LOCATION_PRINT_WIZARD_TITLE))
            .model(Wizard.class.getName())
            .add("form", "stock-location-print-wizard-form")
            .param("popup", "true")
            .param("show-toolbar", "false")
            .param("show-confirm", "false")
            .param("popup-save", "false")
            .context("_ids", lstSelectedLocations)
            .context("_stockLocation", stockLocation)
            .map());
  }
}
