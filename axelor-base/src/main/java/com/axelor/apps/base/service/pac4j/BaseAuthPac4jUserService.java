package com.axelor.apps.base.service.pac4j;

import com.axelor.auth.db.User;
import com.axelor.auth.pac4j.AuthPac4jUserService;
import org.pac4j.core.profile.CommonProfile;

public class BaseAuthPac4jUserService extends AuthPac4jUserService {

  @Override
  protected void updateUser(User user, CommonProfile profile) {
    super.updateUser(user, profile);

    if (user.getId() == null && user.getBlocked()) {
      user.setBlocked(false);
    }
  }
}
