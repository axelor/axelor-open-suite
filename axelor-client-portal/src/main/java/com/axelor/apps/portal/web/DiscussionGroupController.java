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
import com.axelor.apps.client.portal.db.DiscussionGroup;
import com.axelor.apps.client.portal.db.DiscussionPost;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.mail.db.MailFollower;
import com.axelor.mail.db.repo.MailFollowerRepository;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.List;
import java.util.stream.Collectors;

public class DiscussionGroupController {

  public void openGroup(ActionRequest request, ActionResponse response) {

    DiscussionGroup discussionGroup = request.getContext().asType(DiscussionGroup.class);
    response.setView(
        ActionView.define(I18n.get("Discussion posts"))
            .model(DiscussionPost.class.getCanonicalName())
            .add("grid", "discussion-post-grid")
            .add("form", "discussion-post-form")
            .domain("self.discussionGroup.id = :discussionGroupId")
            .context("discussionGroupId", discussionGroup.getId())
            .param("details-view", "true")
            .param("grid-width", "25%")
            .map());
  }

  public void openAllPost(ActionRequest request, ActionResponse response) {

    User currentUser = Beans.get(UserService.class).getUser();
    List<Long> groups =
        Beans.get(MailFollowerRepository.class).all()
            .filter(
                "self.relatedModel = :relatedModel AND self.user = :user AND self.archived = false")
            .bind("relatedModel", DiscussionGroup.class.getName()).bind("user", currentUser).fetch()
            .stream()
            .map(MailFollower::getRelatedId)
            .collect(Collectors.toList());
    ActionViewBuilder view =
        ActionView.define(I18n.get("Discussion posts"))
            .model(DiscussionPost.class.getCanonicalName())
            .add("grid", "discussion-post-grid")
            .add("form", "discussion-post-form")
            .context("groups", groups)
            .param("details-view", "true")
            .param("grid-width", "25%");

    if (ObjectUtils.notEmpty(groups)) {
      view = view.domain("self.discussionGroup.id IN :groups");
    } else {
      view = view.domain("self.id IN (0)");
    }

    response.setView(view.map());
  }
}
