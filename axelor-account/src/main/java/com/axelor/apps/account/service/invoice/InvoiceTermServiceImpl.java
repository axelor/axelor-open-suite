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
package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountConfig;
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
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.SubstitutePfpValidator;
import com.axelor.apps.account.db.Tax;
import com.axelor.apps.account.db.repo.FinancialDiscountRepository;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.InvoiceTermRepository;
import com.axelor.apps.account.db.repo.JournalTypeRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.AccountingSituationService;
import com.axelor.apps.account.service.InvoiceVisibilityService;
import com.axelor.apps.account.service.ReconcileService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentCreateService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import com.axelor.db.Query;
import com.axelor.inject.Beans;
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
import java.util.Objects;
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
    return invoice.getInTaxTotal().compareTo(totalAmount) == 0;
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
    return sum.multiply(BigDecimal.valueOf(100))
        .setScale(AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP);
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
            && moveLineIt.getAccount().getUseForPartnerBalance()
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
        this.computeCompanyAmounts(invoiceTerm, false, false);
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
            null,
            invoice.getBankDetails(),
            pfpUser,
            invoice.getPaymentMode(),
            null,
            null,
            amount,
            paymentConditionLine.getPaymentPercentage(),
            paymentConditionLine.getSequence() + 1,
            paymentConditionLine.getIsHoldback());

    invoiceTerm.setPaymentConditionLine(paymentConditionLine);
    this.computeFinancialDiscount(invoiceTerm, invoice);

    return invoiceTerm;
  }

  @Override
  public void computeCompanyAmounts(InvoiceTerm invoiceTerm, boolean isUpdate, boolean isHoldback) {
    BigDecimal invoiceTermAmount = invoiceTerm.getAmount();
    BigDecimal invoiceTermAmountRemaining = invoiceTerm.getAmountRemaining();
    BigDecimal companyAmount = invoiceTermAmount;
    BigDecimal companyAmountRemaining = invoiceTermAmountRemaining;
    MoveLine moveLine = invoiceTerm.getMoveLine();
    Invoice invoice = invoiceTerm.getInvoice();
    BigDecimal ratioPaid = BigDecimal.ONE;

    if (invoiceTermAmount.signum() != 0 && this.isMultiCurrency(invoiceTerm)) {
      BigDecimal companyTotal =
          invoice != null
              ? invoice.getCompanyInTaxTotal()
              : moveLine.getDebit().max(moveLine.getCredit());

      if (!isUpdate) {
        ratioPaid =
            invoiceTermAmountRemaining.divide(
                invoiceTermAmount, AppBaseService.COMPUTATION_SCALING, RoundingMode.HALF_UP);
      }

      BigDecimal percentage = isHoldback ? BigDecimal.valueOf(100) : invoiceTerm.getPercentage();

      companyAmount =
          companyTotal
              .multiply(percentage)
              .divide(
                  BigDecimal.valueOf(100),
                  AppBaseService.COMPUTATION_SCALING,
                  RoundingMode.HALF_UP);
      companyAmountRemaining =
          companyAmount
              .multiply(ratioPaid)
              .setScale(AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP);
      companyAmount =
          companyAmount.setScale(AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP);
    }

    invoiceTerm.setCompanyAmount(companyAmount);
    invoiceTerm.setCompanyAmountRemaining(companyAmountRemaining);
  }

  @Override
  public void computeFinancialDiscount(InvoiceTerm invoiceTerm, Invoice invoice) {
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

  @Override
  public boolean getPfpValidatorUserCondition(Invoice invoice) {
    return appAccountService.getAppAccount().getActivatePassedForPayment()
        && (invoice.getCompany().getAccountConfig().getIsManagePassedForPayment()
            && (invoice.getOperationTypeSelect()
                    == InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE
                || (invoice.getCompany().getAccountConfig().getIsManagePFPInRefund()
                    && invoice.getOperationTypeSelect()
                        == InvoiceRepository.OPERATION_TYPE_SUPPLIER_REFUND)));
  }

  @Override
  public InvoiceTerm initCustomizedInvoiceTerm(Invoice invoice, InvoiceTerm invoiceTerm)
      throws AxelorException {

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
    this.computeCompanyAmounts(invoiceTerm, false, false);
    this.computeFinancialDiscount(invoiceTerm, invoice);

    if (invoice.getStatusSelect() == InvoiceRepository.STATUS_VENTILATED) {
      findInvoiceTermsInInvoice(invoice.getMove().getMoveLineList(), invoiceTerm, invoice);
    }
    invoiceTerm.setSequence(initInvoiceTermsSequence(invoice, invoiceTerm));

    return invoiceTerm;
  }

  @Override
  public InvoiceTerm initCustomizedInvoiceTerm(
      MoveLine moveLine, InvoiceTerm invoiceTerm, Move move) throws AxelorException {
    invoiceTerm.setMoveLine(moveLine);
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
    this.computeCompanyAmounts(invoiceTerm, false, false);

    if (move != null
        && move.getPaymentCondition() != null
        && CollectionUtils.isNotEmpty(move.getPaymentCondition().getPaymentConditionLineList())) {
      PaymentConditionLine nextPaymentConditionLine =
          move.getPaymentCondition().getPaymentConditionLineList().stream()
              .filter(it -> it.getPaymentPercentage().compareTo(invoiceTerm.getPercentage()) == 0)
              .findFirst()
              .orElse(
                  move.getPaymentCondition().getPaymentConditionLineList().size()
                          > moveLine.getInvoiceTermList().size()
                      ? move.getPaymentCondition()
                          .getPaymentConditionLineList()
                          .get(moveLine.getInvoiceTermList().size())
                      : null);

      if (nextPaymentConditionLine != null) {
        invoiceTerm.setDueDate(this.computeDueDate(move, nextPaymentConditionLine));

        if (nextPaymentConditionLine.getIsHoldback()) {
          invoiceTerm.setIsHoldBack(true);
        }
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
    if (moveLine == null || CollectionUtils.isEmpty(moveLine.getInvoiceTermList())) {
      return 1;
    }
    return moveLine.getInvoiceTermList().stream()
            .max(Comparator.comparing(InvoiceTerm::getSequence))
            .get()
            .getSequence()
        + 1;
  }

  protected int initInvoiceTermsSequence(Invoice invoice, InvoiceTerm invoiceTerm) {

    if (invoiceTerm == null
        || invoice == null
        || CollectionUtils.isEmpty(invoice.getInvoiceTermList())) {
      return 1;
    } else {
      return invoice.getInvoiceTermList().stream()
              .max(Comparator.comparing(InvoiceTerm::getSequence))
              .get()
              .getSequence()
          + 1;
    }
  }

  @Override
  public List<InvoiceTerm> getUnpaidInvoiceTerms(Invoice invoice) throws AxelorException {
    boolean pfpCondition = invoiceVisibilityService.getPfpCondition(invoice);

    return buildUnpaidInvoiceTermsQuery(invoice, pfpCondition);
  }

  @Override
  public List<InvoiceTerm> getUnpaidInvoiceTermsWithoutPfpCheck(Invoice invoice)
      throws AxelorException {

    return buildUnpaidInvoiceTermsQuery(invoice, false);
  }

  protected List<InvoiceTerm> buildUnpaidInvoiceTermsQuery(Invoice invoice, boolean pfpCondition) {
    String queryStr =
        "self.invoice = :invoice AND (self.isPaid IS NOT TRUE OR self.amountRemaining > 0)";

    if (pfpCondition) {
      queryStr =
          queryStr
              + " AND self.pfpValidateStatusSelect IN (:noPfp, :validated, :partiallyValidated)";
    }

    Query<InvoiceTerm> invoiceTermQuery =
        invoiceTermRepo.all().filter(queryStr).bind("invoice", invoice);

    if (pfpCondition) {
      invoiceTermQuery
          .bind("noPfp", InvoiceTermRepository.PFP_STATUS_NO_PFP)
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
  public List<InvoiceTerm> getUnpaidInvoiceTermsFilteredWithoutPfpCheck(Invoice invoice)
      throws AxelorException {

    return filterInvoiceTermsByHoldBack(getUnpaidInvoiceTermsWithoutPfpCheck(invoice));
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
      BigDecimal companyAmountRemaining;

      if (amountRemaining.compareTo(BigDecimal.ZERO) == 0) {
        companyAmountRemaining = BigDecimal.ZERO;
      } else {
        companyAmountRemaining =
            invoiceTerm
                .getCompanyAmountRemaining()
                .subtract(invoiceTermPayment.getCompanyPaidAmount());
      }

      if (amountRemaining.signum() <= 0) {
        amountRemaining = BigDecimal.ZERO;
        invoiceTerm.setIsPaid(true);
        Invoice invoice = invoiceTerm.getInvoice();
        if (invoice != null) {
          invoice.setDueDate(InvoiceToolService.getDueDate(invoice));
        }
      }

      invoiceTerm.setAmountRemaining(amountRemaining);
      invoiceTerm.setCompanyAmountRemaining(companyAmountRemaining);
      invoiceTerm.setPaymentMode(paymentMode);

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
      BigDecimal companyPaidAmount =
          invoiceTermPayment
              .getCompanyPaidAmount()
              .add(invoiceTermPayment.getFinancialDiscountAmount());

      invoiceTerm.setAmountRemaining(invoiceTerm.getAmountRemaining().add(paidAmount));
      invoiceTerm.setCompanyAmountRemaining(
          invoiceTerm.getCompanyAmountRemaining().add(companyPaidAmount));

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
  public List<InvoiceTerm> updateFinancialDiscount(Invoice invoice) {
    invoice.getInvoiceTermList().stream()
        .filter(it -> it.getAmountRemaining().compareTo(it.getAmount()) == 0)
        .forEach(it -> this.computeFinancialDiscount(it, invoice));

    return invoice.getInvoiceTermList();
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
  public BigDecimal getFinancialDiscountTaxAmount(InvoiceTerm invoiceTerm) throws AxelorException {
    if (invoiceTerm.getFinancialDiscount() != null
        && invoiceTerm.getFinancialDiscount().getDiscountBaseSelect()
            == FinancialDiscountRepository.DISCOUNT_BASE_VAT) {
      BigDecimal total = BigDecimal.ZERO;

      if (invoiceTerm.getInvoice() != null) {
        total = invoiceTerm.getInvoice().getTaxTotal();
      }

      if (total.signum() > 0) {
        return total
            .multiply(invoiceTerm.getPercentage())
            .multiply(invoiceTerm.getFinancialDiscount().getDiscountRate())
            .divide(BigDecimal.valueOf(10000), 2, RoundingMode.HALF_UP);
      } else {
        Tax financialDiscountTax =
            this.isPurchase(invoiceTerm)
                ? accountConfigService.getPurchFinancialDiscountTax(
                    accountConfigService.getAccountConfig(this.getCompany(invoiceTerm)))
                : accountConfigService.getSaleFinancialDiscountTax(
                    accountConfigService.getAccountConfig(this.getCompany(invoiceTerm)));
        BigDecimal taxRate = financialDiscountTax.getActiveTaxLine().getValue();

        return invoiceTerm
            .getFinancialDiscountAmount()
            .multiply(taxRate)
            .divide(
                BigDecimal.ONE
                    .add(taxRate.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP))
                    .multiply(BigDecimal.valueOf(100)),
                AppBaseService.DEFAULT_NB_DECIMAL_DIGITS,
                RoundingMode.HALF_UP);
      }
    } else {
      return BigDecimal.ZERO;
    }
  }

  protected Company getCompany(InvoiceTerm invoiceTerm) {
    return invoiceTerm.getInvoice() != null
        ? invoiceTerm.getInvoice().getCompany()
        : invoiceTerm.getMoveLine().getMove().getCompany();
  }

  protected boolean isPurchase(InvoiceTerm invoiceTerm) throws AxelorException {
    if (invoiceTerm.getInvoice() != null) {
      return InvoiceToolService.isPurchase(invoiceTerm.getInvoice());
    } else {
      return invoiceTerm
              .getMoveLine()
              .getMove()
              .getJournal()
              .getJournalType()
              .getTechnicalTypeSelect()
          == JournalTypeRepository.TECHNICAL_TYPE_SELECT_EXPENSE;
    }
  }

  @Override
  public BigDecimal getAmountRemaining(
      InvoiceTerm invoiceTerm, LocalDate date, boolean isCompanyCurrency) {
    BigDecimal amountRemaining;

    boolean applyFinancialDiscount =
        invoiceTerm.getApplyFinancialDiscount()
            && invoiceTerm.getFinancialDiscountDeadlineDate() != null
            && date != null
            && !invoiceTerm.getFinancialDiscountDeadlineDate().isBefore(date);
    if (applyFinancialDiscount) {
      amountRemaining = invoiceTerm.getAmountRemainingAfterFinDiscount();
    } else if (isCompanyCurrency) {
      amountRemaining = invoiceTerm.getCompanyAmountRemaining();
    } else {
      amountRemaining = invoiceTerm.getAmountRemaining();
    }
    return amountRemaining;
  }

  @Override
  public boolean setCustomizedAmounts(
      InvoiceTerm invoiceTerm, List<InvoiceTerm> invoiceTermList, BigDecimal total) {
    BigDecimal totalPercentage =
        invoiceTermList.stream()
            .map(InvoiceTerm::getPercentage)
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO)
            .add(invoiceTerm.getPercentage());
    boolean isLastInvoiceTerm = totalPercentage.compareTo(BigDecimal.valueOf(100)) == 0;

    BigDecimal companyTotal =
        invoiceTerm.getInvoice() != null
            ? invoiceTerm.getInvoice().getCompanyInTaxTotal()
            : invoiceTerm.getMoveLine().getDebit().max(invoiceTerm.getMoveLine().getCredit());

    BigDecimal customizedAmount =
        this.getCustomizedAmount(invoiceTerm, invoiceTermList, total, isLastInvoiceTerm);
    BigDecimal customizedCompanyAmount =
        this.getCustomizedCompanyAmount(
            invoiceTerm, invoiceTermList, companyTotal, isLastInvoiceTerm);

    invoiceTerm.setAmount(customizedAmount);
    invoiceTerm.setAmountRemaining(customizedAmount);

    if (customizedCompanyAmount != null) {
      invoiceTerm.setCompanyAmount(customizedCompanyAmount);
      invoiceTerm.setCompanyAmountRemaining(customizedCompanyAmount);
    }

    return customizedAmount.signum() > 0;
  }

  protected BigDecimal getCustomizedAmount(
      InvoiceTerm invoiceTerm,
      List<InvoiceTerm> invoiceTermList,
      BigDecimal total,
      boolean isLastInvoiceTerm) {
    if (isLastInvoiceTerm) {
      BigDecimal totalWithoutCurrent =
          invoiceTermList.stream()
              .map(InvoiceTerm::getAmount)
              .reduce(BigDecimal::add)
              .orElse(BigDecimal.ZERO);

      return total.subtract(totalWithoutCurrent);
    } else {
      return invoiceTerm
          .getPercentage()
          .multiply(total)
          .divide(
              new BigDecimal(100), AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP);
    }
  }

  protected BigDecimal getCustomizedCompanyAmount(
      InvoiceTerm invoiceTerm,
      List<InvoiceTerm> invoiceTermList,
      BigDecimal total,
      boolean isLastInvoiceTerm) {
    if (isLastInvoiceTerm) {
      BigDecimal totalWithoutCurrent =
          invoiceTermList.stream()
              .map(InvoiceTerm::getCompanyAmount)
              .reduce(BigDecimal::add)
              .orElse(BigDecimal.ZERO);

      return total.subtract(totalWithoutCurrent);
    } else {
      this.computeCompanyAmounts(invoiceTerm, true, false);

      return null;
    }
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
          amount
              .multiply(new BigDecimal(100))
              .divide(inTaxTotal, AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP);
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
      int sequence)
      throws AxelorException {
    return this.createInvoiceTerm(
        null,
        moveLine.getMove(),
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
      Move move,
      MoveLine moveLine,
      BankDetails bankDetails,
      User pfpUser,
      PaymentMode paymentMode,
      LocalDate date,
      LocalDate estimatedPaymentDate,
      BigDecimal amount,
      BigDecimal percentage,
      int sequence,
      boolean isHoldBack)
      throws AxelorException {
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
    newInvoiceTerm.setPfpValidatorUser(pfpUser);
    newInvoiceTerm.setInitialPfpAmount(BigDecimal.ZERO);
    newInvoiceTerm.setRemainingPfpAmount(BigDecimal.ZERO);
    newInvoiceTerm.setPercentage(percentage);

    this.setParentFields(newInvoiceTerm, move, moveLine, invoice);

    if (moveLine != null) {
      moveLine.addInvoiceTermListItem(newInvoiceTerm);
    }

    this.setPfpStatus(newInvoiceTerm, move);
    this.computeCompanyAmounts(newInvoiceTerm, false, isHoldBack);

    return newInvoiceTerm;
  }

  @Override
  public void setPfpStatus(InvoiceTerm invoiceTerm, Move move) throws AxelorException {
    Company company;
    boolean isSupplierPurchase, isSupplierRefund;

    if (invoiceTerm.getInvoice() != null) {
      Invoice invoice = invoiceTerm.getInvoice();

      company = invoice.getCompany();
      isSupplierPurchase =
          invoice.getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE;
      isSupplierRefund =
          invoice.getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_SUPPLIER_REFUND;
    } else {
      if (move == null) {
        move = invoiceTerm.getMoveLine().getMove();
      }

      company = move.getCompany();
      isSupplierPurchase =
          move.getJournal().getJournalType().getTechnicalTypeSelect()
              == JournalTypeRepository.TECHNICAL_TYPE_SELECT_EXPENSE;
      isSupplierRefund =
          move.getJournal().getJournalType().getTechnicalTypeSelect()
              == JournalTypeRepository.TECHNICAL_TYPE_SELECT_CREDIT_NOTE;
    }

    AccountConfig accountConfig = accountConfigService.getAccountConfig(company);

    if (accountConfig.getIsManagePassedForPayment()
        && (isSupplierPurchase || (isSupplierRefund && accountConfig.getIsManagePFPInRefund()))) {
      invoiceTerm.setPfpValidateStatusSelect(InvoiceTermRepository.PFP_STATUS_AWAITING);
    } else {
      invoiceTerm.setPfpValidateStatusSelect(InvoiceTermRepository.PFP_STATUS_NO_PFP);
    }
  }

  @Override
  public void setParentFields(
      InvoiceTerm invoiceTerm, Move move, MoveLine moveLine, Invoice invoice) {
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
      invoiceTerm.setOrigin(moveLine.getOrigin());

      if (move != null) {
        invoiceTerm.setCompany(move.getCompany());
        invoiceTerm.setCurrency(move.getCurrency());
      }

      if (moveLine.getPartner() != null) {
        invoiceTerm.setPartner(moveLine.getPartner());
      } else {
        invoiceTerm.setPartner(move.getPartner());
      }
    }

    if (moveLine != null && invoiceTerm.getOriginDate() == null) {
      invoiceTerm.setOriginDate(move.getOriginDate());
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
  @Transactional(rollbackOn = {Exception.class})
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

    if (invoiceTerm.getInvoice() != null) {
      InvoicePayment invoicePayment =
          invoicePaymentCreateService.createInvoicePayment(invoiceTerm.getInvoice(), amount, move);
      invoicePayment.setReconcile(reconcile);

      List<InvoiceTerm> invoiceTermList = new ArrayList<InvoiceTerm>();

      invoiceTermList.add(invoiceTerm);

      reconcileService.updateInvoiceTerms(invoiceTermList, invoicePayment, amount, reconcile);
    } else {
      invoiceTerm.setAmountRemaining(invoiceTerm.getAmountRemaining().subtract(amount));
    }

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
            .map(it -> this.getAmountRemaining(it, date, false))
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO);

    return amountToPay.compareTo(amount) >= 0;
  }

  @Override
  @Transactional
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
    if (total.compareTo(BigDecimal.ZERO) == 0) {
      return false;
    }

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
        if (ml.getAccount().getUseForPartnerBalance()) {
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
    BigDecimal total = moveLine.getCurrencyAmount().abs();

    if (move != null && move.getMoveLineList() != null) {
      for (MoveLine moveLineIt : move.getMoveLineList()) {
        if (!moveLineIt.getCounter().equals(moveLine.getCounter())
            && moveLineIt.getCredit().signum() == moveLine.getCredit().signum()
            && moveLineIt.getAccount() != null
            && moveLineIt.getAccount().getUseForPartnerBalance()
            && (holdback
                || (holdbackAccount != null && !moveLineIt.getAccount().equals(holdbackAccount)))) {
          total = total.add(moveLineIt.getCurrencyAmount());
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
    return this.isNotReadonlyExceptPfp(invoiceTerm)
        && invoiceTerm.getPfpValidateStatusSelect() <= InvoiceTermRepository.PFP_STATUS_AWAITING;
  }

  @Override
  public boolean isNotReadonlyExceptPfp(InvoiceTerm invoiceTerm) {
    return !invoiceTerm.getIsPaid()
        && invoiceTerm.getAmount().compareTo(invoiceTerm.getAmountRemaining()) == 0
        && this.isNotAwaitingPayment(invoiceTerm);
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

  @Override
  public void toggle(List<InvoiceTerm> invoiceTermList, boolean value) throws AxelorException {
    for (InvoiceTerm invoiceTerm : invoiceTermList) {
      toggle(invoiceTerm, value);
    }
  }

  @Override
  public BigDecimal roundUpLastInvoiceTerm(
      List<InvoiceTerm> invoiceTermList, BigDecimal total, boolean isCompanyAmount)
      throws AxelorException {
    BigDecimal invoiceTermTotal =
        invoiceTermList.stream()
            .map(it -> isCompanyAmount ? it.getCompanyAmount() : it.getAmount())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal diff =
        BigDecimal.valueOf(0.01)
            .multiply(BigDecimal.valueOf(total.subtract(invoiceTermTotal).signum()));

    InvoiceTerm lastInvoiceTerm = invoiceTermList.get(invoiceTermList.size() - 1);

    if (isCompanyAmount) {
      lastInvoiceTerm.setCompanyAmount(lastInvoiceTerm.getCompanyAmount().add(diff));
      lastInvoiceTerm.setCompanyAmountRemaining(
          lastInvoiceTerm.getCompanyAmountRemaining().add(diff));
    } else {
      lastInvoiceTerm.setAmount(lastInvoiceTerm.getAmount().add(diff));
      lastInvoiceTerm.setAmountRemaining(lastInvoiceTerm.getAmountRemaining().add(diff));
    }

    return invoiceTermTotal.add(diff);
  }

  protected Currency getCurrency(InvoiceTerm invoiceTerm) {
    if (invoiceTerm.getInvoice() != null) {
      return Optional.of(invoiceTerm.getInvoice()).map(Invoice::getCurrency).orElse(null);
    } else {
      return Optional.of(invoiceTerm.getMoveLine())
          .map(MoveLine::getMove)
          .map(Move::getCurrency)
          .orElse(null);
    }
  }

  protected Currency getCompanyCurrency(InvoiceTerm invoiceTerm) {
    if (invoiceTerm.getInvoice() != null) {
      return Optional.of(invoiceTerm.getInvoice())
          .map(Invoice::getCompany)
          .map(Company::getCurrency)
          .orElse(null);
    } else {
      return Optional.of(invoiceTerm.getMoveLine())
          .map(MoveLine::getMove)
          .map(Move::getCompany)
          .map(Company::getCurrency)
          .orElse(null);
    }
  }

  @Override
  public boolean isMultiCurrency(InvoiceTerm invoiceTerm) {
    return !Objects.equals(this.getCurrency(invoiceTerm), this.getCompanyCurrency(invoiceTerm));
  }

  @Override
  public List<InvoiceTerm> recomputeInvoiceTermsPercentage(
      List<InvoiceTerm> invoiceTermList, BigDecimal total) {
    InvoiceTerm lastInvoiceTerm = invoiceTermList.remove(invoiceTermList.size() - 1);
    BigDecimal percentageTotal = BigDecimal.ZERO;

    for (InvoiceTerm invoiceTerm : invoiceTermList) {
      BigDecimal percentage = this.computeCustomizedPercentage(invoiceTerm.getAmount(), total);

      invoiceTerm.setPercentage(percentage);
      percentageTotal = percentageTotal.add(percentage);
    }

    lastInvoiceTerm.setPercentage(BigDecimal.valueOf(100).subtract(percentageTotal));
    invoiceTermList.add(lastInvoiceTerm);

    return invoiceTermList;
  }

  @Override
  public BigDecimal adjustAmountInCompanyCurrency(
      List<InvoiceTerm> invoiceTermList,
      BigDecimal companyAmountRemaining,
      BigDecimal amountToPayInCompanyCurrency,
      BigDecimal amountToPay,
      BigDecimal currencyRate) {
    BigDecimal moveLineAmountRemaining =
        companyAmountRemaining.subtract(amountToPayInCompanyCurrency);
    BigDecimal invoiceTermAmountRemaining =
        invoiceTermList.stream()
            .map(InvoiceTerm::getAmountRemaining)
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO)
            .subtract(amountToPay)
            .multiply(currencyRate)
            .setScale(AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP);
    BigDecimal diff = moveLineAmountRemaining.subtract(invoiceTermAmountRemaining);

    return amountToPayInCompanyCurrency
        .add(diff)
        .setScale(AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP);
  }
}
