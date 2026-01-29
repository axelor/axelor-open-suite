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
package com.axelor.apps.production.service.productionorder.manuforder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.service.manuforder.ManufOrderService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.db.JPA;
import com.google.common.base.Strings;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ManufOrderGenerationServiceImpl implements ManufOrderGenerationService {

  protected final ManufOrderService manufOrderService;

  @Inject
  public ManufOrderGenerationServiceImpl(ManufOrderService manufOrderService) {
    this.manufOrderService = manufOrderService;
  }

  @Override
  public ManufOrder generateManufOrder(
      Product product,
      BillOfMaterial billOfMaterial,
      BigDecimal qtyRequested,
      LocalDateTime startDate,
      LocalDateTime endDate,
      SaleOrder saleOrder,
      SaleOrderLine saleOrderLine,
      ManufOrderService.ManufOrderOriginType manufOrderOriginType,
      ManufOrder manufOrderParent)
      throws AxelorException {
    ManufOrder manufOrder =
        manufOrderService.generateManufOrder(
            product,
            qtyRequested,
            ManufOrderService.DEFAULT_PRIORITY,
            ManufOrderService.IS_TO_INVOICE,
            billOfMaterial,
            startDate,
            endDate,
            manufOrderOriginType);

    if (manufOrder != null) {
      if (saleOrder != null) {
        SaleOrder managedSaleOrder = JPA.find(SaleOrder.class, saleOrder.getId());
        manufOrder.addSaleOrderSetItem(managedSaleOrder);
        if (managedSaleOrder.getClientPartner() != null) {
          manufOrder.setClientPartner(managedSaleOrder.getClientPartner());
        }
        manufOrder.setMoCommentFromSaleOrder("");
        manufOrder.setMoCommentFromSaleOrderLine("");

        if (!Strings.isNullOrEmpty(managedSaleOrder.getProductionNote())) {
          manufOrder.setMoCommentFromSaleOrder(managedSaleOrder.getProductionNote());
        }
        if (saleOrderLine != null
            && !Strings.isNullOrEmpty(saleOrderLine.getLineProductionComment())) {
          manufOrder.setMoCommentFromSaleOrderLine(saleOrderLine.getLineProductionComment());
        }
        if (saleOrderLine != null) {
          manufOrder.setSaleOrderLine(JPA.find(SaleOrderLine.class, saleOrderLine.getId()));
        }
      }

      if (manufOrderParent != null) {
        manufOrder.setParentMO(JPA.find(ManufOrder.class, manufOrderParent.getId()));
      }
    }
    return manufOrder;
  }
}
