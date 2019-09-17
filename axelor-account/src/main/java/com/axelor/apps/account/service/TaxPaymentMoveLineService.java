package com.axelor.apps.account.service;

import com.axelor.apps.account.db.TaxPaymentMoveLine;
import com.axelor.exception.AxelorException;

public interface TaxPaymentMoveLineService {

  public TaxPaymentMoveLine computeTaxAmount(TaxPaymentMoveLine taxPaymentMoveLine)
      throws AxelorException;
}
