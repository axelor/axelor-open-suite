package com.axelor.apps.bankpayment.event;

import com.axelor.apps.bankpayment.db.BankOrder;

public class BankOrderEvent {

  public static final String VALIDATE_PAYMENT = "validatePayment";
  public static final String CANCEL_PAYMENT = "cancelPayment";
  public static final String VALIDATE = "validate";

  private BankOrder bankOrder;

  public BankOrderEvent(BankOrder bankOrder) {
    this.bankOrder = bankOrder;
  }

  public BankOrder getBankOrder() {
    return bankOrder;
  }
}
