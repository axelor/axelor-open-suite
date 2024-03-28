package com.axelor.apps.account.service.accountingsituation;

import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.base.db.Partner;

public interface AccountingSituationRecordService {
  void setDefaultCompany(AccountingSituation accountingSituation, Partner partner);
}
