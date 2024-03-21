package com.axelor.apps.account.service.accountingsituation;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import java.util.List;

public interface AccountingSituationCheckService {
  void checkDuplicatedCompaniesInAccountingSituation(Partner partner) throws AxelorException;

  List<Company> getDuplicatedCompanies(Partner partner);
}
