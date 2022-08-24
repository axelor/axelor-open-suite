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
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.PaymentSession;
import com.axelor.apps.account.db.repo.InvoiceTermRepository;
import com.axelor.apps.account.db.repo.JournalTypeRepository;
import com.axelor.apps.account.db.repo.PaymentSessionRepository;
import com.axelor.apps.account.translation.ITranslation;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import com.google.inject.servlet.RequestScoped;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
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
    List<BankDetails> bankDetailsList = this.getBankDetails(paymentSession);

    if (CollectionUtils.isNotEmpty(bankDetailsList)) {
      paymentSession.setBankDetails(bankDetailsList.get(0));
    } else {
      paymentSession.setBankDetails(null);
    }
  }

  @Override
  public void setJournal(PaymentSession paymentSession) {
    List<Journal> journalList = this.getJournals(paymentSession);

    if (CollectionUtils.isNotEmpty(journalList)) {
      paymentSession.setJournal(journalList.get(0));
    } else {
      paymentSession.setJournal(null);
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

  @Override
  public List<BankDetails> getBankDetails(PaymentSession paymentSession) {
    Company company = paymentSession.getCompany();
    PaymentMode paymentMode = paymentSession.getPaymentMode();

    if (paymentMode == null || CollectionUtils.isEmpty(paymentMode.getAccountManagementList())) {
      return new ArrayList<>();
    }

    return paymentMode.getAccountManagementList().stream()
        .filter(it -> Objects.equals(company, it.getCompany()))
        .map(AccountManagement::getBankDetails)
        .collect(Collectors.toList());
  }

  @Override
  public List<Journal> getJournals(PaymentSession paymentSession) {
    Company company = paymentSession.getCompany();
    BankDetails bankDetails = paymentSession.getBankDetails();
    PaymentMode paymentMode = paymentSession.getPaymentMode();

    if (bankDetails == null
        || paymentMode == null
        || CollectionUtils.isEmpty(paymentMode.getAccountManagementList())) {
      return new ArrayList<>();
    }

    return paymentMode.getAccountManagementList().stream()
        .filter(
            it ->
                Objects.equals(company, it.getCompany())
                    && Objects.equals(bankDetails, it.getBankDetails()))
        .map(AccountManagement::getJournal)
        .filter(
            it ->
                it.getJournalType().getTechnicalTypeSelect()
                    == JournalTypeRepository.TECHNICAL_TYPE_SELECT_TREASURY)
        .collect(Collectors.toList());
  }
}
