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
package com.axelor.apps.base.service.tax;

import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.db.TaxEquiv;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.interfaces.OrderLineTax;
import com.axelor.apps.base.interfaces.PricedOrder;
import com.axelor.apps.base.interfaces.PricedOrderLine;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.google.common.base.Joiner;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;

public class OrderLineTaxServiceImpl implements OrderLineTaxService {

  protected CurrencyScaleService currencyScaleService;

  @Inject
  public OrderLineTaxServiceImpl(CurrencyScaleService currencyScaleService) {
    this.currencyScaleService = currencyScaleService;
  }

  @Override
  public boolean isCustomerSpecificNote(PricedOrder pricedOrder) {
    boolean customerSpecificNote = false;
    FiscalPosition fiscalPosition = pricedOrder.getFiscalPosition();
    if (fiscalPosition != null) {
      customerSpecificNote = fiscalPosition.getCustomerSpecificNote();
    }
    return customerSpecificNote;
  }

  @Override
  public void addTaxEquivSpecificNote(
      PricedOrderLine pricedOrderLine, boolean customerSpecificNote, Set<String> specificNotes) {
    if (!customerSpecificNote) {
      TaxEquiv taxEquiv = pricedOrderLine.getTaxEquiv();
      if (taxEquiv != null && taxEquiv.getSpecificNote() != null) {
        specificNotes.add(taxEquiv.getSpecificNote());
      }
    }
  }

  /**
   * compute each OrderLineTax
   *
   * @param orderLineTax
   * @param currency
   * @param sumOfAllDeductibleRateValue
   * @param sumOfAllNonDeductibleRateValue
   */
  @Override
  public void computeTax(
      OrderLineTax orderLineTax,
      Currency currency,
      BigDecimal sumOfAllDeductibleRateValue,
      BigDecimal sumOfAllNonDeductibleRateValue) {
    BigDecimal exTaxBase = orderLineTax.getExTaxBase().abs();
    BigDecimal taxTotal = BigDecimal.ZERO;
    int currencyScale = currencyScaleService.getCurrencyScale(currency);
    Boolean isNonDeductibleTax = orderLineTax.getTaxLine().getTax().getIsNonDeductibleTax();
    BigDecimal originalTaxRateValue = orderLineTax.getTaxLine().getValue();
    BigDecimal adjustedTaxValue = BigDecimal.ZERO;
    if (isNonDeductibleTax) {
      // non-deductible part
      // formula:
      // sum of all original normal tax rate * non-deductible tax rate

      adjustedTaxValue =
          sumOfAllDeductibleRateValue
              .divide(
                  BigDecimal.valueOf(100), AppBaseService.COMPUTATION_SCALING, RoundingMode.HALF_UP)
              .multiply(originalTaxRateValue)
              .divide(
                  BigDecimal.valueOf(100),
                  AppBaseService.COMPUTATION_SCALING,
                  RoundingMode.HALF_UP);
    } else {
      // deductible part
      // formula:
      // sum of all original normal tax rate * ( 1 - All non-deductible tax rate)
      adjustedTaxValue =
          originalTaxRateValue
              .divide(
                  BigDecimal.valueOf(100), AppBaseService.COMPUTATION_SCALING, RoundingMode.HALF_UP)
              .multiply(
                  BigDecimal.ONE.subtract(
                      sumOfAllNonDeductibleRateValue.divide(
                          BigDecimal.valueOf(100),
                          AppBaseService.COMPUTATION_SCALING,
                          RoundingMode.HALF_UP)));
    }

    taxTotal = exTaxBase.multiply(adjustedTaxValue);
    orderLineTax.setTaxTotal(currencyScaleService.getScaledValue(taxTotal, currencyScale));

    orderLineTax.setInTaxTotal(
        currencyScaleService.getScaledValue(exTaxBase.add(taxTotal), currencyScale));
  }

  @Override
  public void setSpecificNotes(
      boolean customerSpecificNote,
      PricedOrder pricedOrder,
      Set<String> specificNotes,
      String partnerNote) {
    if (!customerSpecificNote) {
      pricedOrder.setSpecificNotes(Joiner.on('\n').join(specificNotes));
    } else {
      pricedOrder.setSpecificNotes(partnerNote);
    }
  }
}
