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

import com.axelor.apps.account.db.AnalyticAccount;
import com.axelor.apps.account.db.AnalyticDistributionTemplate;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.repo.AccountConfigRepository;
import com.axelor.apps.account.db.repo.AnalyticMoveLineRepository;
import com.axelor.apps.account.service.analytic.AnalyticMoveLineService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.AxelorException;
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
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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

  @Override
  public PurchaseOrderLine createAnalyticDistributionWithTemplate(
      PurchaseOrderLine purchaseOrderLine) {
    this.clearAnalyticInLine(purchaseOrderLine);

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

  public void clearAnalyticInLine(PurchaseOrderLine purchaseOrderLine) {
    purchaseOrderLine.setAxis1AnalyticAccount(null);
    purchaseOrderLine.setAxis2AnalyticAccount(null);
    purchaseOrderLine.setAxis3AnalyticAccount(null);
    purchaseOrderLine.setAxis4AnalyticAccount(null);
    purchaseOrderLine.setAxis5AnalyticAccount(null);
  }

  @Override
  public PurchaseOrderLine analyzePurchaseOrderLine(
      PurchaseOrderLine purchaseOrderLine, Company company) throws AxelorException {
    if (purchaseOrderLine == null) {
      return null;
    }

    if (purchaseOrderLine.getAnalyticMoveLineList() == null) {
      purchaseOrderLine.setAnalyticMoveLineList(new ArrayList<>());
    } else {
      purchaseOrderLine.getAnalyticMoveLineList().clear();
    }

    for (AnalyticAccount axisAnalyticAccount : this.getAxisAnalyticAccountList(purchaseOrderLine)) {
      AnalyticMoveLine analyticMoveLine =
          this.computeAnalyticMoveLine(purchaseOrderLine, company, axisAnalyticAccount);

      purchaseOrderLine.addAnalyticMoveLineListItem(analyticMoveLine);
    }

    return purchaseOrderLine;
  }

  protected List<AnalyticAccount> getAxisAnalyticAccountList(PurchaseOrderLine purchaseOrderLine) {
    return Stream.of(
            purchaseOrderLine.getAxis1AnalyticAccount(),
            purchaseOrderLine.getAxis2AnalyticAccount(),
            purchaseOrderLine.getAxis3AnalyticAccount(),
            purchaseOrderLine.getAxis4AnalyticAccount(),
            purchaseOrderLine.getAxis5AnalyticAccount())
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  protected AnalyticMoveLine computeAnalyticMoveLine(
      PurchaseOrderLine purchaseOrderLine, Company company, AnalyticAccount analyticAccount)
      throws AxelorException {
    AnalyticMoveLine analyticMoveLine =
        analyticMoveLineService.computeAnalytic(company, analyticAccount);

    analyticMoveLine.setDate(appBaseService.getTodayDate(company));
    analyticMoveLine.setAmount(purchaseOrderLine.getExTaxTotal());
    analyticMoveLine.setTypeSelect(AnalyticMoveLineRepository.STATUS_FORECAST_ORDER);

    return analyticMoveLine;
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
