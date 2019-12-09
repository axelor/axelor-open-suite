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
package com.axelor.studio.service.builder;

import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.db.Template;
import com.axelor.apps.message.db.repo.TemplateRepository;
import com.axelor.apps.message.exception.IExceptionMessage;
import com.axelor.apps.message.service.MessageService;
import com.axelor.apps.message.service.TemplateMessageService;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.meta.CallMethod;
import com.axelor.meta.db.MetaAction;
import com.axelor.meta.db.MetaJsonModel;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.repo.MetaJsonModelRepository;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionResponse;
import com.axelor.studio.db.ActionBuilder;
import com.axelor.studio.service.StudioMetaService;
import com.google.inject.Inject;
import java.io.IOException;
import javax.mail.MessagingException;

public class ActionEmailBuilderService {

  @Inject private MetaModelRepository metaModelRepo;

  @Inject private MetaJsonModelRepository metaJsonModelRepo;

  @Inject private StudioMetaService studioMetaService;

  @Inject private TemplateRepository templateRepo;

  @Inject private TemplateMessageService templateMessageService;

  @Inject private MessageService messageService;

  public MetaAction build(ActionBuilder builder) {
    String name = builder.getName();
    Object model =
        builder.getIsJson()
            ? metaJsonModelRepo.all().filter("self.name = ?", builder.getModel()).fetchOne()
            : metaModelRepo.all().filter("self.fullName = ?", builder.getModel()).fetchOne();

    int sendOption = builder.getEmailSendOptionSelect();
    Template template = builder.getEmailTemplate();

    String xml =
        "<action-method name=\""
            + name
            + "\" id=\"studio-"
            + name
            + "\">\n\t"
            + "<call class=\"com.axelor.studio.service.builder.ActionEmailBuilderService\" method=\"sendEmail(id, '"
            + (builder.getIsJson()
                ? ((MetaJsonModel) model).getName()
                : ((MetaModel) model).getFullName())
            + "', '"
            + (builder.getIsJson()
                ? ((MetaJsonModel) model).getName()
                : ((MetaModel) model).getName())
            + "', '"
            + template.getId()
            + "', '"
            + sendOption
            + "')\" "
            + "if=\"id != null\"/>\n"
            + "</action-method>";

    return studioMetaService.updateMetaAction(name, "action-method", xml, null);
  }

  @CallMethod
  public ActionResponse sendEmail(
      Long objectId, String model, String tag, Long templateId, int sendOption)
      throws ClassNotFoundException, InstantiationException, IllegalAccessException,
          AxelorException, IOException, MessagingException {

    Template template = templateRepo.find(templateId);
    Message message = templateMessageService.generateMessage(objectId, model, tag, template);
    ActionResponse response = new ActionResponse();

    if (sendOption == 0) {
      messageService.sendByEmail(message);
    } else {
      response.setView(
          ActionView.define(I18n.get(IExceptionMessage.MESSAGE_3))
              .model(Message.class.getName())
              .add("form", "message-form")
              .param("forceEdit", "true")
              .context("_showRecord", message.getId().toString())
              .map());
    }
    return response;
  }
}
