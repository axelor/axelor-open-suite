package com.axelor.apps.sale.service.saleorder.views;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.inject.Beans;
import com.axelor.rpc.Context;
import java.util.LinkedHashMap;

public class SaleOrderContextHelper {
  private SaleOrderContextHelper() {}

  public static SaleOrder getSaleOrder(Context context) {

    SaleOrder saleOrder;
    if (context.get("_saleOrderTemplate") != null) {
      LinkedHashMap<String, Object> saleOrderTemplateContext =
          (LinkedHashMap<String, Object>) context.get("_saleOrderTemplate");
      Integer saleOrderId = (Integer) saleOrderTemplateContext.get("id");
      saleOrder = Beans.get(SaleOrderRepository.class).find(Long.valueOf(saleOrderId));
    } else {
      saleOrder = context.asType(SaleOrder.class);
    }

    return saleOrder;
  }
}
