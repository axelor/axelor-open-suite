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
package com.axelor.apps.production.service.manuforder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.ProductionOrder;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.service.productionorder.ProductionOrderSaleOrderMOGenerationService;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.stock.db.StockMoveLine;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.service.StockLocationLineFetchService;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class ManufOrderSaleOrderServiceImpl implements ManufOrderSaleOrderService {

  protected final ProductionOrderSaleOrderMOGenerationService
      productionOrderSaleOrderMOGenerationService;
  protected final StockLocationLineFetchService stockLocationLineFetchService;

  @Inject
  public ManufOrderSaleOrderServiceImpl(
      ProductionOrderSaleOrderMOGenerationService productionOrderSaleOrderMOGenerationService,
      StockLocationLineFetchService stockLocationLineFetchService) {
    this.productionOrderSaleOrderMOGenerationService = productionOrderSaleOrderMOGenerationService;
    this.stockLocationLineFetchService = stockLocationLineFetchService;
  }

  @Override
  public ProductionOrder generateManufOrders(
      ProductionOrder productionOrder, SaleOrderLine saleOrderLine) throws AxelorException {

    Product product = saleOrderLine.getProduct();

    // Produce everything
    if (saleOrderLine.getSaleSupplySelect() == SaleOrderLineRepository.SALE_SUPPLY_PRODUCE
        && product != null
        && product.getProductTypeSelect().equals(ProductRepository.PRODUCT_TYPE_STORABLE)) {

      BigDecimal qtyToProduce = computeQuantityToProduceLeft(saleOrderLine);

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
      BigDecimal qtyToProduce = computeQuantityToProduceLeft(saleOrderLine).subtract(availableQty);

      if (qtyToProduce.compareTo(BigDecimal.ZERO) > 0) {
        return productionOrderSaleOrderMOGenerationService.generateManufOrders(
            productionOrder, saleOrderLine, product, qtyToProduce);
      }
    }

    return null;
  }

  @Override
  public BigDecimal computeQuantityToProduceLeft(SaleOrderLine saleOrderLine) {
    BigDecimal qtyToProduce = saleOrderLine.getQtyToProduce();
    Product product = saleOrderLine.getProduct();
    List<ManufOrder> manufOrderList = saleOrderLine.getManufOrderList();

    if (CollectionUtils.isEmpty(manufOrderList)) {
      return qtyToProduce;
    }

    List<StockMoveLine> producedStockMoveLineList =
        manufOrderList.stream()
            .map(ManufOrder::getProducedStockMoveLineList)
            .filter(Objects::nonNull)
            .flatMap(List::stream)
            .filter(line -> line.getProduct().equals(saleOrderLine.getProduct()))
            .collect(Collectors.toList());

    BigDecimal plannedOrProducedQty =
        producedStockMoveLineList.stream()
            .filter(
                line ->
                    line.getStockMove().getStatusSelect() != StockMoveRepository.STATUS_PLANNED
                        || line.getStockMove().getStatusSelect()
                            != StockMoveRepository.STATUS_REALIZED)
            .map(StockMoveLine::getQty)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    plannedOrProducedQty =
        plannedOrProducedQty.add(
            manufOrderList.stream()
                .filter(
                    order ->
                        order.getProduct().equals(product)
                            && order.getStatusSelect() == ManufOrderRepository.STATUS_DRAFT)
                .map(ManufOrder::getQty)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
    return qtyToProduce.subtract(plannedOrProducedQty);
  }
}
