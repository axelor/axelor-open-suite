/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service.payment.paymentsession;

import com.axelor.apps.account.db.AccountManagement;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.InvoiceTermPayment;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.PaymentSession;
import com.axelor.apps.account.db.repo.AccountTypeRepository;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.InvoiceTermRepository;
import com.axelor.apps.account.db.repo.JournalTypeRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.db.repo.PaymentModeRepository;
import com.axelor.apps.account.db.repo.PaymentSessionRepository;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.account.translation.ITranslation;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Blocking;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.BlockingRepository;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import com.google.inject.servlet.RequestScoped;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

@RequestScoped
public class PaymentSessionServiceImpl implements PaymentSessionService {

  protected PaymentSessionRepository paymentSessionRepository;
  protected InvoiceTermRepository invoiceTermRepository;
  protected InvoiceTermService invoiceTermService;
  protected PaymentSessionValidateService paymentSessionValidateService;

  @Inject
  public PaymentSessionServiceImpl(
      PaymentSessionRepository paymentSessionRepository,
      InvoiceTermRepository invoiceTermRepository,
      InvoiceTermService invoiceTermService,
      PaymentSessionValidateService paymentSessionValidateService) {
    this.paymentSessionRepository = paymentSessionRepository;
    this.invoiceTermRepository = invoiceTermRepository;
    this.invoiceTermService = invoiceTermService;
    this.paymentSessionValidateService = paymentSessionValidateService;
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
    return getTermsBySession(paymentSession, false).count() > 0;
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

  @Override
  @Transactional
  public int removeMultiplePaymentSessions(List<Long> paymentSessionIds) {
    List<PaymentSession> paymentSessionList =
        paymentSessionRepository
            .all()
            .filter("self.id IN :paymentSessionIds AND self.statusSelect = :cancelledStatus")
            .bind("paymentSessionIds", paymentSessionIds)
            .bind("cancelledStatus", PaymentSessionRepository.STATUS_CANCELLED)
            .fetch();
    for (PaymentSession paymentSession : paymentSessionList) {
      paymentSessionRepository.remove(paymentSession);
    }
    return paymentSessionList.size();
  }

  @Override
  @Transactional
  public void selectAll(PaymentSession paymentSession) throws AxelorException {
    List<InvoiceTerm> invoiceTermList = getTermsBySession(paymentSession, false).fetch();
    invoiceTermService.toggle(invoiceTermList, true);
    computeTotalPaymentSession(paymentSession);
  }

  @Override
  @Transactional
  public void unSelectAll(PaymentSession paymentSession) throws AxelorException {
    List<InvoiceTerm> invoiceTermList = getTermsBySession(paymentSession, true).fetch();
    invoiceTermService.toggle(invoiceTermList, false);
    computeTotalPaymentSession(paymentSession);
  }

  protected Query<InvoiceTerm> getTermsBySession(
      PaymentSession paymentSession, boolean isSelectedOnPaymentSession) {
    return invoiceTermRepository
        .all()
        .filter(
            "self.paymentSession = :paymentSession AND self.isSelectedOnPaymentSession IS :isSelectedOnPaymentSession")
        .bind("paymentSession", paymentSession.getId())
        .bind("isSelectedOnPaymentSession", isSelectedOnPaymentSession);
  }

  @Override
  @Transactional
  public void retrieveEligibleTerms(PaymentSession paymentSession) {
    List<InvoiceTerm> eligibleInvoiceTermList =
        invoiceTermRepository
            .all()
            .filter(retrieveEligibleTermsQuery())
            .bind("company", paymentSession.getCompany())
            .bind("paymentModeTypeSelect", paymentSession.getPaymentMode().getTypeSelect())
            .bind(
                "paymentDatePlusMargin",
                paymentSession
                    .getPaymentDate()
                    .plusDays(paymentSession.getDaysMarginOnPaySession()))
            .bind("currency", paymentSession.getCurrency())
            .bind("partnerTypeSelect", paymentSession.getPartnerTypeSelect())
            .bind("receivable", AccountTypeRepository.TYPE_RECEIVABLE)
            .bind("payable", AccountTypeRepository.TYPE_PAYABLE)
            .bind("partnerTypeClient", PaymentSessionRepository.PARTNER_TYPE_CUSTOMER)
            .bind("partnerTypeSupplier", PaymentSessionRepository.PARTNER_TYPE_SUPPLIER)
            .bind("functionalOriginClient", MoveRepository.FUNCTIONAL_ORIGIN_SALE)
            .bind("functionalOriginSupplier", MoveRepository.FUNCTIONAL_ORIGIN_PURCHASE)
            .bind("pfpValidateStatusValidated", InvoiceTermRepository.PFP_STATUS_VALIDATED)
            .bind(
                "pfpValidateStatusPartiallyValidated",
                InvoiceTermRepository.PFP_STATUS_PARTIALLY_VALIDATED)
            .fetch();

    eligibleInvoiceTermList = this.filterNotAwaitingPayment(eligibleInvoiceTermList);
    eligibleInvoiceTermList = this.filterBlocking(eligibleInvoiceTermList, paymentSession);
    eligibleInvoiceTermList.forEach(
        invoiceTerm -> {
          fillEligibleTerm(paymentSession, invoiceTerm);
          invoiceTermRepository.save(invoiceTerm);
        });

    computeTotalPaymentSession(paymentSession);
  }

  protected String retrieveEligibleTermsQuery() {
    String generalCondition =
        "self.moveLine.move.company = :company "
            + " AND self.dueDate <= :paymentDatePlusMargin "
            + " AND self.moveLine.move.currency = :currency "
            + " AND self.bankDetails IS NOT NULL "
            + " AND self.paymentMode.typeSelect = :paymentModeTypeSelect"
            + " AND self.moveLine.account.isRetrievedOnPaymentSession = TRUE ";
    String termsMoveLineCondition =
        " AND ((self.moveLine.partner.isCustomer = TRUE "
            + " AND :partnerTypeSelect = :partnerTypeClient"
            + " AND self.moveLine.move.functionalOriginSelect = :functionalOriginClient)"
            + " OR ( self.moveLine.partner.isSupplier = TRUE "
            + " AND :partnerTypeSelect = :partnerTypeSupplier "
            + " AND self.moveLine.move.functionalOriginSelect = :functionalOriginSupplier "
            + " AND (self.moveLine.move.company.accountConfig.isManagePassedForPayment is NULL "
            + " OR self.moveLine.move.company.accountConfig.isManagePassedForPayment = FALSE  "
            + " OR (self.moveLine.move.company.accountConfig.isManagePassedForPayment = TRUE "
            + " AND (self.pfpValidateStatusSelect = :pfpValidateStatusValidated OR self.pfpValidateStatusSelect = :pfpValidateStatusPartiallyValidated))))) ";
    String paymentHistoryCondition =
        " AND self.isPaid = FALSE"
            + " AND self.amountRemaining > 0"
            + " AND self.paymentSession IS NULL";
    return generalCondition + termsMoveLineCondition + paymentHistoryCondition;
  }

  public List<InvoiceTerm> filterNotAwaitingPayment(List<InvoiceTerm> invoiceTermList) {
    return invoiceTermList.stream().filter(this::isNotAwaitingPayment).collect(Collectors.toList());
  }

  public boolean isNotAwaitingPayment(InvoiceTerm invoiceTerm) {
    if (invoiceTerm == null) {
      return false;
    } else if (invoiceTerm.getInvoice() != null) {
      Invoice invoice = invoiceTerm.getInvoice();

      if (CollectionUtils.isNotEmpty(invoice.getInvoicePaymentList())) {
        return invoice.getInvoicePaymentList().stream()
            .filter(it -> it.getStatusSelect() == InvoicePaymentRepository.STATUS_PENDING)
            .map(InvoicePayment::getInvoiceTermPaymentList)
            .flatMap(Collection::stream)
            .map(InvoiceTermPayment::getInvoiceTerm)
            .noneMatch(it -> it.getId().equals(invoiceTerm.getId()));
      }
    }

    return true;
  }

  protected List<InvoiceTerm> filterBlocking(
      List<InvoiceTerm> invoiceTermList, PaymentSession paymentSession) {
    return invoiceTermList.stream()
        .filter(it -> !this.isBlocking(it, paymentSession))
        .collect(Collectors.toList());
  }

  protected boolean isBlocking(InvoiceTerm invoiceTerm, PaymentSession paymentSession) {
    if (paymentSession.getPaymentMode().getTypeSelect() != PaymentModeRepository.TYPE_DD) {
      return false;
    }

    if (invoiceTerm.getInvoice() != null) {
      Invoice invoice = invoiceTerm.getInvoice();

      if (invoice.getDebitBlockingOk()
          && !paymentSession.getPaymentDate().isAfter(invoice.getDebitBlockingToDate())) {
        return true;
      }

      if (this.isBlocking(invoice.getPartner(), paymentSession)) {
        return true;
      }
    }

    if (invoiceTerm.getMoveLine() != null) {
      MoveLine moveLine = invoiceTerm.getMoveLine();

      if (moveLine.getPartner() != null && this.isBlocking(moveLine.getPartner(), paymentSession)) {
        return true;
      } else if (moveLine.getMove().getPartner() != null
          && this.isBlocking(moveLine.getMove().getPartner(), paymentSession)) {
        return true;
      }
    }

    return false;
  }

  protected boolean isBlocking(Partner partner, PaymentSession paymentSession) {
    for (Blocking blocking : partner.getBlockingList()) {
      if (blocking.getBlockingSelect().equals(BlockingRepository.DEBIT_BLOCKING)
          && !paymentSession.getPaymentDate().isAfter(blocking.getBlockingToDate())) {
        return true;
      }
    }

    return false;
  }

  protected void fillEligibleTerm(PaymentSession paymentSession, InvoiceTerm invoiceTerm) {
    LocalDate nextSessionDate = paymentSession.getNextSessionDate();
    LocalDate paymentDate =
        paymentSessionValidateService.getAccountingDate(paymentSession, invoiceTerm);
    LocalDate financialDiscountDeadlineDate = invoiceTerm.getFinancialDiscountDeadlineDate();
    boolean isSignedNegative = this.getIsSignedNegative(invoiceTerm);

    invoiceTerm.setPaymentSession(paymentSession);
    invoiceTerm.setIsSelectedOnPaymentSession(true);
    if (isSignedNegative) {
      invoiceTerm.setPaymentAmount(invoiceTerm.getAmountRemaining().negate());

    } else {
      invoiceTerm.setPaymentAmount(invoiceTerm.getAmountRemaining());
    }

    if (invoiceTerm.getApplyFinancialDiscount() && financialDiscountDeadlineDate != null) {

      if (invoiceTerm.getFinancialDiscountAmount().compareTo(invoiceTerm.getAmountRemaining())
          > 0) {
        invoiceTerm.setApplyFinancialDiscountOnPaymentSession(false);
      } else if (paymentDate != null && !financialDiscountDeadlineDate.isBefore(paymentDate)) {
        invoiceTerm.setApplyFinancialDiscountOnPaymentSession(true);
      }
      if (nextSessionDate != null && !financialDiscountDeadlineDate.isBefore(nextSessionDate)) {
        invoiceTerm.setIsSelectedOnPaymentSession(false);
      }
    }

    Beans.get(InvoiceTermService.class).computeAmountPaid(invoiceTerm);
  }

  protected boolean getIsSignedNegative(InvoiceTerm invoiceTerm) {
    boolean isSignedNegative = false;
    if (invoiceTerm.getMoveLine() != null) {
      if (invoiceTerm.getMoveLine().getMove().getFunctionalOriginSelect()
          == MoveRepository.FUNCTIONAL_ORIGIN_SALE) {
        isSignedNegative =
            invoiceTerm
                    .getMoveLine()
                    .getDebit()
                    .subtract(invoiceTerm.getMoveLine().getCredit())
                    .signum()
                < 0;
      } else if (invoiceTerm.getMoveLine().getMove().getFunctionalOriginSelect()
          == MoveRepository.FUNCTIONAL_ORIGIN_PURCHASE) {
        isSignedNegative =
            invoiceTerm
                    .getMoveLine()
                    .getCredit()
                    .subtract(invoiceTerm.getMoveLine().getDebit())
                    .signum()
                < 0;
      }
    }
    return isSignedNegative;
  }
}
