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
package com.axelor.apps.account.service.moveline;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.FinancialDiscount;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.InvoiceLineTax;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Tax;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.repo.TaxRepository;
import com.axelor.apps.account.service.FinancialDiscountService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.invoice.InvoiceTermFinancialDiscountService;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.account.service.invoice.InvoiceToolService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.common.ObjectUtils;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;

public class MoveLineFinancialDiscountServiceImpl implements MoveLineFinancialDiscountService {
  protected AppAccountService appAccountService;
  protected InvoiceTermService invoiceTermService;
  protected InvoiceTermFinancialDiscountService invoiceTermFinancialDiscountService;
  protected FinancialDiscountService financialDiscountService;
  protected MoveLineCreateService moveLineCreateService;
  protected CurrencyScaleService currencyScaleService;
  protected MoveLineToolService moveLineToolService;
  protected TaxService taxService;
  protected TaxRepository taxRepository;

  @Inject
  public MoveLineFinancialDiscountServiceImpl(
      AppAccountService appAccountService,
      InvoiceTermService invoiceTermService,
      InvoiceTermFinancialDiscountService invoiceTermFinancialDiscountService,
      FinancialDiscountService financialDiscountService,
      MoveLineCreateService moveLineCreateService,
      CurrencyScaleService currencyScaleService,
      MoveLineToolService moveLineToolService,
      TaxService taxService,
      TaxRepository taxRepository) {
    this.appAccountService = appAccountService;
    this.invoiceTermService = invoiceTermService;
    this.invoiceTermFinancialDiscountService = invoiceTermFinancialDiscountService;
    this.financialDiscountService = financialDiscountService;
    this.moveLineCreateService = moveLineCreateService;
    this.currencyScaleService = currencyScaleService;
    this.moveLineToolService = moveLineToolService;
    this.taxService = taxService;
    this.taxRepository = taxRepository;
  }

  @Override
  public LocalDate getFinancialDiscountDeadlineDate(MoveLine moveLine) {
    if (moveLine == null) {
      return null;
    }

    int discountDelay =
        Optional.of(moveLine)
            .map(MoveLine::getFinancialDiscount)
            .map(FinancialDiscount::getDiscountDelay)
            .orElse(0);

    LocalDate deadlineDate = moveLine.getDueDate().minusDays(discountDelay);

    return deadlineDate.isBefore(moveLine.getDate()) ? moveLine.getDate() : deadlineDate;
  }

  @Override
  public void computeFinancialDiscount(MoveLine moveLine, Move move) {
    if (!appAccountService.getAppAccount().getManageFinancialDiscount()) {
      return;
    }

    if (moveLine.getAccount() != null
        && moveLine.getAccount().getUseForPartnerBalance()
        && moveLine.getFinancialDiscount() != null) {
      FinancialDiscount financialDiscount = moveLine.getFinancialDiscount();
      BigDecimal amount = moveLine.getCurrencyAmount().abs();

      moveLine.setFinancialDiscountRate(financialDiscount.getDiscountRate());
      moveLine.setFinancialDiscountTotalAmount(
          currencyScaleService.getCompanyScaledValue(
              moveLine,
              this.computeFinancialDiscountTotalAmount(
                  financialDiscount, moveLine, amount, move.getCurrency())));
      moveLine.setRemainingAmountAfterFinDiscount(
          amount.subtract(moveLine.getFinancialDiscountTotalAmount()));
    } else {
      moveLine.setFinancialDiscount(null);
      moveLine.setFinancialDiscountRate(BigDecimal.ZERO);
      moveLine.setFinancialDiscountTotalAmount(BigDecimal.ZERO);
      moveLine.setRemainingAmountAfterFinDiscount(BigDecimal.ZERO);
    }

    this.computeInvoiceTermsFinancialDiscount(moveLine);
  }

  protected BigDecimal computeFinancialDiscountTotalAmount(
      FinancialDiscount financialDiscount,
      MoveLine moveLine,
      BigDecimal amount,
      Currency currency) {
    BigDecimal taxAmount =
        Optional.of(moveLine).map(MoveLine::getMove).map(Move::getMoveLineList).stream()
            .flatMap(Collection::stream)
            .filter(moveLineToolService::isMoveLineTaxAccount)
            .map(MoveLine::getCurrencyAmount)
            .map(BigDecimal::abs)
            .findFirst()
            .orElse(BigDecimal.ZERO);

    return financialDiscountService.computeFinancialDiscountTotalAmount(
        financialDiscount, amount, taxAmount, currency);
  }

  protected void computeInvoiceTermsFinancialDiscount(MoveLine moveLine) {
    if (CollectionUtils.isNotEmpty(moveLine.getInvoiceTermList())) {
      moveLine.getInvoiceTermList().stream()
          .filter(it -> !it.getIsPaid() && it.getAmountRemaining().compareTo(it.getAmount()) == 0)
          .forEach(
              it -> invoiceTermFinancialDiscountService.computeFinancialDiscount(it, moveLine));
    }
  }

  @Override
  public boolean isFinancialDiscountLine(MoveLine moveLine, Company company)
      throws AxelorException {
    Account financialDiscountAccount =
        financialDiscountService.getFinancialDiscountAccount(
            company, moveLine.getCredit().signum() > 0);

    return moveLine.getAccount().equals(financialDiscountAccount);
  }

  @Override
  public int createFinancialDiscountMoveLine(
      Invoice invoice,
      Move move,
      InvoicePayment invoicePayment,
      String origin,
      int counter,
      boolean isDebit,
      boolean financialDiscountVat)
      throws AxelorException {
    Map<String, Pair<BigDecimal, BigDecimal>> taxMap = this.getFinancialDiscountTaxMap(invoice);
    Map<String, Integer> vatSystemTaxMap = this.getVatSystemTaxMap(invoice.getMove());
    Map<String, Account> accountTaxMap = this.getAccountTaxMap(invoice.getMove());

    Account financialDiscountAccount =
        financialDiscountService.getFinancialDiscountAccount(
            invoice.getCompany(), InvoiceToolService.isPurchase(invoice));

    return this.createFinancialDiscountMoveLine(
        move,
        invoice.getPartner(),
        taxMap,
        vatSystemTaxMap,
        accountTaxMap,
        financialDiscountAccount,
        origin,
        invoicePayment.getDescription(),
        invoicePayment.getFinancialDiscountAmount(),
        invoicePayment.getFinancialDiscountTaxAmount(),
        invoicePayment.getPaymentDate(),
        counter,
        isDebit,
        financialDiscountVat);
  }

  @Override
  public int createFinancialDiscountMoveLine(
      Move move,
      Partner partner,
      Map<String, Pair<BigDecimal, BigDecimal>> taxMap,
      Map<String, Integer> vatSystemTaxMap,
      Map<String, Account> accountTaxMap,
      Account financialDiscountAccount,
      String origin,
      String description,
      BigDecimal financialDiscountAmount,
      BigDecimal financialDiscountTaxAmount,
      LocalDate paymentDate,
      int counter,
      boolean isDebit,
      boolean financialDiscountVat)
      throws AxelorException {
    for (String taxcode : taxMap.keySet()) {
      counter =
          this.createFinancialDiscountMoveLine(
              move,
              partner,
              taxcode,
              financialDiscountAccount,
              accountTaxMap.get(taxcode),
              origin,
              description,
              financialDiscountAmount,
              financialDiscountTaxAmount,
              taxMap.get(taxcode),
              paymentDate,
              counter,
              vatSystemTaxMap.get(taxcode),
              isDebit,
              financialDiscountVat);
    }

    return counter;
  }

  protected int createFinancialDiscountMoveLine(
      Move move,
      Partner partner,
      String taxCode,
      Account financialDiscountAccount,
      Account taxAccount,
      String origin,
      String description,
      BigDecimal financialDiscountAmount,
      BigDecimal financialDiscountTaxAmount,
      Pair<BigDecimal, BigDecimal> prorata,
      LocalDate paymentDate,
      int counter,
      int vatSystem,
      boolean isDebit,
      boolean financialDiscountVat)
      throws AxelorException {
    int scale = currencyScaleService.getScale(move);

    financialDiscountAmount =
        financialDiscountAmount.multiply(prorata.getLeft()).setScale(scale, RoundingMode.HALF_UP);
    financialDiscountTaxAmount =
        financialDiscountTaxAmount
            .multiply(prorata.getRight())
            .setScale(scale, RoundingMode.HALF_UP);

    MoveLine moveLine =
        moveLineCreateService.createMoveLine(
            move,
            partner,
            financialDiscountAccount,
            financialDiscountAmount,
            isDebit,
            paymentDate,
            null,
            counter++,
            origin,
            description);

    Set<TaxLine> taxLineSet =
        Arrays.stream(taxCode.split("/"))
            .map(code -> taxRepository.findByCode(code))
            .map(Tax::getActiveTaxLine)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    if (financialDiscountVat && ObjectUtils.notEmpty(taxLineSet)) {
      moveLine.setTaxLineSet(taxLineSet);
      moveLine.setTaxRate(taxService.getTotalTaxRate(taxLineSet));
      moveLine.setTaxCode(taxCode);
    }

    move.addMoveLineListItem(moveLine);

    if (moveLine != null && financialDiscountVat && taxAccount != null) {
      counter =
          this.createOrUpdateFinancialDiscountTaxMoveLine(
              move,
              moveLine,
              taxAccount,
              taxLineSet,
              partner,
              origin,
              description,
              financialDiscountTaxAmount,
              paymentDate,
              counter,
              vatSystem,
              isDebit);
    }

    return counter;
  }

  protected int createOrUpdateFinancialDiscountTaxMoveLine(
      Move move,
      MoveLine moveLine,
      Account taxAccount,
      Set<TaxLine> taxLineSet,
      Partner partner,
      String origin,
      String description,
      BigDecimal financialDiscountTaxAmount,
      LocalDate paymentDate,
      int counter,
      int vatSystem,
      boolean isDebit)
      throws AxelorException {
    MoveLine taxMoveLine = this.getTaxMoveLine(move, taxAccount, taxLineSet, vatSystem);

    if (taxMoveLine == null) {
      counter =
          this.createFinancialDiscountTaxMoveLine(
              move,
              moveLine,
              taxAccount,
              partner,
              origin,
              description,
              financialDiscountTaxAmount,
              paymentDate,
              counter,
              vatSystem,
              isDebit);
    } else {
      this.updateFinancialDiscountTaxMoveLine(moveLine, financialDiscountTaxAmount, isDebit);
    }

    return counter;
  }

  protected MoveLine getTaxMoveLine(
      Move move, Account account, Set<TaxLine> taxLineSet, int vatSystem) {
    return move.getMoveLineList().stream()
        .filter(
            ml -> moveLineToolService.isEqualTaxMoveLine(account, taxLineSet, vatSystem, null, ml))
        .findFirst()
        .orElse(null);
  }

  protected int createFinancialDiscountTaxMoveLine(
      Move move,
      MoveLine financialDiscountMoveLine,
      Account financialDiscountTaxAccount,
      Partner partner,
      String origin,
      String description,
      BigDecimal financialDiscountTaxAmount,
      LocalDate paymentDate,
      int counter,
      int vatSystem,
      boolean isDebit)
      throws AxelorException {
    MoveLine financialDiscountVatMoveLine =
        moveLineCreateService.createMoveLine(
            move,
            partner,
            financialDiscountTaxAccount,
            financialDiscountTaxAmount,
            isDebit,
            paymentDate,
            null,
            counter++,
            origin,
            description);

    financialDiscountVatMoveLine.setTaxLineSet(
        Sets.newHashSet(financialDiscountMoveLine.getTaxLineSet()));
    financialDiscountVatMoveLine.setTaxRate(financialDiscountMoveLine.getTaxRate());
    financialDiscountVatMoveLine.setTaxCode(financialDiscountMoveLine.getTaxCode());
    financialDiscountVatMoveLine.setVatSystemSelect(vatSystem);

    move.addMoveLineListItem(financialDiscountVatMoveLine);

    return counter;
  }

  protected void updateFinancialDiscountTaxMoveLine(
      MoveLine moveLine, BigDecimal amount, boolean isDebit) {
    BigDecimal signum = BigDecimal.valueOf(moveLine.getCurrencyAmount().signum());
    BigDecimal signedAmount;

    if (isDebit) {
      moveLine.setDebit(moveLine.getDebit().add(amount));
      signedAmount = moveLine.getDebit().multiply(signum);
    } else {
      moveLine.setCredit(moveLine.getCredit().add(amount));
      signedAmount = moveLine.getCredit().multiply(signum);
    }

    moveLine.setCurrencyAmount(signedAmount);
    moveLine.setAmountRemaining(signedAmount);
  }

  public Map<String, Account> getAccountTaxMap(Move move) {
    Map<String, Account> accountTaxMap = new HashMap<>();

    if (ObjectUtils.notEmpty(move.getMoveLineList())) {
      for (MoveLine moveLine : move.getMoveLineList()) {
        if (moveLineToolService.isMoveLineTaxAccount(moveLine)) {
          accountTaxMap.put(
              taxService.computeTaxCode(moveLine.getTaxLineSet()), moveLine.getAccount());
        }
      }
    }

    return accountTaxMap;
  }

  public Map<String, Integer> getVatSystemTaxMap(Move move) {
    Map<String, Integer> vatSystemMap = new HashMap<>();

    if (ObjectUtils.notEmpty(move.getMoveLineList())) {
      for (MoveLine moveLine : move.getMoveLineList()) {
        if (moveLineToolService.isMoveLineTaxAccount(moveLine)) {
          vatSystemMap.put(
              taxService.computeTaxCode(moveLine.getTaxLineSet()), moveLine.getVatSystemSelect());
        }
      }
    }

    return vatSystemMap;
  }

  @Override
  public Map<String, Pair<BigDecimal, BigDecimal>> getFinancialDiscountTaxMap(MoveLine moveLine) {
    Invoice invoice = moveLine.getMove().getInvoice();

    if (invoice != null) {
      return this.getFinancialDiscountTaxMap(invoice);
    } else {
      Map<String, Pair<BigDecimal, BigDecimal>> taxMap = new HashMap<>();
      BigDecimal baseTotal = BigDecimal.ZERO;
      BigDecimal taxTotal = BigDecimal.ZERO;

      for (MoveLine moveLineIt : moveLine.getMove().getMoveLineList()) {
        if (moveLineToolService.isMoveLineTaxAccount(moveLineIt)) {
          BigDecimal baseAmount =
              moveLine.getMove().getMoveLineList().stream()
                  .filter(
                      it ->
                          ObjectUtils.notEmpty(it.getTaxLineSet())
                              && it.getTaxLineSet().equals(moveLineIt.getTaxLineSet())
                              && !it.equals(moveLineIt))
                  .map(MoveLine::getCurrencyAmount)
                  .map(BigDecimal::abs)
                  .findFirst()
                  .orElse(BigDecimal.ONE);
          BigDecimal taxAmount = moveLineIt.getCurrencyAmount().abs();

          taxMap.put(
              taxService.computeTaxCode(moveLineIt.getTaxLineSet()),
              Pair.of(baseAmount, taxAmount));

          baseTotal = baseTotal.add(baseAmount);
          taxTotal = taxTotal.add(taxAmount);
        }
      }
      if (baseTotal.compareTo(BigDecimal.ZERO) == 0 || taxTotal.compareTo(BigDecimal.ZERO) == 0) {
        return taxMap;
      }

      for (String taxCode : taxMap.keySet()) {
        Pair<BigDecimal, BigDecimal> pair = taxMap.get(taxCode);

        taxMap.replace(
            taxCode,
            Pair.of(
                pair.getLeft()
                    .divide(baseTotal, AppBaseService.COMPUTATION_SCALING, RoundingMode.HALF_UP),
                pair.getRight()
                    .divide(taxTotal, AppBaseService.COMPUTATION_SCALING, RoundingMode.HALF_UP)));
      }

      return taxMap;
    }
  }

  protected Map<String, Pair<BigDecimal, BigDecimal>> getFinancialDiscountTaxMap(Invoice invoice) {
    Map<String, Pair<BigDecimal, BigDecimal>> taxMap = new HashMap<>();
    BigDecimal taxTotal = invoice.getTaxTotal();

    for (InvoiceLineTax invoiceLineTax : invoice.getInvoiceLineTaxList()) {
      TaxLine taxLine = invoiceLineTax.getTaxLine();
      List<InvoiceLine> invoiceLineList = invoiceLineTax.getInvoice().getInvoiceLineList();
      long noOfLines =
          invoiceLineList.stream().filter(il -> il.getTaxLineSet().contains(taxLine)).count();
      long count =
          invoiceLineList.stream()
              .filter(il -> il.getTaxLineSet().contains(taxLine))
              .flatMap(il -> il.getTaxLineSet().stream())
              .count();

      BigDecimal amountProrata =
          invoiceLineTax
              .getExTaxBase()
              .multiply(BigDecimal.valueOf(noOfLines))
              .divide(
                  invoice.getExTaxTotal().multiply(BigDecimal.valueOf(count)),
                  AppBaseService.COMPUTATION_SCALING,
                  RoundingMode.HALF_UP);

      BigDecimal taxProrata = BigDecimal.ONE;
      if (taxTotal.compareTo(BigDecimal.ZERO) != 0) {
        taxProrata =
            invoiceLineTax
                .getTaxTotal()
                .divide(taxTotal, AppAccountService.COMPUTATION_SCALING, RoundingMode.HALF_UP);
      }

      taxMap.put(
          invoiceLineTax.getTaxLine().getTax().getCode(), Pair.of(amountProrata, taxProrata));
    }

    return taxMap;
  }
}
