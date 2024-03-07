package com.axelor.apps.base.service.user;

import com.axelor.auth.db.Role;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.google.inject.servlet.RequestScoped;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RequestScoped
public class UserRoleToolService {

  /**
   * Method to check if the user's roles and group's roles are in a role set. Return true if role
   * set is empty
   *
   * @param user
   * @param roleSet
   * @return
   */
  public static boolean checkUserRolesPermissionIncludingEmpty(User user, Set<Role> roleSet) {
    if (ObjectUtils.isEmpty(roleSet)) {
      return true;
    }

    return checkUserRolesInRoleSet(user, roleSet);
  }

  /**
   * Method to check if the user's roles and group's roles are in a role set. Return false if role
   * set is empty
   *
   * @param user
   * @param roleSet
   * @return
   */
  public static boolean checkUserRolesPermissionExcludingEmpty(User user, Set<Role> roleSet) {
    if (ObjectUtils.isEmpty(roleSet)) {
      return false;
    }

    return checkUserRolesInRoleSet(user, roleSet);
  }

  protected static boolean checkUserRolesInRoleSet(User user, Set<Role> roleSet) {
    if (user == null) {
      return false;
    }
    List<Role> userRoleList =
        user.getRoles() != null ? new ArrayList<>(user.getRoles()) : new ArrayList<>();
    if (user.getGroup() != null && !ObjectUtils.isEmpty(user.getGroup().getRoles())) {
      userRoleList.addAll(user.getGroup().getRoles());
    }

    return roleSet.stream().anyMatch(userRoleList::contains);
  }
}
