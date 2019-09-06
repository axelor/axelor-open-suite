package com.axelor.apps.account.service;

import com.axelor.apps.account.db.TaxPaymentMoveLine;
import com.axelor.exception.AxelorException;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class TaxPaymentMoveLineServiceImpl implements TaxPaymentMoveLineService {

  @Override
  public TaxPaymentMoveLine computeTaxAmount(TaxPaymentMoveLine taxPaymentMoveLine)
      throws AxelorException {
    BigDecimal taxRate = taxPaymentMoveLine.getTaxRate();
    BigDecimal base = taxPaymentMoveLine.getDetailPaymentAmount();
    taxPaymentMoveLine.setTaxAmount(base.multiply(taxRate).setScale(2, RoundingMode.HALF_UP));
    return taxPaymentMoveLine;
  }
}
