package com.axelor.apps.base.service;

import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Currency;

public interface BankDetailsControlService {

  /**
   * Method to checks if currency is authorized with bank details.
   *
   * @param bankDetails
   * @param currency
   * @return true if yes, else false
   */
  boolean isAuthorizedWithCurrency(BankDetails bankDetails, Currency currency);
}
