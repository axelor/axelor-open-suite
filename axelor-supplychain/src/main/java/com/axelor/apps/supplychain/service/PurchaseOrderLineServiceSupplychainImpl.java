/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.db.AnalyticAccount;
import com.axelor.apps.account.db.AnalyticAxisByCompany;
import com.axelor.apps.account.db.AnalyticDistributionTemplate;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.BudgetDistribution;
import com.axelor.apps.account.db.repo.AccountConfigRepository;
import com.axelor.apps.account.db.repo.AnalyticMoveLineRepository;
import com.axelor.apps.account.service.analytic.AnalyticMoveLineService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.service.UnitConversionService;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.service.PurchaseOrderLineServiceImpl;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PurchaseOrderLineServiceSupplychainImpl extends PurchaseOrderLineServiceImpl
    implements PurchaseOrderLineServiceSupplyChain {

  @Inject protected AnalyticMoveLineService analyticMoveLineService;

  @Inject protected UnitConversionService unitConversionService;

  @Inject protected AppAccountService appAccountService;

  @Inject protected AccountConfigService accountConfigService;

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
    this.getAndComputeAnalyticDistribution(purchaseOrderLine, purchaseOrder);
    return purchaseOrderLine;
  }

  public PurchaseOrderLine getAndComputeAnalyticDistribution(
      PurchaseOrderLine purchaseOrderLine, PurchaseOrder purchaseOrder) throws AxelorException {

    if (accountConfigService
            .getAccountConfig(purchaseOrder.getCompany())
            .getAnalyticDistributionTypeSelect()
        == AccountConfigRepository.DISTRIBUTION_TYPE_FREE) {
      return purchaseOrderLine;
    }

    AnalyticDistributionTemplate analyticDistributionTemplate =
        analyticMoveLineService.getAnalyticDistributionTemplate(
            purchaseOrder.getSupplierPartner(),
            purchaseOrderLine.getProduct(),
            purchaseOrder.getCompany(),
            true);

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
      LocalDate date =
          appAccountService.getTodayDate(purchaseOrderLine.getPurchaseOrder().getCompany());
      for (AnalyticMoveLine analyticMoveLine : analyticMoveLineList) {
        analyticMoveLineService.updateAnalyticMoveLine(
            analyticMoveLine, purchaseOrderLine.getCompanyExTaxTotal(), date);
      }
    }
    return purchaseOrderLine;
  }

  public PurchaseOrderLine createAnalyticDistributionWithTemplate(
      PurchaseOrderLine purchaseOrderLine) {

    clearAnalyticInLine(purchaseOrderLine);

    List<AnalyticMoveLine> analyticMoveLineList =
        analyticMoveLineService.generateLines(
            purchaseOrderLine.getAnalyticDistributionTemplate(),
            purchaseOrderLine.getExTaxTotal(),
            AnalyticMoveLineRepository.STATUS_FORECAST_ORDER,
            appBaseService.getTodayDate(
                purchaseOrderLine.getPurchaseOrder() != null
                    ? purchaseOrderLine.getPurchaseOrder().getCompany()
                    : Optional.ofNullable(AuthUtils.getUser())
                        .map(User::getActiveCompany)
                        .orElse(null)));

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

  @Override
  public PurchaseOrderLine analyzePurchaseOrderLine(
      PurchaseOrderLine purchaseOrderLine, PurchaseOrder purchaseOrder, Company company)
      throws AxelorException {
    if (purchaseOrderLine != null) {

      if (purchaseOrderLine.getAnalyticMoveLineList() == null) {
        purchaseOrderLine.setAnalyticMoveLineList(new ArrayList<>());
      } else {
        purchaseOrderLine.getAnalyticMoveLineList().clear();
      }

      AnalyticMoveLine analyticMoveLine = null;

      if (purchaseOrderLine.getAxis1AnalyticAccount() != null) {
        analyticMoveLine =
            this.computeAnalyticMoveLine(
                purchaseOrderLine,
                purchaseOrder,
                company,
                purchaseOrderLine.getAxis1AnalyticAccount());
        purchaseOrderLine.addAnalyticMoveLineListItem(analyticMoveLine);
      }
      if (purchaseOrderLine.getAxis2AnalyticAccount() != null) {
        analyticMoveLine =
            this.computeAnalyticMoveLine(
                purchaseOrderLine,
                purchaseOrder,
                company,
                purchaseOrderLine.getAxis2AnalyticAccount());
        purchaseOrderLine.addAnalyticMoveLineListItem(analyticMoveLine);
      }
      if (purchaseOrderLine.getAxis3AnalyticAccount() != null) {
        analyticMoveLine =
            this.computeAnalyticMoveLine(
                purchaseOrderLine,
                purchaseOrder,
                company,
                purchaseOrderLine.getAxis3AnalyticAccount());
        purchaseOrderLine.addAnalyticMoveLineListItem(analyticMoveLine);
      }
      if (purchaseOrderLine.getAxis4AnalyticAccount() != null) {
        analyticMoveLine =
            this.computeAnalyticMoveLine(
                purchaseOrderLine,
                purchaseOrder,
                company,
                purchaseOrderLine.getAxis4AnalyticAccount());
        purchaseOrderLine.addAnalyticMoveLineListItem(analyticMoveLine);
      }
      if (purchaseOrderLine.getAxis5AnalyticAccount() != null) {
        analyticMoveLine =
            this.computeAnalyticMoveLine(
                purchaseOrderLine,
                purchaseOrder,
                company,
                purchaseOrderLine.getAxis5AnalyticAccount());
        purchaseOrderLine.addAnalyticMoveLineListItem(analyticMoveLine);
      }
    }
    return purchaseOrderLine;
  }

  @Override
  public AnalyticMoveLine computeAnalyticMoveLine(
      PurchaseOrderLine purchaseOrderLine,
      PurchaseOrder purchaseOrder,
      Company company,
      AnalyticAccount analyticAccount)
      throws AxelorException {
    AnalyticMoveLine analyticMoveLine =
        analyticMoveLineService.computeAnalytic(company, analyticAccount);

    analyticMoveLine.setDate(
        appBaseService.getTodayDate(
            purchaseOrderLine.getPurchaseOrder() != null
                ? purchaseOrderLine.getPurchaseOrder().getCompany()
                : Optional.ofNullable(AuthUtils.getUser())
                    .map(User::getActiveCompany)
                    .orElse(null)));
    analyticMoveLine.setAmount(purchaseOrderLine.getExTaxTotal());
    analyticMoveLine.setTypeSelect(AnalyticMoveLineRepository.STATUS_FORECAST_ORDER);

    return analyticMoveLine;
  }

  @Override
  public PurchaseOrderLine printAnalyticAccount(PurchaseOrderLine line, Company company)
      throws AxelorException {
    if (line.getAnalyticMoveLineList() != null
        && !line.getAnalyticMoveLineList().isEmpty()
        && company != null) {
      List<AnalyticMoveLine> analyticMoveLineList = Lists.newArrayList();
      for (AnalyticAxisByCompany analyticAxisByCompany :
          accountConfigService.getAccountConfig(company).getAnalyticAxisByCompanyList()) {
        for (AnalyticMoveLine analyticMoveLine : line.getAnalyticMoveLineList()) {
          if (analyticMoveLine.getAnalyticAxis().equals(analyticAxisByCompany.getAnalyticAxis())) {
            analyticMoveLineList.add(analyticMoveLine);
          }
        }

        if (!analyticMoveLineList.isEmpty()) {

          AnalyticMoveLine analyticMoveLine = analyticMoveLineList.get(0);
          if (analyticMoveLineList.size() == 1
              && analyticMoveLine.getPercentage().compareTo(new BigDecimal(100)) == 0) {
            AnalyticAccount analyticAccount = analyticMoveLine.getAnalyticAccount();
            switch (analyticAxisByCompany.getSequence()) {
              case 0:
                line.setAxis1AnalyticAccount(analyticAccount);
                break;
              case 1:
                line.setAxis2AnalyticAccount(analyticAccount);
                break;
              case 2:
                line.setAxis3AnalyticAccount(analyticAccount);
                break;
              case 3:
                line.setAxis4AnalyticAccount(analyticAccount);
                break;
              case 4:
                line.setAxis5AnalyticAccount(analyticAccount);
                break;
              default:
                break;
            }
          }
        }
        analyticMoveLineList.clear();
      }
    }
    return line;
  }

  public void clearAnalyticInLine(PurchaseOrderLine purchaseOrderLine) {
    purchaseOrderLine.setAxis1AnalyticAccount(null);
    purchaseOrderLine.setAxis2AnalyticAccount(null);
    purchaseOrderLine.setAxis3AnalyticAccount(null);
    purchaseOrderLine.setAxis4AnalyticAccount(null);
    purchaseOrderLine.setAxis5AnalyticAccount(null);
  }
}
