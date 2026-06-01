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
package com.axelor.apps.supplychain.web;

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.supplychain.db.StockRotationCategory;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.apps.supplychain.service.StockRotationCategoryService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import javax.script.ScriptException;

public class StockRotationCategoryController {

  public void checkFormula(ActionRequest request, ActionResponse response) {
    try {
      StockRotationCategory stockRotationCategory =
          request.getContext().asType(StockRotationCategory.class);
      Beans.get(StockRotationCategoryService.class).checkFormula(stockRotationCategory);
    } catch (ScriptException e) {
      response.addError(
          "formula",
          I18n.get(SupplychainExceptionMessage.STOCK_ROTATION_CATEGORY_FORMULA_ERROR)
              + e.getMessage());
      TraceBackService.trace(response, e);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
