package com.axelor.apps.production.service;

import static com.axelor.apps.sale.service.saleorderline.view.SaleOrderLineViewService.HIDDEN_ATTR;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SaleOrderLineViewProductionServiceImpl implements SaleOrderLineViewProductionService {

  @Override
  public Map<String, Map<String, Object>> hideBomAndProdProcess(SaleOrderLine saleOrderLine) {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    int saleSupplySelect = saleOrderLine.getSaleSupplySelect();
    String productTypeSelect =
        Optional.ofNullable(saleOrderLine.getProduct())
            .map(Product::getProductTypeSelect)
            .orElse("");
    boolean hideBom =
        (saleSupplySelect != ProductRepository.SALE_SUPPLY_PRODUCE
                && saleSupplySelect != ProductRepository.SALE_SUPPLY_FROM_STOCK_AND_PRODUCE)
            || productTypeSelect.equals(ProductRepository.PRODUCT_TYPE_SERVICE);
    boolean hideProdProcess =
        saleSupplySelect != ProductRepository.SALE_SUPPLY_PRODUCE
            || productTypeSelect.equals(ProductRepository.PRODUCT_TYPE_SERVICE);
    attrs.put("billOfMaterial", Map.of(HIDDEN_ATTR, hideBom));
    attrs.put("customizeBOMBtn", Map.of(HIDDEN_ATTR, hideBom));
    attrs.put("prodProcess", Map.of(HIDDEN_ATTR, hideProdProcess));
    attrs.put("customizeProdProcessBtn", Map.of(HIDDEN_ATTR, hideProdProcess));
    attrs.put(
        "qtyToProduce",
        Map.of(HIDDEN_ATTR, saleSupplySelect != SaleOrderLineRepository.SALE_SUPPLY_PRODUCE));
    return attrs;
  }
}
