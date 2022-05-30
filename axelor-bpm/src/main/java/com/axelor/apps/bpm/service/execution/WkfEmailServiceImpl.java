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
package com.axelor.apps.bpm.service.execution;

import com.axelor.app.AppSettings;
import com.axelor.apps.bpm.db.WkfTaskConfig;
import com.axelor.apps.message.db.EmailAddress;
import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.db.Template;
import com.axelor.apps.message.db.repo.EmailAddressRepository;
import com.axelor.apps.message.db.repo.MessageRepository;
import com.axelor.apps.message.db.repo.TemplateRepository;
import com.axelor.apps.message.service.MessageService;
import com.axelor.apps.message.service.TemplateMessageService;
import com.axelor.apps.tool.context.FullContext;
import com.axelor.auth.db.User;
import com.axelor.common.Inflector;
import com.axelor.db.EntityHelper;
import com.axelor.db.Model;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaAction;
import com.axelor.meta.db.MetaJsonRecord;
import com.axelor.meta.db.repo.MetaActionRepository;
import com.google.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.mail.MessagingException;
import org.camunda.bpm.engine.delegate.DelegateExecution;

public class WkfEmailServiceImpl implements WkfEmailService {

  @Inject protected MessageService messageService;

  @Inject protected WkfUserActionService wkfUserActionService;

  @Inject protected MetaActionRepository metaActionRepository;

  protected Inflector inflector = Inflector.getInstance();

  public static final String EMAIL_CONTENT = /*$$(*/
      "Hello %s<br/> BPM state <b>%s</b> is activated on<br/> <a href=\"%s\">%s</a><br/>" /*)*/;

  @Override
  public void sendEmail(WkfTaskConfig wkfTaskConfig, DelegateExecution execution)
      throws ClassNotFoundException, MessagingException, AxelorException, InstantiationException,
          IllegalAccessException, IOException {

    String title = wkfTaskConfig.getTaskEmailTitle();
    if (title == null) {
      return;
    }

    FullContext wkfContext = wkfUserActionService.getModelCtx(wkfTaskConfig, execution);

    if (wkfContext == null) {
      return;
    }

    String model = null;
    String tag = null;
    Long id = null;

    title = wkfUserActionService.processTitle(title, wkfContext);
    model = wkfContext.getTarget().getClass().getName();
    if (wkfContext.getTarget().getClass().equals(MetaJsonRecord.class)) {
      tag = (String) wkfContext.get("jsonModel");
      model = tag;
    } else {
      tag = wkfContext.getTarget().getClass().getSimpleName();
    }
    id = (Long) wkfContext.get("id");

    String url = createUrl(wkfContext, wkfTaskConfig.getDefaultForm());
    String activeNode = execution.getCurrentActivityName();
    Template template =
        Beans.get(TemplateRepository.class).findByName(wkfTaskConfig.getTemplateName());

    Message message = null;
    if (template != null) {
      url = "<a href=\"" + url + "\" >" + url + "</a>";
      message = Beans.get(TemplateMessageService.class).generateMessage(id, model, tag, template);
      message.setSubject(message.getSubject().replace("{{activeNode}}", activeNode));
      message.setContent(message.getContent().replace("{{activeNode}}", activeNode));
      message.setSubject(message.getSubject().replace("{{recordUrl}}", url));
      message.setContent(message.getContent().replace("{{recordUrl}}", url));
    } else {
      User user = null;
      if (wkfTaskConfig.getUserPath() != null) {
        user = wkfUserActionService.getUser(wkfTaskConfig.getUserPath(), wkfContext);
      }

      if (user == null || user.getEmail() == null) {
        return;
      }

      String content = String.format(EMAIL_CONTENT, user.getName(), activeNode, url, url);

      List<EmailAddress> toEmailAddressList = new ArrayList<EmailAddress>();
      EmailAddress emailAddress =
          Beans.get(EmailAddressRepository.class).findByAddress(user.getEmail());
      if (emailAddress == null) {
        emailAddress = new EmailAddress(user.getEmail());
      }
      toEmailAddressList.add(emailAddress);

      message =
          messageService.createMessage(
              model,
              id,
              title,
              content,
              null,
              null,
              toEmailAddressList,
              null,
              null,
              null,
              null,
              MessageRepository.MEDIA_TYPE_EMAIL,
              null,
              null);
    }
    messageService.sendByEmail(message);
  }

  @Override
  public String createUrl(FullContext wkfContext, String formName) {

    if (wkfContext == null) {
      return "";
    }

    String url = AppSettings.get().getBaseURL();

    Model model = (Model) EntityHelper.getEntity(wkfContext.getTarget());

    if (formName == null) {
      if (model instanceof MetaJsonRecord) {
        formName = "custom-model-" + ((MetaJsonRecord) model).getJsonModel() + "-form";
      } else {
        formName = inflector.dasherize(model.getClass().getSimpleName());
      }
    }

    String action = getAction(formName);

    if (action == null) {
      url += "/#ds/form::" + model.getClass().getName() + "/edit/" + wkfContext.get("id");
    } else {
      url += "/#ds/" + action + "/edit/" + wkfContext.get("id");
    }

    return url;
  }

  private String getAction(String formName) {

    MetaAction metaAction =
        metaActionRepository.all().filter("self.xml like '%\"" + formName + "\"%'").fetchOne();

    if (metaAction != null) {
      return metaAction.getName();
    }

    return null;
  }
}
