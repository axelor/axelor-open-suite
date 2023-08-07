/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.crm.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.crm.db.Catalog;
import com.axelor.message.db.EmailAddress;
import com.axelor.message.db.Message;
import com.axelor.message.db.Template;
import com.axelor.message.service.MessageService;
import com.axelor.message.service.TemplateMessageService;
import com.google.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.mail.MessagingException;

public class CatalogServiceImpl implements CatalogService {

  protected MessageService messageService;
  protected TemplateMessageService templateMessageService;

  @Inject
  public CatalogServiceImpl(
      MessageService messageService, TemplateMessageService templateMessageService) {
    this.messageService = messageService;
    this.templateMessageService = templateMessageService;
  }

  @Override
  public void sendEmail(Catalog catalog, Template template, List<Partner> contactList)
      throws ClassNotFoundException, InstantiationException, IllegalAccessException,
          AxelorException, IOException, MessagingException {

    Message message = templateMessageService.generateMessage(catalog, template);

    Set<EmailAddress> toEmailAddressSet =
        contactList.stream()
            .map(Partner::getEmailAddress)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

    message.setToEmailAddressSet(toEmailAddressSet);
    messageService.attachMetaFiles(message, Set.of(catalog.getPdfFile()));

    messageService.sendByEmail(message);
  }
}
