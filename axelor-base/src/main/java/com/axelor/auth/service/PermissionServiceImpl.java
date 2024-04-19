/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
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
package com.axelor.auth.service;

import com.axelor.auth.db.Permission;
import com.axelor.auth.db.repo.PermissionRepository;
import com.axelor.common.ObjectUtils;
import com.axelor.db.JPA;
import com.google.inject.Inject;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.persistence.metamodel.Metamodel;

public class PermissionServiceImpl implements PermissionService {

  private Set<String> objectOrPackages;

  protected PermissionRepository permissionRepository;

  @Inject
  public PermissionServiceImpl(PermissionRepository permissionRepository) {
    this.permissionRepository = permissionRepository;
  }

  @Override
  public List<Long> checkPermissionsObject() {
    List<Permission> permissionList = permissionRepository.all().fetch();
    if (ObjectUtils.isEmpty(permissionList)) {
      return null;
    }
    initObjectOrPackages();
    return permissionList.stream()
        .filter(permission -> !isValidObject(permission.getObject()))
        .map(Permission::getId)
        .collect(Collectors.toList());
  }

  protected void initObjectOrPackages() {
    Metamodel metamodel = JPA.em().getMetamodel();
    objectOrPackages =
        metamodel.getEntities().stream()
            .flatMap(
                entityType ->
                    Stream.of(
                        entityType.getJavaType().getPackage().getName(),
                        entityType.getJavaType().getName()))
            .collect(Collectors.toSet());
  }

  protected boolean isValidObject(String object) {
    String regex = object.replace("*", ".*");

    return objectOrPackages.stream().anyMatch(entityPackage -> entityPackage.matches(regex));
  }
}
