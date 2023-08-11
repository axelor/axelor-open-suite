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

import com.axelor.apps.account.service.analytic.AnalyticMoveLineService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.service.PurchaseOrderLineServiceImpl;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.supplychain.model.AnalyticLineModel;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PurchaseOrderLineServiceSupplyChainImpl extends PurchaseOrderLineServiceImpl
    implements PurchaseOrderLineServiceSupplyChain {

  protected AnalyticMoveLineService analyticMoveLineService;

  protected UnitConversionService unitConversionService;

  protected AppAccountService appAccountService;

  protected AccountConfigService accountConfigService;
  protected AnalyticLineModelService analyticLineModelService;

  @Inject
  public PurchaseOrderLineServiceSupplyChainImpl(
      AnalyticMoveLineService analyticMoveLineService,
      UnitConversionService unitConversionService,
      AppAccountService appAccountService,
      AccountConfigService accountConfigService,
      AnalyticLineModelService analyticLineModelService) {
    this.analyticMoveLineService = analyticMoveLineService;
    this.unitConversionService = unitConversionService;
    this.appAccountService = appAccountService;
    this.accountConfigService = accountConfigService;
    this.analyticLineModelService = analyticLineModelService;
  }

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public PurchaseOrderLine fill(PurchaseOrderLine purchaseOrderLine, PurchaseOrder purchaseOrder)
      throws AxelorException {

    purchaseOrderLine = super.fill(purchaseOrderLine, purchaseOrder);

    AnalyticLineModel analyticLineModel = new AnalyticLineModel(purchaseOrderLine);
    analyticLineModelService.getAndComputeAnalyticDistribution(analyticLineModel);

    return purchaseOrderLine;
  }

  @Override
  public PurchaseOrderLine createPurchaseOrderLine(
      PurchaseOrder purchaseOrder, SaleOrderLine saleOrderLine) throws AxelorException {

    LOG.debug(
        "Creation of a purchase order line for the product : {}", saleOrderLine.getProductName());

    Unit unit = null;
    BigDecimal qty = BigDecimal.ZERO;

    if (saleOrderLine.getTypeSelect() == SaleOrderLineRepository.TYPE_NORMAL) {

      if (saleOrderLine.getProduct() != null) {
        unit = saleOrderLine.getProduct().getPurchasesUnit();
      }
      qty = saleOrderLine.getQty();
      if (unit == null) {
        unit = saleOrderLine.getUnit();
      } else {
        qty =
            unitConversionService.convert(
                saleOrderLine.getUnit(), unit, qty, qty.scale(), saleOrderLine.getProduct());
      }
    }

    PurchaseOrderLine purchaseOrderLine =
        super.createPurchaseOrderLine(
            purchaseOrder,
            saleOrderLine.getProduct(),
            saleOrderLine.getProductName(),
            null,
            qty,
            unit);

    purchaseOrderLine.setIsTitleLine(
        !(saleOrderLine.getTypeSelect() == SaleOrderLineRepository.TYPE_NORMAL));

    AnalyticLineModel analyticLineModel = new AnalyticLineModel(purchaseOrderLine);
    analyticLineModelService.getAndComputeAnalyticDistribution(analyticLineModel);

    return purchaseOrderLine;
  }

  public BigDecimal computeUndeliveredQty(PurchaseOrderLine purchaseOrderLine) {
    Preconditions.checkNotNull(purchaseOrderLine);

    BigDecimal undeliveryQty =
        purchaseOrderLine.getQty().subtract(purchaseOrderLine.getReceivedQty());

    if (undeliveryQty.signum() > 0) {
      return undeliveryQty;
    }
    return BigDecimal.ZERO;
  }
}
