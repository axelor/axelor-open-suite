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
package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import java.math.BigDecimal;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class SaleOrderOrderingStatusServiceImpl implements SaleOrderOrderingStatusService {
  @Override
  public void updateOrderingStatus(SaleOrder saleOrder) {
    List<SaleOrderLine> saleOrderLineList = saleOrder.getSaleOrderLineList();
    if (CollectionUtils.isEmpty(saleOrderLineList)) {
      saleOrder.setOrderingStatus(null);
      return;
    }
    if (saleOrderLineList.stream()
        .allMatch(line -> line.getQty().compareTo(line.getOrderedQty()) == 0)) {
      saleOrder.setOrderingStatus(SaleOrderRepository.ORDERING_STATUS_CLOSED);
    } else if (saleOrderLineList.stream()
        .anyMatch(line -> line.getOrderedQty().compareTo(BigDecimal.ZERO) > 0)) {
      saleOrder.setOrderingStatus(SaleOrderRepository.ORDERING_STATUS_PARTIALLY_ORDERED);
    }
  }
}
