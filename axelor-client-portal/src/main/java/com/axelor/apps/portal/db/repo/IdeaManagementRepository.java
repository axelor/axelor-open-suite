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

import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.client.portal.db.Idea;
import com.axelor.apps.client.portal.db.repo.IdeaRepository;
import com.axelor.auth.db.User;
import com.axelor.auth.db.repo.UserRepository;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class IdeaManagementRepository extends IdeaRepository {

  @Inject UserRepository userRepo;

  @Override
  public Idea save(Idea idea) {
    if (idea.getVersion() != 0) {
      return super.save(idea);
    }

    idea = super.save(idea);
    User currentUser = Beans.get(UserService.class).getUser();
    List<User> users =
        userRepo.all().filter("self.id != :id").bind("id", currentUser.getId()).fetch();
    for (User user : users) {
      String ids = "";
      if (StringUtils.notBlank(user.getIdeaUnreadIds())) {
        ids = String.format("%s,", user.getIdeaUnreadIds());
      }
      user.setIdeaUnreadIds(String.format("%s%s", ids, idea.getId().toString()));
      userRepo.save(user);
    }

    return idea;
  }

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {

    Map<String, Object> map = super.populate(json, context);

    boolean unread = false;
    if (json != null && json.get("id") != null) {
      final Idea idea = find((Long) json.get("id"));
      User currentUser = Beans.get(UserService.class).getUser();
      String ids = currentUser.getIdeaUnreadIds();
      if (StringUtils.notBlank(ids)) {
        List<String> idList = Arrays.asList(ids.split(","));
        if (!ObjectUtils.isEmpty(idList) && idList.contains(idea.getId().toString())) {
          unread = true;
        }
      }
      map.put("$unread", unread);
    }

    return map;
  }
}
