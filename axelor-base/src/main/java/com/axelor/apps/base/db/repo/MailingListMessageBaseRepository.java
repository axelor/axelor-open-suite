package com.axelor.apps.base.db.repo;

import com.axelor.apps.base.db.MailingListMessage;
import com.axelor.auth.AuthUtils;
import com.axelor.inject.Beans;
import com.axelor.mail.db.MailFollower;
import com.axelor.mail.db.repo.MailFollowerRepository;
import java.util.Map;

public class MailingListMessageBaseRepository extends MailingListMessageRepository {

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {
    if (json != null && json.get("id") != null) {
      final MailingListMessage entity = find((Long) json.get("id"));

      final MailFollowerRepository followers = Beans.get(MailFollowerRepository.class);
      final MailFollower follower = followers.findOne(entity, AuthUtils.getUser());

      json.put("_following", follower != null && follower.getArchived() == Boolean.FALSE);
      json.put("_image", entity.getImage() != null);
    }
    return json;
  }
}
