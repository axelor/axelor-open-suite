package com.axelor.apps.sale.service;

import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import java.util.Map;

public class SaleOrderLineSaleRepository extends SaleOrderLineRepository {

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {
    if (context.get("_model") != null
        && context.get("_model").toString().contains("SaleOrder")
        && context.get("id") != null) {
      Long id = (Long) json.get("id");
      if (id != null) {
        SaleOrderLine saleOrderLine = find(id);
        json.put(
            "$hasWarning",
            saleOrderLine.getSaleOrder() != null
                && (saleOrderLine.getSaleOrder().getStatusSelect()
                        == SaleOrderRepository.STATUS_DRAFT_QUOTATION
                    || (saleOrderLine.getSaleOrder().getStatusSelect()
                            == SaleOrderRepository.STATUS_ORDER_CONFIRMED
                        && saleOrderLine.getSaleOrder().getOrderBeingEdited()))
                && saleOrderLine.getDiscountsNeedReview());
      }
    }
    return super.populate(json, context);
  }
}
