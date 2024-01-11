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
package com.axelor.apps.helpdesk.service;

import com.axelor.apps.base.service.MailServiceBaseImpl;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.helpdesk.db.Ticket;
import com.axelor.db.Model;
import com.axelor.inject.Beans;
import com.axelor.mail.db.MailMessage;
import com.axelor.message.service.MailAccountService;
import com.axelor.studio.app.service.AppService;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.io.IOException;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class MailServiceHelpDeskImpl extends MailServiceBaseImpl {

  @Inject
  public MailServiceHelpDeskImpl(
      MailAccountService mailAccountService, AppBaseService appBaseService) {
    super(mailAccountService, appBaseService);
  }

  @Override
  protected String getSubject(MailMessage message, Model entity) {
    if (!(Ticket.class.isInstance(entity)) || !Beans.get(AppService.class).isApp("helpdesk")) {
      return super.getSubject(message, entity);
    }
    Ticket ticket = (Ticket) entity;
    if (!Strings.isNullOrEmpty(ticket.getMailSubject())) {
      return ticket.getMailSubject();
    }
    return super.getSubject(message, entity);
  }

  @Override
  protected MailMessage messageReceived(MimeMessage email) throws MessagingException, IOException {
    MailMessage message = super.messageReceived(email);

    if (!Beans.get(AppService.class).isApp("helpdesk")) {
      return message;
    }
    Document doc = Jsoup.parse(message.getBody());
    doc.select("div.gmail_extra").remove();
    message.setBody(doc.outerHtml());

    return message;
  }
}
