package com.axelor.apps.sale.service.saleorderline.product;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.event.SaleOrderLineProductOnChange;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineComputeService;
import com.axelor.event.Event;
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
    saleOrderLineProductOnChangeEvent.fire(saleOrderLineProductOnChange);
    Map<String, Object> saleOrderLineMap =
        new HashMap<>(saleOrderLineProductOnChange.getSaleOrderLineMap());
    saleOrderLineMap.putAll(saleOrderLineComputeService.computeValues(saleOrder, saleOrderLine));
    return saleOrderLineMap;
  }
}
