package com.axelor.apps.bankpayment.service.bankorder;

import com.axelor.apps.bankpayment.db.BankOrder;
import com.axelor.apps.bankpayment.db.BankOrderFileFormat;
import com.axelor.apps.bankpayment.db.BankOrderLine;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;

public interface BankOrderCheckService {

  void checkLines(BankOrder bankOrder) throws AxelorException;

  void checkBankDetails(BankDetails bankDetails, BankOrder bankOrder) throws AxelorException;

  boolean checkBankDetailsTypeCompatible(
      BankDetails bankDetails, BankOrderFileFormat bankOrderFileFormat);

  boolean checkBankDetailsCurrencyCompatible(BankDetails bankDetails, BankOrder bankOrder);

  BankDetails getDefaultBankDetails(BankOrder bankOrder);

  void checkBankDetails(BankDetails bankDetails, BankOrder bankOrder, BankOrderLine bankOrderLine)
      throws AxelorException;

  void checkPreconditions(BankOrderLine bankOrderLine) throws AxelorException;
}
