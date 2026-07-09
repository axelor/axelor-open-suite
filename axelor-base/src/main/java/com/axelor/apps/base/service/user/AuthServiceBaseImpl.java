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

/**
 * Overrides the platform {@link AuthService} to plug the AOS "password changed" email notification
 * onto the native password-change flow.
 *
 * <p>Since AOP 8.2, all password changes (self-service change button, admin change on another user,
 * forced change at login and password reset) go through {@link AuthService#changePassword(User,
 * String)}. Overriding it here centralizes the notification for every path. The email is only sent
 * when the per-user flag {@code sendEmailUponPasswordChange} is set (exposed to admins editing
 * another user, and set by the password-change batch), so a self-service change never leaks a
 * clear-text password by email.
 */
@Singleton
public class AuthServiceBaseImpl extends AuthService {

  @Override
  public void changePassword(User user, String password) {
    super.changePassword(user, password);
    sendPasswordChangedEmail(user, password);
  }

  /**
   * Sends the "password changed" email when the user opted in. Runs in the same transaction as the
   * password change so the transient password fed to the template ({@code $transientPassword$})
   * resolves from the first-level cache. A failure to send the email must not roll back the
   * password change itself.
   *
   * <p>{@link UserService#processChangedPassword(User)} resets the opt-in flag once the email is
   * sent (one-shot), so a later self-initiated change (e.g. the forced change at next login after a
   * batch reset) does not re-send the freshly chosen password by email.
   */
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
