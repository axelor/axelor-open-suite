/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.production.service.productionorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.production.db.ProductionOrder;
import com.axelor.apps.production.db.repo.ProductionOrderRepository;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.stock.service.StockLocationLineFetchService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class ProductionOrderSaleOrderServiceImpl implements ProductionOrderSaleOrderService {

  protected ProductionOrderService productionOrderService;
  protected ProductionOrderRepository productionOrderRepo;
  protected AppProductionService appProductionService;
  protected ProductionOrderSaleOrderMOGenerationService productionOrderSaleOrderMOGenerationService;
  protected StockLocationLineFetchService stockLocationLineFetchService;

  @Inject
  public ProductionOrderSaleOrderServiceImpl(
      ProductionOrderService productionOrderService,
      ProductionOrderRepository productionOrderRepo,
      AppProductionService appProductionService,
      ProductionOrderSaleOrderMOGenerationService productionOrderSaleOrderMOGenerationService,
      StockLocationLineFetchService stockLocationLineFetchService) {

    this.productionOrderService = productionOrderService;
    this.productionOrderRepo = productionOrderRepo;
    this.appProductionService = appProductionService;
    this.productionOrderSaleOrderMOGenerationService = productionOrderSaleOrderMOGenerationService;
    this.stockLocationLineFetchService = stockLocationLineFetchService;
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class})
  public Set<Long> generateProductionOrder(SaleOrder saleOrder) throws AxelorException {

    boolean oneProdOrderPerSO = appProductionService.getAppProduction().getOneProdOrderPerSO();

    Set<Long> productionOrderIds = new HashSet<>();
    if (saleOrder.getSaleOrderLineList() == null) {
      return productionOrderIds;
    }

    List<SaleOrderLine> saleOrderLineList =
        saleOrder.getSaleOrderLineList().stream()
            .filter(line -> CollectionUtils.isEmpty(line.getManufOrderList()))
            .collect(Collectors.toList());

    if (oneProdOrderPerSO) {
      ProductionOrder productionOrder = generateOnePoPerSaleOrder(saleOrder, saleOrderLineList);
      productionOrderIds.add(productionOrder.getId());
    } else {
      List<ProductionOrder> productionOrderList = generateOnePoPerSol(saleOrder, saleOrderLineList);
      productionOrderIds.addAll(
          productionOrderList.stream().map(ProductionOrder::getId).collect(Collectors.toList()));
      productionOrderIds.addAll(
          productionOrderRepo
              .all()
              .filter("self.saleOrder = :saleOrder")
              .bind("saleOrder", saleOrder)
              .fetch()
              .stream()
              .map(ProductionOrder::getId)
              .collect(Collectors.toList()));
    }

    return productionOrderIds;
  }

  protected ProductionOrder generateOnePoPerSaleOrder(
      SaleOrder saleOrder, List<SaleOrderLine> saleOrderLineList) throws AxelorException {
    ProductionOrder productionOrder = this.createProductionOrder(saleOrder);
    for (SaleOrderLine saleOrderLine : saleOrderLineList) {
      productionOrder = this.generateManufOrders(productionOrder, saleOrderLine);
    }
    return productionOrder;
  }

  protected List<ProductionOrder> generateOnePoPerSol(
      SaleOrder saleOrder, List<SaleOrderLine> saleOrderLineList) throws AxelorException {
    List<ProductionOrder> productionOrderList = new ArrayList<>();
    for (SaleOrderLine saleOrderLine : saleOrderLineList) {
      ProductionOrder productionOrder = this.createProductionOrder(saleOrder);
      productionOrder = this.generateManufOrders(productionOrder, saleOrderLine);
      productionOrderList.add(productionOrder);
    }
    return productionOrderList;
  }

  @Override
  public ProductionOrder createProductionOrder(SaleOrder saleOrder) throws AxelorException {
    boolean oneProdOrderPerSO = appProductionService.getAppProduction().getOneProdOrderPerSO();
    ProductionOrder productionOrder =
        productionOrderRepo
            .all()
            .filter("self.saleOrder = :saleOrder")
            .bind("saleOrder", saleOrder)
            .fetchOne();
    if (productionOrder != null && oneProdOrderPerSO) {
      return productionOrder;
    }

    return productionOrderService.createProductionOrder(saleOrder, null);
  }

  @Override
  public boolean productionOrderForSaleOrderExists(SaleOrder saleOrder) {
    boolean oneProdOrderPerSO = appProductionService.getAppProduction().getOneProdOrderPerSO();
    ProductionOrder productionOrder =
        productionOrderRepo
            .all()
            .filter("self.saleOrder = :saleOrder")
            .bind("saleOrder", saleOrder)
            .fetchOne();
    return oneProdOrderPerSO && productionOrder != null;
  }

  @Override
  public void checkGeneratedProductionOrders(SaleOrder saleOrder) throws AxelorException {
    boolean oneProdOrderPerSO = appProductionService.getAppProduction().getOneProdOrderPerSO();
    List<SaleOrderLine> saleOrderLineList = saleOrder.getSaleOrderLineList();
    if (!oneProdOrderPerSO
        && saleOrderLineList.stream()
            .allMatch(line -> CollectionUtils.isNotEmpty(line.getManufOrderList()))) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(ProductionExceptionMessage.SALE_ORDER_EVERY_PO_ALREADY_GENERATED));
    }
  }

  @Override
  public int getNumberOfMoOrPo(SaleOrder saleOrder) {
    boolean oneProdOrderPerSO = appProductionService.getAppProduction().getOneProdOrderPerSO();
    if (oneProdOrderPerSO) {
      ProductionOrder productionOrder =
          productionOrderRepo
              .all()
              .filter("self.saleOrder = :saleOrder")
              .bind("saleOrder", saleOrder)
              .fetchOne();
      if (productionOrder != null) {
        return productionOrder.getManufOrderSet().size();
      }
    } else {
      return productionOrderRepo
          .all()
          .filter("self.saleOrder = :saleOrder")
          .bind("saleOrder", saleOrder)
          .fetch()
          .size();
    }

    return 0;
  }

  @Override
  public ProductionOrder generateManufOrders(
      ProductionOrder productionOrder, SaleOrderLine saleOrderLine) throws AxelorException {

    Product product = saleOrderLine.getProduct();

    // Produce everything
    if (saleOrderLine.getSaleSupplySelect() == SaleOrderLineRepository.SALE_SUPPLY_PRODUCE
        && product != null
        && product.getProductTypeSelect().equals(ProductRepository.PRODUCT_TYPE_STORABLE)) {

      BigDecimal qtyToProduce = saleOrderLine.getQty();

      return productionOrderSaleOrderMOGenerationService.generateManufOrders(
          productionOrder, saleOrderLine, product, qtyToProduce);

    }
    // Produce only missing qty
    else if (saleOrderLine.getSaleSupplySelect()
            == SaleOrderLineRepository.SALE_SUPPLY_FROM_STOCK_AND_PRODUCE
        && product != null
        && product.getProductTypeSelect().equals(ProductRepository.PRODUCT_TYPE_STORABLE)) {

      BigDecimal availableQty =
          stockLocationLineFetchService.getAvailableQty(
              saleOrderLine.getSaleOrder().getStockLocation(), product);
      BigDecimal qtyToProduce = saleOrderLine.getQty().subtract(availableQty);

      if (qtyToProduce.compareTo(BigDecimal.ZERO) > 0) {
        return productionOrderSaleOrderMOGenerationService.generateManufOrders(
            productionOrder, saleOrderLine, product, qtyToProduce);
      }
    }

    return null;
  }
}
