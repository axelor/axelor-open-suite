package com.axelor.apps.businessproduction.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.ProductionOrder;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.production.service.productionorder.ProductionOrderSaleOrderService;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.studio.db.AppProduction;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class BusinessProjectProdOrderServiceImpl implements BusinessProjectProdOrderService {

  protected final ProductionOrderSaleOrderService productionOrderSaleOrderService;
  protected final AppProductionService appProductionService;

  @Inject
  public BusinessProjectProdOrderServiceImpl(
      ProductionOrderSaleOrderService productionOrderSaleOrderService,
      AppProductionService appProductionService) {
    this.productionOrderSaleOrderService = productionOrderSaleOrderService;
    this.appProductionService = appProductionService;
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public List<ProductionOrder> generateProductionOrders(Project project) throws AxelorException {
    List<SaleOrderLine> saleOrderLineList = project.getSaleOrderLineList();

    if (CollectionUtils.isEmpty(saleOrderLineList)) {
      return Collections.emptyList();
    }

    productionOrderSaleOrderService.checkProdOrderSolList(saleOrderLineList);

    saleOrderLineList =
        saleOrderLineList.stream()
            .filter(line -> line.getProdOrder() == null)
            .collect(Collectors.toList());

    List<ProductionOrder> productionOrderList = new ArrayList<>();
    AppProduction appProduction = appProductionService.getAppProduction();
    boolean oneProdOrderPerSo = appProduction.getOneProdOrderPerSO();

    if (oneProdOrderPerSo) {
      generateOnePoPerSo(saleOrderLineList, productionOrderList);
    } else {
      generateOnePoPerSol(saleOrderLineList, productionOrderList);
    }

    return productionOrderList;
  }

  protected void generateOnePoPerSo(
      List<SaleOrderLine> saleOrderLineList, List<ProductionOrder> productionOrderList)
      throws AxelorException {
    Set<SaleOrder> saleOrderSet =
        saleOrderLineList.stream().map(SaleOrderLine::getSaleOrder).collect(Collectors.toSet());
    for (SaleOrder saleOrder : saleOrderSet) {
      List<SaleOrderLine> filteredSaleOrderLineList =
          saleOrderLineList.stream()
              .filter(line -> line.getSaleOrder().equals(saleOrder))
              .collect(Collectors.toList());
      ProductionOrder productionOrder =
          productionOrderSaleOrderService.createProductionOrder(saleOrder);
      for (SaleOrderLine saleOrderLine : filteredSaleOrderLineList) {
        productionOrderSaleOrderService.generateManufOrders(productionOrder, saleOrderLine);
        saleOrderLine.setProdOrder(productionOrder);
      }
      productionOrderList.add(productionOrder);
    }
  }

  protected void generateOnePoPerSol(
      List<SaleOrderLine> saleOrderLineList, List<ProductionOrder> productionOrderList)
      throws AxelorException {
    for (SaleOrderLine saleOrderLine : saleOrderLineList) {
      SaleOrder saleOrder = saleOrderLine.getSaleOrder();
      ProductionOrder productionOrder =
          productionOrderSaleOrderService.createProductionOrder(saleOrder);
      productionOrderSaleOrderService.generateManufOrders(productionOrder, saleOrderLine);
      saleOrderLine.setProdOrder(productionOrder);
      productionOrderList.add(productionOrder);
    }
  }
}
