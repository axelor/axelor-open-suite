/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service.pac4j;

import com.axelor.apps.base.module.BaseModule;
import com.axelor.auth.db.User;
import com.axelor.auth.pac4j.AuthPac4jUserService;
import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;
import org.pac4j.core.profile.CommonProfile;

@Alternative
@Priority(BaseModule.PRIORITY)
public class BaseAuthPac4jUserService extends AuthPac4jUserService {

  @Override
  protected void updateUser(User user, CommonProfile profile) {
    super.updateUser(user, profile);

    if (user.getId() == null && user.getBlocked()) {
      user.setBlocked(false);
    }
  }
}
