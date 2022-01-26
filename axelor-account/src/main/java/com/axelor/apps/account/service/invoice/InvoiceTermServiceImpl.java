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
import com.axelor.apps.account.db.PaymentSession;
import com.axelor.apps.account.db.PfpPartialReason;
import com.axelor.apps.account.db.SubstitutePfpValidator;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.InvoiceTermRepository;
import com.axelor.apps.account.service.PaymentSessionService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.db.CancelReason;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
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
  protected AccountConfigService accountConfigService;

  @Inject
  public InvoiceTermServiceImpl(
      InvoiceTermRepository invoiceTermRepo,
      InvoiceRepository invoiceRepo,
      InvoiceService invoiceService,
      AppAccountService appAccountService,
      InvoiceToolService invoiceToolService,
      AccountConfigService accountConfigService) {
    this.invoiceTermRepo = invoiceTermRepo;
    this.invoiceRepo = invoiceRepo;
    this.invoiceService = invoiceService;
    this.appAccountService = appAccountService;
    this.invoiceToolService = invoiceToolService;
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
    if (appAccountService.getAppAccount().getManageFinancialDiscount()) {
      invoiceTerm.setFinancialDiscount(invoice.getFinancialDiscount());
    }
    if (getPfpValidatorUserCondition(invoice)) {
      invoiceTerm.setPfpValidatorUser(invoiceService.getPfpValidatorUser(invoice));
    }
    invoiceTerm.setPaymentMode(invoice.getPaymentMode());
    invoiceTerm.setPfpValidateStatusSelect(InvoiceTermRepository.PFP_STATUS_AWAITING);
    invoiceTerm.setBankDetails(invoice.getBankDetails());
    return invoiceTerm;
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
    if (appAccountService.getAppAccount().getManageFinancialDiscount()) {
      invoiceTerm.setFinancialDiscount(invoice.getFinancialDiscount());
    }
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

    if (invoice.getStatusSelect() == InvoiceRepository.STATUS_VENTILATED) {

      invoiceTerm.setMoveLine(getExistingInvoiceTermMoveLine(invoice));
    }

    return invoiceTerm;
  }

  @Override
  public InvoiceTerm initCustomizedInvoiceTerm(MoveLine moveLine, InvoiceTerm invoiceTerm) {

    invoiceTerm.setMoveLine(moveLine);
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
      }
    }

    initInvoiceTermsSequence(invoice);
    return invoice;
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

    return invoiceTermRepo
        .all()
        .filter("self.invoice = ?1 AND self.isPaid is not true", invoice)
        .order("dueDate")
        .fetch();
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
    for (InvoiceTermPayment invoiceTermPayment : invoicePayment.getInvoiceTermPaymentList()) {
      InvoiceTerm invoiceTerm = invoiceTermPayment.getInvoiceTerm();
      BigDecimal paidAmount = invoiceTermPayment.getPaidAmount();

      if (appAccountService.getAppAccount().getManageFinancialDiscount()
          && invoicePayment.getApplyFinancialDiscount()) {
        invoiceTerm.setFinancialDiscount(invoicePayment.getFinancialDiscount());
        invoiceTerm.setFinancialDiscountAmount(invoicePayment.getFinancialDiscountTotalAmount());
      } else {
        invoiceTerm.setFinancialDiscount(null);
      }

      BigDecimal amountRemaining = invoiceTerm.getAmountRemaining().subtract(paidAmount);
      invoiceTerm.setPaymentMode(invoicePayment.getPaymentMode());

      if (amountRemaining.compareTo(BigDecimal.ZERO) <= 0) {
        amountRemaining = BigDecimal.ZERO;
        invoiceTerm.setIsPaid(true);
        Invoice invoice = invoiceTerm.getInvoice();
        if (invoice != null) {
          invoice.setDueDate(invoiceToolService.getDueDate(invoice));
        }
      }
      invoiceTerm.setAmountRemaining(amountRemaining);
    }
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void updateInvoiceTermsAmountRemaining(InvoicePayment invoicePayment)
      throws AxelorException {

    for (InvoiceTermPayment invoiceTermPayment : invoicePayment.getInvoiceTermPaymentList()) {
      InvoiceTerm invoiceTerm = invoiceTermPayment.getInvoiceTerm();
      BigDecimal paidAmount = invoiceTermPayment.getPaidAmount();
      invoiceTerm.setAmountRemaining(invoiceTerm.getAmountRemaining().add(paidAmount));
      if (invoiceTerm.getAmountRemaining().compareTo(BigDecimal.ZERO) > 0) {
        invoiceTerm.setIsPaid(false);
        Invoice invoice = invoiceTerm.getInvoice();
        if (invoice != null) {
          invoice.setDueDate(invoiceToolService.getDueDate(invoice));
        }
        invoiceTermRepo.save(invoiceTerm);
      }
    }
  }

  @Override
  public List<InvoiceTerm> updateFinancialDiscount(Invoice invoice) {
    FinancialDiscount financialDiscount = invoice.getFinancialDiscount();
    List<InvoiceTerm> invoiceTerms = Lists.newArrayList();
    for (InvoiceTerm invoiceTerm : invoice.getInvoiceTermList()) {
      if (invoiceTerm.getAmountRemaining().compareTo(invoiceTerm.getAmount()) == 0) {
        invoiceTerm.setFinancialDiscount(financialDiscount);
      }
      invoiceTerms.add(invoiceTerm);
    }
    return invoiceTerms;
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
                    .plusDays(paymentSession.getPaymentMode().getDaysMarginOnPaySession()))
            .bind("currency", paymentSession.getCurrency())
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
        " AND (self.moveLine IS NOT NULL"
            + " AND (self.moveLine.move.partner.isCustomer = TRUE"
            + " OR self.moveLine.move.partner.isSupplier = TRUE"
            + " OR self.moveLine.move.partner.isEmployee = TRUE)"
            + " OR self.moveLine IS NULL"
            + " AND (self.invoice.partner.isCustomer = TRUE"
            + " OR self.invoice.partner.isSupplier = TRUE"
            + " OR self.invoice.partner.isEmployee = TRUE))"
            + " AND ((self.moveLine IS NOT NULL"
            + " AND self.moveLine.account.isRetrievedOnPaymentSession = TRUE)"
            + " OR (self.moveLine IS NULL AND self.invoice.partnerAccount.isRetrievedOnPaymentSession = TRUE))";
    String pfpCondition =
        " AND (self.invoice.company.accountConfig.isManagePassedForPayment = FALSE"
            + " OR ((self.invoice.company.accountConfig.isManagePassedForPayment = TRUE"
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
    invoiceTerm.setPaymentSession(paymentSession);
    invoiceTerm.setIsSelectedOnPaymentSession(true);
    if (paymentSession.getNextSessionDate() != null
        && ((invoiceTerm
                    .getInvoice()
                    .getFinancialDiscountDeadlineDate()
                    .isAfter(paymentSession.getNextSessionDate())
                || invoiceTerm
                    .getInvoice()
                    .getFinancialDiscountDeadlineDate()
                    .isEqual(paymentSession.getNextSessionDate()))
            || (invoiceTerm
                    .getDueDate()
                    .minusDays(
                        invoiceTerm
                            .getMoveLine()
                            .getPartner()
                            .getFinancialDiscount()
                            .getDiscountDelay())
                    .isAfter(paymentSession.getNextSessionDate())
                || invoiceTerm
                    .getDueDate()
                    .minusDays(
                        invoiceTerm
                            .getMoveLine()
                            .getPartner()
                            .getFinancialDiscount()
                            .getDiscountDelay())
                    .isEqual(paymentSession.getNextSessionDate())))) {
      if (paymentSession.getCompany().getAccountConfig().getIsManagePassedForPayment()) {
        invoiceTerm.setPaymentAmount(invoiceTerm.getPfpGrantedAmount());
      } else {
        invoiceTerm.setPaymentAmount(invoiceTerm.getAmountRemaining());
      }
    } else if (invoiceTerm.getInvoice().getFinancialDiscountDeadlineDate() != null
        && (invoiceTerm
                .getInvoice()
                .getFinancialDiscountDeadlineDate()
                .isAfter(paymentSession.getPaymentDate())
            || invoiceTerm
                .getInvoice()
                .getFinancialDiscountDeadlineDate()
                .isEqual(paymentSession.getPaymentDate())
            || invoiceTerm
                .getDueDate()
                .minusDays(
                    invoiceTerm
                        .getMoveLine()
                        .getPartner()
                        .getFinancialDiscount()
                        .getDiscountDelay())
                .isAfter(paymentSession.getPaymentDate())
            || invoiceTerm
                .getDueDate()
                .minusDays(
                    invoiceTerm
                        .getMoveLine()
                        .getPartner()
                        .getFinancialDiscount()
                        .getDiscountDelay())
                .isEqual(paymentSession.getPaymentDate()))) {
      if (paymentSession.getCompany().getAccountConfig().getIsManagePassedForPayment()) {
        invoiceTerm.setPaymentAmount(
            invoiceTerm.getPfpGrantedAmount().subtract(invoiceTerm.getFinancialDiscountAmount()));
      } else {
        invoiceTerm.setPaymentAmount(
            invoiceTerm.getAmountRemaining().subtract(invoiceTerm.getFinancialDiscountAmount()));
      }
    } else {
      if (invoiceTerm.getAmountRemaining().equals(invoiceTerm.getAmount())) {
        invoiceTerm.setPaymentAmount(invoiceTerm.getAmount());
      } else {
        invoiceTerm.setPaymentAmount(invoiceTerm.getAmountRemaining());
      }
    }
    invoiceTerm.setAmountPaid(invoiceTerm.getPaymentAmount());
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
    User currenctUser = AuthUtils.getUser();
    int updatedRecords = 0;
    for (InvoiceTerm invoiceTerm : invoiceTermList) {
      if (canUpdateInvoiceTerm(invoiceTerm, currenctUser)) {
        invoiceTerm.setDecisionPfpTakenDate(
            Beans.get(AppBaseService.class).getTodayDate(invoiceTerm.getInvoice().getCompany()));
        invoiceTerm.setPfpGrantedAmount(invoiceTerm.getAmount());
        invoiceTerm.setPfpValidateStatusSelect(InvoiceTermRepository.PFP_STATUS_VALIDATED);
        invoiceTerm.setPfpValidatorUser(currenctUser);
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
    User currenctUser = AuthUtils.getUser();
    int updatedRecords = 0;
    for (InvoiceTerm invoiceTerm : invoiceTermList) {
      boolean invoiceTermCheck =
          ObjectUtils.notEmpty(invoiceTerm.getInvoice())
              && ObjectUtils.notEmpty(invoiceTerm.getInvoice().getCompany())
              && ObjectUtils.notEmpty(reasonOfRefusalToPay);
      if (invoiceTermCheck && canUpdateInvoiceTerm(invoiceTerm, currenctUser)) {
        refusalToPay(invoiceTerm, reasonOfRefusalToPay, reasonOfRefusalToPayStr);
        updatedRecords++;
      }
    }
    return updatedRecords;
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
    newInvoiceTerm.setMoveLine(originalInvoiceTerm.getMoveLine());
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
