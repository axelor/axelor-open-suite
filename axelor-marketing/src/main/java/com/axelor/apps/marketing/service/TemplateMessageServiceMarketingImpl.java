/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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
package com.axelor.apps.marketing.service;

import com.axelor.apps.base.service.app.AppService;
import com.axelor.apps.base.service.message.TemplateMessageServiceBaseImpl;
import com.axelor.apps.message.db.EmailAccount;
import com.axelor.apps.message.db.Template;
import com.axelor.apps.message.db.repo.MessageRepository;
import com.axelor.apps.message.db.repo.TemplateRepository;
import com.axelor.apps.message.service.MessageService;
import com.axelor.apps.message.service.TemplateContextService;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TemplateMessageServiceMarketingImpl extends TemplateMessageServiceBaseImpl
    implements TemplateMessageMarketingService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected EmailAccount emailAccount;

  @Inject
  public TemplateMessageServiceMarketingImpl(
      MessageService messageService, TemplateContextService templateContextService) {
    super(messageService, templateContextService);
  }

  @Override
  protected Integer getMediaTypeSelect(Template template) {

    if (template.getMediaTypeSelect() == TemplateRepository.MEDIA_TYPE_EMAILING
        && Beans.get(AppService.class).isApp("marketing")) {
      return MessageRepository.MEDIA_TYPE_EMAIL;
    }

    return super.getMediaTypeSelect(template);
  }

  @Override
  protected EmailAccount getMailAccount() {

    if (emailAccount != null) {
      log.debug("Email account ::: {}", emailAccount);
      return emailAccount;
    }

    return super.getMailAccount();
  }

  @Override
  public void setEmailAccount(EmailAccount emailAccount) {

    this.emailAccount = emailAccount;
  }
}
