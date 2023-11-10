package com.axelor.apps.bankpayment.service.bankdetails;

import com.axelor.apps.base.db.BankDetails;
import java.util.List;

public interface BankDetailsBankPaymentService {
  void updateBankDetailsBalanceAndDate(List<BankDetails> bankDetails);
}
