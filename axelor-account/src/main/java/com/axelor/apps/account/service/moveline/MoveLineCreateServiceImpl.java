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
package com.axelor.apps.account.service.moveline;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.db.AnalyticAccount;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.InvoiceLineTax;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Tax;
import com.axelor.apps.account.db.TaxEquiv;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.repo.AccountRepository;
import com.axelor.apps.account.db.repo.AccountTypeRepository;
import com.axelor.apps.account.db.repo.AccountingSituationRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.AccountingSituationService;
import com.axelor.apps.account.service.FiscalPositionAccountService;
import com.axelor.apps.account.service.TaxAccountService;
import com.axelor.apps.account.service.analytic.AnalyticLineService;
import com.axelor.apps.account.service.analytic.AnalyticMoveLineGenerateRealService;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.account.service.invoice.InvoiceToolService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.config.CompanyConfigService;
import com.axelor.apps.base.service.tax.FiscalPositionService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.axelor.utils.StringTool;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
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

@RequestScoped
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
  protected InvoiceTermService invoiceTermService;
  protected MoveLineTaxService moveLineTaxService;
  protected AccountingSituationRepository accountingSituationRepository;
  protected AccountingSituationService accountingSituationService;
  protected FiscalPositionService fiscalPositionService;
  protected TaxService taxService;
  protected AppBaseService appBaseService;
  protected AnalyticLineService analyticLineService;

  @Inject
  public MoveLineCreateServiceImpl(
      CompanyConfigService companyConfigService,
      CurrencyService currencyService,
      FiscalPositionAccountService fiscalPositionAccountService,
      AnalyticMoveLineGenerateRealService analyticMoveLineGenerateRealService,
      TaxAccountService taxAccountService,
      MoveLineToolService moveLineToolService,
      MoveLineComputeAnalyticService moveLineComputeAnalyticService,
      MoveLineConsolidateService moveLineConsolidateService,
      InvoiceTermService invoiceTermService,
      MoveLineTaxService moveLineTaxService,
      AccountingSituationRepository accountingSituationRepository,
      AccountingSituationService accountingSituationService,
      FiscalPositionService fiscalPositionService,
      TaxService taxService,
      AppBaseService appBaseService,
      AnalyticLineService analyticLineService) {
    this.companyConfigService = companyConfigService;
    this.currencyService = currencyService;
    this.fiscalPositionAccountService = fiscalPositionAccountService;
    this.analyticMoveLineGenerateRealService = analyticMoveLineGenerateRealService;
    this.taxAccountService = taxAccountService;
    this.moveLineToolService = moveLineToolService;
    this.moveLineComputeAnalyticService = moveLineComputeAnalyticService;
    this.moveLineConsolidateService = moveLineConsolidateService;
    this.invoiceTermService = invoiceTermService;
    this.moveLineTaxService = moveLineTaxService;
    this.accountingSituationRepository = accountingSituationRepository;
    this.accountingSituationService = accountingSituationService;
    this.fiscalPositionService = fiscalPositionService;
    this.taxService = taxService;
    this.appBaseService = appBaseService;
    this.analyticLineService = analyticLineService;
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

    if (amountInSpecificMoveCurrency != null) {
      amountInSpecificMoveCurrency = amountInSpecificMoveCurrency.abs();
    }

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
      if (amountInSpecificMoveCurrency == null
          || amountInSpecificMoveCurrency.compareTo(BigDecimal.ZERO) == 0) {
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
            Strings.isNullOrEmpty(move.getDescription())
                ? StringTool.cutTooLongString(
                    moveLineToolService.determineDescriptionMoveLine(
                        move.getJournal(), origin, description))
                : move.getDescription(),
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
    AccountingSituation accountingSituation = null;
    Partner partnerAccountingSituation = null;
    if (isPurchase) {
      accountingSituation = accountingSituationRepository.findByCompanyAndPartner(company, partner);
      partnerAccountingSituation = partner;
    } else {
      accountingSituation =
          accountingSituationRepository.findByCompanyAndPartner(company, company.getPartner());
      partnerAccountingSituation = company.getPartner();
    }

    String origin = invoice.getInvoiceId();

    if (InvoiceToolService.isPurchase(invoice)) {
      origin = invoice.getSupplierInvoiceNb();
    }

    if (partnerAccount.getUseForPartnerBalance()) {
      moveLines.addAll(
          addInvoiceTermMoveLines(invoice, partnerAccount, move, partner, isDebitCustomer, origin));
    } else {
      MoveLine moveLine =
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
              1,
              origin,
              null);
      moveLines.add(moveLine);
    }

    int moveLineId = moveLines.size() + 1;

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
        if (!CollectionUtils.isEmpty(invoiceLine.getAnalyticMoveLineList())) {
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

        analyticLineService.printAnalyticAccount(moveLine, move.getCompany());

        TaxLine taxLine = invoiceLine.getTaxLine();
        if (taxLine != null) {
          moveLine.setTaxLine(taxLine);
          moveLine.setTaxRate(taxLine.getValue());
          moveLine.setTaxCode(taxLine.getTax().getCode());
        }

        // Cut off
        if (invoiceLine.getAccount() != null && invoiceLine.getAccount().getManageCutOffPeriod()) {
          moveLine.setCutOffStartDate(invoiceLine.getCutOffStartDate());
          moveLine.setCutOffEndDate(invoiceLine.getCutOffEndDate());
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
          if (accountingSituation == null) {
            throw new AxelorException(
                TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
                I18n.get(
                    AccountExceptionMessage.MISSING_VAT_SYSTEM_ON_MISSING_ACCOUNTING_SITUATION),
                partnerAccountingSituation.getFullName(),
                company.getCode());
          }
          int vatSystemSelect =
              accountingSituationService.determineVatSystemSelect(
                  accountingSituation, invoiceLineTax);

          account =
              taxAccountService.getAccount(
                  tax,
                  company,
                  move.getJournal(),
                  vatSystemSelect,
                  true,
                  move.getFunctionalOriginSelect());
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
          moveLine.setVatSystemSelect(invoiceLineTax.getVatSystemSelect());
          moveLines.add(moveLine);
        }

        if (hasOtherAssets
            && invoiceLineTax.getCompanySubTotalExcludingFixedAssets().compareTo(BigDecimal.ZERO)
                != 0) {
          if (accountingSituation == null) {
            throw new AxelorException(
                TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
                I18n.get(
                    AccountExceptionMessage.MISSING_VAT_SYSTEM_ON_MISSING_ACCOUNTING_SITUATION),
                partnerAccountingSituation.getFullName(),
                company.getCode());
          }
          int vatSystemSelect =
              accountingSituationService.determineVatSystemSelect(
                  accountingSituation, invoiceLineTax);
          account =
              taxAccountService.getAccount(
                  tax,
                  company,
                  move.getJournal(),
                  vatSystemSelect,
                  false,
                  move.getFunctionalOriginSelect());
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
          moveLine.setVatSystemSelect(invoiceLineTax.getVatSystemSelect());
          moveLines.add(moveLine);
        }
      }
    }

    if (consolidate) {
      moveLineConsolidateService.consolidateMoveLines(moveLines);
    }

    return moveLines;
  }

  protected List<MoveLine> addInvoiceTermMoveLines(
      Invoice invoice,
      Account partnerAccount,
      Move move,
      Partner partner,
      boolean isDebitCustomer,
      String origin)
      throws AxelorException {
    int moveLineId = 1;
    BigDecimal totalCompanyAmount = BigDecimal.ZERO;
    List<MoveLine> moveLines = new ArrayList<MoveLine>();
    Currency companyCurrency = companyConfigService.getCompanyCurrency(move.getCompany());
    MoveLine moveLine = null;
    MoveLine holdBackMoveLine;
    LocalDate latestDueDate = invoiceTermService.getLatestInvoiceTermDueDate(invoice);
    BigDecimal currencyRate =
        currencyService.getCurrencyConversionRate(
            invoice.getCurrency(), companyCurrency, invoice.getInvoiceDate());
    BigDecimal companyAmount;

    for (InvoiceTerm invoiceTerm : invoice.getInvoiceTermList()) {
      companyAmount =
          invoiceTerm.equals(
                  invoice.getInvoiceTermList().get(invoice.getInvoiceTermList().size() - 1))
              ? (invoice.getCompanyInTaxTotal().subtract(totalCompanyAmount))
              : invoiceTerm.getCompanyAmount();
      totalCompanyAmount = totalCompanyAmount.add(invoiceTerm.getCompanyAmount());

      Account account = partnerAccount;
      if (invoiceTerm.getIsHoldBack()) {
        account = accountingSituationService.getPartnerAccount(invoice, true);
        holdBackMoveLine =
            this.createMoveLine(
                move,
                partner,
                account,
                invoiceTerm.getAmount(),
                companyAmount,
                currencyRate,
                isDebitCustomer,
                invoice.getInvoiceDate(),
                invoiceTerm.getDueDate(),
                invoice.getOriginDate(),
                moveLineId++,
                origin,
                null);
        holdBackMoveLine.addInvoiceTermListItem(invoiceTerm);
        moveLines.add(holdBackMoveLine);
      } else {
        if (moveLine == null) {
          moveLine =
              this.createMoveLine(
                  move,
                  partner,
                  account,
                  invoiceTerm.getAmount(),
                  companyAmount,
                  currencyRate,
                  isDebitCustomer,
                  invoice.getInvoiceDate(),
                  latestDueDate,
                  invoice.getOriginDate(),
                  moveLineId++,
                  origin,
                  null);
        } else {
          if (moveLine.getDebit().compareTo(BigDecimal.ZERO) != 0) {
            // Debit
            BigDecimal currencyAmount = moveLine.getCurrencyAmount().add(invoiceTerm.getAmount());
            moveLine.setDebit(moveLine.getDebit().add(companyAmount));
            moveLine.setCurrencyAmount(currencyAmount);
          } else {
            // Credit
            BigDecimal currencyAmount =
                moveLine.getCurrencyAmount().subtract(invoiceTerm.getAmount());
            moveLine.setCredit(moveLine.getCredit().add(companyAmount));
            moveLine.setCurrencyAmount(currencyAmount);
          }
          moveLine.setCurrencyAmount(moveLine.getCurrencyAmount().add(invoiceTerm.getAmount()));
        }
      }
    }

    if (moveLine != null) {
      for (InvoiceTerm invoiceTerm : invoice.getInvoiceTermList()) {
        if (!invoiceTerm.getIsHoldBack()) {
          moveLine.addInvoiceTermListItem(invoiceTerm);
        }
      }

      moveLine.setDebit(
          moveLine
              .getDebit()
              .setScale(AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP));
      moveLine.setCredit(
          moveLine
              .getCredit()
              .setScale(AppBaseService.DEFAULT_NB_DECIMAL_DIGITS, RoundingMode.HALF_UP));

      moveLines.add(moveLine);
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
      String accountType,
      Account newAccount,
      boolean percentMoveTemplate)
      throws AxelorException {
    BigDecimal debit = moveLine.getDebit();
    BigDecimal credit = moveLine.getCredit();
    LocalDate date = moveLine.getDate();
    Company company = move.getCompany();
    Partner partner = move.getPartner();
    TaxEquiv taxEquiv = null;
    TaxLine taxLineRC = null;
    TaxLine taxLineBeforeReverse = null;
    Account newAccountRC = null;
    String newSourceTaxLineRCKey = null;

    FiscalPosition fiscalPosition = move.getFiscalPosition();

    if (newAccount == null && fiscalPosition != null) {
      newAccount = fiscalPositionAccountService.getAccount(fiscalPosition, newAccount);
      taxEquiv = moveLine.getTaxEquiv();
      if (taxEquiv != null && taxEquiv.getReverseCharge()) {
        if (ObjectUtils.isEmpty(taxEquiv.getReverseChargeTax())) {
          throw new AxelorException(
              fiscalPosition,
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(AccountExceptionMessage.REVERSE_CHARGE_TAX_MISSING_ON_FISCAL_POSITION),
              fiscalPosition.getName(),
              taxEquiv.getFromTax().getName(),
              taxEquiv.getToTax().getName());
        }
        taxLineBeforeReverse = moveLine.getTaxLineBeforeReverse();
        taxLineRC =
            taxEquiv.getReverseChargeTax().getActiveTaxLine() != null
                ? taxEquiv.getReverseChargeTax().getActiveTaxLine()
                : taxService.getTaxLine(
                    taxEquiv.getReverseChargeTax(), appBaseService.getTodayDate(move.getCompany()));
      }
    }

    if (newAccount == null) {
      newAccount =
          this.getTaxAccount(
              taxLine, company, accountType, move.getJournal(), partner, moveLine, move);
    }

    if (newAccount == null) {
      if (fiscalPosition != null) {
        throw new AxelorException(
            move,
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(AccountExceptionMessage.MOVE_LINE_MISSING_ACCOUNT_ON_TAX_AND_FISCAL_POSITION),
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

    Integer vatSystem = moveLine.getVatSystemSelect();
    if (moveLine.getVatSystemSelect() == null
        || moveLine.getVatSystemSelect() == AccountRepository.VAT_SYSTEM_DEFAULT) {
      vatSystem = moveLineTaxService.getVatSystem(move, moveLine);
    }

    String newSourceTaxLineKey = newAccount.getCode() + taxLine.getId() + " " + vatSystem;
    if (taxLineRC != null) {
      newAccountRC =
          this.getTaxAccount(
              taxLineRC, company, accountType, move.getJournal(), partner, moveLine, move);
      if (newAccountRC != null) {
        newSourceTaxLineRCKey = newAccountRC.getCode() + taxLineRC.getId() + " " + vatSystem;
      }
    }
    MoveLine newOrUpdatedMoveLine = new MoveLine();
    MoveLine newOrUpdatedMoveLineRC = null;

    if (!map.containsKey(newSourceTaxLineKey) && !newMap.containsKey(newSourceTaxLineKey)) {

      newOrUpdatedMoveLine = this.createMoveLine(date, taxLine, newAccount, move);
    } else if (newMap.containsKey(newSourceTaxLineKey)) {
      newOrUpdatedMoveLine = newMap.get(newSourceTaxLineKey);
    } else {
      newOrUpdatedMoveLine = map.get(newSourceTaxLineKey);
    }
    if (newSourceTaxLineRCKey != null) {
      if (!map.containsKey(newSourceTaxLineRCKey) && !newMap.containsKey(newSourceTaxLineRCKey)) {

        newOrUpdatedMoveLineRC = this.createMoveLine(date, taxLineRC, newAccountRC, move);
      } else if (newMap.containsKey(newSourceTaxLineRCKey)) {
        newOrUpdatedMoveLineRC = newMap.get(newSourceTaxLineRCKey);
      } else {
        newOrUpdatedMoveLineRC = map.get(newSourceTaxLineRCKey);
      }
    }
    newOrUpdatedMoveLine.setMove(move);
    newOrUpdatedMoveLine = moveLineToolService.setCurrencyAmount(newOrUpdatedMoveLine);
    newOrUpdatedMoveLine.setVatSystemSelect(vatSystem);
    newOrUpdatedMoveLine.setOrigin(move.getOrigin());
    newOrUpdatedMoveLine.setDescription(
        StringTool.cutTooLongString(
            moveLineToolService.determineDescriptionMoveLine(
                move.getJournal(), move.getOrigin(), move.getDescription())));

    BigDecimal taxLineValue =
        taxLineBeforeReverse != null ? taxLineBeforeReverse.getValue() : taxLine.getValue();

    if (percentMoveTemplate) {
      debit = sumMoveLinesByAccountType(move.getMoveLineList(), AccountTypeRepository.TYPE_PAYABLE);
      credit =
          sumMoveLinesByAccountType(move.getMoveLineList(), AccountTypeRepository.TYPE_RECEIVABLE);
    }

    BigDecimal newMoveLineDebit =
        debit
            .multiply(taxLineValue)
            .divide(
                BigDecimal.valueOf(100),
                AppBaseService.DEFAULT_NB_DECIMAL_DIGITS,
                RoundingMode.HALF_UP);
    BigDecimal newMoveLineCredit =
        credit
            .multiply(taxLineValue)
            .divide(
                BigDecimal.valueOf(100),
                AppBaseService.DEFAULT_NB_DECIMAL_DIGITS,
                RoundingMode.HALF_UP);
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

    if (newOrUpdatedMoveLineRC != null) {
      newOrUpdatedMoveLineRC.setMove(move);
      newOrUpdatedMoveLineRC.setVatSystemSelect(vatSystem);
      newOrUpdatedMoveLineRC.setOrigin(move.getOrigin());
      newOrUpdatedMoveLineRC.setDescription(
          StringTool.cutTooLongString(
              moveLineToolService.determineDescriptionMoveLine(
                  move.getJournal(), move.getOrigin(), move.getDescription())));

      newOrUpdatedMoveLineRC.setDebit(newOrUpdatedMoveLineRC.getDebit().add(newMoveLineCredit));
      newOrUpdatedMoveLineRC.setCredit(newOrUpdatedMoveLineRC.getCredit().add(newMoveLineDebit));
      newOrUpdatedMoveLineRC = moveLineToolService.setCurrencyAmount(newOrUpdatedMoveLineRC);

      newOrUpdatedMoveLineRC.setOriginDate(move.getOriginDate());

      if (newOrUpdatedMoveLineRC.getPartner() == null) {
        newOrUpdatedMoveLineRC.setPartner(move.getPartner());
      }

      if (newOrUpdatedMoveLineRC.getDebit().signum() != 0
          || newOrUpdatedMoveLineRC.getCredit().signum() != 0) {
        newMap.put(newSourceTaxLineRCKey, newOrUpdatedMoveLineRC);
      }

      moveLineToolService.setCurrencyAmount(newOrUpdatedMoveLineRC);
    }
    return newOrUpdatedMoveLine;
  }

  protected void setTaxLineAmount(BigDecimal debit, BigDecimal credit, MoveLine moveLine) {
    if ((debit.compareTo(BigDecimal.ZERO) > 0
            && moveLine.getCredit().compareTo(BigDecimal.ZERO) > 0)
        || (credit.compareTo(BigDecimal.ZERO) > 0
            && moveLine.getDebit().compareTo(BigDecimal.ZERO) > 0)) {
      BigDecimal creditTaxLineDiff = moveLine.getCredit().subtract(credit).abs();
      BigDecimal debitTaxLineDiff = moveLine.getDebit().subtract(debit).abs();
      BigDecimal taxLineAMount = debitTaxLineDiff.subtract(creditTaxLineDiff);

      if (taxLineAMount.compareTo(BigDecimal.ZERO) > 0) {
        moveLine.setDebit(taxLineAMount);
        moveLine.setCredit(BigDecimal.ZERO);
      } else {
        moveLine.setDebit(BigDecimal.ZERO);
        moveLine.setCredit(taxLineAMount.abs());
      }
    } else {
      moveLine.setDebit(moveLine.getDebit().add(debit));
      moveLine.setCredit(moveLine.getCredit().add(credit));
    }
  }

  protected BigDecimal sumMoveLinesByAccountType(List<MoveLine> moveLines, String accountType) {
    return moveLines.stream()
        .filter(ml -> ml.getAccount().getAccountType().getTechnicalTypeSelect().equals(accountType))
        .map(it -> it.getDebit().add(it.getCredit()))
        .reduce(BigDecimal::add)
        .orElse(BigDecimal.ZERO);
  }

  protected Account getTaxAccount(
      TaxLine taxLine,
      Company company,
      String accountType,
      Journal journal,
      Partner partner,
      MoveLine moveLine,
      Move move)
      throws AxelorException {
    Account newAccount = null;

    if (accountType.equals(AccountTypeRepository.TYPE_DEBT)
        || accountType.equals(AccountTypeRepository.TYPE_CHARGE)) {
      AccountingSituation accountingSituation =
          accountingSituationRepository.findByCompanyAndPartner(company, partner);

      int vatSystemSelect = this.getVatSystemSelect(accountingSituation, moveLine);

      newAccount =
          taxAccountService.getAccount(
              taxLine.getTax(),
              company,
              journal,
              vatSystemSelect,
              false,
              move.getFunctionalOriginSelect());

    } else if (accountType.equals(AccountTypeRepository.TYPE_INCOME)) {
      AccountingSituation accountingSituation =
          accountingSituationRepository.findByCompanyAndPartner(company, company.getPartner());

      int vatSystemSelect = this.getVatSystemSelect(accountingSituation, moveLine);
      newAccount =
          taxAccountService.getAccount(
              taxLine.getTax(),
              company,
              journal,
              vatSystemSelect,
              false,
              move.getFunctionalOriginSelect());
    } else if (accountType.equals(AccountTypeRepository.TYPE_IMMOBILISATION)) {

      AccountingSituation accountingSituation =
          accountingSituationRepository.findByCompanyAndPartner(company, partner);
      if (accountingSituation == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(AccountExceptionMessage.MISSING_VAT_SYSTEM_ON_MISSING_ACCOUNTING_SITUATION),
            partner.getFullName(),
            company.getCode());
      }
      int vatSystemSelect = this.getVatSystemSelect(accountingSituation, moveLine);

      newAccount =
          taxAccountService.getAccount(
              taxLine.getTax(),
              company,
              journal,
              vatSystemSelect,
              true,
              move.getFunctionalOriginSelect());
    }
    return newAccount;
  }

  protected int getVatSystemSelect(AccountingSituation accountingSituation, MoveLine moveLine)
      throws AxelorException {
    if (moveLine.getVatSystemSelect() == null
        || moveLine.getVatSystemSelect() == AccountRepository.VAT_SYSTEM_DEFAULT) {
      return accountingSituationService.determineVatSystemSelect(
          accountingSituation, moveLine.getAccount());
    }

    return moveLine.getVatSystemSelect();
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
      String description,
      LocalDate cutOffStartDate,
      LocalDate cutOffEndDate)
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
    moveLine.setCutOffStartDate(cutOffStartDate);
    moveLine.setCutOffEndDate(cutOffEndDate);
    return moveLine;
  }

  @Override
  public MoveLine createMoveLine(
      Move move,
      Partner partner,
      Account account,
      BigDecimal amount,
      boolean isDebit,
      TaxLine taxLine,
      LocalDate date,
      int ref,
      String origin,
      String description)
      throws AxelorException {

    MoveLine moveLine =
        this.createMoveLine(
            move, partner, account, amount, isDebit, date, date, ref, origin, description);

    if (taxLine != null) {
      moveLine.setTaxLine(taxLine);
      moveLine.setTaxRate(taxLine.getValue());
      moveLine.setTaxCode(taxLine.getTax() != null ? taxLine.getTax().getCode() : "");
    }

    return moveLine;
  }
}
