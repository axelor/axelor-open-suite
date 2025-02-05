package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineComputeService;
import com.axelor.apps.sale.service.saleorderline.pack.SaleOrderLinePackService;
import com.axelor.apps.sale.service.saleorderline.subline.SubSaleOrderLineComputeService;
import com.axelor.apps.sale.service.saleorderline.tax.SaleOrderLineCreateTaxLineService;
import com.axelor.apps.supplychain.service.invoice.AdvancePaymentRefundService;
import com.axelor.apps.supplychain.service.saleorder.SaleOrderComputeServiceSupplychainImpl;
import com.google.inject.Inject;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class SaleOrderComputeServiceProductionImpl extends SaleOrderComputeServiceSupplychainImpl {

  @Inject
  public SaleOrderComputeServiceProductionImpl(
      SaleOrderLineCreateTaxLineService saleOrderLineCreateTaxLineService,
      SaleOrderLineComputeService saleOrderLineComputeService,
      SaleOrderLinePackService saleOrderLinePackService,
      SubSaleOrderLineComputeService subSaleOrderLineComputeService,
      AdvancePaymentRefundService refundService,
      AppSaleService appSaleService) {
    super(
        saleOrderLineCreateTaxLineService,
        saleOrderLineComputeService,
        saleOrderLinePackService,
        subSaleOrderLineComputeService,
        refundService,
        appSaleService);
  }

  @Override
  public SaleOrder _computeSaleOrderLineList(SaleOrder saleOrder) throws AxelorException {

    List<SaleOrderLine> saleOrderLineList = saleOrder.getSaleOrderLineList();

    if (CollectionUtils.isEmpty(saleOrderLineList)) {
      return saleOrder;
    }

    for (SaleOrderLine saleOrderLine : saleOrderLineList) {
      subSaleOrderLineComputeService.computeSumSubLineList(saleOrderLine, saleOrder);
    }

    return saleOrder;
  }
}
