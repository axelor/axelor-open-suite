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
package com.axelor.apps.account.service.moveline;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AnalyticAccount;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.InvoiceLineTax;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Tax;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.repo.AccountTypeRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.FiscalPositionAccountService;
import com.axelor.apps.account.service.TaxAccountService;
import com.axelor.apps.account.service.analytic.AnalyticMoveLineGenerateRealService;
import com.axelor.apps.account.service.invoice.InvoiceToolService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.config.CompanyConfigService;
import com.axelor.apps.tool.StringTool;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoveLineCreateServiceImpl implements MoveLineCreateService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  protected CompanyConfigService companyConfigService;
  protected CurrencyService currencyService;
  protected FiscalPositionAccountService fiscalPositionAccountService;
  protected AnalyticMoveLineGenerateRealService analyticMoveLineGenerateRealService;
  protected TaxAccountService taxAccountService;
  protected MoveLineToolService moveLineToolService;
  protected MoveLineComputeAnalyticService moveLineComputeAnalyticService;
  protected MoveLineConsolidateService moveLineConsolidateService;

  @Inject
  public MoveLineCreateServiceImpl(
      CompanyConfigService companyConfigService,
      CurrencyService currencyService,
      FiscalPositionAccountService fiscalPositionAccountService,
      AnalyticMoveLineGenerateRealService analyticMoveLineGenerateRealService,
      TaxAccountService taxAccountService,
      MoveLineToolService moveLineToolService,
      MoveLineComputeAnalyticService moveLineComputeAnalyticService,
      MoveLineConsolidateService moveLineConsolidateService) {
    this.companyConfigService = companyConfigService;
    this.currencyService = currencyService;
    this.fiscalPositionAccountService = fiscalPositionAccountService;
    this.analyticMoveLineGenerateRealService = analyticMoveLineGenerateRealService;
    this.taxAccountService = taxAccountService;
    this.moveLineToolService = moveLineToolService;
    this.moveLineComputeAnalyticService = moveLineComputeAnalyticService;
    this.moveLineConsolidateService = moveLineConsolidateService;
  }

  /**
   * Creating accounting move line method using move currency
   *
   * @param move
   * @param partner
   * @param account
   * @param amountInSpecificMoveCurrency
   * @param isDebit <code>true = debit</code>, <code>false = credit</code>
   * @param date
   * @param dueDate
   * @param counter
   * @param origin
   * @return
   * @throws AxelorException
   */
  @Override
  public MoveLine createMoveLine(
      Move move,
      Partner partner,
      Account account,
      BigDecimal amountInSpecificMoveCurrency,
      boolean isDebit,
      LocalDate date,
      LocalDate dueDate,
      int counter,
      String origin,
      String description)
      throws AxelorException {

    log.debug(
        "Creating accounting move line (Account : {}, Amount in specific move currency : {}, debit ? : {}, date : {}, counter : {}, reference : {}",
        new Object[] {
          account.getName(), amountInSpecificMoveCurrency, isDebit, date, counter, origin
        });

    Currency currency = move.getCurrency();
    Currency companyCurrency = companyConfigService.getCompanyCurrency(move.getCompany());

    BigDecimal currencyRate =
        currencyService.getCurrencyConversionRate(currency, companyCurrency, date);

    BigDecimal amountConvertedInCompanyCurrency =
        currencyService.getAmountCurrencyConvertedUsingExchangeRate(
            amountInSpecificMoveCurrency, currencyRate);

    return this.createMoveLine(
        move,
        partner,
        account,
        amountInSpecificMoveCurrency,
        amountConvertedInCompanyCurrency,
        currencyRate,
        isDebit,
        date,
        dueDate,
        date,
        counter,
        origin,
        description);
  }

  /**
   * Creating accounting move line method using all currency information (amount in specific move
   * currency, amount in company currency, currency rate)
   *
   * @param move
   * @param partner
   * @param account
   * @param amountInSpecificMoveCurrency
   * @param amountInCompanyCurrency
   * @param currencyRate
   * @param isDebit
   * @param date
   * @param dueDate
   * @param counter
   * @param origin
   * @return
   * @throws AxelorException
   */
  @Override
  public MoveLine createMoveLine(
      Move move,
      Partner partner,
      Account account,
      BigDecimal amountInSpecificMoveCurrency,
      BigDecimal amountInCompanyCurrency,
      BigDecimal currencyRate,
      boolean isDebit,
      LocalDate date,
      LocalDate dueDate,
      LocalDate originDate,
      int counter,
      String origin,
      String description)
      throws AxelorException {

    amountInSpecificMoveCurrency = amountInSpecificMoveCurrency.abs();

    log.debug(
        "Creating accounting move line (Account : {}, Amount in specific move currency : {}, debit ? : {}, date : {}, counter : {}, reference : {}",
        new Object[] {
          account.getName(), amountInSpecificMoveCurrency, isDebit, date, counter, origin
        });

    if (partner != null) {
      FiscalPosition fiscalPosition = null;
      if (move.getInvoice() != null) {
        fiscalPosition = move.getInvoice().getFiscalPosition();
        if (fiscalPosition == null) {
          fiscalPosition = move.getInvoice().getPartner().getFiscalPosition();
        }
      } else {
        fiscalPosition = partner.getFiscalPosition();
      }

      account = fiscalPositionAccountService.getAccount(fiscalPosition, account);
    }

    BigDecimal debit = BigDecimal.ZERO;
    BigDecimal credit = BigDecimal.ZERO;

    if (amountInCompanyCurrency.compareTo(BigDecimal.ZERO) < 0) {
      isDebit = !isDebit;
      amountInCompanyCurrency = amountInCompanyCurrency.negate();
    }

    if (isDebit) {
      debit = amountInCompanyCurrency;
    } else {
      credit = amountInCompanyCurrency;
    }

    if (currencyRate == null) {
      if (amountInSpecificMoveCurrency.compareTo(BigDecimal.ZERO) == 0) {
        currencyRate = BigDecimal.ONE;
      } else {
        currencyRate =
            amountInCompanyCurrency.divide(amountInSpecificMoveCurrency, 5, RoundingMode.HALF_UP);
      }
    }

    if (originDate == null) {
      originDate = date;
    }

    MoveLine moveLine =
        new MoveLine(
            move,
            partner,
            account,
            date,
            dueDate,
            counter,
            debit,
            credit,
            StringTool.cutTooLongString(
                moveLineToolService.determineDescriptionMoveLine(
                    move.getJournal(), origin, description)),
            origin,
            currencyRate.setScale(5, RoundingMode.HALF_UP),
            amountInSpecificMoveCurrency,
            originDate);

    moveLine.setIsOtherCurrency(!move.getCurrency().equals(move.getCompanyCurrency()));

    analyticMoveLineGenerateRealService.computeAnalyticDistribution(
        move, moveLine, credit.add(debit));

    return moveLine;
  }

  /**
   * Créer une ligne d'écriture comptable
   *
   * @param move
   * @param partner
   * @param account
   * @param amount
   * @param isDebit <code>true = débit</code>, <code>false = crédit</code>
   * @param date
   * @param ref
   * @param origin
   * @return
   * @throws AxelorException
   */
  @Override
  public MoveLine createMoveLine(
      Move move,
      Partner partner,
      Account account,
      BigDecimal amount,
      boolean isDebit,
      LocalDate date,
      int ref,
      String origin,
      String description)
      throws AxelorException {

    return this.createMoveLine(
        move, partner, account, amount, isDebit, date, date, ref, origin, description);
  }

  /**
   * Créer les lignes d'écritures comptables d'une facture.
   *
   * @param invoice
   * @param move
   * @param consolidate
   * @return
   */
  @Override
  public List<MoveLine> createMoveLines(
      Invoice invoice,
      Move move,
      Company company,
      Partner partner,
      Account partnerAccount,
      boolean consolidate,
      boolean isPurchase,
      boolean isDebitCustomer)
      throws AxelorException {

    log.debug("Creation of move lines of the invoice : {}", invoice.getInvoiceId());

    List<MoveLine> moveLines = new ArrayList<MoveLine>();

    Set<AnalyticAccount> analyticAccounts = new HashSet<AnalyticAccount>();

    int moveLineId = 1;

    if (partner == null) {
      throw new AxelorException(
          invoice,
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(AccountExceptionMessage.MOVE_LINE_1),
          invoice.getInvoiceId());
    }
    if (partnerAccount == null) {
      throw new AxelorException(
          invoice,
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(AccountExceptionMessage.MOVE_LINE_2),
          invoice.getInvoiceId());
    }

    String origin = invoice.getInvoiceId();

    if (InvoiceToolService.isPurchase(invoice)) {
      origin = invoice.getSupplierInvoiceNb();
    }

    // Creation of partner move line
    MoveLine moveLine1 =
        this.createMoveLine(
            move,
            partner,
            partnerAccount,
            invoice.getInTaxTotal(),
            invoice.getCompanyInTaxTotal(),
            null,
            isDebitCustomer,
            invoice.getInvoiceDate(),
            invoice.getDueDate(),
            invoice.getOriginDate(),
            moveLineId++,
            origin,
            null);
    moveLines.add(moveLine1);

    // Creation of product move lines for each invoice line
    for (InvoiceLine invoiceLine : invoice.getInvoiceLineList()) {

      BigDecimal companyExTaxTotal = invoiceLine.getCompanyExTaxTotal();

      if (companyExTaxTotal.compareTo(BigDecimal.ZERO) != 0) {

        analyticAccounts.clear();

        Account account = invoiceLine.getAccount();

        if (account == null) {
          throw new AxelorException(
              move,
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(AccountExceptionMessage.MOVE_LINE_4),
              invoiceLine.getName(),
              company.getName());
        }

        companyExTaxTotal = invoiceLine.getCompanyExTaxTotal();

        log.debug(
            "Processing of the invoice line : account = {}, amount = {}",
            new Object[] {account.getName(), companyExTaxTotal});

        if (invoiceLine.getAnalyticDistributionTemplate() == null
            && (invoiceLine.getAnalyticMoveLineList() == null
                || invoiceLine.getAnalyticMoveLineList().isEmpty())
            && account.getAnalyticDistributionAuthorized()
            && account.getAnalyticDistributionRequiredOnInvoiceLines()) {
          throw new AxelorException(
              move,
              TraceBackRepository.CATEGORY_MISSING_FIELD,
              I18n.get(AccountExceptionMessage.ANALYTIC_DISTRIBUTION_MISSING),
              invoiceLine.getName(),
              company.getName());
        }

        MoveLine moveLine =
            this.createMoveLine(
                move,
                partner,
                account,
                invoiceLine.getExTaxTotal(),
                companyExTaxTotal,
                null,
                !isDebitCustomer,
                invoice.getInvoiceDate(),
                null,
                invoice.getOriginDate(),
                moveLineId++,
                origin,
                invoiceLine.getProductName());

        List<AnalyticMoveLine> analyticMoveLineList =
            CollectionUtils.isEmpty(moveLine.getAnalyticMoveLineList())
                ? new ArrayList<>()
                : new ArrayList<>(moveLine.getAnalyticMoveLineList());
        moveLine.clearAnalyticMoveLineList();

        moveLine.setAnalyticDistributionTemplate(invoiceLine.getAnalyticDistributionTemplate());
        if (invoiceLine.getAnalyticMoveLineList() != null
            && !invoiceLine.getAnalyticMoveLineList().isEmpty()) {
          for (AnalyticMoveLine invoiceAnalyticMoveLine : invoiceLine.getAnalyticMoveLineList()) {
            AnalyticMoveLine analyticMoveLine =
                analyticMoveLineGenerateRealService.createFromForecast(
                    invoiceAnalyticMoveLine, moveLine);
            moveLine.addAnalyticMoveLineListItem(analyticMoveLine);
          }
        } else {
          moveLineComputeAnalyticService.generateAnalyticMoveLines(moveLine);
        }

        if (CollectionUtils.isEmpty(moveLine.getAnalyticMoveLineList())) {
          moveLine.setAnalyticMoveLineList(analyticMoveLineList);
        }

        TaxLine taxLine = invoiceLine.getTaxLine();
        if (taxLine != null) {
          moveLine.setTaxLine(taxLine);
          moveLine.setTaxRate(taxLine.getValue());
          moveLine.setTaxCode(taxLine.getTax().getCode());
        }

        moveLines.add(moveLine);
      }
    }

    // Creation of tax move lines for each invoice line tax
    for (InvoiceLineTax invoiceLineTax : invoice.getInvoiceLineTaxList()) {

      BigDecimal companyTaxTotal = invoiceLineTax.getCompanyTaxTotal();

      if (companyTaxTotal.compareTo(BigDecimal.ZERO) != 0) {

        Tax tax = invoiceLineTax.getTaxLine().getTax();
        boolean hasFixedAssets = !invoiceLineTax.getSubTotalOfFixedAssets().equals(BigDecimal.ZERO);
        boolean hasOtherAssets =
            !invoiceLineTax.getSubTotalExcludingFixedAssets().equals(BigDecimal.ZERO);
        Account account;
        MoveLine moveLine;
        if (hasFixedAssets
            && invoiceLineTax.getCompanySubTotalOfFixedAssets().compareTo(BigDecimal.ZERO) != 0) {
          account = taxAccountService.getAccount(tax, company, isPurchase, true);
          if (account == null) {
            throw new AxelorException(
                move,
                TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
                I18n.get(AccountExceptionMessage.MOVE_LINE_6),
                tax.getName(),
                company.getName());
          }
          moveLine =
              this.createMoveLine(
                  move,
                  partner,
                  account,
                  invoiceLineTax.getSubTotalOfFixedAssets(),
                  invoiceLineTax.getCompanySubTotalOfFixedAssets(),
                  null,
                  !isDebitCustomer,
                  invoice.getInvoiceDate(),
                  null,
                  invoice.getOriginDate(),
                  moveLineId++,
                  origin,
                  null);

          moveLine.setTaxLine(invoiceLineTax.getTaxLine());
          moveLine.setTaxRate(invoiceLineTax.getTaxLine().getValue());
          moveLine.setTaxCode(tax.getCode());
          moveLines.add(moveLine);
        }

        if (hasOtherAssets
            && invoiceLineTax.getCompanySubTotalExcludingFixedAssets().compareTo(BigDecimal.ZERO)
                != 0) {
          account = taxAccountService.getAccount(tax, company, isPurchase, false);
          if (account == null) {
            throw new AxelorException(
                move,
                TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
                I18n.get(AccountExceptionMessage.MOVE_LINE_6),
                tax.getName(),
                company.getName());
          }
          moveLine =
              this.createMoveLine(
                  move,
                  partner,
                  account,
                  invoiceLineTax.getSubTotalExcludingFixedAssets(),
                  invoiceLineTax.getCompanySubTotalExcludingFixedAssets(),
                  null,
                  !isDebitCustomer,
                  invoice.getInvoiceDate(),
                  null,
                  invoice.getOriginDate(),
                  moveLineId++,
                  origin,
                  null);
          moveLine.setTaxLine(invoiceLineTax.getTaxLine());
          moveLine.setTaxRate(invoiceLineTax.getTaxLine().getValue());
          moveLine.setTaxCode(tax.getCode());
          moveLines.add(moveLine);
        }
      }
    }

    if (consolidate) {
      moveLineConsolidateService.consolidateMoveLines(moveLines);
    }

    return moveLines;
  }

  @Override
  public MoveLine createMoveLineForAutoTax(
      Move move,
      Map<String, MoveLine> map,
      Map<String, MoveLine> newMap,
      MoveLine moveLine,
      TaxLine taxLine,
      String accountType)
      throws AxelorException {
    BigDecimal debit = moveLine.getDebit();
    BigDecimal credit = moveLine.getCredit();
    LocalDate date = moveLine.getDate();
    Company company = move.getCompany();
    Account newAccount = null;

    FiscalPosition fiscalPosition = move.getFiscalPosition();

    if (fiscalPosition != null) {
      newAccount = fiscalPositionAccountService.getAccount(fiscalPosition, newAccount);
    }

    if (newAccount == null) {

      if (accountType.equals(AccountTypeRepository.TYPE_DEBT)
          || accountType.equals(AccountTypeRepository.TYPE_CHARGE)) {
        newAccount = taxAccountService.getAccount(taxLine.getTax(), company, true, false);
      } else if (accountType.equals(AccountTypeRepository.TYPE_INCOME)) {
        newAccount = taxAccountService.getAccount(taxLine.getTax(), company, false, false);
      } else if (accountType.equals(AccountTypeRepository.TYPE_IMMOBILISATION)) {
        newAccount = taxAccountService.getAccount(taxLine.getTax(), company, true, true);
      }
    }

    if (newAccount == null) {
      if (fiscalPosition != null) {
        throw new AxelorException(
            move,
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(AccountExceptionMessage.MOVE_LINE_8),
            taxLine.getName(),
            fiscalPosition.getName(),
            company.getName());
      } else {
        throw new AxelorException(
            move,
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(AccountExceptionMessage.MOVE_LINE_6),
            taxLine.getName(),
            company.getName());
      }
    }

    String newSourceTaxLineKey = newAccount.getCode() + taxLine.getId();
    MoveLine newOrUpdatedMoveLine;

    if (!map.containsKey(newSourceTaxLineKey) && !newMap.containsKey(newSourceTaxLineKey)) {

      newOrUpdatedMoveLine = this.createMoveLine(date, taxLine, newAccount, move);
    } else if (newMap.containsKey(newSourceTaxLineKey)) {
      newOrUpdatedMoveLine = newMap.get(newSourceTaxLineKey);
    } else {
      newOrUpdatedMoveLine = map.get(newSourceTaxLineKey);
    }

    newOrUpdatedMoveLine.setMove(move);
    newOrUpdatedMoveLine.setOrigin(move.getOrigin());
    newOrUpdatedMoveLine.setDescription(move.getDescription());

    BigDecimal newMoveLineDebit =
        newOrUpdatedMoveLine.getDebit().add(debit.multiply(taxLine.getValue()));
    BigDecimal newMoveLineCredit =
        newOrUpdatedMoveLine.getCredit().add(credit.multiply(taxLine.getValue()));

    this.setTaxLineAmount(newMoveLineDebit, newMoveLineCredit, newOrUpdatedMoveLine);

    newOrUpdatedMoveLine.setOriginDate(move.getOriginDate());
    newOrUpdatedMoveLine = moveLineToolService.setCurrencyAmount(newOrUpdatedMoveLine);

    if (newOrUpdatedMoveLine.getPartner() == null) {
      newOrUpdatedMoveLine.setPartner(move.getPartner());
    }

    if (newOrUpdatedMoveLine.getDebit().signum() != 0
        || newOrUpdatedMoveLine.getCredit().signum() != 0) {
      newMap.put(newSourceTaxLineKey, newOrUpdatedMoveLine);
    } else if (newOrUpdatedMoveLine
            .getDebit()
            .add(newOrUpdatedMoveLine.getCredit())
            .compareTo(BigDecimal.ZERO)
        == 0) {
      newMap.remove(newSourceTaxLineKey, newOrUpdatedMoveLine);
    }

    return newOrUpdatedMoveLine;
  }

  protected void setTaxLineAmount(BigDecimal debit, BigDecimal credit, MoveLine moveLine) {
    if ((debit.compareTo(BigDecimal.ZERO) > 0
            && moveLine.getCredit().compareTo(BigDecimal.ZERO) > 0)
        || (credit.compareTo(BigDecimal.ZERO) > 0
            && moveLine.getDebit().compareTo(BigDecimal.ZERO) > 0)) {
      BigDecimal taxLineAmount = debit.subtract(credit);

      if (taxLineAmount.compareTo(BigDecimal.ZERO) > 0) {
        moveLine.setDebit(taxLineAmount);
        moveLine.setCredit(BigDecimal.ZERO);
      } else {
        moveLine.setDebit(BigDecimal.ZERO);
        moveLine.setCredit(taxLineAmount.abs());
      }
    } else {
      moveLine.setDebit(moveLine.getDebit().add(debit));
      moveLine.setCredit(moveLine.getCredit().add(credit));
    }
  }

  protected MoveLine createMoveLine(LocalDate date, TaxLine taxLine, Account account, Move move)
      throws AxelorException {
    MoveLine moveLine;
    BigDecimal debit = BigDecimal.ZERO;
    BigDecimal credit = BigDecimal.ZERO;
    boolean isDebit = false;
    int counter = move.getMoveLineList().size() + 1;

    moveLine =
        createMoveLine(
            move,
            move.getPartner(),
            account,
            debit.add(credit),
            isDebit,
            date,
            counter,
            move.getOrigin(),
            move.getDescription());
    moveLine.setSourceTaxLine(taxLine);
    moveLine.setTaxLine(taxLine);
    moveLine.setDescription(move.getDescription());
    return moveLine;
  }

  @Override
  public MoveLine createMoveLine(
      Move move,
      Partner partner,
      Account account,
      BigDecimal currencyAmount,
      TaxLine taxLine,
      BigDecimal amount,
      BigDecimal currencyRate,
      boolean isDebit,
      LocalDate date,
      LocalDate dueDate,
      LocalDate originDate,
      Integer counter,
      String origin,
      String description)
      throws AxelorException {
    MoveLine moveLine =
        createMoveLine(
            move,
            partner,
            account,
            currencyAmount,
            amount,
            currencyRate,
            isDebit,
            date,
            dueDate,
            originDate,
            counter,
            origin,
            description);
    moveLine.setTaxLine(taxLine);
    return moveLine;
  }
}
