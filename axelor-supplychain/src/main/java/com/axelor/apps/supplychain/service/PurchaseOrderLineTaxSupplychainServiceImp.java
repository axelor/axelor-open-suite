package com.axelor.apps.supplychain.service;

import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.OrderLineTaxService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.apps.purchase.db.PurchaseOrderLineTax;
import com.axelor.apps.purchase.service.PurchaseOrderLineTaxServiceImpl;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class PurchaseOrderLineTaxSupplychainServiceImp extends PurchaseOrderLineTaxServiceImpl {

  @Inject
  public PurchaseOrderLineTaxSupplychainServiceImp(
      OrderLineTaxService orderLineTaxService,
      TaxService taxService,
      AppBaseService appBaseService,
      CurrencyScaleService currencyScaleService) {
    super(orderLineTaxService, taxService, appBaseService, currencyScaleService);
  }

  @Override
  protected void computeAndAddTaxToList(
      Map<TaxLine, PurchaseOrderLineTax> map,
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
                deductibleTaxList));
    deductibleTaxList.forEach(
        it ->
            computeAndAddPurchaseOrderLineTax(
                it,
                purchaseOrderLineTaxList,
                currency,
                currentPurchaseOrderLineTaxList,
                nonDeductibleTaxList));
  }

  protected void computeAndAddPurchaseOrderLineTax(
      PurchaseOrderLineTax purchaseOrderLineTax,
      List<PurchaseOrderLineTax> purchaseOrderLineTaxList,
      Currency currency,
      List<PurchaseOrderLineTax> currentPurchaseOrderLineTaxList,
      List<PurchaseOrderLineTax> nonDeductibleTaxList) {
    TaxLine taxLine = purchaseOrderLineTax.getTaxLine();
    BigDecimal taxTotal = this.computeTaxLineTaxTotal(taxLine, purchaseOrderLineTax);

    if (taxLine.getTax().getIsNonDeductibleTax()) {
      taxTotal = this.getAdjustedNonDeductibleTaxValue(taxTotal, nonDeductibleTaxList);
    } else {
      taxTotal = this.getAdjustedTaxValue(taxTotal, nonDeductibleTaxList);
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
      BigDecimal taxValue, List<PurchaseOrderLineTax> nonDeductibleTaxList) {
    BigDecimal deductibleTaxValue =
        nonDeductibleTaxList.stream()
            .map(PurchaseOrderLineTax::getTaxLine)
            .map(TaxLine::getValue)
            .reduce(BigDecimal::multiply)
            .orElse(BigDecimal.ZERO)
            .divide(
                BigDecimal.valueOf(100), AppBaseService.COMPUTATION_SCALING, RoundingMode.HALF_UP);

    return BigDecimal.ONE
        .subtract(deductibleTaxValue)
        .multiply(taxValue)
        .setScale(AppBaseService.COMPUTATION_SCALING, RoundingMode.HALF_UP);
  }

  protected BigDecimal getAdjustedNonDeductibleTaxValue(
      BigDecimal taxValue, List<PurchaseOrderLineTax> deductibleTaxList) {
    BigDecimal nonDeductibleTaxValue = BigDecimal.ZERO;

    for (PurchaseOrderLineTax purchaseOrderLineTax : deductibleTaxList) {
      nonDeductibleTaxValue =
          nonDeductibleTaxValue.add(
              taxValue.multiply(
                  purchaseOrderLineTax
                      .getTaxLine()
                      .getValue()
                      .divide(
                          BigDecimal.valueOf(100),
                          AppBaseService.COMPUTATION_SCALING,
                          RoundingMode.HALF_UP)));
    }
    return nonDeductibleTaxValue.setScale(AppBaseService.COMPUTATION_SCALING, RoundingMode.HALF_UP);
  }
}
