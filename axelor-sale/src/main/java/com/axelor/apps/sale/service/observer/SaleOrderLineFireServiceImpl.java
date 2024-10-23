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
package com.axelor.apps.sale.service.observer;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.event.SaleOrderLineProductOnChange;
import com.axelor.apps.sale.service.event.SaleOrderLineViewOnLoad;
import com.axelor.apps.sale.service.event.SaleOrderLineViewOnNew;
import com.axelor.event.Event;
import com.google.inject.Inject;
import java.util.Map;

public class SaleOrderLineFireServiceImpl implements SaleOrderLineFireService {
  protected Event<SaleOrderLineViewOnNew> saleOrderLineViewOnNewEvent;
  protected Event<SaleOrderLineViewOnLoad> saleOrderLineViewOnLoadEvent;
  protected Event<SaleOrderLineProductOnChange> saleOrderLineProductOnChangeEvent;

  @Inject
  public SaleOrderLineFireServiceImpl(
      Event<SaleOrderLineViewOnNew> saleOrderLineViewOnNewEvent,
      Event<SaleOrderLineViewOnLoad> saleOrderLineViewOnLoadEvent,
      Event<SaleOrderLineProductOnChange> saleOrderLineProductOnChangeEvent) {
    this.saleOrderLineViewOnNewEvent = saleOrderLineViewOnNewEvent;
    this.saleOrderLineViewOnLoadEvent = saleOrderLineViewOnLoadEvent;
    this.saleOrderLineProductOnChangeEvent = saleOrderLineProductOnChangeEvent;
  }

  @Override
  public Map<String, Map<String, Object>> getOnNewAttrs(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) {
    SaleOrderLineViewOnNew saleOrderLineViewOnNew =
        new SaleOrderLineViewOnNew(saleOrderLine, saleOrder);
    saleOrderLineViewOnNewEvent.fire(saleOrderLineViewOnNew);
    return saleOrderLineViewOnNew.getSaleOrderLineMap();
  }

  @Override
  public Map<String, Map<String, Object>> getOnLoadAttrs(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) {
    SaleOrderLineViewOnLoad saleOrderLineViewOnLoad =
        new SaleOrderLineViewOnLoad(saleOrderLine, saleOrder);
    saleOrderLineViewOnLoadEvent.fire(saleOrderLineViewOnLoad);
    return saleOrderLineViewOnLoad.getSaleOrderLineMap();
  }
}
