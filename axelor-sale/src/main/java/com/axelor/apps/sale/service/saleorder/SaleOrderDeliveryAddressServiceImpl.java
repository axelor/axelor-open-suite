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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.exception.SaleExceptionMessage;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import java.util.List;
import java.util.Objects;
import org.apache.commons.collections.CollectionUtils;

public class SaleOrderDeliveryAddressServiceImpl implements SaleOrderDeliveryAddressService {

  @Override
  public List<SaleOrderLine> updateSaleOrderLinesDeliveryAddress(SaleOrder saleOrder) {
    List<SaleOrderLine> saleOrderLineList = saleOrder.getSaleOrderLineList();
    if (CollectionUtils.isEmpty(saleOrderLineList)) {
      return saleOrderLineList;
    }
    for (SaleOrderLine saleOrderLine : saleOrderLineList) {
      updateSaleOrderLineDeliveryAddress(saleOrder, saleOrderLine);
    }
    return saleOrderLineList;
  }

  protected void updateSaleOrderLineDeliveryAddress(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine) {
    saleOrderLine.setDeliveryAddress(saleOrder.getDeliveryAddress());
    saleOrderLine.setDeliveryAddressStr(saleOrder.getDeliveryAddressStr());
  }

  @Override
  public Address getDeliveryAddress(SaleOrder saleOrder, List<SaleOrderLine> saleOrderLineList) {
    return saleOrderLineList.stream()
        .map(SaleOrderLine::getDeliveryAddress)
        .filter(Objects::nonNull)
        .findFirst()
        .orElse(saleOrder.getDeliveryAddress());
  }

  @Override
  public void checkSaleOrderLinesDeliveryAddress(List<SaleOrderLine> saleOrderLineList)
      throws AxelorException {
    if (CollectionUtils.isEmpty(saleOrderLineList)) {
      return;
    }
    if (saleOrderLineList.stream()
            .map(SaleOrderLine::getDeliveryAddressStr)
            .filter(StringUtils::notBlank)
            .distinct()
            .count()
        > 1) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(SaleExceptionMessage.DELIVERY_ADDRESS_MUST_BE_SAME_FOR_ALL_LINES));
    }
  }
}
