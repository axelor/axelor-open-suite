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
package com.axelor.apps.production.service.operationorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.production.service.ProdProcessLineOutsourceService;
import com.axelor.apps.production.service.manuforder.ManufOrderOutsourceService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.service.PurchaseOrderLineService;
import com.axelor.i18n.I18n;
import com.axelor.studio.db.AppBase;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class OperationOrderOutsourceServiceImpl implements OperationOrderOutsourceService {

  protected ProdProcessLineOutsourceService prodProcessLineOutsourceService;
  protected AppBaseService appBaseService;
  protected PurchaseOrderLineService purchaseOrderLineService;
  protected ManufOrderOutsourceService manufOrderOutsourceService;

  @Inject
  public OperationOrderOutsourceServiceImpl(
      ProdProcessLineOutsourceService prodProcessLineOutsourceService,
      ManufOrderOutsourceService manufOrderOutsourceService,
      AppBaseService appBaseService,
      PurchaseOrderLineService purchaseOrderLineService) {
    this.prodProcessLineOutsourceService = prodProcessLineOutsourceService;
    this.manufOrderOutsourceService = manufOrderOutsourceService;
    this.appBaseService = appBaseService;
    this.purchaseOrderLineService = purchaseOrderLineService;
  }

  @Override
  public Optional<Partner> getOutsourcePartner(OperationOrder operationOrder) {
    Objects.requireNonNull(operationOrder);
    Objects.requireNonNull(operationOrder.getManufOrder());

    // Fetching from manufOrder
    if (operationOrder.getOutsourcing() && operationOrder.getManufOrder().getOutsourcing()) {
      return manufOrderOutsourceService.getOutsourcePartner(operationOrder.getManufOrder());
      // Fetching from prodProcessLine or itself
    } else if (operationOrder.getOutsourcing()
        && !operationOrder.getManufOrder().getOutsourcing()) {
      ProdProcessLine prodProcessLine = operationOrder.getProdProcessLine();
      if ((prodProcessLine.getOutsourcing() || prodProcessLine.getOutsourcable())
          && operationOrder.getOutsourcingPartner() == null) {
        return prodProcessLineOutsourceService.getOutsourcePartner(prodProcessLine);
      } else {
        return Optional.ofNullable(operationOrder.getOutsourcingPartner());
      }
    }
    return Optional.empty();
  }

  @Override
  public List<PurchaseOrderLine> createPurchaseOrderLines(
      OperationOrder operationOrder, PurchaseOrder purchaseOrder) throws AxelorException {

    Objects.requireNonNull(operationOrder);
    Objects.requireNonNull(purchaseOrder);

    // Get products for purchaseOrder from prodProcessLine
    if (operationOrder.getProdProcessLine().getGeneratedPurchaseOrderProductSet() != null) {
      List<PurchaseOrderLine> purchaseOrderLineList = new ArrayList<>();
      for (Product product :
          operationOrder.getProdProcessLine().getGeneratedPurchaseOrderProductSet()) {
        PurchaseOrderLine purchaseOrderLine =
            this.createPurchaseOrderLine(operationOrder, purchaseOrder, product).orElse(null);
        if (purchaseOrderLine != null) {
          purchaseOrderLineList.add(purchaseOrderLine);
          purchaseOrder.addPurchaseOrderLineListItem(purchaseOrderLine);
        }
      }
      return purchaseOrderLineList;
    }

    return Collections.emptyList();
  }

  @Override
  public Optional<PurchaseOrderLine> createPurchaseOrderLine(
      OperationOrder operationOrder, PurchaseOrder purchaseOrder, Product product)
      throws AxelorException {

    Unit productUnit = getProductUnit(product);

    BigDecimal quantity = recomputeQty(operationOrder.getPlannedHumanDuration(), productUnit);

    return Optional.ofNullable(
        purchaseOrderLineService.createPurchaseOrderLine(
            purchaseOrder, product, null, null, quantity, productUnit));
  }

  protected Unit getProductUnit(Product product) throws AxelorException {
    return Optional.ofNullable(product.getPurchasesUnit())
        .or(() -> Optional.ofNullable(product.getUnit()))
        .orElseThrow(
            () ->
                new AxelorException(
                    TraceBackRepository.CATEGORY_NO_VALUE,
                    I18n.get(ProductionExceptionMessage.PURCHASE_ORDER_NO_HOURS_UNIT)));
  }

  @Override
  public Optional<PurchaseOrderLine> createPurchaseOrderLine(
      ManufOrder manufOrder, PurchaseOrder purchaseOrder, Product product) throws AxelorException {

    Unit productUnit = getProductUnit(product);

    BigDecimal quantity =
        recomputeQty(
            manufOrder.getOperationOrderList().stream()
                .map(OperationOrder::getPlannedHumanDuration)
                .reduce(Long::sum)
                .orElse(0L),
            productUnit);

    return Optional.ofNullable(
        purchaseOrderLineService.createPurchaseOrderLine(
            purchaseOrder, product, null, null, quantity, productUnit));
  }

  @Override
  public List<PurchaseOrderLine> createPurchaseOrderLines(
      ManufOrder manufOrder, Set<Product> productSet, PurchaseOrder purchaseOrder)
      throws AxelorException {
    Objects.requireNonNull(manufOrder);
    Objects.requireNonNull(purchaseOrder);

    List<PurchaseOrderLine> list = new ArrayList<>();
    for (Product product : manufOrder.getProdProcess().getGeneratedPurchaseOrderProductSet()) {
      PurchaseOrderLine purchaseOrderLine =
          this.createPurchaseOrderLine(manufOrder, purchaseOrder, product).orElse(null);
      if (purchaseOrderLine != null) {
        list.add(purchaseOrderLine);
        purchaseOrder.addPurchaseOrderLineListItem(purchaseOrderLine);
      }
    }
    return list;
  }

  protected BigDecimal recomputeQty(Long duration, Unit productUnit) {
    AppBase appBase = appBaseService.getAppBase();

    if (List.of(appBase.getUnitDays(), appBase.getUnitHours(), appBase.getUnitMinutes())
        .contains(productUnit)) {
      long secondsInProductUnit;
      // Product is in unit day
      if (productUnit.equals(appBase.getUnitDays())) {
        secondsInProductUnit = 86400;
      }
      // Product is in unit hours
      else if (productUnit.equals(appBase.getUnitHours())) {
        secondsInProductUnit = 3600;
      }
      // Product is in unit minute
      else {
        secondsInProductUnit = 60;
      }
      return new BigDecimal(duration)
          .divide(
              BigDecimal.valueOf(secondsInProductUnit),
              appBaseService.getNbDecimalDigitForQty(),
              RoundingMode.HALF_UP);
    }

    return BigDecimal.ONE;
  }

  @Override
  public long getOutsourcingDuration(OperationOrder operationOrder) {
    return Optional.ofNullable(operationOrder.getProdProcessLine())
        .map(ProdProcessLine::getOutsourcingDuration)
        .orElse(0l);
  }
}
