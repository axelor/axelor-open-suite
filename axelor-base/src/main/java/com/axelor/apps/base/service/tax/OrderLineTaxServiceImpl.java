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

  @Override
  public void computeTax(OrderLineTax orderLineTax, Currency currency) {
    BigDecimal exTaxBase = orderLineTax.getExTaxBase().abs();
    BigDecimal taxTotal = BigDecimal.ZERO;
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
      orderLineTax.setTaxTotal(
          currencyScaleService.getScaledValue(taxTotal, currency.getNumberOfDecimals()));
    }
    orderLineTax.setInTaxTotal(
        currencyScaleService.getScaledValue(
            exTaxBase.add(taxTotal), currency.getNumberOfDecimals()));
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
