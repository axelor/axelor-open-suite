package com.axelor.apps.production.service;

import static com.axelor.apps.sale.service.saleorderline.view.SaleOrderLineViewService.HIDDEN_ATTR;

import com.axelor.apps.base.utils.MapTools;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class SaleOrderLineProductionOnLoadServiceImpl
    implements SaleOrderLineProductionOnLoadService {

  protected SaleOrderLineProductionViewService saleOrderLineProductionViewService;
  protected ManufOrderRepository manufOrderRepository;

  @Inject
  public SaleOrderLineProductionOnLoadServiceImpl(
      SaleOrderLineProductionViewService saleOrderLineProductionViewService,
      ManufOrderRepository manufOrderRepository) {
    this.saleOrderLineProductionViewService = saleOrderLineProductionViewService;
    this.manufOrderRepository = manufOrderRepository;
  }

  @Override
  public Map<String, Map<String, Object>> getProductionOnLoadAttrs(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) {
    Map<String, Map<String, Object>> attrs =
        saleOrderLineProductionViewService.hideBomAndProdProcess(saleOrderLine);
    MapTools.addMap(attrs, hideQtyProduced(saleOrderLine, saleOrder));
    return attrs;
  }

  protected Map<String, Map<String, Object>> hideQtyProduced(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    ManufOrder manufOrder = null;
    if (saleOrderLine.getProduct() != null) {
      manufOrder =
          manufOrderRepository
              .all()
              .filter("self.product.id=:productId and self.saleOrderLine.id=:saleOrderLineId")
              .bind("saleOrderLineId", saleOrderLine.getId())
              .bind("productId", saleOrderLine.getProduct().getId())
              .fetchOne();
    }
    attrs.put(
        "qtyProducedPanel",
        Map.of(
            HIDDEN_ATTR,
            saleOrder.getStatusSelect() < SaleOrderRepository.STATUS_ORDER_CONFIRMED
                || manufOrder == null));
    return attrs;
  }
}
