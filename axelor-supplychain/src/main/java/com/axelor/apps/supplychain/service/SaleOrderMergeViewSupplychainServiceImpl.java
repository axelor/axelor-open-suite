package com.axelor.apps.supplychain.service;

import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorder.SaleOrderMergeViewServiceImpl;
import com.axelor.apps.sale.service.saleorder.model.SaleOrderMergeObject;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.google.inject.Inject;
import java.util.List;
import java.util.Map;

public class SaleOrderMergeViewSupplychainServiceImpl extends SaleOrderMergeViewServiceImpl {

  protected AppSaleService appSaleService;

  @Inject
  public SaleOrderMergeViewSupplychainServiceImpl(AppSaleService appSaleService) {
    this.appSaleService = appSaleService;
  }

  @Override
  public boolean existDiffForConfirmView(Map<String, SaleOrderMergeObject> commonMap) {
    if (!appSaleService.isApp("supplychain")) {
      return super.existDiffForConfirmView(commonMap);
    }

    if (commonMap.get("stockLocation") == null) {
      throw new IllegalStateException(
          "Entry of stockLocation in map should not be null when calling this function");
    }
    return super.existDiffForConfirmView(commonMap)
        || commonMap.get("stockLocation").getExistDiff();
  }

  @Override
  public ActionViewBuilder buildConfirmView(
      Map<String, SaleOrderMergeObject> commonMap, String lineToMerge, List<Long> saleOrderIdList) {
    if (!appSaleService.isApp("supplychain")) {
      return super.buildConfirmView(commonMap, lineToMerge, saleOrderIdList);
    }
    if (commonMap.get("stockLocation") == null) {
      throw new IllegalStateException(
          "Entry of stockLocation in map should not be null when calling this function");
    }
    ActionViewBuilder confirmView = super.buildConfirmView(commonMap, lineToMerge, saleOrderIdList);
    if (commonMap.get("stockLocation").getExistDiff()) {
      confirmView.context("contextLocationToCheck", "true");
    }
    return confirmView;
  }
}
