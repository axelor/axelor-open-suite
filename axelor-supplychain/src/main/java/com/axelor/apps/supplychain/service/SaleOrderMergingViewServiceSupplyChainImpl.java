package com.axelor.apps.supplychain.service;

import java.util.List;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorder.SaleOrderMergingService;
import com.axelor.apps.sale.service.saleorder.SaleOrderMergingService.SaleOrderMergingResult;
import com.axelor.apps.sale.service.saleorder.SaleOrderMergingViewServiceImpl;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.google.inject.Inject;

public class SaleOrderMergeViewSupplychainServiceImpl extends SaleOrderMergingViewServiceImpl {

  protected AppSaleService appSaleService;

  @Inject
  public SaleOrderMergeViewSupplychainServiceImpl(SaleOrderMergingService saleOrderMergingService, AppSaleService appSaleService) {
	  super(saleOrderMergingService);
	  this.appSaleService = appSaleService;    
  }

  @Override
  public ActionViewBuilder buildConfirmView(
		  SaleOrderMergingResult result, String lineToMerge, List<SaleOrder> saleOrderToMerge) {
    if (!appSaleService.isApp("supplychain")) {
      return super.buildConfirmView(result, lineToMerge, saleOrderToMerge);
    }

    ActionViewBuilder confirmView = super.buildConfirmView(result, lineToMerge, saleOrderToMerge);
    //TODO check Location diff
//    if (saleOrderMergingService.getChecks(result).is) {
//      confirmView.context("contextLocationToCheck", "true");
//    }
    return confirmView;
  }
}
