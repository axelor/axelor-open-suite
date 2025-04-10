package com.axelor.apps.supplychain.service.saleorderline.view;

import static com.axelor.apps.sale.service.saleorderline.view.SaleOrderLineViewService.HIDDEN_ATTR;

import com.axelor.apps.account.service.analytic.AnalyticAttrsService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.utils.MapTools;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SaleOrderLineSupplychainOnLoadServiceImpl
    implements SaleOrderLineSupplychainOnLoadService {

  protected AnalyticAttrsService analyticAttrsService;
  protected SaleOrderLineSupplychainViewService saleOrderLineSupplychainViewService;

  @Inject
  public SaleOrderLineSupplychainOnLoadServiceImpl(
      AnalyticAttrsService analyticAttrsService,
      SaleOrderLineSupplychainViewService saleOrderLineSupplychainViewService) {
    this.analyticAttrsService = analyticAttrsService;
    this.saleOrderLineSupplychainViewService = saleOrderLineSupplychainViewService;
  }

  @Override
  public Map<String, Map<String, Object>> getSupplychainOnLoadAttrs(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) throws AxelorException {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    MapTools.addMap(attrs, saleOrderLineSupplychainViewService.hideSupplychainPanels(saleOrder));
    MapTools.addMap(attrs, saleOrderLineSupplychainViewService.hideDeliveredQty(saleOrder));
    MapTools.addMap(
        attrs, saleOrderLineSupplychainViewService.hideAllocatedQtyBtn(saleOrder, saleOrderLine));
    MapTools.addMap(attrs, hideReservedQty(saleOrder, saleOrderLine));
    analyticAttrsService.addAnalyticAxisAttrs(saleOrder.getCompany(), null, attrs);
    MapTools.addMap(
        attrs,
        saleOrderLineSupplychainViewService.setAnalyticDistributionPanelHidden(
            saleOrder, saleOrderLine));
    MapTools.addMap(attrs, saleOrderLineSupplychainViewService.setReservedQtyReadonly(saleOrder));
    return attrs;
  }

  protected Map<String, Map<String, Object>> hideReservedQty(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine) {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    String productTypeSelect =
        Optional.ofNullable(saleOrderLine.getProduct())
            .map(Product::getProductTypeSelect)
            .orElse("");
    int statusSelect = saleOrder.getStatusSelect();
    attrs.put(
        "reservedQty",
        Map.of(
            HIDDEN_ATTR,
            statusSelect != SaleOrderRepository.STATUS_ORDER_CONFIRMED
                || productTypeSelect.equals(ProductRepository.PRODUCT_TYPE_SERVICE)));
    return attrs;
  }
}
