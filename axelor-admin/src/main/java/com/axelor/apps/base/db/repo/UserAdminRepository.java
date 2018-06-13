/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.db.repo;

import java.util.HashSet;
import java.util.Set;

import com.axelor.apps.base.db.UserAccessConfig;
import com.axelor.auth.db.Role;
import com.axelor.auth.db.User;
import com.axelor.auth.db.repo.UserRepository;

public class UserAdminRepository extends UserRepository {

  @Override
  public User save(User user) {

    if (user.getRoles() == null) {
      user.setRoles(new HashSet<>());
    }
    
    if (user.getUserAccessConfigList() != null) {
      for (UserAccessConfig config : user.getUserAccessConfigList()) {
        Set<Role> roles = config.getAccessConfig().getRoleSet();
        if (roles != null) {
          user.getRoles().addAll(roles);
        }
      }
    }
    
    return super.save(user);
  }
}
