/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.repo.StockLocationLineHistoryRepository;
import com.axelor.apps.stock.db.repo.StockLocationLineRepository;
import com.axelor.apps.stock.service.StockLocationLineHistoryService;
import com.axelor.apps.stock.service.WeightedAveragePriceService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.time.LocalDateTime;
import java.util.Optional;

public class StockLocationLineController {

  public void addWapHistoryLine(ActionRequest request, ActionResponse response) {
    try {
      StockLocationLine stockLocationLine = request.getContext().asType(StockLocationLine.class);
      stockLocationLine =
          Beans.get(StockLocationLineRepository.class).find(stockLocationLine.getId());
      LocalDateTime dateT =
          Beans.get(AppBaseService.class)
              .getTodayDateTime(
                  stockLocationLine.getStockLocation() != null
                      ? stockLocationLine.getStockLocation().getCompany()
                      : Optional.ofNullable(AuthUtils.getUser())
                          .map(User::getActiveCompany)
                          .orElse(null))
              .toLocalDateTime();

      Beans.get(WeightedAveragePriceService.class)
          .computeAvgPriceForProduct(stockLocationLine.getProduct());
      Beans.get(StockLocationLineHistoryService.class)
          .saveHistory(
              stockLocationLine,
              dateT,
              "",
              StockLocationLineHistoryRepository.TYPE_SELECT_WAP_CORRECTION);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
