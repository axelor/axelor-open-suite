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
package com.axelor.apps.sale.service.saleorderline.creation;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class SaleOrderLineInitValueServiceImpl implements SaleOrderLineInitValueService {

  @Inject
  public SaleOrderLineInitValueServiceImpl() {}

  @Override
  public Map<String, Object> onNewInitValues(SaleOrder saleOrder, SaleOrderLine saleOrderLine)
      throws AxelorException {
    Map<String, Object> values = new HashMap<>();
    values.putAll(fillEstimatedDate(saleOrder, saleOrderLine));
    values.putAll(initQty(saleOrderLine));
    return values;
  }

  @Override
  public Map<String, Object> onLoadInitValues(SaleOrder saleOrder, SaleOrderLine saleOrderLine)
      throws AxelorException {
    Map<String, Object> values = new HashMap<>();
    return values;
  }

  @Override
  public Map<String, Object> onNewEditableInitValues(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine) {
    Map<String, Object> values = new HashMap<>();
    values.putAll(fillEstimatedDate(saleOrder, saleOrderLine));
    values.putAll(initQty(saleOrderLine));
    return values;
  }

  protected Map<String, Object> fillEstimatedDate(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine) {
    Map<String, Object> values = new HashMap<>();
    LocalDate estimatedShippingDate = saleOrder.getEstimatedShippingDate();
    saleOrderLine.setEstimatedShippingDate(estimatedShippingDate);
    values.put("estimatedShippingDate", estimatedShippingDate);
    return values;
  }

  protected Map<String, Object> initQty(SaleOrderLine saleOrderLine) {
    Map<String, Object> values = new HashMap<>();
    saleOrderLine.setQty(BigDecimal.ONE);
    values.put("qty", saleOrderLine.getQty());
    return values;
  }
}
