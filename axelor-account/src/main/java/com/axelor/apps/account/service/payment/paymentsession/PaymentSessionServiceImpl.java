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

import com.axelor.apps.account.db.AccountConfig;
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
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.account.translation.ITranslation;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Blocking;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.BlockingRepository;
import com.axelor.apps.base.service.DateService;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import com.google.inject.servlet.RequestScoped;
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
  protected AccountConfigService accountConfigService;
  protected DateService dateService;
  protected PaymentSessionCancelService paymentSessionCancelService;
  protected int jpaLimit = 4;

  @Inject
  public PaymentSessionServiceImpl(
      PaymentSessionRepository paymentSessionRepository,
      InvoiceTermRepository invoiceTermRepository,
      InvoiceTermService invoiceTermService,
      PaymentSessionValidateService paymentSessionValidateService,
      AccountConfigService accountConfigService,
      DateService dateService,
      PaymentSessionCancelService paymentSessionCancelService) {
    this.paymentSessionRepository = paymentSessionRepository;
    this.invoiceTermRepository = invoiceTermRepository;
    this.invoiceTermService = invoiceTermService;
    this.paymentSessionValidateService = paymentSessionValidateService;
    this.accountConfigService = accountConfigService;
    this.dateService = dateService;
    this.paymentSessionCancelService = paymentSessionCancelService;
  }

  @Override
  public String computeName(PaymentSession paymentSession) throws AxelorException {
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
              paymentSession.getCreatedOn().format(dateService.getDateTimeFormat())));
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
        .filter(Objects::nonNull)
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
  @Transactional(rollbackOn = {Exception.class})
  public void selectAll(PaymentSession paymentSession) throws AxelorException {
    List<InvoiceTerm> invoiceTermList = getTermsBySession(paymentSession, false).fetch();
    invoiceTermService.toggle(invoiceTermList, true);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void unSelectAll(PaymentSession paymentSession) throws AxelorException {
    List<InvoiceTerm> invoiceTermList = getTermsBySession(paymentSession, true).fetch();
    invoiceTermService.toggle(invoiceTermList, false);
  }

  protected Query<InvoiceTerm> getTermsBySession(
      PaymentSession paymentSession, boolean isSelectedOnPaymentSession) {
    return invoiceTermRepository
        .all()
        .filter(
            "self.paymentSession = :paymentSession AND (self.isSelectedOnPaymentSession IS :isSelectedOnPaymentSession )")
        .bind("paymentSession", paymentSession.getId())
        .bind("isSelectedOnPaymentSession", isSelectedOnPaymentSession);
  }

  protected Query<InvoiceTerm> getTermsBySession(PaymentSession paymentSession) {
    return invoiceTermRepository
        .all()
        .filter("self.paymentSession = :paymentSession ")
        .bind("paymentSession", paymentSession.getId());
  }

  protected void retrieveEligibleTerms(PaymentSession paymentSession) throws AxelorException {
    Query<InvoiceTerm> eligibleInvoiceTermQuery =
        invoiceTermRepository
            .all()
            .filter(retrieveEligibleTermsQuery(paymentSession.getCompany()))
            .bind("company", paymentSession.getCompany())
            .bind("paymentMode", paymentSession.getPaymentMode())
            .bind("paymentModeInOutSelect", paymentSession.getPaymentMode().getInOutSelect())
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
            .order("id");

    this.filterInvoiceTerms(eligibleInvoiceTermQuery, paymentSession);
  }

  protected String retrieveEligibleTermsQuery(Company company) throws AxelorException {
    String generalCondition =
        "self.moveLine.move.company = :company "
            + " AND self.dueDate <= :paymentDatePlusMargin "
            + " AND self.moveLine.move.currency = :currency "
            + " AND self.bankDetails IS NOT NULL "
            + " AND (self.paymentMode = :paymentMode OR self.paymentMode.inOutSelect != :paymentModeInOutSelect)"
            + " AND self.moveLine.account.isRetrievedOnPaymentSession = TRUE "
            + " AND (self.moveLine.move.statusSelect = "
            + MoveRepository.STATUS_ACCOUNTED;
    AccountConfig accountConfig = accountConfigService.getAccountConfig(company);
    if (accountConfig.getRetrieveDaybookMovesInPaymentSession()) {
      generalCondition += " OR self.moveLine.move.statusSelect = " + MoveRepository.STATUS_DAYBOOK;
    }

    String termsMoveLineCondition =
        ") AND ((self.moveLine.partner.isCustomer = TRUE "
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

  public void filterInvoiceTerms(
      Query<InvoiceTerm> eligibleInvoiceTermQuery, PaymentSession paymentSession) {
    List<InvoiceTerm> invoiceTermList;

    while (!(invoiceTermList = eligibleInvoiceTermQuery.fetch(jpaLimit)).isEmpty()) {
      for (InvoiceTerm invoiceTerm : invoiceTermList) {
        if (this.isNotAwaitingPayment(invoiceTerm)
            && !this.isBlocking(invoiceTerm, paymentSession)) {
          this.saveFilledInvoiceTermWithPaymentSession(paymentSession, invoiceTerm);
        }
      }
      JPA.clear();
      paymentSession = paymentSessionRepository.find(paymentSession.getId());
    }
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

  @Override
  public boolean hasInvoiceTerm(PaymentSession paymentSession) {
    return getTermsBySession(paymentSession).count() > 0;
  }

  @Transactional
  protected void saveFilledInvoiceTermWithPaymentSession(
      PaymentSession paymentSession, InvoiceTerm invoiceTerm) {
    fillEligibleTerm(paymentSession, invoiceTerm);
    invoiceTermRepository.save(invoiceTerm);
  }

  @Override
  public void searchEligibleTerms(PaymentSession paymentSession) throws AxelorException {
    paymentSessionCancelService.cancelInvoiceTerms(paymentSession);
    paymentSession = paymentSessionRepository.find(paymentSession.getId());
    retrieveEligibleTerms(paymentSession);
  }
}
