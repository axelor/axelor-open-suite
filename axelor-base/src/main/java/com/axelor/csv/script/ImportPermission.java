/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.csv.script;

import com.axelor.auth.db.Group;
import com.axelor.auth.db.Permission;
import com.axelor.auth.db.Role;
import com.axelor.auth.db.repo.GroupRepository;
import com.axelor.auth.db.repo.PermissionRepository;
import com.axelor.auth.db.repo.RoleRepository;
import com.axelor.inject.Beans;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ImportPermission {

  @Inject PermissionRepository permissionRepo;

  @Transactional
  public Object importPermission(Object bean, Map<String, Object> values) {
    assert bean instanceof Permission;
    try {

      GroupRepository groupRepository = Beans.get(GroupRepository.class);

      Permission permission = (Permission) bean;
      String groups = (String) values.get("group");
      if (permission.getId() != null) {
        if (groups != null && !groups.isEmpty()) {
          for (Group group :
              groupRepository
                  .all()
                  .filter("code in ?1", Arrays.asList(groups.split("\\|")))
                  .fetch()) {
            Set<Permission> permissions = group.getPermissions();
            if (permissions == null) permissions = new HashSet<Permission>();
            permissions.add(permissionRepo.find(permission.getId()));
            group.setPermissions(permissions);
            groupRepository.save(group);
          }
        }
      }
      return permission;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return bean;
  }

  @Transactional
  public Object importPermissionToRole(Object bean, Map<String, Object> values) {

    assert bean instanceof Permission;

    Permission permission = (Permission) bean;
    String roleName = values.get("roleName").toString();
    if (Strings.isNullOrEmpty(roleName)) {
      return bean;
    }

    RoleRepository roleRepository = Beans.get(RoleRepository.class);
    Role role = roleRepository.findByName(roleName);

    if (role == null) {
      return bean;
    }

    role.addPermission(permission);
    return bean;
  }
}
