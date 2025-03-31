package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.ProductMultipleQtyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.utils.MapTools;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorderline.view.SaleOrderLineViewServiceImpl;
import java.util.HashMap;
import java.util.Map;

public class SaleOrderViewProductionServiceImpl extends SaleOrderLineViewServiceImpl {
  protected ManufOrderRepository manufOrderRepository;

  public SaleOrderViewProductionServiceImpl(
      AppBaseService appBaseService,
      AppSaleService appSaleService,
      ProductMultipleQtyService productMultipleQtyService,
      ManufOrderRepository manufOrderRepository) {
    super(appBaseService, appSaleService, productMultipleQtyService);
    this.manufOrderRepository = manufOrderRepository;
  }

  @Override
  public Map<String, Map<String, Object>> getOnLoadAttrs(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) throws AxelorException {
    Map<String, Map<String, Object>> attrs = super.getOnLoadAttrs(saleOrderLine, saleOrder);
    MapTools.addMap(attrs, hideQtyProduced(saleOrderLine));
    return attrs;
  }

  public Map<String, Map<String, Object>> hideQtyProduced(SaleOrderLine saleOrderLine) {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    SaleOrder saleOrder = saleOrderLine.getSaleOrder();
    ManufOrder manufOrder =
        manufOrderRepository
            .all()
            .filter("self.product.id=:productId and :saleOrderLineId =self.saleOrderLine.id")
            .bind("saleOrderLineId", saleOrderLine.getId())
            .bind("productId", saleOrderLine.getProduct().getId())
            .fetchOne();

    boolean hideQtyProduced =
        saleOrder.getStatusSelect() < SaleOrderRepository.STATUS_ORDER_CONFIRMED
            || manufOrder == null;

    attrs.put("qtyProducedPanel", Map.of(HIDDEN_ATTR, hideQtyProduced));
    return attrs;
  }
}
