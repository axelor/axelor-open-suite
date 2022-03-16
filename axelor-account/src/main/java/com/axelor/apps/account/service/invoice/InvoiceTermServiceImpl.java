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
package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.FinancialDiscount;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.InvoiceTermPayment;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentConditionLine;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.PaymentSession;
import com.axelor.apps.account.db.PfpPartialReason;
import com.axelor.apps.account.db.SubstitutePfpValidator;
import com.axelor.apps.account.db.repo.AccountTypeRepository;
import com.axelor.apps.account.db.repo.FinancialDiscountRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.InvoiceTermRepository;
import com.axelor.apps.account.service.InvoiceVisibilityService;
import com.axelor.apps.account.service.PaymentSessionService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.db.CancelReason;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.axelor.db.Query;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;

public class InvoiceTermServiceImpl implements InvoiceTermService {

  protected InvoiceTermRepository invoiceTermRepo;
  protected InvoiceRepository invoiceRepo;
  protected InvoiceService invoiceService;
  protected AppAccountService appAccountService;
  protected InvoiceToolService invoiceToolService;
  protected InvoiceVisibilityService invoiceVisibilityService;
  protected AccountConfigService accountConfigService;

  @Inject
  public InvoiceTermServiceImpl(
      InvoiceTermRepository invoiceTermRepo,
      InvoiceRepository invoiceRepo,
      InvoiceService invoiceService,
      AppAccountService appAccountService,
      InvoiceToolService invoiceToolService,
      InvoiceVisibilityService invoiceVisibilityService,
      AccountConfigService accountConfigService) {
    this.invoiceTermRepo = invoiceTermRepo;
    this.invoiceRepo = invoiceRepo;
    this.invoiceService = invoiceService;
    this.appAccountService = appAccountService;
    this.invoiceToolService = invoiceToolService;
    this.invoiceVisibilityService = invoiceVisibilityService;
    this.accountConfigService = accountConfigService;
  }

  @Override
  public boolean checkInvoiceTermsSum(Invoice invoice) throws AxelorException {

    BigDecimal totalAmount = BigDecimal.ZERO;
    for (InvoiceTerm invoiceTerm : invoice.getInvoiceTermList()) {
      totalAmount = totalAmount.add(invoiceTerm.getAmount());
    }
    if (invoice.getInTaxTotal().compareTo(totalAmount) != 0) {
      return false;
    }
    return true;
  }

  @Override
  public boolean checkInvoiceTermsPercentageSum(Invoice invoice) throws AxelorException {

    return new BigDecimal(100).compareTo(computePercentageSum(invoice)) == 0;
  }

  @Override
  public BigDecimal computePercentageSum(Invoice invoice) {

    BigDecimal sum = BigDecimal.ZERO;
    if (CollectionUtils.isNotEmpty(invoice.getInvoiceTermList())) {
      for (InvoiceTerm invoiceTerm : invoice.getInvoiceTermList()) {
        sum = sum.add(invoiceTerm.getPercentage());
      }
    }
    return sum;
  }

  protected BigDecimal computePercentageSum(MoveLine moveLine) {

    BigDecimal sum = BigDecimal.ZERO;
    if (CollectionUtils.isNotEmpty(moveLine.getInvoiceTermList())) {
      for (InvoiceTerm invoiceTerm : moveLine.getInvoiceTermList()) {
        sum = sum.add(invoiceTerm.getPercentage());
      }
    }
    return sum;
  }

  @Override
  public boolean checkIfCustomizedInvoiceTerms(Invoice invoice) {

    if (!CollectionUtils.isEmpty(invoice.getInvoiceTermList())) {
      for (InvoiceTerm invoiceTerm : invoice.getInvoiceTermList()) {
        if (invoiceTerm.getIsCustomized()) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public Invoice computeInvoiceTerms(Invoice invoice) throws AxelorException {

    if (invoice.getPaymentCondition() == null
        || CollectionUtils.isEmpty(invoice.getPaymentCondition().getPaymentConditionLineList())) {
      return invoice;
    }

    invoice.clearInvoiceTermList();

    Set<PaymentConditionLine> paymentConditionLines =
        new HashSet<>(invoice.getPaymentCondition().getPaymentConditionLineList());
    Iterator<PaymentConditionLine> iterator = paymentConditionLines.iterator();
    BigDecimal total = BigDecimal.ZERO;
    while (iterator.hasNext()) {
      PaymentConditionLine paymentConditionLine = iterator.next();
      InvoiceTerm invoiceTerm = computeInvoiceTerm(invoice, paymentConditionLine);
      if (!iterator.hasNext()) {
        invoiceTerm.setAmount(invoice.getInTaxTotal().subtract(total));
        invoiceTerm.setAmountRemaining(invoice.getInTaxTotal().subtract(total));
        this.computeAmountRemainingAfterFinDiscount(invoiceTerm);
      } else {
        total = total.add(invoiceTerm.getAmount());
      }
      invoice.addInvoiceTermListItem(invoiceTerm);
    }

    return invoice;
  }

  @Override
  public InvoiceTerm computeInvoiceTerm(Invoice invoice, PaymentConditionLine paymentConditionLine)
      throws AxelorException {

    InvoiceTerm invoiceTerm = new InvoiceTerm();

    invoiceTerm.setPaymentConditionLine(paymentConditionLine);
    BigDecimal amount =
        invoice
            .getInTaxTotal()
            .multiply(paymentConditionLine.getPaymentPercentage())
            .divide(
                BigDecimal.valueOf(100),
                AppBaseService.DEFAULT_NB_DECIMAL_DIGITS,
                RoundingMode.HALF_UP);
    invoiceTerm.setAmount(amount);
    invoiceTerm.setAmountRemaining(amount);

    invoiceTerm.setIsHoldBack(paymentConditionLine.getIsHoldback());
    invoiceTerm.setIsPaid(false);
    invoiceTerm.setPercentage(paymentConditionLine.getPaymentPercentage());

    this.computeFinancialDiscount(invoiceTerm, invoice);

    if (getPfpValidatorUserCondition(invoice)) {
      invoiceTerm.setPfpValidatorUser(invoiceService.getPfpValidatorUser(invoice));
    }
    invoiceTerm.setPaymentMode(invoice.getPaymentMode());
    invoiceTerm.setPfpValidateStatusSelect(InvoiceTermRepository.PFP_STATUS_AWAITING);
    invoiceTerm.setBankDetails(invoice.getBankDetails());
    return invoiceTerm;
  }

  protected void computeFinancialDiscount(InvoiceTerm invoiceTerm, Invoice invoice) {
    this.computeFinancialDiscount(
        invoiceTerm,
        invoice.getFinancialDiscount(),
        invoice.getFinancialDiscountTotalAmount(),
        invoice.getRemainingAmountAfterFinDiscount());
  }

  protected void computeFinancialDiscount(
      InvoiceTerm invoiceTerm,
      FinancialDiscount financialDiscount,
      BigDecimal financialDiscountAmount,
      BigDecimal remainingAmountAfterFinDiscount) {
    if (appAccountService.getAppAccount().getManageFinancialDiscount()) {
      BigDecimal percentage =
          invoiceTerm.getPercentage().divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);

      invoiceTerm.setApplyFinancialDiscount(financialDiscount != null);
      invoiceTerm.setFinancialDiscount(financialDiscount);
      invoiceTerm.setFinancialDiscountAmount(
          financialDiscountAmount.multiply(percentage).setScale(2, RoundingMode.HALF_UP));
      invoiceTerm.setRemainingAmountAfterFinDiscount(
          remainingAmountAfterFinDiscount.multiply(percentage).setScale(2, RoundingMode.HALF_UP));
      this.computeAmountRemainingAfterFinDiscount(invoiceTerm);
    }
  }

  protected void computeAmountRemainingAfterFinDiscount(InvoiceTerm invoiceTerm) {
    if (invoiceTerm.getAmount().signum() > 0) {
      invoiceTerm.setAmountRemainingAfterFinDiscount(
          invoiceTerm
              .getAmountRemaining()
              .multiply(invoiceTerm.getRemainingAmountAfterFinDiscount())
              .divide(invoiceTerm.getAmount(), 2, RoundingMode.HALF_UP));
    }
  }

  protected boolean getPfpValidatorUserCondition(Invoice invoice) {
    return appAccountService.getAppAccount().getActivatePassedForPayment()
        && (invoice.getCompany().getAccountConfig().getIsManagePassedForPayment()
            && (invoice.getOperationTypeSelect()
                    == InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE
                || (invoice.getCompany().getAccountConfig().getIsManagePFPInRefund()
                    && invoice.getOperationTypeSelect()
                        == InvoiceRepository.OPERATION_TYPE_SUPPLIER_REFUND)));
  }

  @Override
  public InvoiceTerm initCustomizedInvoiceTerm(Invoice invoice, InvoiceTerm invoiceTerm) {

    invoiceTerm.setInvoice(invoice);
    invoiceTerm.setIsCustomized(true);
    invoiceTerm.setIsPaid(false);
    invoiceTerm.setIsHoldBack(false);
    invoiceTerm.setPaymentMode(invoice.getPaymentMode());
    BigDecimal invoiceTermPercentage = BigDecimal.ZERO;
    BigDecimal percentageSum = computePercentageSum(invoice);
    if (percentageSum.compareTo(BigDecimal.ZERO) > 0) {
      invoiceTermPercentage = new BigDecimal(100).subtract(percentageSum);
    }
    invoiceTerm.setPercentage(invoiceTermPercentage);
    BigDecimal amount =
        invoice
            .getInTaxTotal()
            .multiply(invoiceTermPercentage)
            .divide(
                BigDecimal.valueOf(100),
                AppBaseService.DEFAULT_NB_DECIMAL_DIGITS,
                RoundingMode.HALF_UP);
    invoiceTerm.setAmount(amount);
    invoiceTerm.setAmountRemaining(amount);
    this.computeFinancialDiscount(invoiceTerm, invoice);

    if (invoice.getStatusSelect() == InvoiceRepository.STATUS_VENTILATED) {
      MoveLine moveLine = getExistingInvoiceTermMoveLine(invoice);
      moveLine.addInvoiceTermListItem(invoiceTerm);
    }

    return invoiceTerm;
  }

  @Override
  public InvoiceTerm initCustomizedInvoiceTerm(MoveLine moveLine, InvoiceTerm invoiceTerm) {

    invoiceTerm.setInvoice(moveLine.getMove().getInvoice());
    invoiceTerm.setSequence(initInvoiceTermsSequence(moveLine));

    invoiceTerm.setIsCustomized(true);
    invoiceTerm.setIsPaid(false);
    invoiceTerm.setIsHoldBack(false);
    BigDecimal invoiceTermPercentage = BigDecimal.ZERO;
    BigDecimal percentageSum = computePercentageSum(moveLine);
    if (percentageSum.compareTo(BigDecimal.ZERO) > 0) {
      invoiceTermPercentage = new BigDecimal(100).subtract(percentageSum);
    }
    invoiceTerm.setPercentage(invoiceTermPercentage);
    BigDecimal amount;
    if (moveLine.getCredit().compareTo(moveLine.getDebit()) <= 0) {
      amount = moveLine.getDebit();
    } else {
      amount = moveLine.getCredit();
    }
    amount =
        amount
            .multiply(invoiceTermPercentage)
            .divide(
                BigDecimal.valueOf(100),
                AppBaseService.DEFAULT_NB_DECIMAL_DIGITS,
                RoundingMode.HALF_UP);
    invoiceTerm.setAmount(amount);
    invoiceTerm.setAmountRemaining(amount);

    return invoiceTerm;
  }

  @Override
  public MoveLine getExistingInvoiceTermMoveLine(Invoice invoice) {

    InvoiceTerm invoiceTerm =
        invoiceTermRepo
            .all()
            .filter("self.invoice.id = ?1 AND self.isHoldBack is not true", invoice.getId())
            .fetchOne();
    if (invoiceTerm == null) {
      return null;
    } else {
      return invoiceTerm.getMoveLine();
    }
  }

  @Override
  public Invoice setDueDates(Invoice invoice, LocalDate invoiceDate) {

    if (invoice.getPaymentCondition() == null
        || CollectionUtils.isEmpty(invoice.getInvoiceTermList())) {
      return invoice;
    }

    for (InvoiceTerm invoiceTerm : invoice.getInvoiceTermList()) {
      if (!invoiceTerm.getIsCustomized()) {
        LocalDate dueDate =
            InvoiceToolService.getDueDate(invoiceTerm.getPaymentConditionLine(), invoiceDate);
        invoiceTerm.setDueDate(dueDate);

        if (appAccountService.getAppAccount().getManageFinancialDiscount()
            && invoiceTerm.getApplyFinancialDiscount()
            && invoiceTerm.getFinancialDiscount() != null) {
          invoiceTerm.setFinancialDiscountDeadlineDate(
              this.computeFinancialDiscountDeadlineDate(invoiceTerm));
        }
      }
    }

    initInvoiceTermsSequence(invoice);
    return invoice;
  }

  protected LocalDate computeFinancialDiscountDeadlineDate(InvoiceTerm invoiceTerm) {
    LocalDate deadlineDate =
        invoiceTerm.getDueDate().minusDays(invoiceTerm.getFinancialDiscount().getDiscountDelay());

    if (invoiceTerm.getInvoice() != null && invoiceTerm.getInvoice().getInvoiceDate() != null) {
      LocalDate invoiceDate = invoiceTerm.getInvoice().getInvoiceDate();
      deadlineDate = deadlineDate.isBefore(invoiceDate) ? invoiceDate : deadlineDate;
    }

    return deadlineDate;
  }

  @Override
  public void initInvoiceTermsSequence(Invoice invoice) {

    invoice.getInvoiceTermList().sort(Comparator.comparing(InvoiceTerm::getDueDate));
    int sequence = 1;
    for (InvoiceTerm invoiceTerm : invoice.getInvoiceTermList()) {
      invoiceTerm.setSequence(sequence);
      sequence++;
    }
  }

  protected int initInvoiceTermsSequence(MoveLine moveLine) {
    if (CollectionUtils.isEmpty(moveLine.getInvoiceTermList())) {
      return 1;
    }
    return moveLine.getInvoiceTermList().stream()
            .max(Comparator.comparing(InvoiceTerm::getSequence))
            .get()
            .getSequence()
        + 1;
  }

  @Override
  public List<InvoiceTerm> getUnpaidInvoiceTerms(Invoice invoice) {
    String queryStr = "self.invoice = :invoice AND self.isPaid IS NOT TRUE";
    boolean pfpCondition =
        appAccountService.getAppAccount().getActivatePassedForPayment()
            && invoiceVisibilityService.getManagePfpCondition(invoice)
            && invoiceVisibilityService.getOperationTypePurchaseCondition(invoice);

    if (pfpCondition) {
      queryStr =
          queryStr + " AND self.pfpValidateStatusSelect IN (:validated, :partiallyValidated)";
    }

    Query<InvoiceTerm> invoiceTermQuery =
        invoiceTermRepo.all().filter(queryStr).bind("invoice", invoice);

    if (pfpCondition) {
      invoiceTermQuery
          .bind("validated", InvoiceTermRepository.PFP_STATUS_VALIDATED)
          .bind("partiallyValidated", InvoiceTermRepository.PFP_STATUS_PARTIALLY_VALIDATED);
    }

    return invoiceTermQuery.order("dueDate").fetch();
  }

  @Override
  public List<InvoiceTerm> filterInvoiceTermsByHoldBack(List<InvoiceTerm> invoiceTerms) {

    if (CollectionUtils.isEmpty(invoiceTerms)) {
      return invoiceTerms;
    }

    boolean isFirstHoldBack = invoiceTerms.get(0).getIsHoldBack();
    invoiceTerms.removeIf(it -> it.getIsHoldBack() != isFirstHoldBack);

    return invoiceTerms;
  }

  @Override
  public List<InvoiceTerm> getUnpaidInvoiceTermsFiltered(Invoice invoice) {

    return filterInvoiceTermsByHoldBack(getUnpaidInvoiceTerms(invoice));
  }

  @Override
  public LocalDate getLatestInvoiceTermDueDate(Invoice invoice) {

    List<InvoiceTerm> invoiceTerms = invoice.getInvoiceTermList();
    if (CollectionUtils.isEmpty(invoiceTerms)) {
      return invoice.getInvoiceDate();
    }
    LocalDate dueDate = null;
    for (InvoiceTerm invoiceTerm : invoiceTerms) {
      if (!invoiceTerm.getIsHoldBack()
          && (dueDate == null || dueDate.isBefore(invoiceTerm.getDueDate()))) {
        dueDate = invoiceTerm.getDueDate();
      }
    }
    return dueDate;
  }

  @Override
  public void updateInvoiceTermsPaidAmount(InvoicePayment invoicePayment) throws AxelorException {

    if (CollectionUtils.isEmpty(invoicePayment.getInvoiceTermPaymentList())) {
      return;
    }

    this.updateInvoiceTermsPaidAmount(
        invoicePayment.getInvoiceTermPaymentList(), invoicePayment.getPaymentMode());
  }

  @Override
  public void updateInvoiceTermsPaidAmount(
      InvoicePayment invoicePayment,
      InvoiceTerm invoiceTermToPay,
      InvoiceTermPayment invoiceTermPayment)
      throws AxelorException {
    this.updateInvoiceTermsPaidAmount(
        Collections.singletonList(invoiceTermPayment), invoiceTermToPay.getPaymentMode());
  }

  protected void updateInvoiceTermsPaidAmount(
      List<InvoiceTermPayment> invoiceTermPaymentList, PaymentMode paymentMode)
      throws AxelorException {
    for (InvoiceTermPayment invoiceTermPayment : invoiceTermPaymentList) {
      InvoiceTerm invoiceTerm = invoiceTermPayment.getInvoiceTerm();
      BigDecimal paidAmount =
          invoiceTermPayment.getPaidAmount().add(invoiceTermPayment.getFinancialDiscountAmount());

      BigDecimal amountRemaining = invoiceTerm.getAmountRemaining().subtract(paidAmount);
      invoiceTerm.setPaymentMode(paymentMode);

      if (amountRemaining.signum() <= 0) {
        amountRemaining = BigDecimal.ZERO;
        invoiceTerm.setIsPaid(true);
        Invoice invoice = invoiceTerm.getInvoice();
        if (invoice != null) {
          invoice.setDueDate(InvoiceToolService.getDueDate(invoice));
        }
      }
      invoiceTerm.setAmountRemaining(amountRemaining);
      this.computeAmountRemainingAfterFinDiscount(invoiceTerm);
    }
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void updateInvoiceTermsAmountRemaining(InvoicePayment invoicePayment)
      throws AxelorException {
    this.updateInvoiceTermsAmountRemaining(invoicePayment.getInvoiceTermPaymentList());
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void updateInvoiceTermsAmountRemaining(List<InvoiceTermPayment> invoiceTermPaymentList)
      throws AxelorException {

    for (InvoiceTermPayment invoiceTermPayment : invoiceTermPaymentList) {
      InvoiceTerm invoiceTerm = invoiceTermPayment.getInvoiceTerm();
      BigDecimal paidAmount =
          invoiceTermPayment.getPaidAmount().add(invoiceTermPayment.getFinancialDiscountAmount());
      invoiceTerm.setAmountRemaining(invoiceTerm.getAmountRemaining().add(paidAmount));
      this.computeAmountRemainingAfterFinDiscount(invoiceTerm);
      if (invoiceTerm.getAmountRemaining().signum() > 0) {
        invoiceTerm.setIsPaid(false);
        Invoice invoice = invoiceTerm.getInvoice();
        if (invoice != null) {
          invoice.setDueDate(InvoiceToolService.getDueDate(invoice));
        }
        invoiceTermRepo.save(invoiceTerm);
      }
    }
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void updateFinancialDiscount(Invoice invoice) {
    invoice.getInvoiceTermList().stream()
        .filter(it -> it.getAmountRemaining().compareTo(it.getAmount()) == 0)
        .forEach(it -> this.computeFinancialDiscount(it, invoice));

    invoiceRepo.save(invoice);
  }

  @Override
  public boolean checkInvoiceTermCreationConditions(Invoice invoice) {

    if (invoice.getId() == null
        || invoice.getAmountRemaining().compareTo(BigDecimal.ZERO) > 0
        || CollectionUtils.isEmpty(invoice.getInvoiceTermList())) {
      return false;
    }
    for (InvoiceTerm invoiceTerm : invoice.getInvoiceTermList()) {
      if (invoiceTerm.getId() == null) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean checkIfThereIsDeletedHoldbackInvoiceTerms(Invoice invoice) {

    if (invoice.getId() == null) {

      return false;
    }
    if (invoice.getStatusSelect() == InvoiceRepository.STATUS_VENTILATED) {

      List<InvoiceTerm> invoiceTermWithHoldback =
          invoiceTermRepo
              .all()
              .filter("self.invoice.id = ?1 AND self.isHoldBack is true", invoice.getId())
              .fetch();

      if (CollectionUtils.isEmpty(invoiceTermWithHoldback)) {
        return false;
      }
      List<InvoiceTerm> invoiceTerms = invoice.getInvoiceTermList();

      for (InvoiceTerm persistedInvoiceTermWithHoldback : invoiceTermWithHoldback) {
        if (!invoiceTerms.contains(persistedInvoiceTermWithHoldback)) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public boolean checkInvoiceTermDeletionConditions(Invoice invoice) {

    if (invoice.getId() == null || invoice.getAmountPaid().compareTo(BigDecimal.ZERO) == 0) {
      return false;
    }

    Invoice persistedInvoice = invoiceRepo.find(invoice.getId());

    if (CollectionUtils.isEmpty(persistedInvoice.getInvoiceTermList())) {
      return false;

    } else {

      List<InvoiceTerm> invoiceTerms = invoice.getInvoiceTermList();
      if (CollectionUtils.isEmpty(invoiceTerms)) {
        return true;
      }
      for (InvoiceTerm persistedInvoiceTerm : persistedInvoice.getInvoiceTermList()) {
        if (!invoiceTerms.contains(persistedInvoiceTerm)) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void refusalToPay(
      InvoiceTerm invoiceTerm, CancelReason reasonOfRefusalToPay, String reasonOfRefusalToPayStr) {
    invoiceTerm.setPfpValidateStatusSelect(InvoiceTermRepository.PFP_STATUS_LITIGATION);
    invoiceTerm.setDecisionPfpTakenDate(
        Beans.get(AppBaseService.class).getTodayDate(invoiceTerm.getInvoice().getCompany()));
    invoiceTerm.setPfpGrantedAmount(BigDecimal.ZERO);
    invoiceTerm.setPfpRejectedAmount(invoiceTerm.getAmount());
    invoiceTerm.setPfpValidatorUser(AuthUtils.getUser());
    invoiceTerm.setReasonOfRefusalToPay(reasonOfRefusalToPay);
    invoiceTerm.setReasonOfRefusalToPayStr(
        reasonOfRefusalToPayStr != null ? reasonOfRefusalToPayStr : reasonOfRefusalToPay.getName());

    invoiceTermRepo.save(invoiceTerm);
  }

  @Override
  @Transactional
  public void retrieveEligibleTerms(PaymentSession paymentSession) {
    List<InvoiceTerm> eligibleInvoiceTermList =
        invoiceTermRepo
            .all()
            .filter(retrieveEligibleTermsQuery())
            .bind("company", paymentSession.getCompany())
            .bind("paymentMode", paymentSession.getPaymentMode())
            .bind(
                "paymentDatePlusMargin",
                paymentSession
                    .getPaymentDate()
                    .plusDays(paymentSession.getDaysMarginOnPaySession()))
            .bind("currency", paymentSession.getCurrency())
            .bind("partnerTypeSelect", paymentSession.getPartnerTypeSelect())
            .bind("receivable", AccountTypeRepository.TYPE_RECEIVABLE)
            .bind("payable", AccountTypeRepository.TYPE_PAYABLE)
            .fetch();
    eligibleInvoiceTermList.forEach(
        invoiceTerm -> {
          fillEligibleTerm(paymentSession, invoiceTerm);
          invoiceTermRepo.save(invoiceTerm);
        });
    Beans.get(PaymentSessionService.class).computeTotalPaymentSession(paymentSession);
  }

  private String retrieveEligibleTermsQuery() {
    String generalCondition =
        "self.moveLine.move.company = :company"
            + " AND self.paymentMode = :paymentMode"
            + " AND self.dueDate <= :paymentDatePlusMargin"
            + " AND (self.invoice.currency = :currency OR self.moveLine.move.currency = :currency)"
            + " AND self.bankDetails IS NOT NULL";
    String termsFromInvoiceAndMoveLineCondition =
        " AND (self.moveLine.partner.isCustomer = TRUE AND :partnerTypeSelect = 3"
            + " OR self.moveLine.partner.isSupplier = TRUE AND :partnerTypeSelect = 1"
            + " OR self.moveLine.partner.isEmployee = TRUE AND :partnerTypeSelect = 2"
            + " OR self.invoice.partner.isCustomer = TRUE AND :partnerTypeSelect = 3"
            + " OR self.invoice.partner.isSupplier = TRUE AND :partnerTypeSelect = 1"
            + " OR self.invoice.partner.isEmployee = TRUE AND :partnerTypeSelect = 2)"
            + " AND (self.moveLine.account.isRetrievedOnPaymentSession = TRUE"
            + " OR self.invoice.partnerAccount.isRetrievedOnPaymentSession = TRUE)";
    String pfpCondition =
        " AND (self.invoice.operationTypeSelect = 3"
            + " OR self.invoice.operationTypeSelect = 4"
            + " OR self.moveLine.account.accountType.technicalTypeSelect = :receivable"
            + " OR self.invoice.company.accountConfig.isManagePassedForPayment = FALSE"
            + " OR self.moveLine.move.company.accountConfig.isManagePassedForPayment = FALSE"
            + " OR ((self.invoice.operationTypeSelect = 1"
            + " OR self.invoice.operationTypeSelect = 2"
            + " OR self.moveLine.account.accountType.technicalTypeSelect = :payable)"
            + " AND (self.invoice.company.accountConfig.isManagePassedForPayment = TRUE"
            + " OR self.moveLine.move.company.accountConfig.isManagePassedForPayment = TRUE)"
            + " AND (self.pfpValidateStatusSelect = 2 OR self.pfpValidateStatusSelect = 4)))";
    String paymentHistoryCondition =
        " AND self.isPaid = FALSE"
            + " AND self.amountRemaining > 0"
            + " AND self.paymentSession IS NULL";
    return generalCondition
        + termsFromInvoiceAndMoveLineCondition
        + pfpCondition
        + paymentHistoryCondition;
  }

  private void fillEligibleTerm(PaymentSession paymentSession, InvoiceTerm invoiceTerm) {
    LocalDate nextSessionDate = paymentSession.getNextSessionDate();
    LocalDate paymentDate = paymentSession.getPaymentDate();
    LocalDate financialDiscountDeadlineDate = null;
    LocalDate dueDate = invoiceTerm.getDueDate();
    Integer discountDelay = null;
    if (invoiceTerm.getInvoice() != null) {
      financialDiscountDeadlineDate = invoiceTerm.getInvoice().getFinancialDiscountDeadlineDate();
    }
    if (invoiceTerm.getMoveLine() != null
        && invoiceTerm.getMoveLine().getPartner() != null
        && invoiceTerm.getMoveLine().getPartner().getFinancialDiscount() != null
        && invoiceTerm.getMoveLine().getPartner().getFinancialDiscount().getDiscountDelay()
            != null) {
      discountDelay =
          invoiceTerm.getMoveLine().getPartner().getFinancialDiscount().getDiscountDelay();
    }

    invoiceTerm.setPaymentSession(paymentSession);
    invoiceTerm.setIsSelectedOnPaymentSession(true);
    if (nextSessionDate != null
        && (financialDiscountDeadlineDate != null
                && (financialDiscountDeadlineDate.isAfter(nextSessionDate)
                    || financialDiscountDeadlineDate.isEqual(nextSessionDate))
            || (dueDate != null
                && discountDelay != null
                && (dueDate.minusDays(discountDelay).isAfter(nextSessionDate)
                    || dueDate.minusDays(discountDelay).isEqual(nextSessionDate))))) {
      invoiceTerm.setPaymentAmount(invoiceTerm.getAmountRemaining());
    } else if (paymentDate != null
        && (financialDiscountDeadlineDate != null
                && (financialDiscountDeadlineDate.isAfter(paymentDate)
                    || financialDiscountDeadlineDate.isEqual(paymentDate))
            || dueDate != null
                && discountDelay != null
                && (dueDate.minusDays(discountDelay).isAfter(paymentDate)
                    || dueDate.minusDays(discountDelay).isEqual(paymentDate)))) {
      invoiceTerm.setPaymentAmount(
          invoiceTerm.getAmountRemaining().subtract(invoiceTerm.getFinancialDiscountAmount()));
    } else {
      invoiceTerm.setPaymentAmount(invoiceTerm.getAmountRemaining());
    }
    invoiceTerm.setAmountPaid(invoiceTerm.getPaymentAmount());
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void validatePfp(InvoiceTerm invoiceTerm, User currentUser) {
    invoiceTerm.setDecisionPfpTakenDate(
        Beans.get(AppBaseService.class).getTodayDate(invoiceTerm.getInvoice().getCompany()));
    invoiceTerm.setPfpGrantedAmount(invoiceTerm.getAmount());
    invoiceTerm.setPfpValidateStatusSelect(InvoiceTermRepository.PFP_STATUS_VALIDATED);
    invoiceTerm.setPfpValidatorUser(currentUser);
    invoiceTermRepo.save(invoiceTerm);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Integer massValidatePfp(List<Long> invoiceTermIds) {
    List<InvoiceTerm> invoiceTermList =
        invoiceTermRepo
            .all()
            .filter(
                "self.id in ? AND self.pfpValidateStatusSelect != ?",
                invoiceTermIds,
                InvoiceTermRepository.PFP_STATUS_VALIDATED)
            .fetch();
    User currentUser = AuthUtils.getUser();
    int updatedRecords = 0;
    for (InvoiceTerm invoiceTerm : invoiceTermList) {
      if (canUpdateInvoiceTerm(invoiceTerm, currentUser)) {
        validatePfp(invoiceTerm, currentUser);
        updatedRecords++;
      }
    }
    return updatedRecords;
  }

  @Override
  public Integer massRefusePfp(
      List<Long> invoiceTermIds,
      CancelReason reasonOfRefusalToPay,
      String reasonOfRefusalToPayStr) {
    List<InvoiceTerm> invoiceTermList =
        invoiceTermRepo
            .all()
            .filter(
                "self.id in ? AND self.pfpValidateStatusSelect != ?",
                invoiceTermIds,
                InvoiceTermRepository.PFP_STATUS_LITIGATION)
            .fetch();
    User currentUser = AuthUtils.getUser();
    int updatedRecords = 0;
    for (InvoiceTerm invoiceTerm : invoiceTermList) {
      boolean invoiceTermCheck =
          ObjectUtils.notEmpty(invoiceTerm.getInvoice())
              && ObjectUtils.notEmpty(invoiceTerm.getInvoice().getCompany())
              && ObjectUtils.notEmpty(reasonOfRefusalToPay);
      if (invoiceTermCheck && canUpdateInvoiceTerm(invoiceTerm, currentUser)) {
        refusalToPay(invoiceTerm, reasonOfRefusalToPay, reasonOfRefusalToPayStr);
        updatedRecords++;
      }
    }
    return updatedRecords;
  }

  @Override
  public BigDecimal getFinancialDiscountTaxAmount(InvoiceTerm invoiceTerm) {
    if (invoiceTerm.getInvoice() != null
        && invoiceTerm.getFinancialDiscount() != null
        && invoiceTerm.getFinancialDiscount().getDiscountBaseSelect()
            == FinancialDiscountRepository.DISCOUNT_BASE_VAT) {
      return invoiceTerm
          .getInvoice()
          .getTaxTotal()
          .multiply(invoiceTerm.getPercentage())
          .multiply(invoiceTerm.getFinancialDiscount().getDiscountRate())
          .divide(BigDecimal.valueOf(10000), 2, RoundingMode.HALF_UP);
    } else {
      return BigDecimal.ZERO;
    }
  }

  @Override
  public BigDecimal getAmountRemaining(InvoiceTerm invoiceTerm) {
    return invoiceTerm.getApplyFinancialDiscount()
        ? invoiceTerm.getAmountRemainingAfterFinDiscount()
        : invoiceTerm.getAmountRemaining();
  }

  protected boolean canUpdateInvoiceTerm(InvoiceTerm invoiceTerm, User currentUser) {
    boolean isValidUser =
        currentUser.getIsSuperPfpUser()
            || (ObjectUtils.notEmpty(invoiceTerm.getPfpValidatorUser())
                && currentUser.equals(invoiceTerm.getPfpValidatorUser()));
    if (isValidUser) {
      return true;
    }
    return validateUser(invoiceTerm, currentUser)
        && (ObjectUtils.notEmpty(invoiceTerm.getPfpValidatorUser())
            && invoiceTerm
                .getPfpValidatorUser()
                .equals(invoiceService.getPfpValidatorUser(invoiceTerm.getInvoice())))
        && !invoiceTerm.getIsPaid();
  }

  protected boolean validateUser(InvoiceTerm invoiceTerm, User currentUser) {
    boolean isValidUser = false;
    if (ObjectUtils.notEmpty(invoiceTerm.getPfpValidatorUser())) {
      List<SubstitutePfpValidator> substitutePfpValidatorList =
          invoiceTerm.getPfpValidatorUser().getSubstitutePfpValidatorList();
      LocalDate todayDate =
          Beans.get(AppBaseService.class).getTodayDate(invoiceTerm.getInvoice().getCompany());

      for (SubstitutePfpValidator substitutePfpValidator : substitutePfpValidatorList) {
        if (substitutePfpValidator.getSubstitutePfpValidatorUser().equals(currentUser)) {
          LocalDate substituteStartDate = substitutePfpValidator.getSubstituteStartDate();
          LocalDate substituteEndDate = substitutePfpValidator.getSubstituteEndDate();
          if (substituteStartDate == null) {
            if (substituteEndDate == null || substituteEndDate.isAfter(todayDate)) {
              isValidUser = true;
              break;
            }
          } else {
            if (substituteEndDate == null && substituteStartDate.isBefore(todayDate)) {
              isValidUser = true;
              break;
            } else if (substituteStartDate.isBefore(todayDate)
                && substituteEndDate.isAfter(todayDate)) {
              isValidUser = true;
              break;
            }
          }
        }
      }
    }
    return isValidUser;
  }

  @Override
  public BigDecimal computeCustomizedPercentage(BigDecimal amount, BigDecimal inTaxTotal) {
    BigDecimal percentage = BigDecimal.ZERO;
    if (inTaxTotal.compareTo(BigDecimal.ZERO) != 0) {
      percentage =
          amount
              .multiply(new BigDecimal(100))
              .divide(inTaxTotal, AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP);
    }
    return percentage;
  }

  @Override
  @Transactional
  public void generateInvoiceTerm(
      InvoiceTerm originalInvoiceTerm,
      BigDecimal invoiceAmount,
      BigDecimal pfpGrantedAmount,
      PfpPartialReason partialReason) {
    BigDecimal amount = invoiceAmount.subtract(pfpGrantedAmount);
    Invoice invoice = originalInvoiceTerm.getInvoice();
    createNewTerm(originalInvoiceTerm, invoice, amount);
    updateOriginalTerm(originalInvoiceTerm, pfpGrantedAmount, partialReason, amount, invoice);

    initInvoiceTermsSequence(originalInvoiceTerm.getInvoice());
  }

  @Transactional
  protected InvoiceTerm createNewTerm(
      InvoiceTerm originalInvoiceTerm, Invoice invoice, BigDecimal amount) {
    InvoiceTerm newInvoiceTerm = new InvoiceTerm();
    newInvoiceTerm.setInvoice(invoice);
    newInvoiceTerm.setIsCustomized(true);
    newInvoiceTerm.setIsPaid(false);
    originalInvoiceTerm.getMoveLine().addInvoiceTermListItem(newInvoiceTerm);
    newInvoiceTerm.setDueDate(originalInvoiceTerm.getDueDate());
    newInvoiceTerm.setIsHoldBack(originalInvoiceTerm.getIsHoldBack());
    newInvoiceTerm.setEstimatedPaymentDate(originalInvoiceTerm.getEstimatedPaymentDate());
    newInvoiceTerm.setAmount(amount);
    newInvoiceTerm.setPercentage(computeCustomizedPercentage(amount, invoice.getInTaxTotal()));
    newInvoiceTerm.setAmountRemaining(amount);
    newInvoiceTerm.setPaymentMode(originalInvoiceTerm.getPaymentMode());
    newInvoiceTerm.setBankDetails(originalInvoiceTerm.getBankDetails());
    newInvoiceTerm.setPfpValidateStatusSelect(InvoiceTermRepository.PFP_STATUS_AWAITING);
    newInvoiceTerm.setPfpValidatorUser(originalInvoiceTerm.getPfpValidatorUser());
    newInvoiceTerm.setPfpGrantedAmount(BigDecimal.ZERO);
    newInvoiceTerm.setPfpRejectedAmount(BigDecimal.ZERO);
    return invoiceTermRepo.save(newInvoiceTerm);
  }

  @Transactional
  protected void updateOriginalTerm(
      InvoiceTerm originalInvoiceTerm,
      BigDecimal pfpGrantedAmount,
      PfpPartialReason partialReason,
      BigDecimal amount,
      Invoice invoice) {
    originalInvoiceTerm.setIsCustomized(true);
    originalInvoiceTerm.setIsPaid(false);
    originalInvoiceTerm.setAmount(pfpGrantedAmount);
    originalInvoiceTerm.setPercentage(
        computeCustomizedPercentage(pfpGrantedAmount, invoice.getInTaxTotal()));
    originalInvoiceTerm.setAmountRemaining(pfpGrantedAmount);
    originalInvoiceTerm.setPfpValidateStatusSelect(
        InvoiceTermRepository.PFP_STATUS_PARTIALLY_VALIDATED);
    originalInvoiceTerm.setPfpGrantedAmount(pfpGrantedAmount);
    originalInvoiceTerm.setPfpRejectedAmount(amount);
    originalInvoiceTerm.setDecisionPfpTakenDate(LocalDate.now());
    originalInvoiceTerm.setPfpPartialReason(partialReason);
  }

  public void managePassedForPayment(InvoiceTerm invoiceTerm) throws AxelorException {
    if (invoiceTerm.getInvoice() != null && invoiceTerm.getInvoice().getCompany() != null) {
      if (accountConfigService
          .getAccountConfig(invoiceTerm.getInvoice().getCompany())
          .getIsManagePassedForPayment()) {
        invoiceTerm.setPaymentAmount(invoiceTerm.getPfpGrantedAmount());
      } else {
        invoiceTerm.setPaymentAmount(invoiceTerm.getAmountRemaining());
      }
    }
  }

  @Override
  @Transactional
  public void select(InvoiceTerm invoiceTerm) throws AxelorException {
    if (invoiceTerm != null) {
      invoiceTerm.setIsSelectedOnPaymentSession(true);
      managePassedForPayment(invoiceTerm);
      invoiceTermRepo.save(invoiceTerm);
    }
  }

  @Override
  @Transactional
  public void unselect(InvoiceTerm invoiceTerm) throws AxelorException {
    if (invoiceTerm != null) {
      invoiceTerm.setIsSelectedOnPaymentSession(false);
      managePassedForPayment(invoiceTerm);
      invoiceTermRepo.save(invoiceTerm);
    }
  }
}
