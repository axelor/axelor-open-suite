/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.account.web;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Notification;
import com.axelor.apps.account.db.repo.NotificationRepository;
import com.axelor.apps.account.service.NotificationService;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Joiner;
import com.google.inject.Singleton;
import java.util.List;

@Singleton
public class NotificationController {

  public void populateNotificationItemList(ActionRequest request, ActionResponse response) {
    try {
      Notification notification = request.getContext().asType(Notification.class);
      Beans.get(NotificationService.class).populateNotificationItemList(notification);
      response.setValue("notificationItemList", notification.getNotificationItemList());
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void validate(ActionRequest request, ActionResponse response) {
    try {
      Notification notification = request.getContext().asType(Notification.class);
      notification = Beans.get(NotificationRepository.class).find(notification.getId());
      Beans.get(NotificationService.class).validate(notification);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void displayMoveLines(ActionRequest request, ActionResponse response) {
    try {
      Notification notification = request.getContext().asType(Notification.class);
      List<Long> moveLineIdList = Beans.get(NotificationService.class).getMoveLines(notification);
      response.setView(
          ActionView.define(I18n.get("MoveLines"))
              .model(MoveLine.class.getName())
              .add("grid", "move-line-grid")
              .add("form", "move-line-form")
              .param("search-filters", "move-line-filters")
              .domain("self.id in (" + Joiner.on(",").join(moveLineIdList) + ")")
              .map());

    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }
}
