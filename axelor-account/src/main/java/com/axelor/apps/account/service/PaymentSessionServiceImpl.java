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
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.PaymentSession;
import com.axelor.apps.account.db.repo.InvoiceTermRepository;
import com.axelor.apps.account.db.repo.PaymentSessionRepository;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.service.administration.AbstractBatch;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.apache.commons.collections.CollectionUtils;

public class PaymentSessionServiceImpl implements PaymentSessionService {

  protected PaymentSessionRepository paymentSessionRepo;
  protected InvoiceTermRepository invoiceTermRepo;

  @Inject
  public PaymentSessionServiceImpl(
      PaymentSessionRepository paymentSessionRepo, InvoiceTermRepository invoiceTermRepo) {
    this.paymentSessionRepo = paymentSessionRepo;
    this.invoiceTermRepo = invoiceTermRepo;
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
  @Transactional(rollbackOn = {Exception.class})
  public void cancelPaymentSession(PaymentSession paymentSession) {
    paymentSession.setStatusSelect(PaymentSessionRepository.STATUS_CANCELLED);
    paymentSessionRepo.save(paymentSession);

    this.cancelInvoiceTerms(paymentSession);
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void cancelInvoiceTerms(PaymentSession paymentSession) {
    int offset = 0;
    List<InvoiceTerm> invoiceTermList;
    Query<InvoiceTerm> invoiceTermQuery =
        invoiceTermRepo.all().filter("self.paymentSession = ?", paymentSession).order("id");

    while (!(invoiceTermList = invoiceTermQuery.fetch(AbstractBatch.FETCH_LIMIT, offset))
        .isEmpty()) {
      for (InvoiceTerm invoiceTerm : invoiceTermList) {
        offset++;

        invoiceTerm.setPaymentSession(null);
        invoiceTerm.setIsSelectedOnPaymentSession(false);
        invoiceTerm.setApplyFinancialDiscount(false);
        invoiceTerm.setPaymentAmount(BigDecimal.ZERO);
        invoiceTerm.setAmountPaid(BigDecimal.ZERO);

        invoiceTermRepo.save(invoiceTerm);
      }

      JPA.clear();
    }
  }
}
