/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.supplychain.web;

import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.supplychain.service.AccountingSituationSupplychainServiceImpl;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;

@Singleton
public class AccountingSituationSupplychainController {

  public void computeUsedCredit(ActionRequest request, ActionResponse response) {
    AccountingSituation accountingSituation =
        request.getContext().asType(AccountingSituation.class);
    try {
      accountingSituation =
          Beans.get(AccountingSituationSupplychainServiceImpl.class)
              .computeUsedCredit(accountingSituation);
      response.setValue("usedCredit", accountingSituation.getUsedCredit());
    } catch (Exception e) {
      TraceBackService.trace(e);
      response.setError(e.getMessage());
    }
  }
}
