/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.quality.web;

import com.axelor.apps.quality.exception.QualityExceptionMessage;
import com.axelor.apps.quality.service.StockMoveLineQualityService;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import jakarta.inject.Singleton;
import java.util.List;

@Singleton
public class StockMoveLineQualityController {

  public void checkOpenQualityImprovements(ActionRequest request, ActionResponse response) {
    StockMoveLine stockMoveLine = request.getContext().asType(StockMoveLine.class);
    List<String> openQualityImprovementSequences =
        Beans.get(StockMoveLineQualityService.class)
            .getOpenQualityImprovementSequences(stockMoveLine);
    if (openQualityImprovementSequences.isEmpty()) {
      return;
    }
    response.setAlert(
        String.format(
            I18n.get(QualityExceptionMessage.STOCK_MOVE_LINE_OPEN_QUALITY_IMPROVEMENTS),
            String.join(", ", openQualityImprovementSequences)));
  }
}
