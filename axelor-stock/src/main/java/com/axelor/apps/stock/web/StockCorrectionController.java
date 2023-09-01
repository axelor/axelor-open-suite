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

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.stock.db.StockCorrection;
import com.axelor.apps.stock.db.StockLocationLine;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.repo.StockCorrectionRepository;
import com.axelor.apps.stock.db.repo.StockLocationLineRepository;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.service.StockCorrectionService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;
import java.util.Map;

@Singleton
public class StockCorrectionController {

  public void setDefaultDetails(ActionRequest request, ActionResponse response) {
    try {
      Long stockLocaLocationLineId =
          Long.valueOf(request.getContext().get("_stockLocationLineId").toString());
      StockLocationLine stockLocationLine =
          Beans.get(StockLocationLineRepository.class).find(stockLocaLocationLineId);
      Map<String, Object> stockCorrectionDetails;

      if (stockLocationLine != null) {
        stockCorrectionDetails =
            Beans.get(StockCorrectionService.class).fillDefaultValues(stockLocationLine);
        response.setValues(stockCorrectionDetails);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void setDefaultQtys(ActionRequest request, ActionResponse response) {
    try {
      StockCorrection stockCorrection = request.getContext().asType(StockCorrection.class);

      Map<String, Object> stockCorrectionQtys =
          Beans.get(StockCorrectionService.class).fillDeafultQtys(stockCorrection);
      response.setValues(stockCorrectionQtys);

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void validate(ActionRequest request, ActionResponse response) {
    try {
      Long id = request.getContext().asType(StockCorrection.class).getId();
      StockCorrection stockCorrection = Beans.get(StockCorrectionRepository.class).find(id);
      boolean success = Beans.get(StockCorrectionService.class).validate(stockCorrection);
      if (success) {
        response.setReload(true);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void showGeneratedStockMove(ActionRequest request, ActionResponse response) {
    try {
      Long stockCorrectionId = request.getContext().asType(StockCorrection.class).getId();
      StockMove stockMove =
          Beans.get(StockMoveRepository.class)
              .all()
              .filter("self.stockCorrection.id = ?", stockCorrectionId)
              .fetchOne();
      if (stockMove != null) {
        response.setView(
            ActionView.define(I18n.get("Stock move"))
                .model(StockMove.class.getName())
                .add("grid", "stock-move-grid")
                .add("form", "stock-move-form")
                .param("search-filters", "internal-stock-move-filters")
                .context("_showRecord", stockMove.getId().toString())
                .map());
      } else {
        response.setInfo(I18n.get("No record found"));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
