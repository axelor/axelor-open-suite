/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.stock.service.config.StockConfigService;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.i18n.I18n;
import com.axelor.meta.schema.actions.ActionView;
import com.google.common.base.Joiner;
import com.google.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PurchaseOrderFromSaleOrderLinesServiceImpl
    implements PurchaseOrderFromSaleOrderLinesService {

  protected SaleOrderLineRepository saleOrderLineRepository;
  protected StockConfigService stockConfigService;
  protected SaleOrderPurchaseService saleOrderPurchaseService;
  protected SaleOrderRepository saleOrderRepository;

  @Inject
  public PurchaseOrderFromSaleOrderLinesServiceImpl(
      SaleOrderLineRepository saleOrderLineRepository,
      StockConfigService stockConfigService,
      SaleOrderPurchaseService saleOrderPurchaseService,
      SaleOrderRepository saleOrderRepository) {
    this.saleOrderLineRepository = saleOrderLineRepository;
    this.stockConfigService = stockConfigService;
    this.saleOrderPurchaseService = saleOrderPurchaseService;
    this.saleOrderRepository = saleOrderRepository;
  }

  @Override
  public Map<String, Object> generatePurchaseOrdersFromSOLines(
      SaleOrder saleOrder,
      List<SaleOrderLine> saleOrderLines,
      Partner supplierPartner,
      String saleOrderLinesIdStr)
      throws AxelorException {

    if (isDirectOrderLocation(saleOrder)) {
      supplierPartner = saleOrder.getStockLocation().getPartner();
    } else if (saleOrderLinesIdStr != null) {
      saleOrderLines =
          Arrays.stream(saleOrderLinesIdStr.split(","))
              .map(
                  saleOrderLineIdStr ->
                      saleOrderLineRepository.find(Long.parseLong(saleOrderLineIdStr)))
              .collect(Collectors.toList());
    }

    boolean noProduct =
        saleOrderLines.stream().noneMatch(saleOrderLine -> saleOrderLine.getProduct() != null);

    if (saleOrderLines.isEmpty() || noProduct) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_NO_VALUE,
          I18n.get(SupplychainExceptionMessage.SO_LINE_PURCHASE_AT_LEAST_ONE));
    }

    if (supplierPartner == null) {
      supplierPartner =
          saleOrderLines.stream()
              .filter(saleOrderLine -> saleOrderLine.getSupplierPartner() != null)
              .findFirst()
              .map(SaleOrderLine::getSupplierPartner)
              .orElse(null);

      return selectSupplierPartner(saleOrder, saleOrderLines, supplierPartner);
    } else {
      PurchaseOrder purchaseOrder =
          saleOrderPurchaseService.createPurchaseOrder(
              supplierPartner, saleOrderLines, saleOrderRepository.find(saleOrder.getId()));
      return showPurchaseOrderForm(purchaseOrder);
    }
  }

  @Override
  public Boolean isDirectOrderLocation(SaleOrder saleOrder) {
    return saleOrder.getDirectOrderLocation()
        && saleOrder.getStockLocation() != null
        && saleOrder.getStockLocation().getPartner() != null
        && saleOrder.getStockLocation().getPartner().getIsSupplier();
  }

  @Override
  public Map<String, Object> selectSupplierPartner(
      SaleOrder saleOrder, List<SaleOrderLine> saleOrderLines, Partner supplierPartner) {
    return ActionView.define(I18n.get("SaleOrder"))
        .model(SaleOrder.class.getName())
        .add("form", "sale-order-generate-po-select-supplierpartner-form")
        .param("popup", "true")
        .param("show-toolbar", "false")
        .param("show-confirm", "false")
        .param("popup-save", "false")
        .param("forceEdit", "true")
        .context("_showRecord", ((saleOrder != null) ? String.valueOf(saleOrder.getId()) : 0L))
        .context("supplierPartnerId", ((supplierPartner != null) ? supplierPartner.getId() : 0L))
        .context(
            "saleOrderLineIdSelected",
            ((saleOrderLines != null)
                ? Joiner.on(",")
                    .join(
                        saleOrderLines.stream()
                            .map(SaleOrderLine::getId)
                            .collect(Collectors.toList()))
                : null))
        .map();
  }

  @Override
  public Map<String, Object> showPurchaseOrderForm(PurchaseOrder purchaseOrder) {
    return ActionView.define(I18n.get("Purchase order"))
        .model(PurchaseOrder.class.getName())
        .add("form", "purchase-order-form")
        .param("forceEdit", "true")
        .context("_showRecord", String.valueOf(purchaseOrder.getId()))
        .map();
  }
}
