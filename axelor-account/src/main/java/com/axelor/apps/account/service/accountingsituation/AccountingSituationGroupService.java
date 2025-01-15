/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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
