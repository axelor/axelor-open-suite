/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
package com.axelor.apps.sale.service.saleorderline.subline;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class SubSaleOrderLineServiceImpl implements SubSaleOrderLineService {

  @Override
  public void setMainSaleOrder(SaleOrder saleOrder) throws AxelorException {
    if (saleOrder == null || CollectionUtils.isEmpty(saleOrder.getSaleOrderLineList())) {
      return;
    }
    for (SaleOrderLine saleOrderLine : saleOrder.getSaleOrderLineList()) {
      setMainSaleOrder(saleOrderLine, saleOrder);
    }
  }

  protected void setMainSaleOrder(SaleOrderLine saleOrderLine, SaleOrder mainSaleOrder) {
    if (saleOrderLine == null) {
      return;
    }
    List<SaleOrderLine> subSaleOrderLineList = saleOrderLine.getSubSaleOrderLineList();
    if (CollectionUtils.isEmpty(subSaleOrderLineList)) {
      return;
    }
    for (SaleOrderLine subSaleOrderLine : subSaleOrderLineList) {
      if (subSaleOrderLine.getMainSaleOrder() == null
          || !subSaleOrderLine.getMainSaleOrder().equals(mainSaleOrder)) {
        subSaleOrderLine.setMainSaleOrder(mainSaleOrder);
      }
      setMainSaleOrder(subSaleOrderLine, mainSaleOrder);
    }
  }
}
