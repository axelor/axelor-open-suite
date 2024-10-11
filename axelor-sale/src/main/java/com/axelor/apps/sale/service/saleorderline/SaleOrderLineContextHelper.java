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
package com.axelor.apps.sale.service.saleorderline;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.inject.Beans;
import com.axelor.rpc.Context;
import com.axelor.utils.helpers.ContextHelper;

import java.util.Optional;

public class SaleOrderLineContextHelper {

  private SaleOrderLineContextHelper() {}

  public static SaleOrder getSaleOrder(Context context, SaleOrderLine saleOrderLine) {
    SaleOrder saleOrder = ContextHelper.getOriginParent(context, SaleOrder.class);
    if(saleOrder != null) {
      return saleOrder;
    }

    SaleOrderLineRepository saleOrderLineRepository = Beans.get(SaleOrderLineRepository.class);

    //Line is persisted and is not a subline
    if (saleOrderLine.getId() != null) {
      saleOrder =
              saleOrderLineRepository.find(saleOrderLine.getId()).getSaleOrder();
      if (saleOrder != null) {
        return saleOrder;
      }
    }

    //Line is not persisted
    SaleOrderLine saleOrderLine1 = getParentSol(context);
    saleOrderLine1 = saleOrderLineRepository.find(saleOrderLine1.getId());
    saleOrder = saleOrderLine1.getSaleOrder();
    if (saleOrder == null) {
      saleOrder = getParentOrder(saleOrderLine1);
    }
    return saleOrder;
  }

  protected static SaleOrderLine getParentSol(Context context) {
    SaleOrderLine saleOrderLine = context.asType(SaleOrderLine.class);
    Long id = saleOrderLine.getId();
    if (id == null) {
      saleOrderLine = getParentSol(context.getParent());
    }
    saleOrderLine = Beans.get(SaleOrderLineRepository.class).find(saleOrderLine.getId());

    return saleOrderLine;
  }

  protected static SaleOrder getParentOrder(SaleOrderLine saleOrderLine) {
    if (saleOrderLine.getSaleOrder() != null) {
      return saleOrderLine.getSaleOrder();
    }

    return Optional.ofNullable(getPersistedParentSol(saleOrderLine)).map(SaleOrderLine::getSaleOrder).orElse(null);
  }

  protected static SaleOrderLine getPersistedParentSol(SaleOrderLine saleOrderLine) {
    if (saleOrderLine.getParentSaleOrderLine() != null) {
      return getPersistedParentSol(saleOrderLine.getParentSaleOrderLine());
    }
    if (saleOrderLine.getSaleOrder() != null) {
      return saleOrderLine;
    }
    return null;
  }
}
