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
package com.axelor.apps.sale.web;

import com.axelor.apps.sale.db.ConfiguratorCreator;
import com.axelor.apps.sale.db.ConfiguratorFormula;
import com.axelor.apps.sale.exception.SaleExceptionMessage;
import com.axelor.apps.sale.service.configurator.ConfiguratorFormulaService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;

@Singleton
public class ConfiguratorFormulaController {

  /**
   * Check the groovy script in the context
   *
   * @param request
   * @param response
   */
  public void checkGroovyFormula(ActionRequest request, ActionResponse response) {
    ConfiguratorFormula configuratorFormula =
        request.getContext().asType(ConfiguratorFormula.class);
    ConfiguratorCreator creator =
        request.getContext().getParent().asType(ConfiguratorCreator.class);
    try {
      Beans.get(ConfiguratorFormulaService.class).checkFormula(configuratorFormula, creator);
      response.setAlert(I18n.get(SaleExceptionMessage.CONFIGURATOR_CREATOR_SCRIPT_WORKING));
    } catch (Exception e) {
      response.setError(e.getMessage());
    }
  }
}
