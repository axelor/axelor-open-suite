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
  protected SaleOrderLineTaxService saleOrderLineTaxService;
  protected SaleOrderLinePriceService saleOrderLinePriceService;

  @Inject
  public SaleOrderLineOnChangeServiceImpl(
      SaleOrderLineDiscountService saleOrderLineDiscountService,
      SaleOrderLineComputeService saleOrderLineComputeService,
      SaleOrderLineTaxService saleOrderLineTaxService,
      SaleOrderLinePriceService saleOrderLinePriceService) {
    this.saleOrderLineDiscountService = saleOrderLineDiscountService;
    this.saleOrderLineComputeService = saleOrderLineComputeService;
    this.saleOrderLineTaxService = saleOrderLineTaxService;
    this.saleOrderLinePriceService = saleOrderLinePriceService;
  }

  @Override
  public Map<String, Object> qtyOnChange(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException {
    Map<String, Object> saleOrderLineMap = new HashMap<>();
    saleOrderLineMap.putAll(saleOrderLineDiscountService.getDiscount(saleOrderLine, saleOrder));
    saleOrderLineMap.putAll(compute(saleOrderLine, saleOrder));

    return saleOrderLineMap;
  }

  @Override
  public Map<String, Object> taxLineOnChange(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException {
    Map<String, Object> saleOrderLineMap = new HashMap<>();

    saleOrderLineMap.putAll(saleOrderLineTaxService.setTaxEquiv(saleOrder, saleOrderLine));
    saleOrderLineMap.putAll(saleOrderLinePriceService.updateInTaxPrice(saleOrder, saleOrderLine));
    saleOrderLineMap.putAll(compute(saleOrderLine, saleOrder));
    return saleOrderLineMap;
  }

  @Override
  public Map<String, Object> priceOnChange(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException {
    Map<String, Object> saleOrderLineMap = new HashMap<>();
    saleOrderLineMap.putAll(saleOrderLinePriceService.updateInTaxPrice(saleOrder, saleOrderLine));
    saleOrderLineMap.putAll(compute(saleOrderLine, saleOrder));
    return saleOrderLineMap;
  }

  @Override
  public Map<String, Object> inTaxPriceOnChange(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException {
    Map<String, Object> saleOrderLineMap = new HashMap<>();
    saleOrderLineMap.putAll(saleOrderLinePriceService.updatePrice(saleOrder, saleOrderLine));
    saleOrderLineMap.putAll(compute(saleOrderLine, saleOrder));
    return saleOrderLineMap;
  }

  @Override
  public Map<String, Object> compute(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException {
    Map<String, Object> saleOrderLineMap = new HashMap<>();
    saleOrderLineMap.putAll(saleOrderLineComputeService.computeValues(saleOrder, saleOrderLine));
    return saleOrderLineMap;
  }
}
