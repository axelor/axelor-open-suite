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

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.db.FinancialDiscount;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.InvoiceTermPayment;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentConditionLine;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.PaymentSession;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.SubstitutePfpValidator;
import com.axelor.apps.account.db.repo.AccountTypeRepository;
import com.axelor.apps.account.db.repo.FinancialDiscountRepository;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.InvoiceTermRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.db.repo.PaymentModeRepository;
import com.axelor.apps.account.db.repo.PaymentSessionRepository;
import com.axelor.apps.account.service.AccountingSituationService;
import com.axelor.apps.account.service.InvoiceVisibilityService;
import com.axelor.apps.account.service.PaymentSessionService;
import com.axelor.apps.account.service.ReconcileService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentCreateService;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Blocking;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.BlockingRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.tool.ContextTool;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import com.axelor.db.Query;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.axelor.rpc.Context;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import com.google.inject.servlet.RequestScoped;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

@RequestScoped
public class InvoiceTermServiceImpl implements InvoiceTermService {

  protected InvoiceTermRepository invoiceTermRepo;
  protected InvoiceRepository invoiceRepo;
  protected AppAccountService appAccountService;
  protected InvoiceToolService invoiceToolService;
  protected InvoiceVisibilityService invoiceVisibilityService;
  protected AccountConfigService accountConfigService;
  protected ReconcileService reconcileService;
  protected InvoicePaymentCreateService invoicePaymentCreateService;

  @Inject
  public InvoiceTermServiceImpl(
      InvoiceTermRepository invoiceTermRepo,
      InvoiceRepository invoiceRepo,
      AppAccountService appAccountService,
      InvoiceToolService invoiceToolService,
      InvoiceVisibilityService invoiceVisibilityService,
      AccountConfigService accountConfigService,
      ReconcileService reconcileService,
      InvoicePaymentCreateService invoicePaymentCreateService) {
    this.invoiceTermRepo = invoiceTermRepo;
    this.invoiceRepo = invoiceRepo;
    this.appAccountService = appAccountService;
    this.invoiceToolService = invoiceToolService;
    this.invoiceVisibilityService = invoiceVisibilityService;
    this.accountConfigService = accountConfigService;
    this.reconcileService = reconcileService;
    this.invoicePaymentCreateService = invoicePaymentCreateService;
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
        sum =
            sum.add(
                invoiceTerm.getAmount().divide(invoice.getInTaxTotal(), 10, RoundingMode.HALF_UP));
      }
    }
    return sum.multiply(BigDecimal.valueOf(100));
  }

  protected BigDecimal computePercentageSum(MoveLine moveLine) {
    BigDecimal sum = BigDecimal.ZERO;
    BigDecimal total = getTotalInvoiceTermsAmount(moveLine);
    Move move = moveLine.getMove();

    if (CollectionUtils.isNotEmpty(moveLine.getInvoiceTermList())) {
      for (InvoiceTerm invoiceTerm : moveLine.getInvoiceTermList()) {
        sum = sum.add(this.computeCustomizedPercentageUnscaled(invoiceTerm.getAmount(), total));
      }
    }
    if (move != null && move.getMoveLineList() != null) {
      for (MoveLine moveLineIt : move.getMoveLineList()) {
        if (!moveLineIt.equals(moveLine)
            && moveLineIt.getAccount() != null
            && moveLineIt.getAccount().getHasInvoiceTerm()
            && moveLineIt.getInvoiceTermList() != null) {
          for (InvoiceTerm invoiceTerm : moveLineIt.getInvoiceTermList()) {
            sum = sum.add(this.computeCustomizedPercentageUnscaled(invoiceTerm.getAmount(), total));
          }
        }
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
  public InvoiceTerm computeInvoiceTerm(
      Invoice invoice, PaymentConditionLine paymentConditionLine) {
    BigDecimal amount =
        invoice
            .getInTaxTotal()
            .multiply(paymentConditionLine.getPaymentPercentage())
            .divide(
                BigDecimal.valueOf(100),
                AppBaseService.DEFAULT_NB_DECIMAL_DIGITS,
                RoundingMode.HALF_UP);

    User pfpUser = null;
    if (getPfpValidatorUserCondition(invoice)) {
      pfpUser = getPfpValidatorUser(invoice.getPartner(), invoice.getCompany());
    }

    InvoiceTerm invoiceTerm =
        this.createInvoiceTerm(
            invoice,
            null,
            invoice.getBankDetails(),
            pfpUser,
            invoice.getPaymentMode(),
            null,
            null,
            amount,
            paymentConditionLine.getPaymentPercentage(),
            paymentConditionLine
                    .getPaymentCondition()
                    .getPaymentConditionLineList()
                    .indexOf(paymentConditionLine)
                + 1,
            paymentConditionLine.getIsHoldback());

    invoiceTerm.setPaymentConditionLine(paymentConditionLine);
    this.computeFinancialDiscount(invoiceTerm, invoice);

    return invoiceTerm;
  }

  protected void computeFinancialDiscount(InvoiceTerm invoiceTerm, Invoice invoice) {
    this.computeFinancialDiscount(
        invoiceTerm,
        invoice.getInTaxTotal(),
        invoice.getFinancialDiscount(),
        invoice.getFinancialDiscountTotalAmount(),
        invoice.getRemainingAmountAfterFinDiscount());
  }

  @Override
  public void computeFinancialDiscount(
      InvoiceTerm invoiceTerm,
      BigDecimal totalAmount,
      FinancialDiscount financialDiscount,
      BigDecimal financialDiscountAmount,
      BigDecimal remainingAmountAfterFinDiscount) {
    if (appAccountService.getAppAccount().getManageFinancialDiscount()
        && financialDiscount != null) {
      BigDecimal percentage =
          this.computeCustomizedPercentageUnscaled(invoiceTerm.getAmount(), totalAmount)
              .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);

      invoiceTerm.setApplyFinancialDiscount(true);
      invoiceTerm.setFinancialDiscount(financialDiscount);
      invoiceTerm.setFinancialDiscountDeadlineDate(
          this.computeFinancialDiscountDeadlineDate(invoiceTerm));
      invoiceTerm.setFinancialDiscountAmount(
          financialDiscountAmount.multiply(percentage).setScale(2, RoundingMode.HALF_UP));
      invoiceTerm.setRemainingAmountAfterFinDiscount(
          remainingAmountAfterFinDiscount.multiply(percentage).setScale(2, RoundingMode.HALF_UP));
      this.computeAmountRemainingAfterFinDiscount(invoiceTerm);

      invoiceTerm.setFinancialDiscountDeadlineDate(
          this.computeFinancialDiscountDeadlineDate(invoiceTerm));
    } else {
      invoiceTerm.setApplyFinancialDiscount(false);
      invoiceTerm.setFinancialDiscount(null);
      invoiceTerm.setFinancialDiscountDeadlineDate(null);
      invoiceTerm.setFinancialDiscountAmount(BigDecimal.ZERO);
      invoiceTerm.setRemainingAmountAfterFinDiscount(BigDecimal.ZERO);
      invoiceTerm.setAmountRemainingAfterFinDiscount(BigDecimal.ZERO);
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

    BigDecimal invoiceTermPercentage = new BigDecimal(100);
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
      findInvoiceTermsInInvoice(invoice.getMove().getMoveLineList(), invoiceTerm, invoice);
    }

    return invoiceTerm;
  }

  @Override
  public InvoiceTerm initCustomizedInvoiceTerm(
      MoveLine moveLine, InvoiceTerm invoiceTerm, Move move) {
    if (move != null) {
      invoiceTerm.setInvoice(move.getInvoice());
      invoiceTerm.setPaymentMode(move.getPaymentMode());
      invoiceTerm.setBankDetails(move.getPartnerBankDetails());
    }

    invoiceTerm.setSequence(initInvoiceTermsSequence(moveLine));

    invoiceTerm.setIsCustomized(true);
    invoiceTerm.setIsPaid(false);
    BigDecimal invoiceTermPercentage = new BigDecimal(100);
    BigDecimal percentageSum = computePercentageSum(moveLine);

    if (percentageSum.compareTo(BigDecimal.ZERO) > 0) {
      invoiceTermPercentage = new BigDecimal(100).subtract(percentageSum);
    }

    invoiceTerm.setPercentage(
        invoiceTermPercentage.setScale(
            AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP));

    BigDecimal amount = getTotalInvoiceTermsAmount(moveLine);

    amount =
        amount
            .multiply(invoiceTermPercentage)
            .divide(
                BigDecimal.valueOf(100),
                AppBaseService.DEFAULT_NB_DECIMAL_DIGITS,
                RoundingMode.HALF_UP);
    invoiceTerm.setAmount(amount);
    invoiceTerm.setAmountRemaining(amount);

    if (move != null
        && move.getPaymentCondition() != null
        && CollectionUtils.isNotEmpty(move.getPaymentCondition().getPaymentConditionLineList())) {
      PaymentConditionLine nextPaymentConditionLine =
          move.getPaymentCondition().getPaymentConditionLineList().stream()
              .filter(it -> it.getPaymentPercentage().compareTo(invoiceTerm.getPercentage()) == 0)
              .findFirst()
              .orElse(
                  move.getPaymentCondition()
                      .getPaymentConditionLineList()
                      .get(moveLine.getInvoiceTermList().size()));

      invoiceTerm.setDueDate(this.computeDueDate(move, nextPaymentConditionLine));

      if (nextPaymentConditionLine.getIsHoldback()) {
        invoiceTerm.setIsHoldBack(true);
      }
    }

    return invoiceTerm;
  }

  @Override
  public LocalDate computeDueDate(Move move, PaymentConditionLine paymentConditionLine) {
    return InvoiceToolService.getDueDate(
        paymentConditionLine, Optional.of(move).map(Move::getOriginDate).orElse(move.getDate()));
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
    if (invoiceTerm.getDueDate() == null || invoiceTerm.getFinancialDiscount() == null) {
      return null;
    }

    LocalDate deadlineDate =
        invoiceTerm.getDueDate().minusDays(invoiceTerm.getFinancialDiscount().getDiscountDelay());

    if (invoiceTerm.getInvoice() != null && invoiceTerm.getInvoice().getInvoiceDate() != null) {
      LocalDate invoiceDate = invoiceTerm.getInvoice().getInvoiceDate();
      deadlineDate = deadlineDate.isBefore(invoiceDate) ? invoiceDate : deadlineDate;
    } else if (invoiceTerm.getMoveLine() != null && invoiceTerm.getMoveLine().getDate() != null) {
      LocalDate moveDate = invoiceTerm.getMoveLine().getDate();
      deadlineDate = deadlineDate.isBefore(moveDate) ? moveDate : deadlineDate;
    }

    return deadlineDate;
  }

  @Override
  public void initInvoiceTermsSequence(Invoice invoice) {
    if (invoice == null) {
      return;
    }
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
  public List<InvoiceTerm> getUnpaidInvoiceTerms(Invoice invoice) throws AxelorException {
    String queryStr =
        "self.invoice = :invoice AND (self.isPaid IS NOT TRUE OR self.amountRemaining > 0)";
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

    return this.filterNotAwaitingPayment(invoiceTermQuery.order("dueDate").fetch());
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
  public List<InvoiceTerm> getUnpaidInvoiceTermsFiltered(Invoice invoice) throws AxelorException {

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
        || ObjectUtils.isEmpty(invoice.getInvoiceTermList())
        || (invoice.getInTaxTotal().signum() == 0
            && invoice.getStatusSelect() == InvoiceRepository.STATUS_DRAFT
            && !ObjectUtils.isEmpty(invoice.getInvoiceLineList()))
        || ObjectUtils.isEmpty(invoice.getInvoiceLineList())
        || invoice.getAmountRemaining().signum() > 0) {
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
            .bind("partnerTypeClient", PaymentSessionRepository.PARTNER_TYPE_CUSTOMER)
            .bind("partnerTypeSupplier", PaymentSessionRepository.PARTNER_TYPE_SUPPLIER)
            .bind("functionalOriginClient", MoveRepository.FUNCTIONAL_ORIGIN_SALE)
            .bind("functionalOriginSupplier", MoveRepository.FUNCTIONAL_ORIGIN_PURCHASE)
            .bind("activatePfp", appAccountService.getAppAccount().getActivatePassedForPayment())
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
          invoiceTermRepo.save(invoiceTerm);
        });

    Beans.get(PaymentSessionService.class).computeTotalPaymentSession(paymentSession);
  }

  protected String retrieveEligibleTermsQuery() {
    String generalCondition =
        "self.moveLine.move.company = :company "
            + " AND self.dueDate <= :paymentDatePlusMargin "
            + " AND self.moveLine.move.currency = :currency "
            + " AND self.bankDetails IS NOT NULL "
            + " AND self.paymentMode = :paymentMode"
            + " AND self.moveLine.account.isRetrievedOnPaymentSession IS TRUE ";

    String termsMoveLineCondition =
        " AND ((self.moveLine.partner.isCustomer IS TRUE "
            + " AND :partnerTypeSelect = :partnerTypeClient"
            + " AND self.moveLine.move.functionalOriginSelect = :functionalOriginClient)"
            + " OR (self.moveLine.partner.isSupplier IS TRUE "
            + " AND :partnerTypeSelect = :partnerTypeSupplier "
            + " AND self.moveLine.move.functionalOriginSelect = :functionalOriginSupplier "
            + " AND (:activatePfp IS FALSE "
            + " OR self.moveLine.move.company.accountConfig.isManagePassedForPayment IS FALSE  "
            + " OR self.pfpValidateStatusSelect IN (:pfpValidateStatusValidated, :pfpValidateStatusPartiallyValidated)))) ";

    String paymentHistoryCondition = " AND self.isPaid = FALSE" + " AND self.amountRemaining > 0";

    return generalCondition + termsMoveLineCondition + paymentHistoryCondition;
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
    LocalDate paymentDate = paymentSession.getPaymentDate();
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

    computeAmountPaid(invoiceTerm);
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
  public BigDecimal getAmountRemaining(InvoiceTerm invoiceTerm, LocalDate date) {
    return Optional.of(
            invoiceTerm.getApplyFinancialDiscount()
                    && invoiceTerm.getFinancialDiscountDeadlineDate() != null
                    && !invoiceTerm.getFinancialDiscountDeadlineDate().isBefore(date)
                ? invoiceTerm.getAmountRemainingAfterFinDiscount()
                : invoiceTerm.getAmountRemaining())
        .orElse(BigDecimal.ZERO);
  }

  @Override
  public BigDecimal getCustomizedAmount(InvoiceTerm invoiceTerm, BigDecimal total) {
    return invoiceTerm
        .getPercentage()
        .multiply(total)
        .divide(
            new BigDecimal(100), AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP);
  }

  @Override
  public BigDecimal computeCustomizedPercentage(BigDecimal amount, BigDecimal inTaxTotal) {
    return this.computeCustomizedPercentageUnscaled(amount, inTaxTotal)
        .setScale(AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP);
  }

  @Override
  public BigDecimal computeCustomizedPercentageUnscaled(BigDecimal amount, BigDecimal inTaxTotal) {
    BigDecimal percentage = BigDecimal.ZERO;
    if (inTaxTotal.compareTo(BigDecimal.ZERO) != 0) {
      percentage =
          amount.multiply(new BigDecimal(100)).divide(inTaxTotal, 10, RoundingMode.HALF_UP);
    }
    return percentage;
  }

  @Override
  public InvoiceTerm createInvoiceTerm(
      MoveLine moveLine,
      BankDetails bankDetails,
      User pfpUser,
      PaymentMode paymentMode,
      LocalDate date,
      BigDecimal amount,
      int sequence) {
    return this.createInvoiceTerm(
        null,
        moveLine,
        bankDetails,
        pfpUser,
        paymentMode,
        date,
        null,
        amount,
        BigDecimal.valueOf(100),
        sequence,
        false);
  }

  @Override
  public InvoiceTerm createInvoiceTerm(
      Invoice invoice,
      MoveLine moveLine,
      BankDetails bankDetails,
      User pfpUser,
      PaymentMode paymentMode,
      LocalDate date,
      LocalDate estimatedPaymentDate,
      BigDecimal amount,
      BigDecimal percentage,
      int sequence,
      boolean isHoldBack) {
    InvoiceTerm newInvoiceTerm = new InvoiceTerm();

    newInvoiceTerm.setSequence(sequence);
    newInvoiceTerm.setInvoice(invoice);
    newInvoiceTerm.setIsCustomized(false);
    newInvoiceTerm.setIsPaid(false);
    newInvoiceTerm.setDueDate(date);
    newInvoiceTerm.setIsHoldBack(isHoldBack);
    newInvoiceTerm.setEstimatedPaymentDate(estimatedPaymentDate);
    newInvoiceTerm.setAmount(amount);
    newInvoiceTerm.setAmountRemaining(amount);
    newInvoiceTerm.setPaymentMode(paymentMode);
    newInvoiceTerm.setBankDetails(bankDetails);
    newInvoiceTerm.setPfpValidateStatusSelect(InvoiceTermRepository.PFP_STATUS_AWAITING);
    newInvoiceTerm.setPfpValidatorUser(pfpUser);
    newInvoiceTerm.setInitialPfpAmount(BigDecimal.ZERO);
    newInvoiceTerm.setRemainingPfpAmount(BigDecimal.ZERO);
    newInvoiceTerm.setPercentage(percentage);

    this.setParentFields(newInvoiceTerm, moveLine, invoice);

    if (moveLine != null) {
      moveLine.addInvoiceTermListItem(newInvoiceTerm);
    }

    return newInvoiceTerm;
  }

  public void setParentFields(InvoiceTerm invoiceTerm, MoveLine moveLine, Invoice invoice) {
    if (invoice != null) {
      invoiceTerm.setCompany(invoice.getCompany());
      invoiceTerm.setPartner(invoice.getPartner());
      invoiceTerm.setCurrency(invoice.getCurrency());

      if (StringUtils.isEmpty(invoice.getSupplierInvoiceNb())) {
        invoiceTerm.setOrigin(invoice.getInvoiceId());
      } else {
        invoiceTerm.setOrigin(invoice.getSupplierInvoiceNb());
      }

      if (invoice.getOriginDate() != null) {
        invoiceTerm.setOriginDate(invoice.getOriginDate());
      }
    } else if (moveLine != null) {
      invoiceTerm.setCompany(moveLine.getMove().getCompany());
      invoiceTerm.setCurrency(moveLine.getMove().getCurrency());
      invoiceTerm.setOrigin(moveLine.getOrigin());

      if (moveLine.getPartner() != null) {
        invoiceTerm.setPartner(moveLine.getPartner());
      } else {
        invoiceTerm.setPartner(moveLine.getMove().getPartner());
      }
    }

    if (moveLine != null && invoiceTerm.getOriginDate() == null) {
      invoiceTerm.setOriginDate(moveLine.getMove().getOriginDate());
    }
  }

  public void setPaymentAmount(InvoiceTerm invoiceTerm) {
    if (invoiceTerm.getInvoice() != null && invoiceTerm.getInvoice().getCompany() != null) {
      if (this.getIsSignedNegative(invoiceTerm)) {
        invoiceTerm.setPaymentAmount(invoiceTerm.getAmountRemaining().negate());
      } else {
        invoiceTerm.setPaymentAmount(invoiceTerm.getAmountRemaining());
      }
    }
  }

  @Override
  @Transactional
  public void toggle(InvoiceTerm invoiceTerm, boolean value) throws AxelorException {
    if (invoiceTerm != null) {
      invoiceTerm.setIsSelectedOnPaymentSession(value);
      setPaymentAmount(invoiceTerm);
      computeAmountPaid(invoiceTerm);
      invoiceTermRepo.save(invoiceTerm);
    }
  }

  @Override
  public void computeAmountPaid(InvoiceTerm invoiceTerm) {
    if (invoiceTerm.getIsSelectedOnPaymentSession()) {
      if (invoiceTerm.getApplyFinancialDiscountOnPaymentSession()) {
        BigDecimal financialDiscountAmount =
            invoiceTerm.getPaymentAmount().compareTo(BigDecimal.ZERO) < 0
                ? invoiceTerm.getFinancialDiscountAmount()
                : invoiceTerm.getFinancialDiscountAmount().negate();
        invoiceTerm.setAmountPaid(invoiceTerm.getPaymentAmount().add(financialDiscountAmount));
      } else {
        invoiceTerm.setAmountPaid(invoiceTerm.getPaymentAmount());
      }
    } else {
      invoiceTerm.setAmountPaid(BigDecimal.ZERO);
    }
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void reconcileAndUpdateInvoiceTermsAmounts(
      InvoiceTerm invoiceTermFromInvoice, InvoiceTerm invoiceTermFromRefund)
      throws AxelorException {
    BigDecimal reconciledAmount =
        invoiceTermFromInvoice.getAmountRemaining().min(invoiceTermFromRefund.getAmountRemaining());

    MoveLine creditMoveLine = null;
    MoveLine debitMoveLine = null;
    if (invoiceTermFromInvoice.getMoveLine().getMove().getFunctionalOriginSelect()
        == MoveRepository.FUNCTIONAL_ORIGIN_SALE) {
      creditMoveLine = invoiceTermFromRefund.getMoveLine();
      debitMoveLine = invoiceTermFromInvoice.getMoveLine();
    } else if (invoiceTermFromInvoice.getMoveLine().getMove().getFunctionalOriginSelect()
        == MoveRepository.FUNCTIONAL_ORIGIN_PURCHASE) {
      creditMoveLine = invoiceTermFromInvoice.getMoveLine();
      debitMoveLine = invoiceTermFromRefund.getMoveLine();
    }
    Reconcile invoiceTermsReconcile =
        reconcileService.createReconcile(debitMoveLine, creditMoveLine, reconciledAmount, true);

    reconcileService.confirmReconcile(invoiceTermsReconcile, false, false);

    updateInvoiceTermsAmounts(
        invoiceTermFromInvoice,
        reconciledAmount,
        invoiceTermsReconcile,
        invoiceTermFromRefund.getMoveLine().getMove());
    updateInvoiceTermsAmounts(
        invoiceTermFromRefund,
        reconciledAmount,
        invoiceTermsReconcile,
        invoiceTermFromInvoice.getMoveLine().getMove());
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public List<InvoiceTerm> reconcileMoveLineInvoiceTermsWithFullRollBack(
      List<InvoiceTerm> invoiceTermList) throws AxelorException {
    List<Partner> partnerList = getPartnersFromInvoiceTermList(invoiceTermList);

    for (Partner partner : partnerList) {

      List<InvoiceTerm> invoiceTermFromInvoiceList =
          getInvoiceTermsInvoiceOrRefundSortedByDueDateAndByPartner(invoiceTermList, partner, true);
      List<InvoiceTerm> invoiceTermFromRefundList =
          getInvoiceTermsInvoiceOrRefundSortedByDueDateAndByPartner(
              invoiceTermList, partner, false);
      int invoiceCounter = 0;
      int refundCounter = 0;
      InvoiceTerm invoiceTermFromInvoice = null;
      InvoiceTerm invoiceTermFromRefund = null;
      while (!ObjectUtils.isEmpty(invoiceTermFromRefundList)
          && !ObjectUtils.isEmpty(invoiceTermFromInvoiceList)
          && invoiceCounter < invoiceTermFromInvoiceList.size()
          && refundCounter < invoiceTermFromRefundList.size()) {
        invoiceTermFromInvoice = invoiceTermFromInvoiceList.get(invoiceCounter);
        invoiceTermFromRefund = invoiceTermFromRefundList.get(refundCounter);
        this.reconcileAndUpdateInvoiceTermsAmounts(invoiceTermFromInvoice, invoiceTermFromRefund);
        if (invoiceTermFromInvoice.getAmountRemaining().signum() == 0) {
          invoiceTermFromInvoice.setIsPaid(true);
          invoiceCounter++;
        }
        if (invoiceTermFromRefund.getAmountRemaining().signum() == 0) {
          invoiceTermFromRefund.setIsPaid(true);
          refundCounter++;
        }
      }
    }
    return invoiceTermList;
  }

  protected List<Partner> getPartnersFromInvoiceTermList(List<InvoiceTerm> invoiceTermList) {
    return invoiceTermList.stream()
        .map(it -> it.getMoveLine().getPartner())
        .distinct()
        .collect(Collectors.toList());
  }

  protected List<InvoiceTerm> getInvoiceTermsInvoiceOrRefundSortedByDueDateAndByPartner(
      List<InvoiceTerm> invoiceTermList, Partner partner, boolean isInvoice) {
    return invoiceTermList.stream()
        .filter(
            it ->
                ((it.getAmountPaid().signum() > 0 && isInvoice)
                        || (it.getAmountPaid().signum() < 0 && !isInvoice))
                    && it.getMoveLine().getPartner().equals(partner))
        .sorted(Comparator.comparing(InvoiceTerm::getDueDate))
        .collect(Collectors.toList());
  }

  protected InvoiceTerm updateInvoiceTermsAmounts(
      InvoiceTerm invoiceTerm, BigDecimal amount, Reconcile reconcile, Move move)
      throws AxelorException {

    InvoicePayment invoicePayment =
        invoicePaymentCreateService.createInvoicePayment(invoiceTerm.getInvoice(), amount, move);
    invoicePayment.addReconcileListItem(reconcile);

    List<InvoiceTerm> invoiceTermList = new ArrayList<InvoiceTerm>();

    invoiceTermList.add(invoiceTerm);

    reconcileService.updateInvoiceTerms(invoiceTermList, invoicePayment, amount, reconcile);

    invoiceTerm = updateInvoiceTermsAmountsSessiontPart(invoiceTerm);
    return invoiceTerm;
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

  protected InvoiceTerm updateInvoiceTermsAmountsSessiontPart(InvoiceTerm invoiceTerm) {
    boolean isSignedNegative = this.getIsSignedNegative(invoiceTerm);

    if (isSignedNegative) {
      invoiceTerm.setPaymentAmount(invoiceTerm.getAmountRemaining().negate());

    } else {
      invoiceTerm.setPaymentAmount(invoiceTerm.getAmountRemaining());
    }
    this.computeAmountPaid(invoiceTerm);

    return invoiceTerm;
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

  public boolean isEnoughAmountToPay(
      List<InvoiceTerm> invoiceTermList, BigDecimal amount, LocalDate date) {
    BigDecimal amountToPay =
        invoiceTermList.stream()
            .filter(this::isNotReadonly)
            .map(it -> this.getAmountRemaining(it, date))
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO);

    return amountToPay.compareTo(amount) >= 0;
  }

  @Override
  public BigDecimal computeParentTotal(Context context) {
    BigDecimal total = BigDecimal.ZERO;
    if (context.getParent() != null) {
      Invoice invoice = ContextTool.getContextParent(context, Invoice.class, 1);
      if (invoice != null) {
        total = invoice.getInTaxTotal();
      } else {
        MoveLine moveLine = ContextTool.getContextParent(context, MoveLine.class, 1);
        if (moveLine != null) {
          total = moveLine.getDebit().max(moveLine.getCredit());
        }
      }
    }
    return total;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void roundPercentages(List<InvoiceTerm> invoiceTermList, BigDecimal total) {
    boolean isSubtract = true;

    for (InvoiceTerm invoiceTerm : invoiceTermList) {
      if (this.isUnevenRounding(invoiceTerm, total)) {
        if (isSubtract) {
          invoiceTerm.setPercentage(invoiceTerm.getPercentage().subtract(BigDecimal.valueOf(0.01)));
        }

        isSubtract = !isSubtract;
      }
    }
  }

  protected boolean isUnevenRounding(InvoiceTerm invoiceTerm, BigDecimal total) {
    BigDecimal percentageUp =
        invoiceTerm
            .getAmount()
            .multiply(BigDecimal.valueOf(100))
            .divide(total, AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP);
    BigDecimal percentageDown =
        invoiceTerm
            .getAmount()
            .multiply(BigDecimal.valueOf(100))
            .divide(total, AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_DOWN);

    return percentageUp.compareTo(percentageDown) != 0;
  }

  @Override
  public User getPfpValidatorUser(Partner partner, Company company) {
    AccountingSituation accountingSituation =
        Beans.get(AccountingSituationService.class).getAccountingSituation(partner, company);
    if (accountingSituation == null) {
      return null;
    }
    return accountingSituation.getPfpValidatorUser();
  }

  @Override
  public String getPfpValidatorUserDomain(Partner partner, Company company) {

    User pfpValidatorUser = getPfpValidatorUser(partner, company);
    if (pfpValidatorUser == null) {
      return "self.id in (0)";
    }
    List<SubstitutePfpValidator> substitutePfpValidatorList =
        pfpValidatorUser.getSubstitutePfpValidatorList();
    List<User> validPfpValidatorUserList = new ArrayList<>();
    StringBuilder pfpValidatorUserDomain = new StringBuilder("self.id in ");
    LocalDate todayDate = appAccountService.getTodayDate(company);

    validPfpValidatorUserList.add(pfpValidatorUser);

    for (SubstitutePfpValidator substitutePfpValidator : substitutePfpValidatorList) {
      LocalDate substituteStartDate = substitutePfpValidator.getSubstituteStartDate();
      LocalDate substituteEndDate = substitutePfpValidator.getSubstituteEndDate();

      if (substituteStartDate == null) {
        if (substituteEndDate == null || substituteEndDate.isAfter(todayDate)) {
          validPfpValidatorUserList.add(substitutePfpValidator.getSubstitutePfpValidatorUser());
        }
      } else {
        if (substituteEndDate == null && substituteStartDate.isBefore(todayDate)) {
          validPfpValidatorUserList.add(substitutePfpValidator.getSubstitutePfpValidatorUser());
        } else if (substituteStartDate.isBefore(todayDate)
            && substituteEndDate.isAfter(todayDate)) {
          validPfpValidatorUserList.add(substitutePfpValidator.getSubstitutePfpValidatorUser());
        }
      }
    }

    pfpValidatorUserDomain
        .append("(")
        .append(
            validPfpValidatorUserList.stream()
                .map(pfpValidator -> pfpValidator.getId().toString())
                .collect(Collectors.joining(",")))
        .append(")");
    return pfpValidatorUserDomain.toString();
  }

  protected void findInvoiceTermsInInvoice(
      List<MoveLine> moveLineList, InvoiceTerm invoiceTerm, Invoice invoice) {
    MoveLine moveLine = getExistingInvoiceTermMoveLine(invoice);
    if (moveLine == null && !CollectionUtils.isEmpty(moveLineList)) {
      for (MoveLine ml : moveLineList) {
        if (ml.getAccount().getHasInvoiceTerm()) {
          ml.addInvoiceTermListItem(invoiceTerm);
          return;
        }
      }
    } else {
      moveLine.addInvoiceTermListItem(invoiceTerm);
    }
  }

  public BigDecimal getTotalInvoiceTermsAmount(MoveLine moveLine) {
    return this.getTotalInvoiceTermsAmount(moveLine, null, true);
  }

  public BigDecimal getTotalInvoiceTermsAmount(
      MoveLine moveLine, Account holdbackAccount, boolean holdback) {
    Move move = moveLine.getMove();
    BigDecimal total = moveLine.getDebit().max(moveLine.getCredit());
    if (move != null && move.getMoveLineList() != null) {
      for (MoveLine moveLineIt : move.getMoveLineList()) {
        if (!moveLineIt.equals(moveLine)
            && moveLineIt.getCredit().signum() == moveLine.getCredit().signum()
            && moveLineIt.getAccount() != null
            && moveLineIt.getAccount().getHasInvoiceTerm()
            && (holdback || !moveLineIt.getAccount().equals(holdbackAccount))) {
          total = total.add(moveLineIt.getDebit().max(moveLineIt.getCredit()));
        }
      }
    }
    return total;
  }

  @Override
  public void updateFromMoveHeader(Move move, InvoiceTerm invoiceTerm) {
    invoiceTerm.setPaymentMode(move.getPaymentMode());
    invoiceTerm.setBankDetails(move.getPartnerBankDetails());
  }

  @Override
  public boolean isNotReadonly(InvoiceTerm invoiceTerm) {
    return !invoiceTerm.getIsPaid()
        && invoiceTerm.getAmount().compareTo(invoiceTerm.getAmountRemaining()) == 0
        && this.isNotAwaitingPayment(invoiceTerm)
        && invoiceTerm.getPfpValidateStatusSelect() == InvoiceTermRepository.PFP_STATUS_AWAITING;
  }

  public LocalDate getDueDate(List<InvoiceTerm> invoiceTermList, LocalDate defaultDate) {
    if (invoiceTermList == null) {
      return defaultDate;
    }
    return invoiceTermList.stream()
        .map(InvoiceTerm::getDueDate)
        .max(LocalDate::compareTo)
        .orElse(defaultDate);
  }
}
