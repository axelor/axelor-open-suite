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
package com.axelor.apps.base.service.tax;

import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.db.Tax;
import com.axelor.apps.account.db.TaxEquiv;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.VatExemptionReason;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.interfaces.OrderLineTax;
import com.axelor.apps.base.interfaces.PricedOrder;
import com.axelor.apps.base.interfaces.PricedOrderLine;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.common.StringUtils;
import com.google.common.base.Joiner;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
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
      if (taxEquiv != null) {
        String vatNote =
            taxEquiv.getVatExemptionReason() != null
                ? taxEquiv.getVatExemptionReason().getNote()
                : null;
        String note = StringUtils.notEmpty(vatNote) ? vatNote : taxEquiv.getSpecificNote();
        if (StringUtils.notEmpty(note)) {
          specificNotes.add(note);
        }
      }
    }
  }

  @Override
  public void computeTax(OrderLineTax orderLineTax, Currency currency) {
    BigDecimal exTaxBase = orderLineTax.getExTaxBase().abs();
    BigDecimal taxTotal = BigDecimal.ZERO;
    int currencyScale = currencyScaleService.getCurrencyScale(currency);

    if (orderLineTax.getTaxLine() != null) {
      taxTotal =
          exTaxBase.multiply(
              orderLineTax
                  .getTaxLine()
                  .getValue()
                  .divide(
                      new BigDecimal(100),
                      AppBaseService.COMPUTATION_SCALING,
                      RoundingMode.HALF_UP));
      orderLineTax.setTaxTotal(currencyScaleService.getScaledValue(taxTotal, currencyScale));
      orderLineTax.setPercentageTaxTotal(orderLineTax.getTaxTotal());
    }
    orderLineTax.setInTaxTotal(
        currencyScaleService.getScaledValue(exTaxBase.add(taxTotal), currencyScale));
  }

  @Override
  public void setSpecificNotes(
      boolean customerSpecificNote,
      PricedOrder pricedOrder,
      Set<String> specificNotes,
      Partner partner) {
    if (!customerSpecificNote) {
      pricedOrder.setSpecificNotes(Joiner.on('\n').join(specificNotes));
    } else {
      String fpNote =
          Optional.ofNullable(pricedOrder.getFiscalPosition())
              .map(fp -> fp.getCustomerSpecificNoteText())
              .orElse(null);
      String partnerNote = null;
      if (partner != null) {
        VatExemptionReason vatExemptionReason = partner.getVatExemptionReason();
        partnerNote = vatExemptionReason != null ? vatExemptionReason.getNote() : null;
      }
      pricedOrder.setSpecificNotes(StringUtils.notEmpty(partnerNote) ? partnerNote : fpNote);
    }
  }

  @Override
  public boolean isManageByAmount(OrderLineTax orderLineTax) {
    return Optional.ofNullable(orderLineTax)
        .map(OrderLineTax::getTaxLine)
        .map(TaxLine::getTax)
        .map(Tax::getManageByAmount)
        .orElse(false);
  }

  @Override
  public BigDecimal computeInTaxTotal(OrderLineTax orderLineTax, Currency currency) {
    int currencyScale = currencyScaleService.getCurrencyScale(currency);
    if (orderLineTax.getTaxTotal().signum() <= 0) {
      return currencyScaleService.getScaledValue(orderLineTax.getExTaxBase(), currencyScale);
    }
    return currencyScaleService.getScaledValue(
        orderLineTax.getExTaxBase().add(orderLineTax.getTaxTotal()), currencyScale);
  }

  @Override
  public VatExemptionReason resolveVatExemptionReason(
      FiscalPosition fiscalPosition, TaxEquiv taxEquiv, Partner partner) {
    if (fiscalPosition != null && fiscalPosition.getCustomerSpecificNote()) {
      VatExemptionReason partnerReason = partner != null ? partner.getVatExemptionReason() : null;
      return partnerReason != null ? partnerReason : fiscalPosition.getVatExemptionReason();
    }
    return taxEquiv != null ? taxEquiv.getVatExemptionReason() : null;
  }
}
