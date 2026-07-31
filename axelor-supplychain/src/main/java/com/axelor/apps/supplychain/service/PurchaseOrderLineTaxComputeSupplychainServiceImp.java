/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.purchase.db.PurchaseOrderLine;
import com.axelor.apps.purchase.db.PurchaseOrderLineTax;
import com.axelor.apps.purchase.service.PurchaseOrderLineTaxComputeServiceImpl;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class PurchaseOrderLineTaxComputeSupplychainServiceImp
    extends PurchaseOrderLineTaxComputeServiceImpl {

  @Inject
  public PurchaseOrderLineTaxComputeSupplychainServiceImp(
      CurrencyScaleService currencyScaleService) {
    super(currencyScaleService);
  }

  @Override
  public void computeAndAddTaxToList(
      Map<?, PurchaseOrderLineTax> map,
      Map<PurchaseOrderLineTax, Set<PurchaseOrderLine>> purchaseOrderLineSetByTax,
      List<PurchaseOrderLineTax> purchaseOrderLineTaxList,
      Currency currency,
      List<PurchaseOrderLineTax> currentPurchaseOrderLineTaxList) {
    List<PurchaseOrderLineTax> deductibleTaxList =
        map.values().stream()
            .filter(it -> !this.isNonDeductibleTax(it))
            .collect(Collectors.toList());
    List<PurchaseOrderLineTax> nonDeductibleTaxList =
        map.values().stream().filter(this::isNonDeductibleTax).collect(Collectors.toList());

    nonDeductibleTaxList.forEach(
        it ->
            computeAndAddPurchaseOrderLineTax(
                it,
                purchaseOrderLineTaxList,
                currency,
                currentPurchaseOrderLineTaxList,
                deductibleTaxList,
                purchaseOrderLineSetByTax));
    deductibleTaxList.forEach(
        it ->
            computeAndAddPurchaseOrderLineTax(
                it,
                purchaseOrderLineTaxList,
                currency,
                currentPurchaseOrderLineTaxList,
                nonDeductibleTaxList,
                purchaseOrderLineSetByTax));
  }

  protected void computeAndAddPurchaseOrderLineTax(
      PurchaseOrderLineTax purchaseOrderLineTax,
      List<PurchaseOrderLineTax> purchaseOrderLineTaxList,
      Currency currency,
      List<PurchaseOrderLineTax> currentPurchaseOrderLineTaxList,
      List<PurchaseOrderLineTax> oppositeTaxList,
      Map<PurchaseOrderLineTax, Set<PurchaseOrderLine>> purchaseOrderLineSetByTax) {
    TaxLine taxLine = purchaseOrderLineTax.getTaxLine();
    BigDecimal taxTotal = this.computeTaxLineTaxTotal(taxLine, purchaseOrderLineTax);

    if (taxLine.getTax().getIsNonDeductibleTax()) {
      taxTotal =
          this.getAdjustedNonDeductibleTaxValue(
              purchaseOrderLineTax, taxTotal, oppositeTaxList, purchaseOrderLineSetByTax);
    } else {
      taxTotal =
          this.getAdjustedTaxValue(
              purchaseOrderLineTax, taxTotal, oppositeTaxList, purchaseOrderLineSetByTax);
    }

    this.computePurchaseOrderLineTax(
        purchaseOrderLineTax,
        currency,
        taxTotal,
        currentPurchaseOrderLineTaxList,
        purchaseOrderLineTaxList);
  }

  protected boolean isNonDeductibleTax(PurchaseOrderLineTax purchaseOrderLineTax) {
    return Optional.of(purchaseOrderLineTax.getTaxLine().getTax().getIsNonDeductibleTax())
        .orElse(false);
  }

  protected BigDecimal getAdjustedTaxValue(
      PurchaseOrderLineTax deductibleTax,
      BigDecimal taxValue,
      List<PurchaseOrderLineTax> nonDeductibleTaxList,
      Map<PurchaseOrderLineTax, Set<PurchaseOrderLine>> purchaseOrderLineSetByTax) {
    BigDecimal deductibleBase = deductibleTax.getExTaxBase();
    if (nonDeductibleTaxList.isEmpty() || deductibleBase.signum() == 0) {
      return taxValue;
    }

    BigDecimal nonDeductibleBase = BigDecimal.ZERO;
    for (PurchaseOrderLineTax nonDeductibleTax : nonDeductibleTaxList) {
      BigDecimal overlapBase =
          getOverlapBase(deductibleTax, nonDeductibleTax, purchaseOrderLineSetByTax);
      nonDeductibleBase = nonDeductibleBase.add(overlapBase.multiply(getTaxRate(nonDeductibleTax)));
    }

    return taxValue
        .multiply(deductibleBase.subtract(nonDeductibleBase))
        .divide(deductibleBase, AppBaseService.COMPUTATION_SCALING, RoundingMode.HALF_UP);
  }

  protected BigDecimal getAdjustedNonDeductibleTaxValue(
      PurchaseOrderLineTax nonDeductibleTax,
      BigDecimal taxValue,
      List<PurchaseOrderLineTax> deductibleTaxList,
      Map<PurchaseOrderLineTax, Set<PurchaseOrderLine>> purchaseOrderLineSetByTax) {
    BigDecimal nonDeductibleBase = nonDeductibleTax.getExTaxBase();
    if (deductibleTaxList.isEmpty() || nonDeductibleBase.signum() == 0) {
      return BigDecimal.ZERO;
    }

    BigDecimal deductibleTaxBase = BigDecimal.ZERO;
    for (PurchaseOrderLineTax deductibleTax : deductibleTaxList) {
      BigDecimal overlapBase =
          getOverlapBase(nonDeductibleTax, deductibleTax, purchaseOrderLineSetByTax);
      deductibleTaxBase = deductibleTaxBase.add(overlapBase.multiply(getTaxRate(deductibleTax)));
    }

    return taxValue
        .multiply(deductibleTaxBase)
        .divide(nonDeductibleBase, AppBaseService.COMPUTATION_SCALING, RoundingMode.HALF_UP);
  }

  protected BigDecimal getOverlapBase(
      PurchaseOrderLineTax firstTax,
      PurchaseOrderLineTax secondTax,
      Map<PurchaseOrderLineTax, Set<PurchaseOrderLine>> purchaseOrderLineSetByTax) {
    Set<PurchaseOrderLine> firstPurchaseOrderLineSet =
        purchaseOrderLineSetByTax.getOrDefault(firstTax, Set.of());
    Set<PurchaseOrderLine> secondPurchaseOrderLineSet =
        purchaseOrderLineSetByTax.getOrDefault(secondTax, Set.of());

    return firstPurchaseOrderLineSet.stream()
        .filter(secondPurchaseOrderLineSet::contains)
        .map(PurchaseOrderLine::getExTaxTotal)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  protected BigDecimal getTaxRate(PurchaseOrderLineTax purchaseOrderLineTax) {
    return purchaseOrderLineTax
        .getTaxLine()
        .getValue()
        .divide(BigDecimal.valueOf(100), AppBaseService.COMPUTATION_SCALING, RoundingMode.HALF_UP);
  }
}
