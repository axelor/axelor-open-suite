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
package com.axelor.apps.production.web;

import com.axelor.apps.production.db.ProductionOrder;
import com.axelor.apps.production.exceptions.IExceptionMessage;
import com.axelor.apps.production.service.productionorder.ProductionOrderWizardService;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.math.BigDecimal;

@Singleton
public class ProductionOrderWizardController {

  @Inject private ProductionOrderWizardService productionOrderWizardService;

  public void validate(ActionRequest request, ActionResponse response) throws AxelorException {

    Context context = request.getContext();

    if (context.get("qty") == null
        || new BigDecimal((String) context.get("qty")).compareTo(BigDecimal.ZERO) <= 0) {
      response.setFlash(I18n.get(IExceptionMessage.PRODUCTION_ORDER_3) + " !");
    } else if (context.get("billOfMaterial") == null) {
      response.setFlash(I18n.get(IExceptionMessage.PRODUCTION_ORDER_4) + " !");
    } else {
      response.setView(
          ActionView.define(I18n.get("Production order generated"))
              .model(ProductionOrder.class.getName())
              .add("form", "production-order-form")
              .add("grid", "production-order-grid")
              .param("forceEdit", "true")
              .context("_showRecord", productionOrderWizardService.validate(context).toString())
              .map());

      response.setCanClose(true);
    }
  }
}
