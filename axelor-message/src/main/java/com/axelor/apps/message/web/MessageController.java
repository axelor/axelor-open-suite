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
package com.axelor.apps.message.web;

import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.db.repo.MessageRepository;
import com.axelor.apps.message.exception.IExceptionMessage;
import com.axelor.apps.message.service.MessageService;
import com.axelor.apps.tool.ModelTool;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.HandleExceptionResponse;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;
import java.io.IOException;
import java.util.List;
import wslite.json.JSONException;

@Singleton
public class MessageController {

  @HandleExceptionResponse
  public void sendMessage(ActionRequest request, ActionResponse response)
      throws AxelorException, JSONException, IOException {
    Message message = request.getContext().asType(Message.class);

    Beans.get(MessageService.class)
        .sendMessage(Beans.get(MessageRepository.class).find(message.getId()));
    response.setReload(true);
    response.setFlash(I18n.get(IExceptionMessage.MESSAGE_4));
  }

  @SuppressWarnings("unchecked")
  @HandleExceptionResponse
  public void sendMessages(ActionRequest request, ActionResponse response) throws AxelorException {
    List<Integer> idList = (List<Integer>) request.getContext().get("_ids");
    if (idList == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(IExceptionMessage.MESSAGE_MISSING_SELECTED_MESSAGES));
    }
    ModelTool.apply(
        Message.class,
        idList,
        model -> Beans.get(MessageService.class).sendMessage((Message) model));
    response.setFlash(
        String.format(I18n.get(IExceptionMessage.MESSAGES_SEND_IN_PROGRESS), idList.size()));
    response.setReload(true);
  }

  @SuppressWarnings("unchecked")
  @HandleExceptionResponse
  public void regenerateMessages(ActionRequest request, ActionResponse response)
      throws AxelorException {
    List<Integer> idList = (List<Integer>) request.getContext().get("_ids");

    if (idList == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(IExceptionMessage.MESSAGE_MISSING_SELECTED_MESSAGES));
    }
    int error =
        ModelTool.apply(
            Message.class,
            idList,
            model -> Beans.get(MessageService.class).regenerateMessage((Message) model));
    response.setFlash(
        String.format(
            I18n.get(IExceptionMessage.MESSAGES_REGENERATED), idList.size() - error, error));
    response.setReload(true);
  }

  public void setContextValues(ActionRequest request, ActionResponse response) {
    response.setValues(request.getContext().get("_message"));
  }
}
