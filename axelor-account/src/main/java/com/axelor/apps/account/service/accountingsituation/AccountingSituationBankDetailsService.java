package com.axelor.apps.account.service.accountingsituation;

import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;

public interface AccountingSituationBankDetailsService {

  void setAccountingSituationBankDetails(
      AccountingSituation accountingSituation, Partner partner, Company company);
}
