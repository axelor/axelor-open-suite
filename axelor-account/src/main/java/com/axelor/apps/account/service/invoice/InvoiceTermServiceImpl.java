/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.InvoiceTermPayment;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentCondition;
import com.axelor.apps.account.db.PaymentConditionLine;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.SubstitutePfpValidator;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.InvoiceTermRepository;
import com.axelor.apps.account.db.repo.JournalTypeRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.db.repo.PaymentSessionRepository;
import com.axelor.apps.account.service.JournalService;
import com.axelor.apps.account.service.PaymentConditionToolService;
import com.axelor.apps.account.service.PfpService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoiceTermPaymentService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.auth.db.User;
import com.axelor.auth.db.repo.UserRepository;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import com.axelor.dms.db.DMSFile;
import com.axelor.dms.db.repo.DMSFileRepository;
import com.axelor.studio.db.AppAccount;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import com.google.inject.servlet.RequestScoped;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;

@RequestScoped
public class InvoiceTermServiceImpl implements InvoiceTermService {

  protected InvoiceTermRepository invoiceTermRepo;
  protected InvoiceRepository invoiceRepo;
  protected AppAccountService appAccountService;
  protected JournalService journalService;
  protected UserRepository userRepo;
  protected InvoiceTermFinancialDiscountService invoiceTermFinancialDiscountService;
  protected PfpService pfpService;
  protected CurrencyScaleService currencyScaleService;
  protected DMSFileRepository DMSFileRepo;
  protected InvoiceTermPaymentService invoiceTermPaymentService;
  protected CurrencyService currencyService;
  protected AppBaseService appBaseService;
  protected InvoiceTermPfpUpdateService invoiceTermPfpUpdateService;
  protected InvoiceTermToolService invoiceTermToolService;
  protected InvoiceTermPfpToolService invoiceTermPfpToolService;
  protected InvoiceTermDateComputeService invoiceTermDateComputeService;

  @Inject
  public InvoiceTermServiceImpl(
      InvoiceTermRepository invoiceTermRepo,
      InvoiceRepository invoiceRepo,
      AppAccountService appAccountService,
      JournalService journalService,
      InvoiceTermFinancialDiscountService invoiceTermFinancialDiscountService,
      UserRepository userRepo,
      PfpService pfpService,
      CurrencyScaleService currencyScaleService,
      DMSFileRepository DMSFileRepo,
      InvoiceTermPaymentService invoiceTermPaymentService,
      CurrencyService currencyService,
      AppBaseService appBaseService,
      InvoiceTermPfpUpdateService invoiceTermPfpUpdateService,
      InvoiceTermToolService invoiceTermToolService,
      InvoiceTermPfpToolService invoiceTermPfpToolService,
      InvoiceTermDateComputeService invoiceTermDateComputeService) {
    this.invoiceTermRepo = invoiceTermRepo;
    this.invoiceRepo = invoiceRepo;
    this.appAccountService = appAccountService;
    this.userRepo = userRepo;
    this.journalService = journalService;
    this.invoiceTermFinancialDiscountService = invoiceTermFinancialDiscountService;
    this.pfpService = pfpService;
    this.currencyScaleService = currencyScaleService;
    this.DMSFileRepo = DMSFileRepo;
    this.invoiceTermPaymentService = invoiceTermPaymentService;
    this.currencyService = currencyService;
    this.appBaseService = appBaseService;
    this.invoiceTermPfpUpdateService = invoiceTermPfpUpdateService;
    this.invoiceTermToolService = invoiceTermToolService;
    this.invoiceTermPfpToolService = invoiceTermPfpToolService;
    this.invoiceTermDateComputeService = invoiceTermDateComputeService;
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
                invoiceTerm
                    .getAmount()
                    .divide(
                        invoice.getInTaxTotal(),
                        AppBaseService.COMPUTATION_SCALING,
                        RoundingMode.HALF_UP));
      }
    }

    return currencyScaleService.getScaledValue(invoice, sum.multiply(BigDecimal.valueOf(100)));
  }

  protected BigDecimal computePercentageSum(MoveLine moveLine) {
    BigDecimal sum = BigDecimal.ZERO;
    BigDecimal total = getTotalInvoiceTermsAmount(moveLine);
    Move move = moveLine.getMove();

    if (CollectionUtils.isNotEmpty(moveLine.getInvoiceTermList())) {
      for (InvoiceTerm invoiceTerm : moveLine.getInvoiceTermList()) {
        sum =
            sum.add(
                invoiceTermToolService.computeCustomizedPercentageUnscaled(
                    invoiceTerm.getAmount(), total));
      }
    }
    if (move != null && move.getMoveLineList() != null) {
      for (MoveLine moveLineIt : move.getMoveLineList()) {
        if (!moveLineIt.getCounter().equals(moveLine.getCounter())
            && moveLineIt.getAccount() != null
            && moveLineIt.getAccount().getUseForPartnerBalance()
            && moveLineIt.getInvoiceTermList() != null) {
          for (InvoiceTerm invoiceTerm : moveLineIt.getInvoiceTermList()) {
            sum =
                sum.add(
                    invoiceTermToolService.computeCustomizedPercentageUnscaled(
                        invoiceTerm.getAmount(), total));
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
        invoiceTermFinancialDiscountService.computeAmountRemainingAfterFinDiscount(invoiceTerm);
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
                currencyScaleService.getScale(invoice),
                RoundingMode.HALF_UP);

    User pfpUser = null;
    if (getPfpValidatorUserCondition(invoice, null)) {
      pfpUser =
          invoiceTermPfpToolService.getPfpValidatorUser(invoice.getPartner(), invoice.getCompany());
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
    invoiceTermFinancialDiscountService.computeFinancialDiscount(invoiceTerm);

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
      BigDecimal lastInvoiceTermCompanyAmount = this.getLastInvoiceTermCompanyAmount(invoiceTerm);

      if (lastInvoiceTermCompanyAmount != null) {
        companyAmount = lastInvoiceTermCompanyAmount;
      } else {
        BigDecimal companyTotal =
            invoice != null
                ? invoice.getCompanyInTaxTotal()
                : moveLine.getDebit().max(moveLine.getCredit());

        BigDecimal percentage = isHoldback ? BigDecimal.valueOf(100) : invoiceTerm.getPercentage();

        companyAmount =
            companyTotal
                .multiply(percentage)
                .divide(
                    BigDecimal.valueOf(100),
                    AppBaseService.COMPUTATION_SCALING,
                    RoundingMode.HALF_UP);
      }

      if (!isUpdate) {
        ratioPaid =
            invoiceTermAmountRemaining.divide(
                invoiceTermAmount, AppBaseService.COMPUTATION_SCALING, RoundingMode.HALF_UP);
      }

      companyAmountRemaining =
          currencyScaleService.getCompanyScaledValue(
              invoiceTerm, companyAmount.multiply(ratioPaid));
      companyAmount = currencyScaleService.getCompanyScaledValue(invoiceTerm, companyAmount);
    }

    invoiceTerm.setCompanyAmount(companyAmount);
    invoiceTerm.setCompanyAmountRemaining(companyAmountRemaining);
  }

  protected BigDecimal getLastInvoiceTermCompanyAmount(InvoiceTerm invoiceTerm) {
    List<InvoiceTerm> invoiceTermList;
    BigDecimal total, companyTotal;

    if (invoiceTerm.getInvoice() != null) {
      invoiceTermList = invoiceTerm.getInvoice().getInvoiceTermList();
      total = invoiceTerm.getInvoice().getInTaxTotal();
      companyTotal = invoiceTerm.getInvoice().getCompanyInTaxTotal();
    } else {
      invoiceTermList = invoiceTerm.getMoveLine().getInvoiceTermList();
      total = invoiceTerm.getMoveLine().getCurrencyAmount();
      companyTotal =
          invoiceTerm.getMoveLine().getDebit().max(invoiceTerm.getMoveLine().getCredit());
    }

    BigDecimal currentTotal =
        Optional.ofNullable(invoiceTermList).stream()
            .flatMap(Collection::stream)
            .map(InvoiceTerm::getAmount)
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO);

    if (currentTotal.add(invoiceTerm.getAmount()).compareTo(total) == 0) {
      BigDecimal currentCompanyTotal =
          Optional.ofNullable(invoiceTermList).stream()
              .flatMap(Collection::stream)
              .filter(it -> !it.equals(invoiceTerm))
              .map(InvoiceTerm::getCompanyAmount)
              .reduce(BigDecimal::add)
              .orElse(BigDecimal.ZERO);

      return companyTotal.subtract(currentCompanyTotal);
    }

    return null;
  }

  @Override
  public boolean getPfpValidatorUserCondition(Invoice invoice, MoveLine moveLine)
      throws AxelorException {
    boolean invoiceCondition =
        invoice != null
            && pfpService.isManagePassedForPayment(invoice.getCompany())
            && (invoice.getOperationTypeSelect()
                    == InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE
                || (pfpService.isManagePFPInRefund(invoice.getCompany())
                    && invoice.getOperationTypeSelect()
                        == InvoiceRepository.OPERATION_TYPE_SUPPLIER_REFUND));

    boolean moveLineCondition =
        invoice == null
            && moveLine != null
            && moveLine.getMove() != null
            && pfpService.isManagePassedForPayment(moveLine.getMove().getCompany())
            && (moveLine.getMove().getJournal().getJournalType().getTechnicalTypeSelect()
                    == JournalTypeRepository.TECHNICAL_TYPE_SELECT_EXPENSE
                || (pfpService.isManagePFPInRefund(moveLine.getMove().getCompany())
                    && moveLine.getMove().getJournal().getJournalType().getTechnicalTypeSelect()
                        == JournalTypeRepository.TECHNICAL_TYPE_SELECT_CREDIT_NOTE));

    return invoiceCondition || moveLineCondition;
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
                currencyScaleService.getScale(invoice),
                RoundingMode.HALF_UP);
    invoiceTerm.setAmount(amount);
    invoiceTerm.setAmountRemaining(amount);
    this.computeCompanyAmounts(invoiceTerm, false, false);
    invoiceTermFinancialDiscountService.computeFinancialDiscount(invoiceTerm);

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
                currencyScaleService.getScale(moveLine),
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
    return PaymentConditionToolService.getDueDate(
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
        invoiceTermDateComputeService.computeDueDateValues(invoiceTerm, invoiceDate);
      }
    }

    initInvoiceTermsSequence(invoice);
    return invoice;
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
        invoicePayment.getInvoiceTermPaymentList(), invoicePayment.getPaymentMode(), null);
  }

  @Override
  public void updateInvoiceTermsPaidAmount(
      InvoicePayment invoicePayment,
      InvoiceTerm invoiceTermToPay,
      InvoiceTermPayment invoiceTermPayment,
      Map<InvoiceTerm, Integer> invoiceTermPfpValidateStatusSelectMap)
      throws AxelorException {
    this.updateInvoiceTermsPaidAmount(
        Collections.singletonList(invoiceTermPayment),
        invoiceTermToPay.getPaymentMode(),
        invoiceTermPfpValidateStatusSelectMap);
  }

  protected void updateInvoiceTermsPaidAmount(
      List<InvoiceTermPayment> invoiceTermPaymentList,
      PaymentMode paymentMode,
      Map<InvoiceTerm, Integer> invoiceTermPfpValidateStatusSelectMap)
      throws AxelorException {
    for (InvoiceTermPayment invoiceTermPayment : invoiceTermPaymentList) {
      InvoiceTerm invoiceTerm = invoiceTermPayment.getInvoiceTerm();
      InvoicePayment invoicePayment = invoiceTermPayment.getInvoicePayment();

      BigDecimal paidAmount;
      if (invoicePayment != null
          && invoicePayment.getCurrency().equals(invoiceTerm.getCurrency())) {
        paidAmount = invoiceTermPayment.getPaidAmount();
      } else {
        paidAmount = this.computePaidAmount(invoiceTermPayment, invoicePayment, invoiceTerm);
      }
      paidAmount = paidAmount.add(invoiceTermPayment.getFinancialDiscountAmount());

      BigDecimal amountRemaining = invoiceTerm.getAmountRemaining().subtract(paidAmount);
      BigDecimal companyAmountRemaining;

      boolean isSameCurrencyRate = true;
      if (invoicePayment != null) {
        isSameCurrencyRate =
            currencyService.isSameCurrencyRate(
                invoiceTerm.getInvoice().getInvoiceDate(),
                invoicePayment.getPaymentDate(),
                invoiceTerm.getCurrency(),
                invoiceTerm.getCompanyCurrency());
      }

      if (amountRemaining.compareTo(BigDecimal.ZERO) == 0 && isSameCurrencyRate) {
        companyAmountRemaining = BigDecimal.ZERO;
      } else {
        companyAmountRemaining =
            invoiceTerm
                .getCompanyAmountRemaining()
                .subtract(invoiceTermPayment.getCompanyPaidAmount());
      }

      if (amountRemaining.signum() <= 0 || companyAmountRemaining.signum() <= 0) {
        amountRemaining = BigDecimal.ZERO;
        invoiceTerm.setIsPaid(true);
        Invoice invoice = invoiceTerm.getInvoice();
        if (invoice != null) {
          invoice.setDueDate(InvoiceToolService.getDueDate(invoice));
        }

        if (companyAmountRemaining.signum() <= 0) {
          companyAmountRemaining = BigDecimal.ZERO;
        }
      }

      invoiceTerm.setAmountRemaining(amountRemaining);
      invoiceTerm.setCompanyAmountRemaining(companyAmountRemaining);
      invoiceTerm.setPaymentMode(paymentMode);

      invoiceTermFinancialDiscountService.computeAmountRemainingAfterFinDiscount(invoiceTerm);
      invoiceTermPfpUpdateService.updatePfp(invoiceTerm, invoiceTermPfpValidateStatusSelectMap);
    }
  }

  protected BigDecimal manageForeignExchange(
      InvoiceTermPayment invoiceTermPayment, InvoicePayment invoicePayment, BigDecimal paidAmount) {
    if (invoicePayment != null
        && invoicePayment.getReconcile() != null
        && invoicePayment.getReconcile().getForeignExchangeMove() != null) {
      BigDecimal foreignExchangeAmount =
          invoicePayment.getReconcile().getForeignExchangeMove().getMoveLineList().stream()
              .map(MoveLine::getCredit)
              .reduce(BigDecimal::add)
              .orElse(BigDecimal.ZERO);

      if (invoiceTermPayment.getCompanyPaidAmount().compareTo(foreignExchangeAmount) == 0) {
        return BigDecimal.ZERO;
      }
    }

    return paidAmount;
  }

  protected BigDecimal computePaidAmount(
      InvoiceTermPayment invoiceTermPayment, InvoicePayment invoicePayment, InvoiceTerm invoiceTerm)
      throws AxelorException {
    Currency currency = null;
    boolean needConvert = true;

    BigDecimal paidAmount = invoiceTermPayment.getCompanyPaidAmount();
    Currency invoiceTermCompanyCurrency = invoiceTerm.getCompanyCurrency();

    if (invoicePayment != null && invoicePayment.getCurrency().equals(invoiceTermCompanyCurrency)) {
      currency = invoicePayment.getCurrency();
    } else if (invoicePayment == null
        && !invoiceTerm.getCurrency().equals(invoiceTermCompanyCurrency)) {
      currency = invoiceTermCompanyCurrency;
    } else {
      needConvert = false;
    }

    if (needConvert) {
      paidAmount =
          currencyService.getAmountCurrencyConvertedAtDate(
              currency,
              invoiceTerm.getCurrency(),
              invoiceTermPayment.getCompanyPaidAmount(),
              invoiceTerm.getDueDate());
    }

    paidAmount = this.manageForeignExchange(invoiceTermPayment, invoicePayment, paidAmount);

    return paidAmount;
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

      if (paidAmount.signum() != 0 && companyPaidAmount.signum() != 0) {
        invoiceTerm.setAmountRemaining(invoiceTerm.getAmountRemaining().add(paidAmount));
        invoiceTerm.setCompanyAmountRemaining(
            invoiceTerm.getCompanyAmountRemaining().add(companyPaidAmount));

        invoiceTermFinancialDiscountService.computeAmountRemainingAfterFinDiscount(invoiceTerm);

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
  public void setCustomizedAmounts(InvoiceTerm invoiceTerm) {
    if (invoiceTerm == null) {
      return;
    }

    BigDecimal total = BigDecimal.ZERO;
    List<InvoiceTerm> invoiceTermList = new ArrayList<>();

    if (invoiceTerm.getMoveLine() != null) {
      total = this.getTotalInvoiceTermsAmount(invoiceTerm.getMoveLine());
      invoiceTermList = invoiceTerm.getMoveLine().getInvoiceTermList();
    } else if (invoiceTerm.getInvoice() != null) {
      total = invoiceTerm.getInvoice().getInTaxTotal();
      invoiceTermList = invoiceTerm.getInvoice().getInvoiceTermList();
    }
    invoiceTermList.remove(invoiceTerm);

    setCustomizedAmounts(invoiceTerm, invoiceTermList, total);
  }

  protected void setCustomizedAmounts(
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

    invoiceTerm.setAmount(customizedAmount);
    invoiceTerm.setAmountRemaining(customizedAmount);

    BigDecimal customizedCompanyAmount =
        this.getCustomizedCompanyAmount(
            invoiceTerm, invoiceTermList, companyTotal, isLastInvoiceTerm);

    if (customizedCompanyAmount != null) {
      invoiceTerm.setCompanyAmount(customizedCompanyAmount);
      invoiceTerm.setCompanyAmountRemaining(customizedCompanyAmount);
    }
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

      return currencyScaleService.getScaledValue(invoiceTerm, total.subtract(totalWithoutCurrent));
    } else {
      return invoiceTerm
          .getPercentage()
          .multiply(total)
          .divide(
              new BigDecimal(100),
              currencyScaleService.getScale(invoiceTerm),
              RoundingMode.HALF_UP);
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
      if (move == null
          && invoiceTerm.getMoveLine() != null
          && invoiceTerm.getMoveLine().getMove() != null) {
        move = invoiceTerm.getMoveLine().getMove();
      }

      if (move == null) {
        invoiceTerm.setPfpValidateStatusSelect(InvoiceTermRepository.PFP_STATUS_NO_PFP);
        return;
      }

      company = move.getCompany();
      isSupplierPurchase =
          move.getJournal().getJournalType().getTechnicalTypeSelect()
              == JournalTypeRepository.TECHNICAL_TYPE_SELECT_EXPENSE;
      isSupplierRefund =
          move.getJournal().getJournalType().getTechnicalTypeSelect()
              == JournalTypeRepository.TECHNICAL_TYPE_SELECT_CREDIT_NOTE;
    }

    if (pfpService.isManagePassedForPayment(company)
        && (isSupplierPurchase || (isSupplierRefund && pfpService.isManagePFPInRefund(company)))) {
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

      this.setThirdPartyPayerPartner(invoiceTerm);

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

      if (moveLine.getPartner() != null) {
        invoiceTerm.setPartner(moveLine.getPartner());
      }

      if (move != null) {
        invoiceTerm.setCompany(move.getCompany());
        invoiceTerm.setCurrency(move.getCurrency());

        if (invoiceTerm.getPartner() == null) {
          invoiceTerm.setPartner(move.getPartner());
        }

        if (journalService.isThirdPartyPayerOk(move.getJournal())) {
          this.setThirdPartyPayerPartner(invoiceTerm);
        }
      }
    }

    if (moveLine != null && move != null && invoiceTerm.getOriginDate() == null) {
      invoiceTerm.setOriginDate(move.getOriginDate());
    }
  }

  protected void setThirdPartyPayerPartner(InvoiceTerm invoiceTerm) {
    if (invoiceTerm.getAmount().compareTo(invoiceTerm.getAmountRemaining()) == 0) {
      if (invoiceTerm.getInvoice() != null) {
        invoiceTerm.setThirdPartyPayerPartner(invoiceTerm.getInvoice().getThirdPartyPayerPartner());
      } else {
        Partner thirdPartyPayerPartner =
            Optional.of(invoiceTerm)
                .map(InvoiceTerm::getMoveLine)
                .map(MoveLine::getMove)
                .map(Move::getThirdPartyPayerPartner)
                .orElse(null);

        invoiceTerm.setThirdPartyPayerPartner(thirdPartyPayerPartner);
      }
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
  public List<InvoiceTerm> reconcileMoveLineInvoiceTermsWithFullRollBack(
      List<InvoiceTerm> invoiceTermList,
      List<Pair<InvoiceTerm, Pair<InvoiceTerm, BigDecimal>>> invoiceTermLinkWithRefundList)
      throws AxelorException {
    List<Partner> partnerList = getPartnersFromInvoiceTermList(invoiceTermList);

    for (Partner partner : partnerList) {

      List<InvoiceTerm> invoiceTermFromInvoiceList =
          getInvoiceTermsInvoiceSortedByDueDateAndByPartner(invoiceTermList, partner);
      List<InvoiceTerm> invoiceTermFromRefundList =
          getInvoiceTermsRefundSortedByDueDateAndByPartner(invoiceTermList, partner);
      int invoiceCounter = 0;
      int refundCounter = 0;
      BigDecimal reconciledAmount = BigDecimal.ZERO;
      BigDecimal availableInvoiceAmount = BigDecimal.ZERO;
      BigDecimal availableRefundAmount = BigDecimal.ZERO;

      InvoiceTerm invoiceTermFromInvoice = null;
      InvoiceTerm invoiceTermFromRefund = null;

      while (!ObjectUtils.isEmpty(invoiceTermFromRefundList)
          && !ObjectUtils.isEmpty(invoiceTermFromInvoiceList)
          && invoiceCounter < invoiceTermFromInvoiceList.size()
          && refundCounter < invoiceTermFromRefundList.size()) {
        invoiceTermFromInvoice = invoiceTermFromInvoiceList.get(invoiceCounter);
        invoiceTermFromRefund = invoiceTermFromRefundList.get(refundCounter);

        if ((BigDecimal.ZERO).compareTo(availableInvoiceAmount) == 0) {
          availableInvoiceAmount = invoiceTermFromInvoice.getAmountRemaining();
        }
        if ((BigDecimal.ZERO).compareTo(availableRefundAmount) == 0) {
          availableRefundAmount = invoiceTermFromRefund.getAmountRemaining();
        }

        reconciledAmount = availableInvoiceAmount.min(availableRefundAmount);

        if (availableInvoiceAmount.subtract(reconciledAmount).signum() == 0) {
          invoiceTermLinkWithRefundList.add(
              Pair.of(invoiceTermFromInvoice, Pair.of(invoiceTermFromRefund, reconciledAmount)));
          invoiceCounter++;
        } else if (availableRefundAmount.subtract(reconciledAmount).signum() == 0) {
          invoiceTermLinkWithRefundList.add(
              Pair.of(invoiceTermFromInvoice, Pair.of(invoiceTermFromRefund, reconciledAmount)));
          invoiceTermFromRefund.setIsPaid(true);
          refundCounter++;
        }
        availableInvoiceAmount = availableInvoiceAmount.subtract(reconciledAmount);
        availableRefundAmount = availableRefundAmount.subtract(reconciledAmount);
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

  protected List<InvoiceTerm> getInvoiceTermsInvoiceSortedByDueDateAndByPartner(
      List<InvoiceTerm> invoiceTermList, Partner partner) {
    return invoiceTermList.stream()
        .filter(
            it -> it.getAmountPaid().signum() > 0 && it.getMoveLine().getPartner().equals(partner))
        .sorted(Comparator.comparing(InvoiceTerm::getDueDate))
        .collect(Collectors.toList());
  }

  protected List<InvoiceTerm> getInvoiceTermsRefundSortedByDueDateAndByPartner(
      List<InvoiceTerm> invoiceTermList, Partner partner) {
    return invoiceTermList.stream()
        .filter(
            it -> it.getAmountPaid().signum() < 0 && it.getMoveLine().getPartner().equals(partner))
        .sorted(Comparator.comparing(InvoiceTerm::getDueDate))
        .collect(Collectors.toList());
  }

  @Override
  public List<InvoiceTermPayment> updateInvoiceTerms(
      List<InvoiceTerm> invoiceTermList,
      InvoicePayment invoicePayment,
      BigDecimal amount,
      Reconcile reconcile,
      Map<InvoiceTerm, Integer> invoiceTermPfpValidateStatusSelectMap)
      throws AxelorException {
    List<InvoiceTermPayment> invoiceTermPaymentList = new ArrayList<>();
    if (invoiceTermList != null) {
      BigDecimal currencyAmount =
          invoicePayment != null
              ? currencyService.getAmountCurrencyConvertedAtDate(
                  invoicePayment.getCompanyCurrency(),
                  invoicePayment.getCurrency(),
                  amount,
                  invoicePayment.getPaymentDate())
              : amount;
      invoiceTermPaymentList =
          invoiceTermPaymentService.initInvoiceTermPaymentsWithAmount(
              invoicePayment, invoiceTermList, amount, currencyAmount, reconcile.getAmount());

      for (InvoiceTermPayment invoiceTermPayment : invoiceTermPaymentList) {
        this.updateInvoiceTermsPaidAmount(
            invoicePayment,
            invoiceTermPayment.getInvoiceTerm(),
            invoiceTermPayment,
            invoiceTermPfpValidateStatusSelectMap);

        if (invoicePayment == null) {
          invoiceTermPayment.addReconcileListItem(reconcile);
        }
      }
    }
    return invoiceTermPaymentList;
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

    if (invoiceTerm.getPaymentSession() != null
        && invoiceTerm.getMoveLine() != null
        && invoiceTerm.getMoveLine().getMove() != null
        && ((invoiceTerm.getPaymentSession().getPartnerTypeSelect()
                    == PaymentSessionRepository.PARTNER_TYPE_CUSTOMER
                && invoiceTerm.getMoveLine().getMove().getFunctionalOriginSelect()
                    == MoveRepository.FUNCTIONAL_ORIGIN_PURCHASE)
            || (invoiceTerm.getPaymentSession().getPartnerTypeSelect()
                    == PaymentSessionRepository.PARTNER_TYPE_SUPPLIER
                && invoiceTerm.getMoveLine().getMove().getFunctionalOriginSelect()
                    == MoveRepository.FUNCTIONAL_ORIGIN_SALE))) {
      isSignedNegative = !isSignedNegative;
    }
    return isSignedNegative;
  }

  @Override
  public InvoiceTerm updateInvoiceTermsAmountsSessionPart(
      InvoiceTerm invoiceTerm, boolean isRefund) {
    boolean isSignedNegative = this.getIsSignedNegative(invoiceTerm);

    BigDecimal paymentAmount = invoiceTerm.getPaymentAmount();

    if (!isRefund) {
      if (isSignedNegative) {
        invoiceTerm.setPaymentAmount(invoiceTerm.getAmountRemaining().negate());
        paymentAmount = paymentAmount.negate();

      } else {
        invoiceTerm.setPaymentAmount(invoiceTerm.getAmountRemaining());
      }

      this.computeAmountPaid(invoiceTerm);
      invoiceTerm.setPaymentAmount(paymentAmount);

    } else {
      if (isSignedNegative) {
        invoiceTerm.setPaymentAmount(
            invoiceTerm.getAmount().subtract(invoiceTerm.getAmountRemaining()).negate());

      } else {
        invoiceTerm.setPaymentAmount(
            invoiceTerm.getAmount().subtract(invoiceTerm.getAmountRemaining()));
      }

      invoiceTerm.setAmountPaid(BigDecimal.ZERO);
    }

    return invoiceTerm;
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
  public String getPfpValidatorUserDomain(Partner partner, Company company) {
    List<User> pfpValidatorUserList = userRepo.all().filter("self.isPfpValidator IS TRUE").fetch();

    if (CollectionUtils.isEmpty(pfpValidatorUserList)) {
      return "self.id in (0)";
    }

    Set<User> validPfpValidatorUserSet = new HashSet<>();
    LocalDate todayDate = appAccountService.getTodayDate(company);

    for (User pfpValidatorUser : pfpValidatorUserList) {
      validPfpValidatorUserSet.add(pfpValidatorUser);

      for (SubstitutePfpValidator substitutePfpValidator :
          pfpValidatorUser.getSubstitutePfpValidatorList()) {
        LocalDate substituteStartDate = substitutePfpValidator.getSubstituteStartDate();
        LocalDate substituteEndDate = substitutePfpValidator.getSubstituteEndDate();

        if (substituteStartDate == null) {
          if (substituteEndDate == null || substituteEndDate.isAfter(todayDate)) {
            validPfpValidatorUserSet.add(substitutePfpValidator.getSubstitutePfpValidatorUser());
          }
        } else {
          if (substituteEndDate == null && substituteStartDate.isBefore(todayDate)) {
            validPfpValidatorUserSet.add(substitutePfpValidator.getSubstitutePfpValidatorUser());
          } else if (substituteStartDate.isBefore(todayDate)
              && substituteEndDate.isAfter(todayDate)) {
            validPfpValidatorUserSet.add(substitutePfpValidator.getSubstitutePfpValidatorUser());
          }
        }
      }
    }

    return String.format(
        "self.id IN (%s)",
        validPfpValidatorUserSet.stream()
            .map(pfpValidator -> pfpValidator.getId().toString())
            .collect(Collectors.joining(",")));
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
          total = total.add(moveLineIt.getCurrencyAmount().abs());
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
      return Optional.of(invoiceTerm)
          .map(InvoiceTerm::getMoveLine)
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
      return Optional.of(invoiceTerm)
          .map(InvoiceTerm::getMoveLine)
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
      BigDecimal percentage =
          invoiceTermToolService.computeCustomizedPercentage(invoiceTerm.getAmount(), total);

      invoiceTerm.setPercentage(percentage);
      percentageTotal = percentageTotal.add(percentage);
    }

    lastInvoiceTerm.setPercentage(BigDecimal.valueOf(100).subtract(percentageTotal));
    invoiceTermList.add(lastInvoiceTerm);

    return invoiceTermList;
  }

  @Override
  public InvoiceTerm initInvoiceTermWithParents(InvoiceTerm invoiceTerm) throws AxelorException {
    Invoice invoice = invoiceTerm.getInvoice();
    MoveLine moveLine = invoiceTerm.getMoveLine();

    if (invoice == null && moveLine != null && moveLine.getMove() != null) {
      this.initCustomizedInvoiceTerm(moveLine, invoiceTerm, moveLine.getMove());
    } else if (invoice != null) {
      this.initCustomizedInvoiceTerm(invoice, invoiceTerm);
    }

    setParentFields(invoiceTerm, moveLine != null ? moveLine.getMove() : null, moveLine, invoice);
    return invoiceTerm;
  }

  @Override
  public boolean setShowFinancialDiscount(InvoiceTerm invoiceTerm) {
    Invoice invoice = invoiceTerm.getInvoice();
    MoveLine moveLine = invoiceTerm.getMoveLine();
    AppAccount appAccount = appAccountService.getAppAccount();
    if (appAccount == null || !appAccount.getManageFinancialDiscount()) {
      return false;
    }

    if (invoice != null) {
      return Arrays.asList(InvoiceRepository.STATUS_VENTILATED, InvoiceRepository.STATUS_CANCELED)
          .contains(invoice.getStatusSelect());
    }
    if (moveLine != null && moveLine.getMove() != null) {
      return Arrays.asList(
              MoveRepository.STATUS_DAYBOOK,
              MoveRepository.STATUS_ACCOUNTED,
              MoveRepository.STATUS_CANCELED)
          .contains(moveLine.getMove().getStatusSelect());
    }
    return false;
  }

  @Override
  public boolean isPaymentConditionFree(InvoiceTerm invoiceTerm) {
    if (invoiceTerm.getInvoice() != null) {
      return Optional.of(invoiceTerm.getInvoice())
          .map(Invoice::getPaymentCondition)
          .map(PaymentCondition::getIsFree)
          .orElse(false);
    } else if (invoiceTerm.getMoveLine() != null) {
      return Optional.of(invoiceTerm.getMoveLine())
          .map(MoveLine::getMove)
          .map(Move::getPaymentCondition)
          .map(PaymentCondition::getIsFree)
          .orElse(false);
    }

    return false;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void payInvoiceTerms(List<InvoiceTerm> invoiceTermList) {
    if (ObjectUtils.isEmpty(invoiceTermList)) {
      return;
    }

    for (InvoiceTerm invoiceTerm : invoiceTermList) {
      if (invoiceTerm != null) {
        invoiceTerm.setIsPaid(true);
        invoiceTerm.setAmountRemaining(BigDecimal.ZERO);
        invoiceTerm.setCompanyAmountRemaining(BigDecimal.ZERO);
        invoiceTermFinancialDiscountService.computeAmountRemainingAfterFinDiscount(invoiceTerm);
      }
    }
  }

  public List<DMSFile> getLinkedDmsFile(InvoiceTerm invoiceTerm) {
    Move move =
        Optional.of(invoiceTerm).map(InvoiceTerm::getMoveLine).map(MoveLine::getMove).orElse(null);
    if (move == null) {
      return new ArrayList<>();
    }
    return DMSFileRepo.all()
        .filter(
            String.format(
                "self.isDirectory = false AND self.relatedId = %d AND self.relatedModel = '%s'",
                move.getId(), Move.class.getName()))
        .fetch();
  }

  @Override
  public void computeCustomizedPercentage(InvoiceTerm invoiceTerm) {
    BigDecimal total = this.getCustomizedTotal(invoiceTerm);

    if (total.compareTo(BigDecimal.ZERO) == 0) {
      return;
    }

    BigDecimal percentage =
        invoiceTermToolService.computeCustomizedPercentage(invoiceTerm.getAmount(), total);

    invoiceTerm.setPercentage(percentage);
    invoiceTerm.setAmountRemaining(invoiceTerm.getAmount());
    this.computeCompanyAmounts(invoiceTerm, true, invoiceTerm.getIsHoldBack());
  }

  protected BigDecimal getCustomizedTotal(InvoiceTerm invoiceTerm) {
    if (invoiceTerm.getInvoice() != null) {
      return invoiceTerm.getInvoice().getInTaxTotal();
    } else if (invoiceTerm.getMoveLine() != null) {
      return this.getTotalInvoiceTermsAmount(invoiceTerm.getMoveLine());
    } else {
      return BigDecimal.ZERO;
    }
  }

  @Override
  public void computeInvoiceTermsDueDates(Invoice invoice) throws AxelorException {
    if (CollectionUtils.isEmpty(invoice.getInvoiceTermList())
        || checkIfCustomizedInvoiceTerms(invoice)) {
      return;
    }
    LocalDate invoiceDate = invoiceTermDateComputeService.getInvoiceDateForTermGeneration(invoice);
    setDueDates(invoice, invoiceDate);
  }

  @Override
  public void checkAndComputeInvoiceTerms(Invoice invoice) throws AxelorException {
    if (invoice.getPaymentCondition() == null
        || CollectionUtils.isEmpty(invoice.getInvoiceLineList())) {
      if (invoice.getInvoiceTermList() != null) {
        invoice.getInvoiceTermList().clear();
      } else {
        invoice.setInvoiceTermList(new ArrayList<>());
      }

      return;
    }

    if (invoice.getStatusSelect() == InvoiceRepository.STATUS_VENTILATED
        || checkIfCustomizedInvoiceTerms(invoice)) {
      return;
    }

    invoice = computeInvoiceTerms(invoice);
  }

  @Override
  public List<InvoiceTerm> getInvoiceTermsFromMoveLine(List<InvoiceTerm> invoiceTermList) {
    return invoiceTermList.stream()
        .filter(it -> !it.getIsPaid())
        .sorted(this::compareInvoiceTerm)
        .collect(Collectors.toList());
  }

  @Override
  public void updateInvoiceTermsAmountRemainingWithoutPayment(
      Reconcile reconcile, MoveLine moveLine) throws AxelorException {
    BigDecimal reconciledAmount = reconcile.getAmount();
    if (reconciledAmount.compareTo(BigDecimal.ZERO) == 0
        || ObjectUtils.isEmpty(moveLine.getInvoiceTermList())) {
      return;
    }

    List<InvoiceTerm> invoiceTermList =
        moveLine.getInvoiceTermList().stream()
            .sorted(Collections.reverseOrder(this::compareInvoiceTerm))
            .collect(Collectors.toList());
    for (InvoiceTerm invoiceTerm : invoiceTermList) {
      if (reconciledAmount.signum() > 0
          && (invoiceTerm.getIsPaid()
              || invoiceTerm.getAmount().compareTo(invoiceTerm.getAmountRemaining()) > 0)) {
        reconciledAmount =
            resetAmountOnInvoiceTerm(reconciledAmount, invoiceTerm, reconcile.getEffectiveDate());
      }
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  protected BigDecimal resetAmountOnInvoiceTerm(
      BigDecimal reconciledAmount, InvoiceTerm invoiceTerm, LocalDate date) throws AxelorException {
    Move move =
        Optional.of(invoiceTerm).map(InvoiceTerm::getMoveLine).map(MoveLine::getMove).orElse(null);
    if (move == null || reconciledAmount.signum() == 0) {
      return BigDecimal.ZERO;
    }

    boolean isSameCurrency = Objects.equals(move.getCompanyCurrency(), move.getCurrency());
    BigDecimal companyRemainingAmount;

    if (isSameCurrency) {
      companyRemainingAmount =
          reconciledAmount.min(invoiceTerm.getAmount().subtract(invoiceTerm.getAmountRemaining()));
      invoiceTerm.setAmountRemaining(invoiceTerm.getAmountRemaining().add(companyRemainingAmount));
    } else {
      companyRemainingAmount =
          reconciledAmount.min(
              invoiceTerm.getCompanyAmount().subtract(invoiceTerm.getCompanyAmountRemaining()));
      invoiceTerm.setCompanyAmountRemaining(
          invoiceTerm.getCompanyAmountRemaining().add(companyRemainingAmount));
      invoiceTerm.setAmountRemaining(
          invoiceTerm
              .getAmountRemaining()
              .add(
                  currencyService.getAmountCurrencyConvertedAtDate(
                      move.getCompanyCurrency(),
                      move.getCurrency(),
                      companyRemainingAmount,
                      date)));
    }

    invoiceTermFinancialDiscountService.computeAmountRemainingAfterFinDiscount(invoiceTerm);

    if (invoiceTerm.getAmountRemaining().signum() > 0) {
      invoiceTerm.setIsPaid(false);

      Invoice invoice = invoiceTerm.getInvoice();
      if (invoice != null) {
        invoice.setDueDate(InvoiceToolService.getDueDate(invoice));
      }

      invoiceTermRepo.save(invoiceTerm);
    }
    return reconciledAmount.subtract(companyRemainingAmount);
  }

  protected int compareInvoiceTerm(InvoiceTerm invoiceTerm1, InvoiceTerm invoiceTerm2) {
    LocalDate date1, date2;

    if (invoiceTerm1.getEstimatedPaymentDate() != null
        && invoiceTerm2.getEstimatedPaymentDate() != null) {
      date1 = invoiceTerm1.getEstimatedPaymentDate();
      date2 = invoiceTerm2.getEstimatedPaymentDate();
    } else {
      date1 = invoiceTerm1.getDueDate();
      date2 = invoiceTerm2.getDueDate();
    }

    return date1.compareTo(date2);
  }
}
