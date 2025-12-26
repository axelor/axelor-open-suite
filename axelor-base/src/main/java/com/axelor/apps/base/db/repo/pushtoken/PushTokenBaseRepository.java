package com.axelor.apps.base.db.repo.pushtoken;

import com.axelor.apps.base.db.PushToken;
import com.axelor.apps.base.db.repo.PushTokenRepository;
import com.axelor.auth.db.User;
import com.axelor.db.Query;

public class PushTokenBaseRepository extends PushTokenRepository {

  public PushToken findByToken(String token) {
    return Query.of(PushToken.class).filter("self.token = :token").bind("token", token).fetchOne();
  }

  public Query<PushToken> findByUser(User employee) {
    return Query.of(PushToken.class)
        .filter("self.employee = :employee AND self.isActive = true")
        .bind("employee", employee);
  }

  public Query<PushToken> findActiveTokens() {
    return Query.of(PushToken.class).filter("self.isActive = true");
  }
}
