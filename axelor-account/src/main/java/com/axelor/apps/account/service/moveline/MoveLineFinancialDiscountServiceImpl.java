package com.axelor.apps.account.service.moveline;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountManagement;
import com.axelor.apps.account.db.FinancialDiscount;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLineTax;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Tax;
import com.axelor.apps.account.db.repo.AccountTypeRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.AccountManagementAccountService;
import com.axelor.apps.account.service.FinancialDiscountService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.invoice.InvoiceTermFinancialDiscountService;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.account.service.invoice.InvoiceToolService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;

public class MoveLineFinancialDiscountServiceImpl implements MoveLineFinancialDiscountService {
  protected AppAccountService appAccountService;
  protected InvoiceTermService invoiceTermService;
  protected InvoiceTermFinancialDiscountService invoiceTermFinancialDiscountService;
  protected FinancialDiscountService financialDiscountService;
  protected MoveLineCreateService moveLineCreateService;
  protected AccountManagementAccountService accountManagementAccountService;

  @Inject
  public MoveLineFinancialDiscountServiceImpl(
      AppAccountService appAccountService,
      InvoiceTermService invoiceTermService,
      InvoiceTermFinancialDiscountService invoiceTermFinancialDiscountService,
      FinancialDiscountService financialDiscountService,
      MoveLineCreateService moveLineCreateService,
      AccountManagementAccountService accountManagementAccountService) {
    this.appAccountService = appAccountService;
    this.invoiceTermService = invoiceTermService;
    this.invoiceTermFinancialDiscountService = invoiceTermFinancialDiscountService;
    this.financialDiscountService = financialDiscountService;
    this.moveLineCreateService = moveLineCreateService;
    this.accountManagementAccountService = accountManagementAccountService;
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
  public void computeFinancialDiscount(MoveLine moveLine) {
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
          this.computeFinancialDiscountTotalAmount(financialDiscount, moveLine, amount));
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
      FinancialDiscount financialDiscount, MoveLine moveLine, BigDecimal amount) {
    BigDecimal taxAmount =
        Optional.of(moveLine).map(MoveLine::getMove).map(Move::getMoveLineList).stream()
            .flatMap(Collection::stream)
            .filter(
                it ->
                    it.getAccount()
                        .getAccountType()
                        .getTechnicalTypeSelect()
                        .equals(AccountTypeRepository.TYPE_TAX))
            .map(MoveLine::getCurrencyAmount)
            .map(BigDecimal::abs)
            .findFirst()
            .orElse(BigDecimal.ZERO);

    return financialDiscountService.computeFinancialDiscountTotalAmount(
        financialDiscount, amount, taxAmount);
  }

  protected void computeInvoiceTermsFinancialDiscount(MoveLine moveLine) {
    if (CollectionUtils.isNotEmpty(moveLine.getInvoiceTermList())) {
      moveLine.getInvoiceTermList().stream()
          .filter(it -> !it.getIsPaid() && it.getAmountRemaining().compareTo(it.getAmount()) == 0)
          .forEach(
              it ->
                  invoiceTermFinancialDiscountService.computeFinancialDiscount(
                      it,
                      moveLine.getCredit().max(moveLine.getDebit()),
                      moveLine.getFinancialDiscount(),
                      moveLine.getFinancialDiscountTotalAmount(),
                      moveLine.getRemainingAmountAfterFinDiscount()));
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
    Map<Tax, Pair<BigDecimal, BigDecimal>> taxMap = null;

    if (financialDiscountVat) {
      taxMap = this.getFinancialDiscountTaxMap(invoice);
    }

    Account financialDiscountAccount =
        financialDiscountService.getFinancialDiscountAccount(
            invoice.getCompany(), InvoiceToolService.isPurchase(invoice));

    return this.createFinancialDiscountMoveLine(
        move,
        invoice.getCompany(),
        invoice.getPartner(),
        taxMap,
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
      Company company,
      Partner partner,
      Map<Tax, Pair<BigDecimal, BigDecimal>> taxMap,
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
    for (Tax tax : taxMap.keySet()) {
      counter =
          this.createFinancialDiscountMoveLine(
              move,
              company,
              partner,
              tax,
              financialDiscountAccount,
              origin,
              description,
              financialDiscountAmount,
              financialDiscountTaxAmount,
              taxMap.get(tax),
              paymentDate,
              counter,
              isDebit,
              financialDiscountVat);
    }

    return counter;
  }

  protected int createFinancialDiscountMoveLine(
      Move move,
      Company company,
      Partner partner,
      Tax tax,
      Account financialDiscountAccount,
      String origin,
      String description,
      BigDecimal financialDiscountAmount,
      BigDecimal financialDiscountTaxAmount,
      Pair<BigDecimal, BigDecimal> prorata,
      LocalDate paymentDate,
      int counter,
      boolean isDebit,
      boolean financialDiscountVat)
      throws AxelorException {
    financialDiscountAmount =
        financialDiscountAmount
            .multiply(prorata.getLeft())
            .setScale(AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP);
    financialDiscountTaxAmount =
        financialDiscountTaxAmount
            .multiply(prorata.getRight())
            .setScale(AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP);

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

    if (financialDiscountVat && tax.getActiveTaxLine() != null) {
      moveLine.setTaxLine(tax.getActiveTaxLine());
      moveLine.setTaxRate(tax.getActiveTaxLine().getValue());
      moveLine.setTaxCode(tax.getCode());
    }

    move.addMoveLineListItem(moveLine);

    if (moveLine != null && financialDiscountVat) {
      counter =
          this.createFinancialDiscountTaxMoveLine(
              move,
              moveLine,
              company,
              partner,
              tax,
              origin,
              description,
              financialDiscountTaxAmount,
              paymentDate,
              counter,
              isDebit);
    }

    return counter;
  }

  protected int createFinancialDiscountTaxMoveLine(
      Move move,
      MoveLine financialDiscountMoveLine,
      Company company,
      Partner partner,
      Tax tax,
      String origin,
      String description,
      BigDecimal financialDiscountTaxAmount,
      LocalDate paymentDate,
      int counter,
      boolean isDebit)
      throws AxelorException {
    Integer vatSystem = financialDiscountMoveLine.getAccount().getVatSystemSelect();

    if (vatSystem == null || vatSystem == 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.MISSING_VAT_SYSTEM_ON_ACCOUNT),
          financialDiscountMoveLine.getAccount().getCode());
    }

    Account financialDiscountTaxAccount =
        this.getFinancialDiscountTaxAccount(move, company, tax, vatSystem);

    if (financialDiscountTaxAccount != null) {
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

      financialDiscountVatMoveLine.setTaxLine(financialDiscountMoveLine.getTaxLine());
      financialDiscountVatMoveLine.setTaxRate(financialDiscountMoveLine.getTaxRate());
      financialDiscountVatMoveLine.setTaxCode(financialDiscountMoveLine.getTaxCode());
      financialDiscountVatMoveLine.setVatSystemSelect(vatSystem);

      move.addMoveLineListItem(financialDiscountVatMoveLine);
    }

    return counter;
  }

  protected Account getFinancialDiscountTaxAccount(
      Move move, Company company, Tax tax, int vatSystem) throws AxelorException {
    return accountManagementAccountService.getTaxAccount(
        this.getAccountManagement(company, tax),
        tax,
        company,
        move.getJournal(),
        vatSystem,
        move.getFunctionalOriginSelect(),
        false,
        true);
  }

  protected AccountManagement getAccountManagement(Company company, Tax tax) {
    return tax.getAccountManagementList().stream()
        .filter(it -> it.getCompany().equals(company))
        .findFirst()
        .orElse(null);
  }

  @Override
  public Map<Tax, Pair<BigDecimal, BigDecimal>> getFinancialDiscountTaxMap(MoveLine moveLine) {
    Invoice invoice = moveLine.getMove().getInvoice();

    if (invoice != null) {
      return this.getFinancialDiscountTaxMap(invoice);
    } else {
      Map<Tax, Pair<BigDecimal, BigDecimal>> taxMap = new HashMap<>();
      BigDecimal baseTotal = BigDecimal.ZERO;
      BigDecimal taxTotal = BigDecimal.ZERO;

      for (MoveLine moveLineIt : moveLine.getMove().getMoveLineList()) {
        if (moveLineIt
            .getAccount()
            .getAccountType()
            .getTechnicalTypeSelect()
            .equals(AccountTypeRepository.TYPE_TAX)) {
          BigDecimal baseAmount =
              moveLine.getMove().getMoveLineList().stream()
                  .filter(
                      it ->
                          it.getTaxLine().equals(moveLineIt.getTaxLine()) && !it.equals(moveLineIt))
                  .map(MoveLine::getCurrencyAmount)
                  .map(BigDecimal::abs)
                  .findFirst()
                  .orElse(BigDecimal.ONE);
          BigDecimal taxAmount = moveLineIt.getCurrencyAmount().abs();

          taxMap.put(moveLineIt.getTaxLine().getTax(), Pair.of(baseAmount, taxAmount));

          baseTotal = baseTotal.add(baseAmount);
          taxTotal = taxTotal.add(taxAmount);
        }
      }

      for (Tax tax : taxMap.keySet()) {
        Pair<BigDecimal, BigDecimal> pair = taxMap.get(tax);

        taxMap.replace(
            tax,
            Pair.of(
                pair.getLeft()
                    .divide(baseTotal, AppBaseService.COMPUTATION_SCALING, RoundingMode.HALF_UP),
                pair.getRight()
                    .divide(taxTotal, AppBaseService.COMPUTATION_SCALING, RoundingMode.HALF_UP)));
      }

      return taxMap;
    }
  }

  protected Map<Tax, Pair<BigDecimal, BigDecimal>> getFinancialDiscountTaxMap(Invoice invoice) {
    Map<Tax, Pair<BigDecimal, BigDecimal>> taxMap = new HashMap<>();
    BigDecimal taxTotal = invoice.getTaxTotal();

    for (InvoiceLineTax invoiceLineTax : invoice.getInvoiceLineTaxList()) {
      BigDecimal amountProrata =
          invoiceLineTax
              .getExTaxBase()
              .divide(
                  invoice.getExTaxTotal(),
                  AppBaseService.COMPUTATION_SCALING,
                  RoundingMode.HALF_UP);

      BigDecimal taxProrata =
          invoiceLineTax
              .getTaxTotal()
              .divide(taxTotal, AppAccountService.COMPUTATION_SCALING, RoundingMode.HALF_UP);

      taxMap.put(invoiceLineTax.getTaxLine().getTax(), Pair.of(amountProrata, taxProrata));
    }

    return taxMap;
  }
}
