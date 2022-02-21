package com.axelor.apps.account.util;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.exception.AxelorException;

public interface TaxAccountToolService {

  int calculateVatSystem(Journal journal, Partner partner, Company company, Account account)
      throws AxelorException;
}
