package com.axelor.apps.account.service.accountingsituation;

import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import java.util.Map;

public interface AccountingSituationGroupService {
  Map<String, Object> getOnNewValuesMap(AccountingSituation accountingSituation, Partner partner)
      throws AxelorException;

  Map<String, Object> getCompanyOnChangeValuesMap(
      AccountingSituation accountingSituation, Partner partner) throws AxelorException;

  Map<String, Map<String, Object>> getCompanyOnChangeAttrsMap(
      AccountingSituation accountingSituation, Partner partner) throws AxelorException;

  Map<String, Map<String, Object>> getOnNewAttrsMap(
      AccountingSituation accountingSituation, Partner partner) throws AxelorException;

  Map<String, Map<String, Object>> getCompanyOnSelectAttrsMap(
      AccountingSituation accountingSituation, Partner partner);

  Map<String, Map<String, Object>> getBankDetailsOnSelectAttrsMap(
      AccountingSituation accountingSituation, Partner partner, boolean isInBankDetails);
}
