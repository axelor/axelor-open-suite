package com.axelor.apps.businessproject.service.observer;

import com.axelor.apps.businessproject.service.SaleOrderLineViewProjectService;
import com.axelor.apps.sale.service.event.SaleOrderLineViewOnLoad;
import com.axelor.apps.sale.service.event.SaleOrderLineViewOnNew;
import com.axelor.event.Observes;
import com.axelor.inject.Beans;
import java.util.Map;

public class SaleOrderLineProjectObserver {
  void onSaleOrderLineOnNew(@Observes SaleOrderLineViewOnNew event) {
    Map<String, Map<String, Object>> saleOrderLineMap = event.getSaleOrderLineMap();
    saleOrderLineMap.putAll(Beans.get(SaleOrderLineViewProjectService.class).getProjectTitle());
  }

  void onSaleOrderLineOnLoad(@Observes SaleOrderLineViewOnLoad event) {
    Map<String, Map<String, Object>> saleOrderLineMap = event.getSaleOrderLineMap();
    saleOrderLineMap.putAll(Beans.get(SaleOrderLineViewProjectService.class).getProjectTitle());
  }
}
