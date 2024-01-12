package com.axelor.apps.bankpayment.service.bankstatementline.afb120;

import com.axelor.apps.bankpayment.db.BankStatementLineAFB120;
import com.axelor.apps.base.db.BankDetails;

public interface BankStatementLineFetchAFB120Service {

  BankStatementLineAFB120 getLastBankStatementLineAFB120FromBankDetails(BankDetails bankDetails);
}
