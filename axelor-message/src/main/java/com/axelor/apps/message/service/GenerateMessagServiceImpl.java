/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.apps.message.service;

import com.axelor.apps.base.db.Wizard;
import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.db.Template;
import com.axelor.apps.message.db.repo.MessageRepository;
import com.axelor.apps.message.exception.MessageExceptionMessage;
import com.axelor.db.Model;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.google.inject.Inject;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenerateMessagServiceImpl implements GenerateMessageService {

  private final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected TemplateMessageService templateMessageService;
  protected MessageService messageService;

  @Inject
  public GenerateMessagServiceImpl(
      TemplateMessageService templateMessageService, MessageService messageService) {
    this.templateMessageService = templateMessageService;
    this.messageService = messageService;
  }

  @Override
  public Map<String, Object> generateMessage(
      long objectId, String model, String tag, Template template)
      throws ClassNotFoundException, InstantiationException, IllegalAccessException,
          AxelorException, IOException {
    LOG.debug("template : {} ", template);
    LOG.debug("object id : {} ", objectId);
    LOG.debug("model : {} ", model);
    LOG.debug("tag : {} ", tag);
    Message message = null;
    if (template != null) {
      message = templateMessageService.generateMessage(objectId, model, tag, template, false);
    } else {
      message =
          messageService.createMessage(
              model,
              Math.toIntExact(objectId),
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              null,
              MessageRepository.MEDIA_TYPE_EMAIL,
              null,
              null,
              false);
    }

    ActionViewBuilder builder = getActionView(1, null, model, null, message);
    return builder.map();
  }

  @Override
  public ActionViewBuilder getActionView(
      long templateNumber, Model context, String model, String simpleModel, Message message) {

    if (templateNumber > 1) {
      return ActionView.define(I18n.get(MessageExceptionMessage.MESSAGE_2))
          .model(Wizard.class.getName())
          .add("form", "generate-message-wizard-form")
          .param("show-confirm", "false")
          .context("_objectId", context.getId().toString())
          .context("_templateContextModel", model)
          .context("_tag", simpleModel);

    } else {
      ActionViewBuilder builder =
          ActionView.define(I18n.get(MessageExceptionMessage.MESSAGE_3))
              .model(Message.class.getName())
              .add("form", "message-form")
              .param("forceEdit", "true");

      if (templateNumber == 0) {
        builder
            .context("_mediaTypeSelect", MessageRepository.MEDIA_TYPE_EMAIL)
            .context("_templateContextModel", model)
            .context("_objectId", context.getId().toString());

      } else {
        builder.context("_showRecord", message.getId() != null ? message.getId().toString() : null);
      }
      return builder;
    }
  }
}
