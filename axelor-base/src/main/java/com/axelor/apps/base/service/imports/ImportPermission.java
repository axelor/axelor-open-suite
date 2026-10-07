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
package com.axelor.apps.base.service.imports;

import com.axelor.auth.db.Permission;
import com.axelor.auth.db.Role;
import com.axelor.auth.db.repo.GroupRepository;
import com.axelor.auth.db.repo.PermissionRepository;
import com.axelor.auth.db.repo.RoleRepository;
import com.google.common.base.Strings;
import com.google.inject.persist.Transactional;
import jakarta.inject.Inject;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ImportPermission {

  PermissionRepository permissionRepo;
  GroupRepository groupRepo;
  RoleRepository roleRepo;

  @Inject
  public ImportPermission(
      PermissionRepository permissionRepo, GroupRepository groupRepo, RoleRepository roleRepo) {
    this.permissionRepo = permissionRepo;
    this.groupRepo = groupRepo;
    this.roleRepo = roleRepo;
  }

  @Transactional(rollbackOn = Exception.class)
  public Object importPermission(Object bean, Map<String, Object> values) {
    assert bean instanceof Permission;
    try {

      Permission permission = (Permission) bean;
      String groups = (String) values.get("group");
      if (permission.getId() != null) {
        if (groups != null && !groups.isEmpty()) {
          groupRepo
              .all()
              .filter("code in ?1", Arrays.asList(groups.split("\\|")))
              .fetch()
              .forEach(
                  group -> {
                    Set<Permission> permissions = group.getPermissions();
                    if (permissions == null) {
                      permissions = new HashSet<>();
                    }
                    permissions.add(permissionRepo.find(permission.getId()));
                    group.setPermissions(permissions);
                    groupRepo.save(group);
                  });
        }
      }
      return permission;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return bean;
  }

  @Transactional(rollbackOn = Exception.class)
  public Object importPermissionToRole(Object bean, Map<String, Object> values) {

    assert bean instanceof Permission;

    Permission permission = (Permission) bean;
    String roleName = values.get("roleName").toString();
    if (Strings.isNullOrEmpty(roleName)) {
      return bean;
    }

    Role role = roleRepo.findByName(roleName);

    if (role == null) {
      return bean;
    }

    role.addPermission(permission);
    return bean;
  }
}
