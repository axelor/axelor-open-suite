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
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.ProductionOrder;
import com.axelor.apps.production.db.repo.BillOfMaterialRepository;
import com.axelor.apps.production.db.repo.ProductionOrderRepository;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.production.service.manuforder.ManufOrderService.ManufOrderOriginTypeProduction;
import com.axelor.apps.production.service.productionorder.ProductionOrderService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Singleton;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Singleton
public class ProductionOrderController {

  @SuppressWarnings("unchecked")
  public void addManufOrder(ActionRequest request, ActionResponse response) throws AxelorException {

    Context context = request.getContext();

    if (context.get("qty") == null
        || new BigDecimal(context.get("qty").toString()).compareTo(BigDecimal.ZERO) <= 0) {
      response.setInfo(I18n.get(ProductionExceptionMessage.PRODUCTION_ORDER_3) + "!");
    } else if (context.get("billOfMaterial") == null) {
      response.setInfo(I18n.get(ProductionExceptionMessage.PRODUCTION_ORDER_4) + "!");
    } else {
      Map<String, Object> bomContext = (Map<String, Object>) context.get("billOfMaterial");
      BillOfMaterial billOfMaterial =
          Beans.get(BillOfMaterialRepository.class)
              .find(((Integer) bomContext.get("id")).longValue());

      BigDecimal qty = new BigDecimal(context.get("qty").toString());

      Product product = null;

      if (context.get("product") != null) {
        Map<String, Object> productContext = (Map<String, Object>) context.get("product");
        product =
            Beans.get(ProductRepository.class)
                .find(((Integer) productContext.get("id")).longValue());
      } else {
        product = billOfMaterial.getProduct();
      }

      ZonedDateTime startDateT;
      if (context.containsKey("_startDate") && context.get("_startDate") != null) {
        startDateT =
            ZonedDateTime.parse(
                (CharSequence) context.get("_startDate"),
                DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.systemDefault()));
      } else {
        startDateT = Beans.get(AppBaseService.class).getTodayDateTime();
      }

      ProductionOrder productionOrder =
          Beans.get(ProductionOrderRepository.class)
              .find(Long.parseLong(request.getContext().get("_id").toString()));

      if (billOfMaterial.getProdProcess() != null) {
        Beans.get(ProductionOrderService.class)
            .addManufOrder(
                productionOrder,
                product,
                billOfMaterial,
                qty,
                startDateT.toLocalDateTime(),
                null,
                productionOrder.getSaleOrder(),
                null,
                ManufOrderOriginTypeProduction.ORIGIN_TYPE_OTHER);
      } else {
        response.setError(I18n.get(ProductionExceptionMessage.MANUF_ORDER_NO_GENERATION));
      }

      response.setCanClose(true);
    }
  }
}
