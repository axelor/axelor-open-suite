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
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.production.db.ProductionOrder;
import com.axelor.apps.production.db.repo.ProductionOrderRepository;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.production.service.SaleOrderLineBlockingProductionService;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.production.service.manuforder.ManufOrderSaleOrderService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class ProductionOrderSaleOrderServiceImpl implements ProductionOrderSaleOrderService {

  protected ProductionOrderService productionOrderService;
  protected ProductionOrderRepository productionOrderRepo;
  protected AppProductionService appProductionService;
  protected ManufOrderSaleOrderService manufOrderSaleOrderService;
  protected final SaleOrderLineBlockingProductionService saleOrderLineBlockingProductionService;

  @Inject
  public ProductionOrderSaleOrderServiceImpl(
      ProductionOrderService productionOrderService,
      ProductionOrderRepository productionOrderRepo,
      AppProductionService appProductionService,
      ManufOrderSaleOrderService manufOrderSaleOrderService,
      SaleOrderLineBlockingProductionService saleOrderLineBlockingProductionService) {

    this.productionOrderService = productionOrderService;
    this.productionOrderRepo = productionOrderRepo;
    this.appProductionService = appProductionService;
    this.manufOrderSaleOrderService = manufOrderSaleOrderService;
    this.saleOrderLineBlockingProductionService = saleOrderLineBlockingProductionService;
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class})
  public String generateProductionOrder(
      SaleOrder saleOrder, List<SaleOrderLine> selectedSaleOrderLine) throws AxelorException {

    boolean oneProdOrderPerSO = appProductionService.getAppProduction().getOneProdOrderPerSO();

    checkSelectedLines(selectedSaleOrderLine);

    List<SaleOrderLine> saleOrderLineList =
        saleOrder.getSaleOrderLineList().stream()
            .filter(this::isGenerationNeeded)
            .filter(line -> !saleOrderLineBlockingProductionService.isProductionBlocked(line))
            .collect(Collectors.toList());

    if (CollectionUtils.isNotEmpty(selectedSaleOrderLine)) {
      saleOrderLineList =
          selectedSaleOrderLine.stream()
              .filter(this::isGenerationNeeded)
              .filter(line -> !saleOrderLineBlockingProductionService.isProductionBlocked(line))
              .collect(Collectors.toList());
    }

    if (oneProdOrderPerSO) {
      return getMessageForOneProdPerSo(saleOrder, selectedSaleOrderLine, saleOrderLineList);
    } else {
      return getMessageForOneProdPerSol(saleOrder, selectedSaleOrderLine, saleOrderLineList);
    }
  }

  @Override
  public boolean areAllBlocked(List<SaleOrderLine> saleOrderLineList) {
    return saleOrderLineList.stream()
        .filter(this::isGenerationNeeded)
        .allMatch(saleOrderLineBlockingProductionService::isProductionBlocked);
  }

  protected String getMessageForOneProdPerSo(
      SaleOrder saleOrder,
      List<SaleOrderLine> selectedSaleOrderLine,
      List<SaleOrderLine> saleOrderLineList)
      throws AxelorException {
    ProductionOrder productionOrderBeforeGeneration =
        productionOrderRepo
            .all()
            .filter("self.saleOrder = :saleOrder")
            .bind("saleOrder", saleOrder)
            .fetchOne();

    int nbOfMoBeforeCreation = getNumberOfMo(saleOrder);
    generateOnePoPerSaleOrder(saleOrder, saleOrderLineList);
    int nbOfMoAfterCreation = getNumberOfMo(saleOrder);

    if (productionOrderBeforeGeneration != null
        && (nbOfMoAfterCreation - nbOfMoBeforeCreation != 0)) {
      return I18n.get(ProductionExceptionMessage.SALE_ORDER_MO_ADDED_TO_EXISTENT_PO);
    } else if (productionOrderBeforeGeneration != null) {
      if (CollectionUtils.isNotEmpty(selectedSaleOrderLine)) {
        return I18n.get(ProductionExceptionMessage.SALE_ORDER_MO_ALREADY_GENERATED_SELECTED);
      } else {
        return I18n.get(ProductionExceptionMessage.SALE_ORDER_MO_ALREADY_GENERATED);
      }
    }
    return null;
  }

  protected String getMessageForOneProdPerSol(
      SaleOrder saleOrder,
      List<SaleOrderLine> selectedSaleOrderLine,
      List<SaleOrderLine> saleOrderLineList)
      throws AxelorException {
    if (saleOrderLineList.stream()
        .allMatch(line -> CollectionUtils.isNotEmpty(line.getManufOrderList()))) {
      if (CollectionUtils.isNotEmpty(selectedSaleOrderLine)) {
        return I18n.get(
            ProductionExceptionMessage.SALE_ORDER_EVERY_PO_ALREADY_GENERATED_FOR_SELECTED);
      } else {
        return I18n.get(ProductionExceptionMessage.SALE_ORDER_EVERY_PO_ALREADY_GENERATED);
      }
    }
    generateOnePoPerSol(saleOrder, saleOrderLineList);
    if (CollectionUtils.isNotEmpty(selectedSaleOrderLine)) {
      return I18n.get(ProductionExceptionMessage.SALE_ORDER_NEW_PO_GENERATED_SELECTED);
    } else {
      return I18n.get(ProductionExceptionMessage.SALE_ORDER_NEW_PO_GENERATED);
    }
  }

  protected void generateOnePoPerSaleOrder(
      SaleOrder saleOrder, List<SaleOrderLine> saleOrderLineList) throws AxelorException {
    ProductionOrder productionOrder = this.fetchOrCreateProductionOrder(saleOrder);
    for (SaleOrderLine saleOrderLine : saleOrderLineList) {
      manufOrderSaleOrderService.generateManufOrders(productionOrder, saleOrderLine);
    }
  }

  protected void generateOnePoPerSol(SaleOrder saleOrder, List<SaleOrderLine> saleOrderLineList)
      throws AxelorException {
    for (SaleOrderLine saleOrderLine : saleOrderLineList) {
      ProductionOrder productionOrder = this.fetchOrCreateProductionOrder(saleOrder);
      manufOrderSaleOrderService.generateManufOrders(productionOrder, saleOrderLine);
    }
  }

  @Override
  public ProductionOrder fetchOrCreateProductionOrder(SaleOrder saleOrder) throws AxelorException {
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
  public List<ProductionOrder> getLinkedProductionOrders(SaleOrder saleOrder) {
    return productionOrderRepo
        .all()
        .filter("self.saleOrder = :saleOrder")
        .bind("saleOrder", saleOrder)
        .fetch();
  }

  protected int getNumberOfMo(SaleOrder saleOrder) {
    ProductionOrder productionOrder =
        productionOrderRepo
            .all()
            .filter("self.saleOrder = :saleOrder")
            .bind("saleOrder", saleOrder)
            .fetchOne();
    if (productionOrder != null) {
      return productionOrder.getManufOrderSet().size();
    }

    return 0;
  }

  protected void checkSelectedLines(List<SaleOrderLine> selectedSaleOrderLine)
      throws AxelorException {

    if (CollectionUtils.isNotEmpty(selectedSaleOrderLine)
        && selectedSaleOrderLine.stream().anyMatch(line -> !isGenerationNeeded(line))) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(ProductionExceptionMessage.SALE_ORDER_SELECT_WRONG_LINE));
    }
  }

  protected boolean isGenerationNeeded(SaleOrderLine line) {
    return isLineHasCorrectSaleSupply(line)
        && manufOrderSaleOrderService.computeQuantityToProduceLeft(line).compareTo(BigDecimal.ZERO)
            > 0;
  }

  protected boolean isLineHasCorrectSaleSupply(SaleOrderLine saleOrderLine) {
    List<Integer> authorizedStatus = new ArrayList<>();
    authorizedStatus.add(SaleOrderLineRepository.SALE_SUPPLY_PRODUCE);
    authorizedStatus.add(SaleOrderLineRepository.SALE_SUPPLY_FROM_STOCK_AND_PRODUCE);
    return authorizedStatus.contains(saleOrderLine.getSaleSupplySelect());
  }
}
