/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2021 Axelor (<http://axelor.com>).
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

import com.axelor.apps.client.portal.db.DiscussionGroup;
import com.axelor.apps.portal.service.DiscussionGroupService;
import com.axelor.db.Model;
import com.axelor.inject.Beans;
import com.axelor.mail.web.MailController;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;

public class MailPortalController extends MailController {

  @Override
  public void follow(ActionRequest request, ActionResponse response) {

    super.follow(request, response);

    final Context ctx = request.getContext();
    final Long id = (Long) ctx.get("id");
    final Model entity = (Model) getEntityManager().find(ctx.getContextClass(), id);

    if (entity.getClass().isAssignableFrom(DiscussionGroup.class)) {
      DiscussionGroup discussionGroup = (DiscussionGroup) entity;
      Beans.get(DiscussionGroupService.class).followDiscussionPosts(discussionGroup);
    }
  }

  @Override
  public void unfollow(ActionRequest request, ActionResponse response) {

    super.unfollow(request, response);

    final Context ctx = request.getContext();
    final Long id = (Long) ctx.get("id");
    final Model entity = (Model) getEntityManager().find(ctx.getContextClass(), id);

    if (entity.getClass().isAssignableFrom(DiscussionGroup.class)) {
      DiscussionGroup discussionGroup = (DiscussionGroup) entity;
      Beans.get(DiscussionGroupService.class).unfollowDiscussionPosts(discussionGroup);
    }
  }
}
