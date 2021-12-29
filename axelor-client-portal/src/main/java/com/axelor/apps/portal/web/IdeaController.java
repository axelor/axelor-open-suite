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
package com.axelor.apps.portal.web;

import com.axelor.apps.base.service.user.UserService;
import com.axelor.apps.client.portal.db.Idea;
import com.axelor.apps.client.portal.db.repo.IdeaRepository;
import com.axelor.auth.db.User;
import com.axelor.auth.db.repo.UserRepository;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IdeaController {

  @Transactional
  public void acceptIdea(ActionRequest request, ActionResponse response) {

    Idea idea = request.getContext().asType(Idea.class);
    idea = Beans.get(IdeaRepository.class).find(idea.getId());
    idea.setAccepted(!idea.getAccepted());
    Beans.get(IdeaRepository.class).save(idea);
    response.setReload(true);
  }

  @Transactional
  public void closeIdea(ActionRequest request, ActionResponse response) {

    Idea idea = request.getContext().asType(Idea.class);
    idea = Beans.get(IdeaRepository.class).find(idea.getId());
    idea.setClose(!idea.getClose());
    Beans.get(IdeaRepository.class).save(idea);
    response.setReload(true);
  }

  @Transactional
  public void markRead(ActionRequest request, ActionResponse response) {

    Idea idea = request.getContext().asType(Idea.class);
    Long id = idea.getId();
    if (id == null) {
      return;
    }

    User currentUser = Beans.get(UserService.class).getUser();
    String ids =
        StringUtils.notBlank(currentUser.getIdeaUnreadIds()) ? currentUser.getIdeaUnreadIds() : "";

    List<String> idList = new ArrayList<String>(Arrays.asList(ids.split(",")));
    String idStr = id.toString();
    if (!ObjectUtils.isEmpty(idList) && idList.contains(idStr)) {
      idList.remove(idStr);
      currentUser.setIdeaUnreadIds(String.join(",", idList));
      Beans.get(UserRepository.class).save(currentUser);
    }
  }
}
