package com.axelor.apps.bankpayment.service.bankorder;

import com.axelor.apps.bankpayment.db.BankOrder;
import com.axelor.apps.base.AxelorException;

public interface BankOrderCancelService {

  void cancelBankOrder(BankOrder bankOrder) throws AxelorException;

  BankOrder cancelPayment(BankOrder bankOrder) throws AxelorException;
}
