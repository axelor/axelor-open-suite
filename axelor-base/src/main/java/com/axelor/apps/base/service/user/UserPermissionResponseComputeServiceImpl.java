/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.rest.dto.MetaPermissionRuleResponse;
import com.axelor.apps.base.rest.dto.PermissionResponse;
import com.axelor.apps.base.rest.dto.UserMetaPermissionRuleResponse;
import com.axelor.apps.base.rest.dto.UserPermissionResponse;
import com.axelor.auth.db.Permission;
import com.axelor.auth.db.User;
import com.axelor.meta.db.MetaPermissionRule;
import com.google.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

public class UserPermissionResponseComputeServiceImpl
    implements UserPermissionResponseComputeService {

  protected UserService userService;

  @Inject
  public UserPermissionResponseComputeServiceImpl(UserService userService) {
    this.userService = userService;
  }

  @Override
  public UserPermissionResponse computeUserPermissionResponse(User user) {
    List<Permission> permissionList = userService.getPermissions(user);
    return new UserPermissionResponse(
        user.getVersion(),
        permissionList.stream()
            .map(
                permission ->
                    new PermissionResponse(
                        permission.getId(),
                        permission.getName(),
                        permission.getObject(),
                        permission.getCanRead(),
                        permission.getCanWrite(),
                        permission.getCanCreate(),
                        permission.getCanRemove()))
            .collect(Collectors.toList()));
  }

  @Override
  public UserMetaPermissionRuleResponse computeUserMetaPermissionRuleResponse(User user) {
    List<MetaPermissionRule> metaPermissionList = userService.getMetaPermissionRules(user);
    return new UserMetaPermissionRuleResponse(
        user.getVersion(),
        metaPermissionList.stream()
            .map(
                metaPermissionRule ->
                    new MetaPermissionRuleResponse(
                        metaPermissionRule.getVersion(),
                        metaPermissionRule.getId(),
                        metaPermissionRule.getField(),
                        metaPermissionRule.getCanRead(),
                        metaPermissionRule.getCanWrite(),
                        metaPermissionRule.getMetaPermission().getName(),
                        metaPermissionRule.getMetaPermission().getObject()))
            .collect(Collectors.toList()));
  }
}
