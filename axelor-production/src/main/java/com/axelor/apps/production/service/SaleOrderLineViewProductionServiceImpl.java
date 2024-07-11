package com.axelor.apps.production.service;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SaleOrderLineViewProductionServiceImpl implements SaleOrderLineViewProductionService {

  protected AppProductionService appProductionService;

  public static final String HIDDEN_ATTR = "hidden";
  public static final String TITLE_ATTR = "title";
  public static final String SCALE_ATTR = "scale";
  public static final String SELECTION_IN_ATTR = "selection-in";
  public static final String READONLY_ATTR = "readonly";

  @Inject
  public SaleOrderLineViewProductionServiceImpl(AppProductionService appProductionService) {
    this.appProductionService = appProductionService;
  }

  @Override
  public Map<String, Map<String, Object>> getOnNewAttrs(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) {
    Map<String, Map<String, Object>> attrs = new HashMap<>();

    if (appProductionService.isApp("production")) {
      attrs.putAll(hideBillOfMaterialAndProd(saleOrderLine));
    }

    return attrs;
  }

  @Override
  public Map<String, Map<String, Object>> getOnLoadAttrs(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) {
    Map<String, Map<String, Object>> attrs = new HashMap<>();

    if (appProductionService.isApp("production")) {
      attrs.putAll(hideBillOfMaterialAndProd(saleOrderLine));
    }

    return attrs;
  }

  protected Map<String, Map<String, Object>> hideBillOfMaterialAndProd(
      SaleOrderLine saleOrderLine) {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    Integer saleSupply = saleOrderLine.getSaleSupplySelect();
    boolean isHidden =
        ProductRepository.PRODUCT_TYPE_SERVICE.equals(
            Optional.of(saleOrderLine)
                .map(SaleOrderLine::getProduct)
                .map(Product::getProductTypeSelect)
                .orElse(""));

    attrs.put(
        "billOfMaterial",
        Map.of(
            HIDDEN_ATTR, isHidden || (saleSupply < SaleOrderLineRepository.SALE_SUPPLY_PRODUCE)));
    attrs.put(
        "customizeBOMBtn",
        Map.of(
            HIDDEN_ATTR, isHidden || (saleSupply < SaleOrderLineRepository.SALE_SUPPLY_PRODUCE)));

    attrs.put(
        "prodProcess",
        Map.of(
            HIDDEN_ATTR, isHidden || (saleSupply != SaleOrderLineRepository.SALE_SUPPLY_PRODUCE)));
    attrs.put(
        "customizeProdProcessBtn",
        Map.of(
            HIDDEN_ATTR, isHidden || (saleSupply != SaleOrderLineRepository.SALE_SUPPLY_PRODUCE)));

    return attrs;
  }
}
