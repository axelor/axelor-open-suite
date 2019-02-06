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
package com.axelor.apps.account.web;

import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;

@Singleton
public class UserController {

  public void checkIsManagePfp(ActionRequest request, ActionResponse response)
      throws AxelorException {
    User user = request.getContext().asType(User.class);
    System.err.println(Beans.get(AccountConfigService.class)
            .getAccountConfig(user.getActiveCompany())
            .getIsManagePassedForPayment());
    response.setAttr(
        "substitutePfpValidatorTabPanel",
        "hidden",
        !Beans.get(AccountConfigService.class)
            .getAccountConfig(user.getActiveCompany())
            .getIsManagePassedForPayment());
  }
}
