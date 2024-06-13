package com.axelor.apps.account.service.accountingsituation;

import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import java.util.Map;

public interface AccountingSituationAttrsService {
  void manageBankDetails(Map<String, Map<String, Object>> attrsMap);

  void managePfpValidatorUser(
      Company company, Partner partner, Map<String, Map<String, Object>> attrsMap)
      throws AxelorException;

  void hideAccountsLinkToPartner(Partner partner, Map<String, Map<String, Object>> attrsMap);

  void manageAnalyticAccountPanel(
      Company company, Partner partner, Map<String, Map<String, Object>> attrsMap);

  void addCompanyDomain(
      AccountingSituation accountingSituation,
      Partner partner,
      Map<String, Map<String, Object>> attrsMap);

  void addCompanyInBankDetailsDomain(
      AccountingSituation accountingSituation,
      Partner partner,
      boolean isInBankDetails,
      Map<String, Map<String, Object>> attrsMap);
}
