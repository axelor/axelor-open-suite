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

import com.axelor.apps.account.db.AccountManagement;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.PaymentSession;
import com.axelor.apps.account.db.repo.InvoiceTermRepository;
import com.axelor.apps.account.db.repo.PaymentSessionRepository;
import com.axelor.apps.account.translation.ITranslation;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import com.google.inject.servlet.RequestScoped;
import java.math.BigDecimal;
import java.util.Optional;
import org.apache.commons.collections.CollectionUtils;

@RequestScoped
public class PaymentSessionServiceImpl implements PaymentSessionService {

  protected PaymentSessionRepository paymentSessionRepository;
  protected InvoiceTermRepository invoiceTermRepository;

  @Inject
  public PaymentSessionServiceImpl(
      PaymentSessionRepository paymentSessionRepository,
      InvoiceTermRepository invoiceTermRepository) {
    this.paymentSessionRepository = paymentSessionRepository;
    this.invoiceTermRepository = invoiceTermRepository;
  }

  @Override
  public String computeName(PaymentSession paymentSession) {
    StringBuilder name = new StringBuilder("Session");
    User createdBy = paymentSession.getCreatedBy();
    if (ObjectUtils.notEmpty(paymentSession.getPaymentMode())) {
      name.append(" " + paymentSession.getPaymentMode().getName());
    }
    if (ObjectUtils.notEmpty(paymentSession.getCreatedOn())) {
      name.append(
          String.format(
              " %s %s",
              I18n.get(ITranslation.PAYMENT_SESSION_COMPUTE_NAME_ON_THE),
              paymentSession
                  .getCreatedOn()
                  .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
    }
    if (ObjectUtils.notEmpty(createdBy)) {
      name.append(
          String.format(
              " %s %s",
              I18n.get(ITranslation.PAYMENT_SESSION_COMPUTE_NAME_BY), createdBy.getName()));
    }
    return name.toString();
  }

  @Override
  public void setBankDetails(PaymentSession paymentSession) {
    if (paymentSession.getCompany() != null
        && paymentSession.getPaymentMode() != null
        && CollectionUtils.isNotEmpty(paymentSession.getPaymentMode().getAccountManagementList())) {
      Optional<BankDetails> bankDetails =
          paymentSession.getPaymentMode().getAccountManagementList().stream()
              .filter(
                  accountManagement ->
                      paymentSession.getCompany().equals(accountManagement.getCompany())
                          && accountManagement.getBankDetails() != null)
              .map(AccountManagement::getBankDetails)
              .findFirst();
      bankDetails.ifPresent(paymentSession::setBankDetails);
    }
  }

  @Override
  public void setJournal(PaymentSession paymentSession) {
    if (paymentSession.getCompany() != null
        && paymentSession.getPaymentMode() != null
        && CollectionUtils.isNotEmpty(paymentSession.getPaymentMode().getAccountManagementList())) {
      Optional<Journal> journal =
          paymentSession.getPaymentMode().getAccountManagementList().stream()
              .filter(
                  accountManagement ->
                      paymentSession.getCompany().equals(accountManagement.getCompany())
                          && accountManagement.getJournal() != null)
              .map(AccountManagement::getJournal)
              .findFirst();
      journal.ifPresent(paymentSession::setJournal);
    }
  }

  @Override
  @Transactional
  public void computeTotalPaymentSession(PaymentSession paymentSession) {
    BigDecimal sessionTotalAmount =
        (BigDecimal)
            JPA.em()
                .createQuery(
                    "select SUM(self.amountPaid) FROM InvoiceTerm as self WHERE self.paymentSession = ?1 AND self.isSelectedOnPaymentSession = TRUE")
                .setParameter(1, paymentSession)
                .getSingleResult();
    paymentSession.setSessionTotalAmount(sessionTotalAmount);
    paymentSessionRepository.save(paymentSession);
  }

  @Override
  public boolean hasUnselectedInvoiceTerm(PaymentSession paymentSession) {
    return invoiceTermRepository
            .all()
            .filter(
                "self.paymentSession = :paymentSession AND self.isSelectedOnPaymentSession IS FALSE")
            .bind("paymentSession", paymentSession.getId())
            .count()
        > 0;
  }
}
