/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import java.util.List;

public interface AccountingSituationInitService {

  /**
   * Creates unexisting accounting situations for a given partner. Created situations will be
   * appended to the partner's AccountingSituationList
   *
   * @param partner Partner to create accounting situation for.
   * @return The created accounting situations (which is the same as calling
   *     partner.getAccountingSituationList())
   * @throws AxelorException In case of configuration issue
   */
  List<AccountingSituation> createAccountingSituation(Partner partner) throws AxelorException;

  AccountingSituation createAccountingSituation(Partner partner, Company company)
      throws AxelorException;

  /**
   * Automatically creates supplier/customer/employee accounts based on situation's company
   * configuration.
   *
   * @param situation Situation on which accounts should be created.
   */
  void createPartnerAccounts(AccountingSituation situation) throws AxelorException;
}
