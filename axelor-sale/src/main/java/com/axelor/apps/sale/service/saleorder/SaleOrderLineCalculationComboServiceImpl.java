package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.Map;

public class SaleOrderLineCalculationComboServiceImpl
    implements SaleOrderLineCalculationComboService {

  protected SaleOrderLineTreeComputationService saleOrderLineTreeCalculationsService;

  @Inject
  public SaleOrderLineCalculationComboServiceImpl(
      SaleOrderLineTreeComputationService saleOrderLineTreeCalculationsService) {
    this.saleOrderLineTreeCalculationsService = saleOrderLineTreeCalculationsService;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Map<String, BigDecimal> computePriceAndRelatedFields(SaleOrderLine saleOrderLine)
      throws AxelorException {

    saleOrderLineTreeCalculationsService.computePrices(saleOrderLine);

    Beans.get(SaleOrderLineService.class)
        .computeValues(saleOrderLine.getSaleOrder(), saleOrderLine);

    return Beans.get(SaleOrderMarginService.class)
        .getSaleOrderLineComputedMarginInfo(saleOrderLine.getSaleOrder(), saleOrderLine);
  }
}
