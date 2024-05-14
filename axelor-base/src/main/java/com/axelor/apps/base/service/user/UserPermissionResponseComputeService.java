package com.axelor.apps.base.service.user;

import com.axelor.apps.base.rest.dto.UserMetaPermissionRuleResponse;
import com.axelor.apps.base.rest.dto.UserPermissionResponse;
import com.axelor.auth.db.User;

public interface UserPermissionResponseComputeService {

  public UserPermissionResponse computeUserPermissionResponse(User user);

  UserMetaPermissionRuleResponse computeUserMetaPermissionRuleResponse(User user);
}
