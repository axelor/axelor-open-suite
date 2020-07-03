/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.db.AnalyticDistributionTemplate;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.BudgetDistribution;
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
import com.axelor.inject.Beans;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PurchaseOrderLineServiceSupplychainImpl extends PurchaseOrderLineServiceImpl {

  @Inject protected AnalyticMoveLineService analyticMoveLineService;

  @Inject protected UnitConversionService unitConversionService;

  @Inject protected AppAccountService appAccountService;

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public PurchaseOrderLine fill(PurchaseOrderLine purchaseOrderLine, PurchaseOrder purchaseOrder)
      throws AxelorException {

    purchaseOrderLine = super.fill(purchaseOrderLine, purchaseOrder);

    this.getAndComputeAnalyticDistribution(purchaseOrderLine, purchaseOrder);

    return purchaseOrderLine;
  }

  public PurchaseOrderLine createPurchaseOrderLine(
      PurchaseOrder purchaseOrder, SaleOrderLine saleOrderLine) throws AxelorException {

    LOG.debug(
        "Création d'une ligne de commande fournisseur pour le produit : {}",
        new Object[] {saleOrderLine.getProductName()});

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
            saleOrderLine.getDescription(),
            qty,
            unit);

    purchaseOrderLine.setIsTitleLine(
        saleOrderLine.getTypeSelect() == SaleOrderLineRepository.TYPE_TITLE);
    this.getAndComputeAnalyticDistribution(purchaseOrderLine, purchaseOrder);
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

  public PurchaseOrderLine getAndComputeAnalyticDistribution(
      PurchaseOrderLine purchaseOrderLine, PurchaseOrder purchaseOrder) {

    if (appAccountService.getAppAccount().getAnalyticDistributionTypeSelect()
        == AppAccountRepository.DISTRIBUTION_TYPE_FREE) {
      return purchaseOrderLine;
    }

    AnalyticDistributionTemplate analyticDistributionTemplate =
        analyticMoveLineService.getAnalyticDistributionTemplate(
            purchaseOrder.getSupplierPartner(),
            purchaseOrderLine.getProduct(),
            purchaseOrder.getCompany());

    purchaseOrderLine.setAnalyticDistributionTemplate(analyticDistributionTemplate);

    if (purchaseOrderLine.getAnalyticMoveLineList() != null) {
      purchaseOrderLine.getAnalyticMoveLineList().clear();
    }

    this.computeAnalyticDistribution(purchaseOrderLine);

    return purchaseOrderLine;
  }

  public PurchaseOrderLine computeAnalyticDistribution(PurchaseOrderLine purchaseOrderLine) {

    List<AnalyticMoveLine> analyticMoveLineList = purchaseOrderLine.getAnalyticMoveLineList();

    if ((analyticMoveLineList == null || analyticMoveLineList.isEmpty())) {
      createAnalyticDistributionWithTemplate(purchaseOrderLine);
    } else {
      LocalDate date = appAccountService.getTodayDate();
      for (AnalyticMoveLine analyticMoveLine : analyticMoveLineList) {
        analyticMoveLineService.updateAnalyticMoveLine(
            analyticMoveLine, purchaseOrderLine.getCompanyExTaxTotal(), date);
      }
    }
    return purchaseOrderLine;
  }

  public PurchaseOrderLine createAnalyticDistributionWithTemplate(
      PurchaseOrderLine purchaseOrderLine) {

    List<AnalyticMoveLine> analyticMoveLineList =
        analyticMoveLineService.generateLines(
            purchaseOrderLine.getAnalyticDistributionTemplate(),
            purchaseOrderLine.getExTaxTotal(),
            AnalyticMoveLineRepository.STATUS_FORECAST_ORDER,
            appBaseService.getTodayDate());

    purchaseOrderLine.clearAnalyticMoveLineList();
    analyticMoveLineList.forEach(purchaseOrderLine::addAnalyticMoveLineListItem);
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

  public void computeBudgetDistributionSumAmount(
      PurchaseOrderLine purchaseOrderLine, PurchaseOrder purchaseOrder) {
    List<BudgetDistribution> budgetDistributionList = purchaseOrderLine.getBudgetDistributionList();
    BigDecimal budgetDistributionSumAmount = BigDecimal.ZERO;
    LocalDate computeDate = purchaseOrder.getOrderDate();

    if (budgetDistributionList != null && !budgetDistributionList.isEmpty()) {

      for (BudgetDistribution budgetDistribution : budgetDistributionList) {
        budgetDistributionSumAmount =
            budgetDistributionSumAmount.add(budgetDistribution.getAmount());
        Beans.get(BudgetSupplychainService.class)
            .computeBudgetDistributionSumAmount(budgetDistribution, computeDate);
      }
    }
    purchaseOrderLine.setBudgetDistributionSumAmount(budgetDistributionSumAmount);
  }
}
