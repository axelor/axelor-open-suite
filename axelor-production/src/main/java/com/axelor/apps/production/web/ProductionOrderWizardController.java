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
package com.axelor.apps.production.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.ProductionOrder;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.production.service.productionorder.ProductionOrderWizardService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Singleton;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Singleton
public class ProductionOrderWizardController {

  public void validate(ActionRequest request, ActionResponse response) throws AxelorException {

    Context context = request.getContext();

    ZonedDateTime startDateT = null, endDateT = null;
    if (context.get("_startDate") != null) {
      startDateT =
          ZonedDateTime.parse(
              context.get("_startDate").toString(),
              DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.systemDefault()));

      if (ChronoUnit.MINUTES.between(
              Beans.get(AppProductionService.class).getTodayDateTime(), startDateT)
          < 0) {
        response.setError(I18n.get(ProductionExceptionMessage.PRODUCTION_ORDER_5));
      }
    }

    if (context.get("_endDate") != null) {
      endDateT =
          ZonedDateTime.parse(
              context.get("_endDate").toString(),
              DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.systemDefault()));

      if ((startDateT != null && ChronoUnit.MINUTES.between(startDateT, endDateT) < 0)
          || ChronoUnit.MINUTES.between(
                  Beans.get(AppProductionService.class).getTodayDateTime(), endDateT)
              < 0) {
        response.setError(I18n.get(ProductionExceptionMessage.PRODUCTION_ORDER_5));
      }
    }

    if (context.get("qty") == null
        || new BigDecimal((String) context.get("qty")).compareTo(BigDecimal.ZERO) <= 0) {
      response.setInfo(I18n.get(ProductionExceptionMessage.PRODUCTION_ORDER_3) + " !");
    } else if (context.get("billOfMaterial") == null) {
      response.setInfo(I18n.get(ProductionExceptionMessage.PRODUCTION_ORDER_4) + " !");
    } else {
      response.setView(
          ActionView.define(I18n.get("Production order generated"))
              .model(ProductionOrder.class.getName())
              .add("form", "production-order-form")
              .add("grid", "production-order-grid")
              .param("search-filters", "production-order-filters")
              .param("forceEdit", "true")
              .context(
                  "_showRecord",
                  Beans.get(ProductionOrderWizardService.class).validate(context).toString())
              .map());

      response.setCanClose(true);
    }
  }
}
