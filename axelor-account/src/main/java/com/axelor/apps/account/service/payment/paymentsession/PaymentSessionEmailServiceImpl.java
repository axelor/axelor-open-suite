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
package com.axelor.apps.account.service.payment.paymentsession;

import com.axelor.apps.account.db.AccountManagement;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.PaymentSession;
import com.axelor.apps.account.db.repo.InvoiceTermRepository;
import com.axelor.apps.account.db.repo.PaymentSessionRepository;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.service.administration.AbstractBatch;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.message.db.Message;
import com.axelor.message.db.Template;
import com.axelor.message.db.repo.MessageRepository;
import com.axelor.message.service.MessageService;
import com.axelor.message.service.TemplateMessageService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import wslite.json.JSONException;

public class PaymentSessionEmailServiceImpl implements PaymentSessionEmailService {
  protected TemplateMessageService templateMessageService;
  protected MessageService messageService;
  protected PaymentSessionRepository paymentSessionRepo;
  protected InvoiceTermRepository invoiceTermRepo;
  protected MessageRepository messageRepo;

  @Inject
  public PaymentSessionEmailServiceImpl(
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
  @Transactional(rollbackOn = {Exception.class})
  public int sendEmails(PaymentSession paymentSession)
      throws ClassNotFoundException, JSONException, IOException {
    if (this.getEmailTemplate(paymentSession) == null) {
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
                    + "AND (self.invoice.partner.payNoticeSendingMethodSelect = :paymentNoticeEmail "
                    + "OR self.moveLine.partner.payNoticeSendingMethodSelect = :paymentNoticeEmail)")
            .bind("paymentSession", paymentSession)
            .bind("paymentNoticeEmail", PartnerRepository.PAYMENT_NOTICE_EMAIL)
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
      paymentSession.setPartnerForEmail(null);
      paymentSessionRepo.save(paymentSession);
    }

    return partnerIdList.size();
  }

  protected Template getEmailTemplate(PaymentSession paymentSession) {
    if (paymentSession.getPaymentMode() != null
        && CollectionUtils.isNotEmpty(paymentSession.getPaymentMode().getAccountManagementList())) {
      AccountManagement accountManagement =
          paymentSession.getPaymentMode().getAccountManagementList().stream()
              .filter(it -> it.getCompany().equals(paymentSession.getCompany()))
              .findFirst()
              .orElse(null);

      if (accountManagement != null) {
        return accountManagement.getPmtNotificationTemplate();
      }
    }

    return null;
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void sendEmailToPartner(
      PaymentSession paymentSession, Partner partner, List<Long> partnerIdList)
      throws ClassNotFoundException, JSONException, IOException {
    if (partner == null || partnerIdList.contains(partner.getId())) {
      return;
    }

    Message message = this.createEmail(paymentSession, partner);
    messageService.sendMessage(message);

    partnerIdList.add(partner.getId());
  }

  @Transactional(rollbackOn = {Exception.class})
  protected Message createEmail(PaymentSession paymentSession, Partner partner)
      throws ClassNotFoundException {
    paymentSession.setPartnerForEmail(partner);
    paymentSessionRepo.save(paymentSession);

    Message message =
        templateMessageService.generateMessage(
            paymentSession.getId(),
            PaymentSession.class.getName(),
            PaymentSession.class.getSimpleName(),
            this.getEmailTemplate(paymentSession));

    messageService.addMessageRelatedTo(
        message, PaymentSession.class.getName(), paymentSession.getId());

    return message;
  }
}
