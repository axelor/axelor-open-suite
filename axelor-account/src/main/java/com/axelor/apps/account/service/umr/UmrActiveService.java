package com.axelor.apps.account.service.umr;

import com.axelor.apps.account.db.Umr;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;

public interface UmrActiveService {
  Umr getActiveUmr(Company company, BankDetails bankDetails);
}
