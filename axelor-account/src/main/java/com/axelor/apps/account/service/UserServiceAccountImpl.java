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
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.repo.AccountingSituationRepository;
import com.axelor.apps.base.service.user.UserServiceImpl;
import com.axelor.auth.db.User;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.persist.Transactional;

public class UserServiceAccountImpl extends UserServiceImpl {

  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public int changePfpValidator(User pfpValidatorUser, User newPfpValidatorUser) {

    return Beans.get(AccountingSituationRepository.class)
        .all()
        .filter(
            "self.pfpValidatorUser = ? and self.company in ? and self.company in ?",
            pfpValidatorUser,
            pfpValidatorUser.getCompanySet(),
            newPfpValidatorUser.getCompanySet())
        .update("pfpValidatorUser", newPfpValidatorUser);
  }
}
