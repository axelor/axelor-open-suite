/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.web;

import com.axelor.apps.account.db.AccountManagement;
import com.axelor.apps.account.db.Tax;
import com.axelor.apps.account.service.AccountManagementAccountService;
import com.axelor.apps.tool.ContextTool;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;

@Singleton
public class AccountManagementController {

  public void setCompanyDomain(ActionRequest request, ActionResponse response) {
    try {
      AccountManagement accountManagement = request.getContext().asType(AccountManagement.class);
      Tax tax = ContextTool.getContextParent(request.getContext(), Tax.class, 1);
      if (tax != null) {
        String domain =
            Beans.get(AccountManagementAccountService.class)
                .getCompanyDomain(accountManagement, tax);
        response.setAttr("company", "domain", domain);
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
