package com.axelor.apps.base.service;

import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Currency;
import java.util.Objects;

public class BankDetailsControlServiceImpl implements BankDetailsControlService {

  @Override
  public boolean isAuthorizedWithCurrency(BankDetails bankDetails, Currency currency) {
    Objects.requireNonNull(bankDetails);

    if (bankDetails.getIsAuthorizedOnDifferentCurrency()) {
      return true;
    }
    return bankDetails.getCurrency() != null && bankDetails.getCurrency().equals(currency);
  }
}
