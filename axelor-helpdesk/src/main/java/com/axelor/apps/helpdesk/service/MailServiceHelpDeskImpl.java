package com.axelor.apps.helpdesk.service;

import com.axelor.apps.base.service.MailServiceBaseImpl;
import com.axelor.apps.helpdesk.db.Ticket;
import com.axelor.db.Model;
import com.axelor.mail.db.MailMessage;
import com.google.common.base.Strings;
import java.io.IOException;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class MailServiceHelpDeskImpl extends MailServiceBaseImpl {

  @Override
  protected String getSubject(MailMessage message, Model entity) {
    if (!(Ticket.class.isInstance(entity))) {
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

    Document doc = Jsoup.parse(message.getBody());
    doc.select("div.gmail_extra").remove();
    message.setBody(doc.outerHtml());

    return message;
  }
}
