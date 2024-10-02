package com.axelor.apps.sale.db.repo;

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.sale.db.ComplementaryProductSelected;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.db.JPA;
import com.google.inject.Inject;
import java.util.Map;

public class ComplementaryProductSelectedSaleRepository
    extends ComplementaryProductSelectedRepository {
  AppSaleService appSaleService;

  @Inject
  public ComplementaryProductSelectedSaleRepository(AppSaleService appSaleService) {
    this.appSaleService = appSaleService;
  }

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {
    try {
      final String saleOrderStatusSelectIsConfirmed = "$saleOrderStatusSelectIsConfirmed";
      if (context.get("_model") != null
          && context
              .get("_model")
              .toString()
              .equals(ComplementaryProductSelected.class.getName())) {
        if (context.get("_parent") != null) {
          Map<String, Object> _parent = (Map<String, Object>) context.get("_parent");

          Class model = Class.forName((String) _parent.get("_model"));

          if (SaleOrderLine.class.equals(model)) {
            SaleOrderLine saleOrderLine =
                JPA.find(SaleOrderLine.class, Long.parseLong(_parent.get("id").toString()));
            Boolean isConfirmed =
                saleOrderLine.getSaleOrder().getStatusSelect()
                    == SaleOrderRepository.STATUS_ORDER_CONFIRMED;
            json.put(
                saleOrderStatusSelectIsConfirmed,
                isConfirmed && !appSaleService.getAppSale().getAllowPendingOrderModification());
          }
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(e);
    }
    return super.populate(json, context);
  }
}
