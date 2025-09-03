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
package com.axelor.apps.sale.service.saleorderline.product;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.exceptions.ObserverBaseException;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.event.SaleOrderLineProductOnChange;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineComputeService;
import com.axelor.event.Event;
import com.axelor.event.ObserverException;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class SaleOrderLineOnProductChangeServiceImpl
    implements SaleOrderLineOnProductChangeService {

  protected Event<SaleOrderLineProductOnChange> saleOrderLineProductOnChangeEvent;
  protected SaleOrderLineComputeService saleOrderLineComputeService;

  @Inject
  public SaleOrderLineOnProductChangeServiceImpl(
      Event<SaleOrderLineProductOnChange> saleOrderLineProductOnChangeEvent,
      SaleOrderLineComputeService saleOrderLineComputeService) {
    this.saleOrderLineProductOnChangeEvent = saleOrderLineProductOnChangeEvent;
    this.saleOrderLineComputeService = saleOrderLineComputeService;
  }

  @Override
  public Map<String, Object> computeLineFromProduct(SaleOrderLine saleOrderLine)
      throws AxelorException {
    return computeLineFromProduct(saleOrderLine.getSaleOrder(), saleOrderLine);
  }

  @Override
  public Map<String, Object> computeLineFromProduct(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine) throws AxelorException {
    SaleOrderLineProductOnChange saleOrderLineProductOnChange =
        new SaleOrderLineProductOnChange(saleOrderLine, saleOrder);
    try {
      saleOrderLineProductOnChangeEvent.fire(saleOrderLineProductOnChange);
    } catch (ObserverException e) {
      throw new ObserverBaseException(e.getCause(), e.getCause().getMessage());
    }

    Map<String, Object> saleOrderLineMap =
        new HashMap<>(saleOrderLineProductOnChange.getSaleOrderLineMap());
    saleOrderLineMap.putAll(saleOrderLineComputeService.computeValues(saleOrder, saleOrderLine));
    return saleOrderLineMap;
  }
}
