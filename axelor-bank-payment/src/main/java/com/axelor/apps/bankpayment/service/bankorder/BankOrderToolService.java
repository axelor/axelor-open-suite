package com.axelor.apps.bankpayment.service.bankorder;

import com.axelor.apps.bankpayment.db.BankOrder;
import com.google.inject.servlet.RequestScoped;
import java.util.Objects;

@RequestScoped
public class BankOrderToolService {

  public static boolean isMultiCurrency(BankOrder bankOrder) {
    return bankOrder != null
        && bankOrder.getBankOrderCurrency() != null
        && bankOrder.getCompanyCurrency() != null
        && !Objects.equals(bankOrder.getBankOrderCurrency(), bankOrder.getCompanyCurrency());
  }
}
