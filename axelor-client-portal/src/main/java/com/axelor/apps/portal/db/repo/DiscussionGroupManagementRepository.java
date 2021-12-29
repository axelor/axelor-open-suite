/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
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
package com.axelor.apps.portal.db.repo;

import com.axelor.apps.client.portal.db.DiscussionGroup;
import com.axelor.apps.client.portal.db.repo.DiscussionGroupRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.inject.Beans;
import com.axelor.mail.db.MailFollower;
import com.axelor.mail.db.repo.MailFollowerRepository;
import java.util.Map;

public class DiscussionGroupManagementRepository extends DiscussionGroupRepository {

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {

    if (json != null && json.get("id") != null) {
      final DiscussionGroup entity = find((Long) json.get("id"));

      final MailFollowerRepository followers = Beans.get(MailFollowerRepository.class);
      final MailFollower follower = followers.findOne(entity, AuthUtils.getUser());

      json.put("_following", follower != null && follower.getArchived() == Boolean.FALSE);
    }
    return json;
  }
}
