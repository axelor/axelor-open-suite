/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.ProductionOrder;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.db.repo.ProductionOrderRepository;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.production.service.manuforder.ManufOrderService;
import com.axelor.apps.production.service.manuforder.ManufOrderService.ManufOrderOriginType;
import com.axelor.apps.production.service.manuforder.ManufOrderService.ManufOrderOriginTypeProduction;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.i18n.I18n;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;

public class ProductionOrderServiceImpl implements ProductionOrderService {

  protected ManufOrderService manufOrderService;
  protected SequenceService sequenceService;
  protected ProductionOrderRepository productionOrderRepo;

  @Inject
  public ProductionOrderServiceImpl(
      ManufOrderService manufOrderService,
      SequenceService sequenceService,
      ProductionOrderRepository productionOrderRepo) {
    this.manufOrderService = manufOrderService;
    this.sequenceService = sequenceService;
    this.productionOrderRepo = productionOrderRepo;
  }

  public ProductionOrder createProductionOrder(SaleOrder saleOrder) throws AxelorException {

    ProductionOrder productionOrder = new ProductionOrder(this.getProductionOrderSeq());
    if (saleOrder != null) {
      productionOrder.setClientPartner(saleOrder.getClientPartner());
      productionOrder.setSaleOrder(saleOrder);
    }
    return productionOrder;
  }

  public String getProductionOrderSeq() throws AxelorException {

    String seq =
        sequenceService.getSequenceNumber(
            SequenceRepository.PRODUCTION_ORDER, ProductionOrder.class, "productionOrderSeq");

    if (seq == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(ProductionExceptionMessage.PRODUCTION_ORDER_SEQ));
    }

    return seq;
  }

  /**
   * Generate a Production Order
   *
   * @param product Product must be passed in param because product can be different of bill of
   *     material product (Product variant)
   * @param billOfMaterial
   * @param qtyRequested
   * @param businessProject
   * @return
   * @throws AxelorException
   */
  @Transactional(rollbackOn = {Exception.class})
  public ProductionOrder generateProductionOrder(
      Product product,
      BillOfMaterial billOfMaterial,
      BigDecimal qtyRequested,
      LocalDateTime startDate)
      throws AxelorException {

    ProductionOrder productionOrder = this.createProductionOrder(null);

    this.addManufOrder(
        productionOrder,
        product,
        billOfMaterial,
        qtyRequested,
        startDate,
        null,
        null,
        null,
        ManufOrderOriginTypeProduction.ORIGIN_TYPE_OTHER);

    return productionOrderRepo.save(productionOrder);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public ProductionOrder addManufOrder(
      ProductionOrder productionOrder,
      Product product,
      BillOfMaterial billOfMaterial,
      BigDecimal qtyRequested,
      LocalDateTime startDate,
      LocalDateTime endDate,
      SaleOrder saleOrder,
      SaleOrderLine saleOrderLine,
      ManufOrderOriginType manufOrderOriginType)
      throws AxelorException {

    ManufOrder manufOrder =
        manufOrderService.generateManufOrder(
            product,
            qtyRequested,
            ManufOrderService.DEFAULT_PRIORITY,
            ManufOrderService.IS_TO_INVOICE,
            billOfMaterial,
            startDate,
            endDate,
            manufOrderOriginType);

    if (manufOrder != null) {
      if (saleOrder != null) {
        manufOrder.addSaleOrderSetItem(saleOrder);
        manufOrder.setClientPartner(saleOrder.getClientPartner());
        manufOrder.setMoCommentFromSaleOrder("");
        manufOrder.setMoCommentFromSaleOrderLine("");
        if (!Strings.isNullOrEmpty(saleOrder.getProductionNote())) {
          manufOrder.setMoCommentFromSaleOrder(saleOrder.getProductionNote());
        }
        if (saleOrderLine != null
            && !Strings.isNullOrEmpty(saleOrderLine.getLineProductionComment())) {
          manufOrder.setMoCommentFromSaleOrderLine(saleOrderLine.getLineProductionComment());
        }
      }
      productionOrder.addManufOrderSetItem(manufOrder);
      manufOrder.addProductionOrderSetItem(productionOrder);
    }

    productionOrder = updateProductionOrderStatus(productionOrder);
    return productionOrderRepo.save(productionOrder);
  }

  @Override
  public Set<ProductionOrder> updateStatus(Set<ProductionOrder> productionOrderSet) {

    if (CollectionUtils.isEmpty(productionOrderSet)) {
      return productionOrderSet;
    }

    for (ProductionOrder productionOrder : productionOrderSet) {
      updateProductionOrderStatus(productionOrder);
    }

    return productionOrderSet;
  }

  protected ProductionOrder updateProductionOrderStatus(ProductionOrder productionOrder) {

    if (productionOrder.getStatusSelect() == null) {
      return productionOrder;
    }

    int statusSelect = productionOrder.getStatusSelect();

    if (productionOrder.getManufOrderSet().stream()
        .allMatch(
            manufOrder -> manufOrder.getStatusSelect() == ManufOrderRepository.STATUS_DRAFT)) {
      statusSelect = ProductionOrderRepository.STATUS_DRAFT;
      productionOrder.setStatusSelect(statusSelect);
      return productionOrderRepo.save(productionOrder);
    }

    boolean oneStarted = false;
    boolean onePlanned = false;
    boolean allCancel = true;
    boolean allCompleted = true;

    for (ManufOrder manufOrder : productionOrder.getManufOrderSet()) {

      switch (manufOrder.getStatusSelect()) {
        case (ManufOrderRepository.STATUS_PLANNED):
          onePlanned = true;
          allCancel = false;
          allCompleted = false;
          break;
        case (ManufOrderRepository.STATUS_IN_PROGRESS):
        case (ManufOrderRepository.STATUS_STANDBY):
          oneStarted = true;
          allCancel = false;
          allCompleted = false;
          break;
        case (ManufOrderRepository.STATUS_FINISHED):
          allCancel = false;
          break;
        case (ManufOrderRepository.STATUS_CANCELED):
          break;
        default:
          allCompleted = false;
          break;
      }
    }

    if (allCancel) {
      statusSelect = ProductionOrderRepository.STATUS_CANCELED;
    } else if (allCompleted) {
      statusSelect = ProductionOrderRepository.STATUS_COMPLETED;
    } else if (oneStarted) {
      statusSelect = ProductionOrderRepository.STATUS_STARTED;
    } else if (onePlanned
        && (productionOrder.getStatusSelect() == ProductionOrderRepository.STATUS_DRAFT
            || productionOrder.getStatusSelect() == ProductionOrderRepository.STATUS_CANCELED
            || productionOrder.getStatusSelect() == ProductionOrderRepository.STATUS_COMPLETED)) {
      statusSelect = ProductionOrderRepository.STATUS_PLANNED;
    }

    productionOrder.setStatusSelect(statusSelect);
    return productionOrderRepo.save(productionOrder);
  }
}
