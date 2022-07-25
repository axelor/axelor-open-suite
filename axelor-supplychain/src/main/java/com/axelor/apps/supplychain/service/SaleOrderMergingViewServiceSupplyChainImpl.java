package com.axelor.apps.supplychain.service;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorder.SaleOrderMergingService;
import com.axelor.apps.sale.service.saleorder.SaleOrderMergingService.SaleOrderMergingResult;
import com.axelor.apps.sale.service.saleorder.SaleOrderMergingViewServiceImpl;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.google.inject.Inject;
import java.util.List;

public class SaleOrderMergingViewServiceSupplyChainImpl extends SaleOrderMergingViewServiceImpl {

  protected AppSaleService appSaleService;
  protected SaleOrderMergingServiceSupplyChainImpl saleOrderMergingSupplyChainService;

  @Inject
  public SaleOrderMergingViewServiceSupplyChainImpl(
      SaleOrderMergingService saleOrderMergingService,
      AppSaleService appSaleService,
      SaleOrderMergingServiceSupplyChainImpl saleOrderMergingSupplyChainService) {
    super(saleOrderMergingService);
    this.appSaleService = appSaleService;
    this.saleOrderMergingSupplyChainService = saleOrderMergingSupplyChainService;
  }

  @Override
  public ActionViewBuilder buildConfirmView(
      SaleOrderMergingResult result, String lineToMerge, List<SaleOrder> saleOrderToMerge) {
    if (!appSaleService.isApp("supplychain")) {
      return super.buildConfirmView(result, lineToMerge, saleOrderToMerge);
    }

    ActionViewBuilder confirmView = super.buildConfirmView(result, lineToMerge, saleOrderToMerge);
    if (saleOrderMergingSupplyChainService.getChecks(result).isExistStockLocationDiff()) {
      confirmView.context("contextLocationToCheck", Boolean.TRUE.toString());
    }
    return confirmView;
  }
}
