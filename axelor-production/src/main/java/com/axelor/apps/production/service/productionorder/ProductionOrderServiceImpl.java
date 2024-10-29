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
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.ProductionOrder;
import com.axelor.apps.production.db.repo.ProductionOrderRepository;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.production.service.manuforder.ManufOrderService.ManufOrderOriginTypeProduction;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;

public class ProductionOrderServiceImpl implements ProductionOrderService {

  protected SequenceService sequenceService;
  protected ProductionOrderRepository productionOrderRepo;
  protected ProductionOrderSaleOrderMOGenerationService productionOrderSaleOrderMOGenerationService;
  protected ProductionOrderUpdateService productionOrderUpdateService;

  @Inject
  public ProductionOrderServiceImpl(
      SequenceService sequenceService,
      ProductionOrderRepository productionOrderRepo,
      ProductionOrderSaleOrderMOGenerationService productionOrderSaleOrderMOGenerationService,
      ProductionOrderUpdateService productionOrderUpdateService) {
    this.sequenceService = sequenceService;
    this.productionOrderRepo = productionOrderRepo;
    this.productionOrderSaleOrderMOGenerationService = productionOrderSaleOrderMOGenerationService;
    this.productionOrderUpdateService = productionOrderUpdateService;
  }

  public ProductionOrder createProductionOrder(SaleOrder saleOrder, BillOfMaterial billOfMaterial)
      throws AxelorException {

    ProductionOrder productionOrder = new ProductionOrder();
    Company company =
        Optional.ofNullable(billOfMaterial).map(BillOfMaterial::getCompany).orElse(null);
    if (saleOrder != null) {
      productionOrder.setClientPartner(saleOrder.getClientPartner());
      productionOrder.setSaleOrder(saleOrder);
      productionOrder.setProductionOrderSeq(
          String.format(
              "%s - %s",
              saleOrder.getFullName(), this.getProductionOrderSeq(productionOrder, company)));
    } else {
      productionOrder.setProductionOrderSeq(this.getProductionOrderSeq(productionOrder, company));
    }

    return productionOrder;
  }

  public String getProductionOrderSeq(ProductionOrder productionOrder, Company company)
      throws AxelorException {

    String seq =
        sequenceService.getSequenceNumber(
            SequenceRepository.PRODUCTION_ORDER,
            company,
            ProductionOrder.class,
            "productionOrderSeq",
            productionOrder);

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

    ProductionOrder productionOrder = this.createProductionOrder(null, billOfMaterial);

    productionOrderSaleOrderMOGenerationService.addManufOrder(
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
  public Set<ProductionOrder> updateStatus(Set<ProductionOrder> productionOrderSet) {

    if (CollectionUtils.isEmpty(productionOrderSet)) {
      return productionOrderSet;
    }

    for (ProductionOrder productionOrder : productionOrderSet) {
      productionOrderUpdateService.updateProductionOrderStatus(productionOrder);
    }

    return productionOrderSet;
  }
}
