/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.supplychain.service;

import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.repo.AnalyticMoveLineRepository;
import com.axelor.apps.account.service.AnalyticMoveLineService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.AppAccountRepository;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.service.PurchaseOrderLineServiceImpl;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PurchaseOrderLineServiceSupplychainImpl extends PurchaseOrderLineServiceImpl {

  @Inject protected AnalyticMoveLineService analyticMoveLineService;

  @Inject protected UnitConversionService unitConversionService;

  @Inject protected AppAccountService appAccountService;

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public PurchaseOrderLine createPurchaseOrderLine(
      PurchaseOrder purchaseOrder, SaleOrderLine saleOrderLine) throws AxelorException {

    LOG.debug(
        "Création d'une ligne de commande fournisseur pour le produit : {}",
        new Object[] {saleOrderLine.getProductName()});

    Unit unit = null;
    BigDecimal qty = BigDecimal.ZERO;

    if (saleOrderLine.getTypeSelect() != SaleOrderLineRepository.TYPE_TITLE) {
      unit = saleOrderLine.getProduct().getPurchasesUnit();
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
            saleOrderLine.getDescription(),
            qty,
            unit);

    purchaseOrderLine.setIsTitleLine(
        saleOrderLine.getTypeSelect() == SaleOrderLineRepository.TYPE_TITLE);
    this.computeAnalyticDistribution(purchaseOrderLine);
    return purchaseOrderLine;
  }

  @Override
  public PurchaseOrderLine createPurchaseOrderLine(
      PurchaseOrder purchaseOrder,
      Product product,
      String productName,
      String description,
      BigDecimal qty,
      Unit unit)
      throws AxelorException {

    PurchaseOrderLine purchaseOrderLine =
        super.createPurchaseOrderLine(purchaseOrder, product, productName, description, qty, unit);

    //		purchaseOrderLine.setAmountInvoiced(BigDecimal.ZERO);
    //
    //		purchaseOrderLine.setIsInvoiced(false);

    return purchaseOrderLine;
  }

  public PurchaseOrderLine computeAnalyticDistribution(PurchaseOrderLine purchaseOrderLine)
      throws AxelorException {

    if (appAccountService.getAppAccount().getAnalyticDistributionTypeSelect()
        == AppAccountRepository.DISTRIBUTION_TYPE_FREE) {
      return purchaseOrderLine;
    }

    PurchaseOrder purchaseOrder = purchaseOrderLine.getPurchaseOrder();
    List<AnalyticMoveLine> analyticMoveLineList = purchaseOrderLine.getAnalyticMoveLineList();
    if ((analyticMoveLineList == null || analyticMoveLineList.isEmpty())) {
      analyticMoveLineList =
          analyticMoveLineService.generateLines(
              purchaseOrder.getSupplierPartner(),
              purchaseOrderLine.getProduct(),
              purchaseOrder.getCompany(),
              purchaseOrderLine.getExTaxTotal());
      purchaseOrderLine.setAnalyticMoveLineList(analyticMoveLineList);
    }
    if (analyticMoveLineList != null) {
      for (AnalyticMoveLine analyticMoveLine : analyticMoveLineList) {
        this.updateAnalyticMoveLine(analyticMoveLine, purchaseOrderLine);
      }
    }
    return purchaseOrderLine;
  }

  public void updateAnalyticMoveLine(
      AnalyticMoveLine analyticMoveLine, PurchaseOrderLine purchaseOrderLine) {

    analyticMoveLine.setPurchaseOrderLine(purchaseOrderLine);
    analyticMoveLine.setAmount(analyticMoveLineService.computeAmount(analyticMoveLine));
    analyticMoveLine.setDate(appBaseService.getTodayDate());
    analyticMoveLine.setTypeSelect(AnalyticMoveLineRepository.STATUS_FORECAST_ORDER);
  }

  public PurchaseOrderLine createAnalyticDistributionWithTemplate(
      PurchaseOrderLine purchaseOrderLine) throws AxelorException {
    List<AnalyticMoveLine> analyticMoveLineList = null;
    analyticMoveLineList =
        analyticMoveLineService.generateLinesWithTemplate(
            purchaseOrderLine.getAnalyticDistributionTemplate(), purchaseOrderLine.getExTaxTotal());
    if (analyticMoveLineList != null) {
      for (AnalyticMoveLine analyticMoveLine : analyticMoveLineList) {
        analyticMoveLine.setPurchaseOrderLine(purchaseOrderLine);
      }
    }
    purchaseOrderLine.setAnalyticMoveLineList(analyticMoveLineList);
    return purchaseOrderLine;
  }
}
