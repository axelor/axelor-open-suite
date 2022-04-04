package com.axelor.apps.bankpayment.event;

import com.axelor.apps.bankpayment.db.BankOrder;

public class BankOrderValidated {
  private BankOrder bankOrder;

  public BankOrderValidated(BankOrder bankOrder) {
    this.bankOrder = bankOrder;
  }

  public BankOrder getBankOrder() {
    return bankOrder;
  }
}
