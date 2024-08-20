package com.axelor.apps.sale.service.observer;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.event.SaleOrderLineViewOnLoad;
import com.axelor.apps.sale.service.event.SaleOrderLineViewOnNew;
import com.axelor.event.Event;
import com.google.inject.Inject;
import java.util.Map;

public class SaleOrderLineFireServiceImpl implements SaleOrderLineFireService {
  protected Event<SaleOrderLineViewOnNew> saleOrderLineViewOnNewEvent;
  protected Event<SaleOrderLineViewOnLoad> saleOrderLineViewOnLoadEvent;

  @Inject
  public SaleOrderLineFireServiceImpl(
      Event<SaleOrderLineViewOnNew> saleOrderLineViewOnNewEvent,
      Event<SaleOrderLineViewOnLoad> saleOrderLineViewOnLoadEvent) {
    this.saleOrderLineViewOnNewEvent = saleOrderLineViewOnNewEvent;
    this.saleOrderLineViewOnLoadEvent = saleOrderLineViewOnLoadEvent;
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
