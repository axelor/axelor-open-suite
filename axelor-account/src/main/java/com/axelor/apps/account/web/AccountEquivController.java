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
package com.axelor.apps.account.web;

import com.axelor.apps.account.db.AccountEquiv;
import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.service.AccountEquivService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.utils.ContextTool;
import com.google.inject.Singleton;

@Singleton
public class AccountEquivController {

  public void setFromAccountDomain(ActionRequest request, ActionResponse response) {
    try {
      AccountEquiv accountEquiv = request.getContext().asType(AccountEquiv.class);
      FiscalPosition fiscalPosition =
          ContextTool.getContextParent(request.getContext(), FiscalPosition.class, 1);
      if (fiscalPosition != null) {
        String domain =
            Beans.get(AccountEquivService.class).getFromAccountDomain(accountEquiv, fiscalPosition);
        response.setAttr("fromAccount", "domain", domain);
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
