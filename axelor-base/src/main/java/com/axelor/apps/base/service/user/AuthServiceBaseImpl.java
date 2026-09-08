/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service.user;

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.auth.AuthService;
import com.axelor.auth.db.User;
import com.axelor.inject.Beans;
import jakarta.inject.Singleton;

@Singleton
public class AuthServiceBaseImpl extends AuthService {

  @Override
  public void changePassword(User user, String password) {
    super.changePassword(user, password);
    sendPasswordChangedEmail(user, password);
  }

  protected void sendPasswordChangedEmail(User user, String password) {
    if (!Boolean.TRUE.equals(user.getSendEmailUponPasswordChange())) {
      return;
    }
    try {
      user.setTransientPassword(password);
      Beans.get(UserService.class).processChangedPassword(user);
    } catch (Exception e) {
      TraceBackService.trace(e);
    }
  }
}
