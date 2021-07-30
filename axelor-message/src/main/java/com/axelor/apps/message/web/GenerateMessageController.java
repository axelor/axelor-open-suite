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

import com.axelor.apps.base.db.Wizard;
import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.db.Template;
import com.axelor.apps.message.db.repo.MessageRepository;
import com.axelor.apps.message.db.repo.TemplateRepository;
import com.axelor.apps.message.exception.IExceptionMessage;
import com.axelor.apps.message.service.MessageService;
import com.axelor.apps.message.service.TemplateMessageService;
import com.axelor.db.Model;
import com.axelor.db.Query;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Singleton;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class GenerateMessageController {

  private static final String RELATED_TO2_SELECT_ID = "_relatedTo2SelectId";
  private static final String RELATED_TO2_SELECT = "_relatedTo2Select";
  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public void callMessageWizard(ActionRequest request, ActionResponse response) {

    Model context = request.getContext().asType(Model.class);
    String model = request.getModel();

    LOG.debug("Call message wizard for model : {} ", model);

    String[] decomposeModel = model.split("\\.");
    String simpleModel = decomposeModel[decomposeModel.length - 1];
    Query<? extends Template> templateQuery =
        Beans.get(TemplateRepository.class)
            .all()
            .filter("self.metaModel.fullName = ?1 AND self.isSystem != true", model);
    MessageService msgService = Beans.get(MessageService.class);
    try {

      long templateNumber = templateQuery.count();

      LOG.debug("Template number : {} ", templateNumber);

      if (templateNumber == 0) {
        ActionViewBuilder builder =
            ActionView.define(I18n.get(IExceptionMessage.MESSAGE_3))
                .model(Message.class.getName())
                .add("form", "message-form")
                .param("forceEdit", "true")
                .context("_mediaTypeSelect", MessageRepository.MEDIA_TYPE_EMAIL)
                .context("_templateContextModel", model)
                .context("_objectId", context.getId().toString());
        msgService.fillContext(builder, null, model, context.getId());
        response.setView(builder.map());

      } else if (templateNumber > 1) {

        ActionViewBuilder builder =
            ActionView.define(I18n.get(IExceptionMessage.MESSAGE_2))
                .model(Wizard.class.getName())
                .add("form", "generate-message-wizard-form")
                .param("show-confirm", "false")
                .context("_objectId", context.getId().toString())
                .context("_templateContextModel", model)
                .context("_tag", simpleModel);

        msgService.fillContext(builder, null, model, context.getId());
        response.setView(builder.map());

      } else {
        Map<String, Object> contextMap = new HashMap<>();
        msgService.fillContext(null, contextMap, model, context.getId());
        String relatedTo2Select = null;
        long relatedTo2SelectId = 0L;
        if (contextMap.get(RELATED_TO2_SELECT) != null
            && contextMap.get(RELATED_TO2_SELECT_ID) != null) {
          relatedTo2Select = (String) contextMap.get(RELATED_TO2_SELECT);
          relatedTo2SelectId = (long) contextMap.get(RELATED_TO2_SELECT_ID);
        }
        response.setView(
            generateMessage(
                context.getId(),
                model,
                simpleModel,
                templateQuery.fetchOne(),
                relatedTo2Select,
                relatedTo2SelectId));
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void generateMessage(ActionRequest request, ActionResponse response) {

    Context context = request.getContext();
    Map<?, ?> templateContext = (Map<?, ?>) context.get("_xTemplate");
    Template template = null;
    if (templateContext != null) {
      template =
          Beans.get(TemplateRepository.class)
              .find(Long.parseLong(templateContext.get("id").toString()));
    }

    Long objectId = Long.parseLong(context.get("_objectId").toString());
    String model = (String) context.get("_templateContextModel");
    String tag = (String) context.get("_tag");

    String relatedTo2Select = null;
    long relatedTo2SelectId = 0L;
    if (context.get(RELATED_TO2_SELECT) != null && context.get(RELATED_TO2_SELECT_ID) != null) {
      relatedTo2Select = (String) context.get(RELATED_TO2_SELECT);
      relatedTo2SelectId = Long.parseLong(((String) context.get(RELATED_TO2_SELECT_ID)));
    }

    try {
      response.setView(
          generateMessage(objectId, model, tag, template, relatedTo2Select, relatedTo2SelectId));
      response.setCanClose(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public Map<String, Object> generateMessage(
      long objectId,
      String model,
      String tag,
      Template template,
      String relatedTo2Select,
      long realtedTo2SelectId)
      throws ClassNotFoundException, InstantiationException, IllegalAccessException,
          AxelorException, IOException {

    LOG.debug("template : {} ", template);
    LOG.debug("object id : {} ", objectId);
    LOG.debug("model : {} ", model);
    LOG.debug("tag : {} ", tag);
    Message message = null;
    if (template != null) {
      message =
          Beans.get(TemplateMessageService.class)
              .generateMessage(
                  objectId, model, tag, template, relatedTo2Select, realtedTo2SelectId);
    } else {
      message =
          Beans.get(MessageService.class)
              .createMessage(
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
                  relatedTo2Select,
                  realtedTo2SelectId);
    }

    return ActionView.define(I18n.get(IExceptionMessage.MESSAGE_3))
        .model(Message.class.getName())
        .add("form", "message-form")
        .param("forceEdit", "true")
        .context("_showRecord", message.getId().toString())
        .map();
  }
}
