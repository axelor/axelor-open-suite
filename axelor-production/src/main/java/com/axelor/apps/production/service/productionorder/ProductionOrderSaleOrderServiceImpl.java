/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.ProductionOrder;
import com.axelor.apps.production.db.repo.ProductionOrderRepository;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.production.service.SaleOrderLineBlockingProductionService;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.production.service.manuforder.ManufOrderSaleOrderService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.stock.service.StockLocationLineFetchService;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import com.axelor.utils.helpers.StringHtmlListBuilder;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class ProductionOrderSaleOrderServiceImpl implements ProductionOrderSaleOrderService {

  protected ProductionOrderService productionOrderService;
  protected ProductionOrderRepository productionOrderRepo;
  protected AppProductionService appProductionService;
  protected ManufOrderSaleOrderService manufOrderSaleOrderService;
  protected final SaleOrderLineBlockingProductionService saleOrderLineBlockingProductionService;
  protected final ProductionOrderSaleOrderMOGenerationService
      productionOrderSaleOrderMOGenerationService;
  protected final StockLocationLineFetchService stockLocationLineFetchService;

  @Inject
  public ProductionOrderSaleOrderServiceImpl(
      ProductionOrderService productionOrderService,
      ProductionOrderRepository productionOrderRepo,
      AppProductionService appProductionService,
      ManufOrderSaleOrderService manufOrderSaleOrderService,
      SaleOrderLineBlockingProductionService saleOrderLineBlockingProductionService,
      ProductionOrderSaleOrderMOGenerationService productionOrderSaleOrderMOGenerationService,
      StockLocationLineFetchService stockLocationLineFetchService) {

    this.productionOrderService = productionOrderService;
    this.productionOrderRepo = productionOrderRepo;
    this.appProductionService = appProductionService;
    this.manufOrderSaleOrderService = manufOrderSaleOrderService;
    this.saleOrderLineBlockingProductionService = saleOrderLineBlockingProductionService;
    this.productionOrderSaleOrderMOGenerationService = productionOrderSaleOrderMOGenerationService;
    this.stockLocationLineFetchService = stockLocationLineFetchService;
  }

  @Override
  @Transactional(rollbackOn = {AxelorException.class})
  public String generateProductionOrder(
      SaleOrder saleOrder, List<SaleOrderLine> selectedSaleOrderLine) throws AxelorException {

    boolean oneProdOrderPerSO = appProductionService.getAppProduction().getOneProdOrderPerSO();

    List<SaleOrderLine> candidateSaleOrderLineList =
        CollectionUtils.isNotEmpty(selectedSaleOrderLine)
            ? selectedSaleOrderLine
            : saleOrder.getSaleOrderLineList();

    List<SaleOrderLine> saleOrderLineList = new ArrayList<>();
    List<String> noGenerationReasonList = new ArrayList<>();
    for (SaleOrderLine saleOrderLine : candidateSaleOrderLineList) {
      String noGenerationReason = getNoGenerationReason(saleOrderLine);
      if (StringUtils.isEmpty(noGenerationReason)) {
        saleOrderLineList.add(saleOrderLine);
      } else {
        noGenerationReasonList.add(noGenerationReason);
      }
    }

    checkSelectedLines(selectedSaleOrderLine, noGenerationReasonList);

    if (CollectionUtils.isEmpty(saleOrderLineList)) {
      return StringHtmlListBuilder.formatMessage(
          I18n.get(ProductionExceptionMessage.PRODUCTION_ORDER_NO_GENERATION),
          noGenerationReasonList);
    }

    if (oneProdOrderPerSO) {
      return getMessageForOneProdPerSo(saleOrder, selectedSaleOrderLine, saleOrderLineList);
    } else {
      return getMessageForOneProdPerSol(saleOrder, selectedSaleOrderLine, saleOrderLineList);
    }
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

    return productionOrderRepo.save(productionOrderService.createProductionOrder(saleOrder, null));
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
    if (productionOrder != null && CollectionUtils.isNotEmpty(productionOrder.getManufOrderSet())) {
      return productionOrder.getManufOrderSet().size();
    }

    return 0;
  }

  protected void checkSelectedLines(
      List<SaleOrderLine> selectedSaleOrderLine, List<String> noGenerationReasonList)
      throws AxelorException {

    if (CollectionUtils.isNotEmpty(selectedSaleOrderLine)
        && CollectionUtils.isNotEmpty(noGenerationReasonList)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          StringHtmlListBuilder.formatMessage(
              I18n.get(ProductionExceptionMessage.SALE_ORDER_SELECT_WRONG_LINE),
              noGenerationReasonList));
    }
  }

  @Override
  public boolean isGenerationNeeded(SaleOrderLine line) {
    return isLineHasCorrectSaleSupply(line)
        && manufOrderSaleOrderService.computeQuantityToProduceLeft(line).compareTo(BigDecimal.ZERO)
            > 0
        && !isLineProductionBlocked(line);
  }

  protected boolean isLineHasCorrectSaleSupply(SaleOrderLine saleOrderLine) {
    List<Integer> authorizedStatus = new ArrayList<>();
    authorizedStatus.add(SaleOrderLineRepository.SALE_SUPPLY_PRODUCE);
    authorizedStatus.add(SaleOrderLineRepository.SALE_SUPPLY_FROM_STOCK_AND_PRODUCE);
    return authorizedStatus.contains(saleOrderLine.getSaleSupplySelect());
  }

  protected boolean isLineProductionBlocked(SaleOrderLine saleOrderLine) {
    boolean isProductionBlocked = saleOrderLine.getIsProductionBlocking();
    LocalDate todayDate = appProductionService.getTodayDate(null);
    LocalDate productionBlockingToDate = saleOrderLine.getProductionBlockingToDate();
    return isProductionBlocked
        && (productionBlockingToDate == null || todayDate.isBefore(productionBlockingToDate));
  }

  protected String getNoGenerationReason(SaleOrderLine saleOrderLine) {
    Product product = saleOrderLine.getProduct();
    if (product == null) {
      return String.format(
          I18n.get(ProductionExceptionMessage.PRODUCTION_ORDER_LINE_NO_PRODUCT),
          saleOrderLine.getFullName());
    }
    String productFullName = product.getFullName();
    if (!isLineHasCorrectSaleSupply(saleOrderLine)) {
      return String.format(
          I18n.get(ProductionExceptionMessage.PRODUCTION_ORDER_LINE_WRONG_SUPPLY_TYPE),
          productFullName);
    }
    if (!ProductRepository.PRODUCT_TYPE_STORABLE.equals(product.getProductTypeSelect())) {
      return String.format(
          I18n.get(ProductionExceptionMessage.PRODUCTION_ORDER_LINE_NOT_STORABLE), productFullName);
    }
    if (saleOrderLineBlockingProductionService.isProductionBlocked(saleOrderLine)) {
      return String.format(
          I18n.get(ProductionExceptionMessage.PRODUCTION_ORDER_LINE_BLOCKED), productFullName);
    }
    BigDecimal qtyToProduceLeft =
        manufOrderSaleOrderService.computeQuantityToProduceLeft(saleOrderLine);
    if (qtyToProduceLeft.compareTo(BigDecimal.ZERO) <= 0) {
      return String.format(
          I18n.get(ProductionExceptionMessage.PRODUCTION_ORDER_LINE_QTY_ALREADY_PRODUCED),
          productFullName);
    }
    if (saleOrderLine.getSaleSupplySelect()
            == SaleOrderLineRepository.SALE_SUPPLY_FROM_STOCK_AND_PRODUCE
        && stockLocationLineFetchService
                .getAvailableQty(saleOrderLine.getSaleOrder().getStockLocation(), product)
                .compareTo(qtyToProduceLeft)
            >= 0) {
      return String.format(
          I18n.get(ProductionExceptionMessage.PRODUCTION_ORDER_LINE_STOCK_COVERS_QTY),
          productFullName);
    }
    BillOfMaterial billOfMaterial =
        productionOrderSaleOrderMOGenerationService.findBillOfMaterial(saleOrderLine, product);
    if (billOfMaterial == null) {
      return String.format(
          I18n.get(ProductionExceptionMessage.PRODUCTION_ORDER_LINE_NO_BOM), productFullName);
    }
    if (billOfMaterial.getProdProcess() == null) {
      return String.format(
          I18n.get(ProductionExceptionMessage.PRODUCTION_ORDER_LINE_BOM_NO_PROD_PROCESS),
          billOfMaterial.getName(),
          productFullName);
    }
    return null;
  }
}
