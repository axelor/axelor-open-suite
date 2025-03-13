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
package com.axelor.apps.sale.service.saleorderline.creation;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.ProductMultipleQtyService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.exception.SaleExceptionMessage;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.sale.service.saleorder.SaleOrderComplementaryProductService;
import com.axelor.apps.sale.service.saleorder.SaleOrderComputeService;
import com.axelor.apps.sale.service.saleorder.SaleOrderService;
import com.axelor.apps.sale.service.saleorder.onchange.SaleOrderOnLineChangeService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineComputeService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineOnChangeService;
import com.axelor.apps.sale.service.saleorderline.product.SaleOrderLineOnProductChangeService;
import com.axelor.apps.sale.service.saleorderline.view.SaleOrderLineDomainService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;

public class SaleOrderLineGeneratorServiceImpl implements SaleOrderLineGeneratorService {
  protected SaleOrderLineInitValueService saleOrderLineInitValueService;
  protected SaleOrderLineOnProductChangeService saleOrderLineOnProductChangeService;
  protected SaleOrderLineRepository saleOrderLineRepository;
  protected ProductRepository productRepository;
  protected SaleOrderRepository saleOrderRepository;
  protected SaleOrderLineComputeService saleOrderLineComputeService;
  protected SaleOrderComputeService saleOrderComputeService;
  protected SaleOrderLineDomainService saleOrderLineDomainService;
  protected SaleOrderService saleOrderService;
  protected SaleOrderOnLineChangeService saleOrderOnLineChangeService;
  protected AppSaleService appSaleService;
  protected SaleOrderLineOnChangeService saleOrderLineOnChangeService;

  protected ProductMultipleQtyService productMultipleQtyService;
  protected SaleOrderComplementaryProductService saleOrderComplementaryProductService;

  @Inject
  public SaleOrderLineGeneratorServiceImpl(
      SaleOrderLineInitValueService saleOrderLineInitValueService,
      SaleOrderLineOnProductChangeService saleOrderLineOnProductChangeService,
      SaleOrderLineRepository saleOrderLineRepository,
      ProductRepository productRepository,
      SaleOrderRepository saleOrderRepository,
      SaleOrderLineComputeService saleOrderLineComputeService,
      SaleOrderComputeService saleOrderComputeService,
      SaleOrderLineDomainService saleOrderLineDomainService,
      SaleOrderService saleOrderService,
      AppSaleService appSaleService,
      SaleOrderLineOnChangeService saleOrderLineOnChangeService,
      ProductMultipleQtyService productMultipleQtyService,
      SaleOrderComplementaryProductService saleOrderComplementaryProductService) {
    this.saleOrderLineInitValueService = saleOrderLineInitValueService;
    this.saleOrderLineOnProductChangeService = saleOrderLineOnProductChangeService;
    this.saleOrderLineRepository = saleOrderLineRepository;
    this.productRepository = productRepository;
    this.saleOrderRepository = saleOrderRepository;
    this.saleOrderLineComputeService = saleOrderLineComputeService;
    this.saleOrderComputeService = saleOrderComputeService;
    this.saleOrderLineDomainService = saleOrderLineDomainService;
    this.saleOrderService = saleOrderService;
    this.appSaleService = appSaleService;
    this.saleOrderLineOnChangeService = saleOrderLineOnChangeService;
    this.productMultipleQtyService = productMultipleQtyService;
    this.saleOrderComplementaryProductService = saleOrderComplementaryProductService;
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public SaleOrderLine createSaleOrderLine(SaleOrder saleOrder, Product product, BigDecimal qty)
      throws AxelorException {
    checkSaleOrderAndProduct(saleOrder, product);
    SaleOrderLine saleOrderLine = new SaleOrderLine();
    saleOrderLineInitValueService.onNewInitValues(saleOrder, saleOrderLine, null);
    checkProduct(saleOrder, saleOrderLine, product);
    saleOrderLine.setProduct(product);
    if (appSaleService.getAppSale().getManageMultipleSaleQuantity()) {
      productMultipleQtyService.checkMultipleQty(product.getSaleProductMultipleQtyList(), qty);
    }
    if (qty == null) {
      qty = BigDecimal.ONE;
    }
    saleOrderLine.setQty(qty);
    saleOrderLineOnProductChangeService.computeLineFromProduct(saleOrder, saleOrderLine);

    saleOrderLineRepository.save(saleOrderLine);

    saleOrder.addSaleOrderLineListItem(saleOrderLine);
    saleOrderComplementaryProductService.handleComplementaryProducts(saleOrder);
    saleOrderComputeService.computeSaleOrder(saleOrder);
    saleOrderRepository.save(saleOrder);

    return saleOrderLine;
  }

  protected void checkSaleOrderAndProduct(SaleOrder saleOrder, Product product)
      throws AxelorException {
    if (saleOrder == null || product == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(SaleExceptionMessage.EITHER_PRODUCT_OR_SALE_ORDER_ARE_NULL));
    }
    if (saleOrder.getStatusSelect() != SaleOrderRepository.STATUS_DRAFT_QUOTATION) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(SaleExceptionMessage.SALE_ORDER_NOT_DRAFT));
    }
  }

  protected void checkProduct(SaleOrder saleOrder, SaleOrderLine saleOrderLine, Product product)
      throws AxelorException {
    String domain =
        saleOrderLineDomainService.computeProductDomain(saleOrderLine, saleOrder, false);
    if (!productRepository
        .all()
        .filter(domain)
        .bind("__date__", appSaleService.getTodayDate(saleOrder.getCompany()))
        .fetch()
        .contains(product)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(SaleExceptionMessage.PRODUCT_DOES_NOT_RESPECT_DOMAIN_RESTRICTIONS),
          product.getName());
    }
  }
}
