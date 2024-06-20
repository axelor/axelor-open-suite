package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class SaleOrderLineOnChangeServiceImpl implements SaleOrderLineOnChangeService {
  protected SaleOrderLineDiscountService saleOrderLineDiscountService;
  protected SaleOrderLineComputeService saleOrderLineComputeService;

  @Inject
  public SaleOrderLineOnChangeServiceImpl(
      SaleOrderLineDiscountService saleOrderLineDiscountService,
      SaleOrderLineComputeService saleOrderLineComputeService) {
    this.saleOrderLineDiscountService = saleOrderLineDiscountService;
    this.saleOrderLineComputeService = saleOrderLineComputeService;
  }

  @Override
  public Map<String, Object> qtyOnChange(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException {
    Map<String, Object> saleOrderLineMap = new HashMap<>();
    saleOrderLineMap.putAll(saleOrderLineDiscountService.getDiscount(saleOrderLine, saleOrder));
    saleOrderLineMap.putAll(saleOrderLineComputeService.computeValues(saleOrder, saleOrderLine));

    return saleOrderLineMap;
  }
}
