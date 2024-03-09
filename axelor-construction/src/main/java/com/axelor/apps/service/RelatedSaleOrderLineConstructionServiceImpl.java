package com.axelor.apps.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.RelatedSaleOrderLineServiceImpl;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

import java.math.RoundingMode;

public class RelatedSaleOrderLineConstructionServiceImpl extends RelatedSaleOrderLineServiceImpl {

  @Inject
  public RelatedSaleOrderLineConstructionServiceImpl(
      SaleOrderLineRepository saleOrderLineRepository,
      SaleOrderRepository saleOrderRepository,
      TaxService taxService,
      AppBaseService appBaseService,
      SaleOrderLineService saleOrderLineService,
      AppSaleService appSaleService) {
    super(
        saleOrderLineRepository,
        saleOrderRepository,
        taxService,
        appBaseService,
        saleOrderLineService,
        appSaleService);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void updateRelatedOrderLines(SaleOrder saleOrder) throws AxelorException {
    super.updateRelatedOrderLines(saleOrder);
    for (SaleOrderLine saleOrderLine : saleOrder.getSaleOrderLineDisplayList()) {
      saleOrderLine.setGrossMarging(
          saleOrderLine
              .getPrice()
              .divide(saleOrderLine.getGeneralExpenses().multiply(saleOrderLine.getCostPrice()),2, RoundingMode.HALF_UP));
    }
  }
}
