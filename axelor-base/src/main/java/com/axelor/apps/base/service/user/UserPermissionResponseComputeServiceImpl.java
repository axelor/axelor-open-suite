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
