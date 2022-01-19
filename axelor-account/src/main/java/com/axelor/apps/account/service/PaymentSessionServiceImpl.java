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
package com.axelor.apps.account.service;

import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.PaymentSession;
import com.axelor.apps.account.db.repo.InvoiceTermRepository;
import com.axelor.apps.account.db.repo.PaymentSessionRepository;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.administration.AbstractBatch;
import com.axelor.apps.message.db.Message;
import com.axelor.apps.message.db.repo.MessageRepository;
import com.axelor.apps.message.service.MessageService;
import com.axelor.apps.message.service.TemplateMessageService;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import wslite.json.JSONException;

public class PaymentSessionServiceImpl implements PaymentSessionService {

  protected TemplateMessageService templateMessageService;
  protected MessageService messageService;
  protected PaymentSessionRepository paymentSessionRepo;
  protected InvoiceTermRepository invoiceTermRepo;
  protected MessageRepository messageRepo;

  @Inject
  public PaymentSessionServiceImpl(
      TemplateMessageService templateMessageService,
      MessageService messageService,
      PaymentSessionRepository paymentSessionRepo,
      InvoiceTermRepository invoiceTermRepo,
      MessageRepository messageRepo) {
    this.templateMessageService = templateMessageService;
    this.messageService = messageService;
    this.paymentSessionRepo = paymentSessionRepo;
    this.invoiceTermRepo = invoiceTermRepo;
    this.messageRepo = messageRepo;
  }

  @Override
  public String computeName(PaymentSession paymentSession) {
    StringBuilder name = new StringBuilder("Session");
    User createdBy = paymentSession.getCreatedBy();
    Boolean isFr =
        ObjectUtils.notEmpty(createdBy)
            && ObjectUtils.notEmpty(createdBy.getLanguage())
            && createdBy.getLanguage().equals(Locale.FRENCH.getLanguage());
    if (ObjectUtils.notEmpty(paymentSession.getPaymentMode())) {
      name.append(" " + paymentSession.getPaymentMode().getName());
    }
    if (ObjectUtils.notEmpty(paymentSession.getCreatedOn())) {
      name.append(
          (isFr ? " du " : " on the ")
              + paymentSession
                  .getCreatedOn()
                  .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
    }
    if (ObjectUtils.notEmpty(createdBy)) {
      name.append((isFr ? " par " : " by ") + createdBy.getName());
    }
    return name.toString();
  }

  @Override
  public int sendEmails(PaymentSession paymentSession)
      throws ClassNotFoundException, InstantiationException, IllegalAccessException,
          AxelorException, IOException, JSONException {
    if (paymentSession.getPaymentMode() == null
        || paymentSession.getPaymentMode().getPmtNotificationTemplate() == null) {
      return 0;
    }

    int offset = 0;
    List<InvoiceTerm> invoiceTermList;
    Query<InvoiceTerm> invoiceTermQuery =
        invoiceTermRepo
            .all()
            .filter(
                "self.paymentSession = :paymentSession "
                    + "AND self.isSelectedOnPaymentSession IS TRUE "
                    + "AND (self.invoice.partner.payNoticeSendingMethodSelect = 1 "
                    + "OR self.moveLine.partner.payNoticeSendingMethodSelect = 1)")
            .bind("paymentSession", paymentSession)
            .order("id");

    List<Long> partnerIdList = new ArrayList<>();

    while (!(invoiceTermList = invoiceTermQuery.fetch(AbstractBatch.FETCH_LIMIT, offset))
        .isEmpty()) {
      paymentSession = paymentSessionRepo.find(paymentSession.getId());

      for (InvoiceTerm invoiceTerm : invoiceTermList) {
        offset++;

        if (invoiceTerm.getInvoice() != null) {
          this.sendEmailToPartner(
              paymentSession, invoiceTerm.getInvoice().getPartner(), partnerIdList);
        }

        if (invoiceTerm.getMoveLine() != null) {
          this.sendEmailToPartner(
              paymentSession, invoiceTerm.getMoveLine().getPartner(), partnerIdList);
        }
      }

      JPA.clear();
    }

    if (partnerIdList.size() > 0) {
      paymentSession = paymentSessionRepo.find(paymentSession.getId());
      paymentSession.setHasEmailsSent(true);
      paymentSessionRepo.save(paymentSession);
    }

    return partnerIdList.size();
  }

  protected void sendEmailToPartner(
      PaymentSession paymentSession, Partner partner, List<Long> partnerIdList)
      throws ClassNotFoundException, InstantiationException, IllegalAccessException,
          AxelorException, IOException, JSONException {
    if (partner == null || partnerIdList.contains(partner.getId())) {
      return;
    }

    Message message = this.createEmail(paymentSession, partner);
    messageService.sendMessage(message);

    partnerIdList.add(partner.getId());
  }

  @Transactional(rollbackOn = {Exception.class})
  protected Message createEmail(PaymentSession paymentSession, Partner partner)
      throws ClassNotFoundException, InstantiationException, IllegalAccessException,
          AxelorException, IOException, JSONException {
    paymentSession.setPartnerForEmail(partner);
    paymentSessionRepo.save(paymentSession);

    Message message =
        templateMessageService.generateMessage(
            paymentSession.getId(),
            PaymentSession.class.getName(),
            PaymentSession.class.getSimpleName(),
            paymentSession.getPaymentMode().getPmtNotificationTemplate());

    messageService.addMessageRelatedTo(
        message, PaymentSession.class.getName(), paymentSession.getId());

    return message;
  }
}
