package com.axelor.apps.supplychain.service;

import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorder.SaleOrderMergeServiceImpl;
import com.axelor.apps.sale.service.saleorder.model.SaleOrderMergeObject;
import com.axelor.apps.stock.db.StockLocation;
import com.axelor.db.JPA;
import com.axelor.rpc.Context;
import com.google.inject.Inject;
import java.util.Map;

public class SaleOrderMergeSupplychainServiceImpl extends SaleOrderMergeServiceImpl {

  protected AppSaleService appSaleService;

  @Inject
  public SaleOrderMergeSupplychainServiceImpl(AppSaleService appSaleService) {
    this.appSaleService = appSaleService;
  }

  @Override
  public void computeMapWithContext(Context context, Map<String, SaleOrderMergeObject> map) {

    super.computeMapWithContext(context, map);
    if (appSaleService.isApp("supplychain")) {
      if (map.get("stockLocation") == null) {
        throw new IllegalStateException(
            "Entry of stockLocation in map should not be null when calling this function");
      }
      // Check if stockLocation is content in parameter
      if (context.get("stockLocation") != null) {
        map.get("stockLocation")
            .setCommonObject(
                JPA.em()
                    .find(
                        StockLocation.class,
                        new Long((Integer) ((Map) context.get("stockLocation")).get("id"))));
      }
    }
  }
}
