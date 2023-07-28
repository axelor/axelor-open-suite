/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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

import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.UserServiceAccountImpl;
import com.axelor.apps.base.db.Company;
import com.axelor.auth.db.User;
import com.axelor.auth.db.repo.UserRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.LinkedHashMap;
import java.util.Set;

public class UserController {

  @SuppressWarnings("unchecked")
  public void changePfpValidator(ActionRequest request, ActionResponse response) {

    Integer pfpValidatorUserId = (Integer) request.getContext().get("_userId");
    LinkedHashMap<String, Object> newPfpValidatorUserMap =
        (LinkedHashMap<String, Object>) request.getContext().get("newPfpValidatorUser");

    if (newPfpValidatorUserMap == null) {
      return;
    }
    UserRepository userRepository = Beans.get(UserRepository.class);
    User newPfpValidatorUser =
        userRepository.find(((Integer) newPfpValidatorUserMap.get("id")).longValue());
    User pfpValidatorUser = userRepository.find(pfpValidatorUserId.longValue());

    int updateCount =
        Beans.get(UserServiceAccountImpl.class)
            .changePfpValidator(pfpValidatorUser, newPfpValidatorUser);
    if (updateCount >= 1) {
      response.setInfo(I18n.get(AccountExceptionMessage.USER_PFP_VALIDATOR_UPDATED));
      response.setCanClose(true);
    } else if (updateCount == 0) {
      response.setAlert(
          String.format(
              I18n.get(AccountExceptionMessage.USER_PFP_VALIDATOR_NO_RELATED_ACCOUNTING_SITUATION),
              pfpValidatorUser.getName()));
    }
  }

  @SuppressWarnings("unchecked")
  public void comparePfpValidatorCompanySet(ActionRequest request, ActionResponse response) {
    Integer pfpValidatorUserId = (Integer) request.getContext().get("_userId");
    LinkedHashMap<String, Object> newPfpValidatorUserMap =
        (LinkedHashMap<String, Object>) request.getContext().get("newPfpValidatorUser");

    if (newPfpValidatorUserMap == null) {
      return;
    }

    UserRepository userRepository = Beans.get(UserRepository.class);
    User newPfpValidatorUser =
        userRepository.find(((Integer) newPfpValidatorUserMap.get("id")).longValue());
    User pfpValidatorUser = userRepository.find(pfpValidatorUserId.longValue());
    Set<Company> pfpValidatorUserCompanySet = pfpValidatorUser.getCompanySet();
    Set<Company> newPfpValidatorUserCompanySet = newPfpValidatorUser.getCompanySet();

    if (!pfpValidatorUserCompanySet.equals(newPfpValidatorUserCompanySet)) {

      response.setAttr(
          "$pfpValidatorCompanySetLabel",
          "title",
          String.format(
              I18n.get(AccountExceptionMessage.USER_PFP_VALIDATOR_COMPANY_SET_NOT_EQUAL),
              newPfpValidatorUser.getName(),
              pfpValidatorUser.getName()));
      response.setAttr("$pfpValidatorCompanySetLabel", "hidden", false);

    } else {
      response.setAttr("$pfpValidatorCompanySetLabel", "hidden", true);
    }
  }
}
