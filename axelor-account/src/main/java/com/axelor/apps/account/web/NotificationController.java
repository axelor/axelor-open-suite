/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.web;

import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Notification;
import com.axelor.apps.account.db.NotificationItem;
import com.axelor.apps.account.db.repo.NotificationRepository;
import com.axelor.apps.account.service.NotificationService;
import com.axelor.exception.ResponseMessageType;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Joiner;
import com.google.inject.Singleton;
import java.util.ArrayList;
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
      List<Long> moveLineIdList = new ArrayList<Long>();
      for (NotificationItem notificationItem : notification.getNotificationItemList()) {
        for (MoveLine moveLine : notificationItem.getMove().getMoveLineList()) {
          moveLineIdList.add(moveLine.getId());
        }
      }
      response.setView(
          ActionView.define("MoveLines")
              .model(MoveLine.class.getName())
              .add("grid", "move-line-grid")
              .add("form", "move-line-form")
              .domain("self.id in (" + Joiner.on(",").join(moveLineIdList) + ")")
              .map());

    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }
}
