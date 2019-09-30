/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AnalyticAccount;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.InvoiceLineTax;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Tax;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.repo.AnalyticMoveLineRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.AccountManagementAccountService;
import com.axelor.apps.account.service.AnalyticMoveLineService;
import com.axelor.apps.account.service.FiscalPositionServiceAccountImpl;
import com.axelor.apps.account.service.TaxAccountService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.payment.PaymentService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.AppAccountRepository;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.config.CompanyConfigService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoveLineService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected AccountManagementAccountService accountManagementService;
  protected TaxAccountService taxAccountService;
  protected FiscalPositionServiceAccountImpl fiscalPositionService;
  protected AnalyticMoveLineService analyticMoveLineService;
  protected AppAccountService appAccountService;
  protected CurrencyService currencyService;
  protected CompanyConfigService companyConfigService;

  public static final boolean IS_CREDIT = false;
  public static final boolean IS_DEBIT = true;

  @Inject
  public MoveLineService(
      AccountManagementAccountService accountManagementService,
      TaxAccountService taxAccountService,
      FiscalPositionServiceAccountImpl fiscalPositionService,
      AppAccountService appAccountService,
      AnalyticMoveLineService analyticMoveLineService,
      CurrencyService currencyService,
      CompanyConfigService companyConfigService) {
    this.accountManagementService = accountManagementService;
    this.taxAccountService = taxAccountService;
    this.fiscalPositionService = fiscalPositionService;
    this.analyticMoveLineService = analyticMoveLineService;
    this.appAccountService = appAccountService;
    this.currencyService = currencyService;
    this.companyConfigService = companyConfigService;
  }

  public MoveLine computeAnalyticDistribution(MoveLine moveLine) {

    List<AnalyticMoveLine> analyticMoveLineList = moveLine.getAnalyticMoveLineList();
    if (analyticMoveLineList != null
        && appAccountService.getAppAccount().getAnalyticDistributionTypeSelect()
            != AppAccountRepository.DISTRIBUTION_TYPE_FREE) {
      for (AnalyticMoveLine analyticDistributionLine : analyticMoveLineList) {
        analyticDistributionLine.setMoveLine(moveLine);
        analyticDistributionLine.setAccount(moveLine.getAccount());
        analyticDistributionLine.setAccountType(moveLine.getAccount().getAccountType());
        analyticDistributionLine.setAmount(
            analyticMoveLineService.computeAmount(analyticDistributionLine));
        analyticDistributionLine.setDate(appAccountService.getTodayDate());
      }
    }
    return moveLine;
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

    if (partner != null) {
      account = fiscalPositionService.getAccount(partner.getFiscalPosition(), account);
    }

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
        counter,
        origin,
        description);
  }

  /**
   * Creating accounting move line method using all currency informations (amount in specific move
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
      account = fiscalPositionService.getAccount(partner.getFiscalPosition(), account);
    }

    BigDecimal debit = BigDecimal.ZERO;
    BigDecimal credit = BigDecimal.ZERO;

    if (amountInCompanyCurrency.compareTo(BigDecimal.ZERO) == -1) {
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
            amountInCompanyCurrency.divide(amountInSpecificMoveCurrency, 5, RoundingMode.HALF_EVEN);
      }
    }

    return new MoveLine(
        move,
        partner,
        account,
        date,
        dueDate,
        counter,
        debit,
        credit,
        this.determineDescriptionMoveLine(move.getJournal(), origin, description),
        origin,
        currencyRate.setScale(5, RoundingMode.HALF_EVEN),
        amountInSpecificMoveCurrency);
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

    log.debug(
        "Création des lignes d'écriture comptable de la facture/l'avoir {}",
        invoice.getInvoiceId());

    List<MoveLine> moveLines = new ArrayList<MoveLine>();

    Set<AnalyticAccount> analyticAccounts = new HashSet<AnalyticAccount>();

    int moveLineId = 1;

    if (partner == null) {
      throw new AxelorException(
          invoice,
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(IExceptionMessage.MOVE_LINE_1),
          invoice.getInvoiceId());
    }
    if (partnerAccount == null) {
      throw new AxelorException(
          invoice,
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(IExceptionMessage.MOVE_LINE_2),
          invoice.getInvoiceId());
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
            moveLineId++,
            invoice.getInvoiceId(),
            null);
    moveLines.add(moveLine1);

    AnalyticMoveLineRepository analyticMoveLineRepository =
        Beans.get(AnalyticMoveLineRepository.class);

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
              I18n.get(IExceptionMessage.MOVE_LINE_4),
              invoiceLine.getName(),
              company.getName());
        }

        companyExTaxTotal = invoiceLine.getCompanyExTaxTotal();

        log.debug(
            "Traitement de la ligne de facture : compte comptable = {}, montant = {}",
            new Object[] {account.getName(), companyExTaxTotal});

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
                moveLineId++,
                invoice.getInvoiceId(),
                invoiceLine.getProductName());

        if (invoiceLine.getAnalyticMoveLineList() != null) {
          for (AnalyticMoveLine invoiceAnalyticMoveLine : invoiceLine.getAnalyticMoveLineList()) {
            AnalyticMoveLine analyticMoveLine =
                analyticMoveLineRepository.copy(invoiceAnalyticMoveLine, false);
            analyticMoveLine.setTypeSelect(AnalyticMoveLineRepository.STATUS_REAL_ACCOUNTING);
            analyticMoveLine.setInvoiceLine(null);
            analyticMoveLine.setAccount(moveLine.getAccount());
            analyticMoveLine.setAccountType(moveLine.getAccount().getAccountType());
            moveLine.addAnalyticMoveLineListItem(analyticMoveLine);
          }
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

        Account account = taxAccountService.getAccount(tax, company);

        if (account == null) {
          throw new AxelorException(
              move,
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(IExceptionMessage.MOVE_LINE_6),
              tax.getName(),
              company.getName());
        }

        MoveLine moveLine =
            this.createMoveLine(
                move,
                partner,
                account,
                invoiceLineTax.getTaxTotal(),
                companyTaxTotal,
                null,
                !isDebitCustomer,
                invoice.getInvoiceDate(),
                null,
                moveLineId++,
                invoice.getInvoiceId(),
                null);
        moveLine.setTaxLine(invoiceLineTax.getTaxLine());
        moveLine.setTaxRate(invoiceLineTax.getTaxLine().getValue());
        moveLine.setTaxCode(tax.getCode());

        moveLines.add(moveLine);
      }
    }

    if (consolidate) {
      this.consolidateMoveLines(moveLines);
    }

    return moveLines;
  }

  public MoveLine findConsolidateMoveLine(
      Map<List<Object>, MoveLine> map, MoveLine moveLine, List<Object> keys) {
    if (map != null && !map.isEmpty()) {

      Map<List<Object>, MoveLine> copyMap = new HashMap<List<Object>, MoveLine>(map);
      while (!copyMap.isEmpty()) {

        if (map.containsKey(keys)) {

          MoveLine moveLineIt = map.get(keys);
          int count = 0;
          if (moveLineIt.getAnalyticMoveLineList() == null
              && moveLine.getAnalyticMoveLineList() == null) {
            return moveLineIt;
          } else if (moveLineIt.getAnalyticMoveLineList() == null
              || moveLine.getAnalyticMoveLineList() == null) {
            break;
          }
          List<AnalyticMoveLine> list1 = moveLineIt.getAnalyticMoveLineList();
          List<AnalyticMoveLine> list2 = moveLine.getAnalyticMoveLineList();
          List<AnalyticMoveLine> copyList = new ArrayList<AnalyticMoveLine>(list1);
          if (list1.size() == list2.size()) {
            for (AnalyticMoveLine analyticDistributionLine : list2) {
              for (AnalyticMoveLine analyticDistributionLineIt : copyList) {
                if (analyticDistributionLine
                        .getAnalyticAxis()
                        .equals(analyticDistributionLineIt.getAnalyticAxis())
                    && analyticDistributionLine
                        .getAnalyticAccount()
                        .equals(analyticDistributionLineIt.getAnalyticAccount())
                    && analyticDistributionLine
                        .getAccount()
                        .equals(analyticDistributionLineIt.getAccount())
                    && analyticDistributionLine
                        .getPercentage()
                        .equals(analyticDistributionLineIt.getPercentage())
                    && ((analyticDistributionLine.getAnalyticJournal() == null
                            && analyticDistributionLineIt.getAnalyticJournal() == null)
                        || analyticDistributionLine
                            .getAnalyticJournal()
                            .equals(analyticDistributionLineIt.getAnalyticJournal()))) {
                  copyList.remove(analyticDistributionLineIt);
                  count++;
                  break;
                }
              }
            }
            if (count == list1.size()) {
              return moveLineIt;
            }
          }
        } else {
          return null;
        }
      }
    }

    return null;
  }

  /**
   * Consolider des lignes d'écritures par compte comptable.
   *
   * @param moveLines
   */
  public List<MoveLine> consolidateMoveLines(List<MoveLine> moveLines) {

    Map<List<Object>, MoveLine> map = new HashMap<List<Object>, MoveLine>();
    MoveLine consolidateMoveLine = null;

    for (MoveLine moveLine : moveLines) {

      List<Object> keys = new ArrayList<Object>();

      keys.add(moveLine.getAccount());
      keys.add(moveLine.getTaxLine());

      consolidateMoveLine = this.findConsolidateMoveLine(map, moveLine, keys);
      if (consolidateMoveLine != null) {

        BigDecimal consolidateCurrencyAmount = BigDecimal.ZERO;

        log.debug(
            "MoveLine :: Debit : {}, Credit : {}, Currency amount : {}",
            moveLine.getDebit(),
            moveLine.getCredit(),
            moveLine.getCurrencyAmount());
        log.debug(
            "Consolidate moveLine :: Debit : {}, Credit : {}, Currency amount : {}",
            consolidateMoveLine.getDebit(),
            consolidateMoveLine.getCredit(),
            consolidateMoveLine.getCurrencyAmount());

        if (moveLine.getDebit().subtract(moveLine.getCredit()).compareTo(BigDecimal.ZERO)
            != consolidateMoveLine
                .getDebit()
                .subtract(consolidateMoveLine.getCredit())
                .compareTo(BigDecimal.ZERO)) {
          consolidateCurrencyAmount =
              consolidateMoveLine.getCurrencyAmount().subtract(moveLine.getCurrencyAmount());
        } else {
          consolidateCurrencyAmount =
              consolidateMoveLine.getCurrencyAmount().add(moveLine.getCurrencyAmount());
        }
        consolidateMoveLine.setCurrencyAmount(consolidateCurrencyAmount.abs());
        consolidateMoveLine.setCredit(consolidateMoveLine.getCredit().add(moveLine.getCredit()));
        consolidateMoveLine.setDebit(consolidateMoveLine.getDebit().add(moveLine.getDebit()));

        if (consolidateMoveLine.getAnalyticMoveLineList() != null
            && !consolidateMoveLine.getAnalyticMoveLineList().isEmpty()) {
          for (AnalyticMoveLine analyticDistributionLine :
              consolidateMoveLine.getAnalyticMoveLineList()) {
            for (AnalyticMoveLine analyticDistributionLineIt : moveLine.getAnalyticMoveLineList()) {
              if (analyticDistributionLine
                      .getAnalyticAxis()
                      .equals(analyticDistributionLineIt.getAnalyticAxis())
                  && analyticDistributionLine
                      .getAnalyticAccount()
                      .equals(analyticDistributionLineIt.getAnalyticAccount())
                  && analyticDistributionLine
                      .getAccount()
                      .equals(analyticDistributionLineIt.getAccount())
                  && analyticDistributionLine
                      .getPercentage()
                      .equals(analyticDistributionLineIt.getPercentage())
                  && ((analyticDistributionLine.getAnalyticJournal() == null
                          && analyticDistributionLineIt.getAnalyticJournal() == null)
                      || analyticDistributionLine
                          .getAnalyticJournal()
                          .equals(analyticDistributionLineIt.getAnalyticJournal()))) {
                analyticDistributionLine.setAmount(
                    analyticDistributionLine
                        .getAmount()
                        .add(analyticDistributionLineIt.getAmount()));
                break;
              }
            }
          }
        }
      } else {
        map.put(keys, moveLine);
      }
    }

    BigDecimal credit = null;
    BigDecimal debit = null;

    int moveLineId = 1;
    moveLines.clear();

    for (MoveLine moveLine : map.values()) {

      credit = moveLine.getCredit();
      debit = moveLine.getDebit();

      moveLine.setCurrencyAmount(moveLine.getCurrencyAmount().abs());

      if (debit.compareTo(BigDecimal.ZERO) == 1 && credit.compareTo(BigDecimal.ZERO) == 1) {

        if (debit.compareTo(credit) == 1) {
          moveLine.setDebit(debit.subtract(credit));
          moveLine.setCredit(BigDecimal.ZERO);
          moveLine.setCounter(moveLineId++);
          moveLines.add(moveLine);
        } else if (credit.compareTo(debit) == 1) {
          moveLine.setCredit(credit.subtract(debit));
          moveLine.setDebit(BigDecimal.ZERO);
          moveLine.setCounter(moveLineId++);
          moveLines.add(moveLine);
        }

      } else if (debit.compareTo(BigDecimal.ZERO) == 1 || credit.compareTo(BigDecimal.ZERO) == 1) {
        moveLine.setCounter(moveLineId++);
        moveLines.add(moveLine);
      }
    }

    return moveLines;
  }

  /**
   * Fonction permettant de récuperer la ligne d'écriture (au credit et non complétement lettrée sur
   * le compte client) de la facture
   *
   * @param invoice Une facture
   * @return
   */
  public MoveLine getCreditCustomerMoveLine(Invoice invoice) {
    if (invoice.getMove() != null) {
      return this.getCreditCustomerMoveLine(invoice.getMove());
    }
    return null;
  }

  /**
   * Fonction permettant de récuperer la ligne d'écriture (au credit et non complétement lettrée sur
   * le compte client) de l'écriture de facture
   *
   * @param move Une écriture de facture
   * @return
   */
  public MoveLine getCreditCustomerMoveLine(Move move) {
    for (MoveLine moveLine : move.getMoveLineList()) {
      if (moveLine.getAccount().getUseForPartnerBalance()
          && moveLine.getCredit().compareTo(BigDecimal.ZERO) > 0
          && moveLine.getAmountRemaining().compareTo(BigDecimal.ZERO) > 0) {
        return moveLine;
      }
    }
    return null;
  }

  /**
   * Fonction permettant de récuperer la ligne d'écriture (au débit et non complétement lettrée sur
   * le compte client) de la facture
   *
   * @param invoice Une facture
   * @return
   */
  public MoveLine getDebitCustomerMoveLine(Invoice invoice) {
    if (invoice.getMove() != null) {
      return this.getDebitCustomerMoveLine(invoice.getMove());
    }
    return null;
  }

  /**
   * Fonction permettant de récuperer la ligne d'écriture (au débit et non complétement lettrée sur
   * le compte client) de l'écriture de facture
   *
   * @param move Une écriture de facture
   * @return
   */
  public MoveLine getDebitCustomerMoveLine(Move move) {
    for (MoveLine moveLine : move.getMoveLineList()) {
      if (moveLine.getAccount().getUseForPartnerBalance()
          && moveLine.getDebit().compareTo(BigDecimal.ZERO) > 0
          && moveLine.getAmountRemaining().compareTo(BigDecimal.ZERO) > 0) {
        return moveLine;
      }
    }
    return null;
  }

  /**
   * Fonction permettant de générér automatiquement la description des lignes d'écritures
   *
   * @param journal Le journal de l'écriture
   * @param origin Le n° pièce réglée, facture, avoir ou de l'opération rejetée
   * @return
   */
  public String determineDescriptionMoveLine(Journal journal, String origin, String description) {
    String descriptionComputed = "";
    if (journal == null) {
      return "";
    }

    if (journal.getDescriptionModel() != null) {
      descriptionComputed += journal.getDescriptionModel();
    }

    if (journal.getDescriptionIdentificationOk() && origin != null) {
      if (!descriptionComputed.isEmpty()) {
        descriptionComputed += " ";
      }
      descriptionComputed += origin;
    }

    if (!journal.getIsInvoiceMoveConsolidated() && description != null) {
      if (!descriptionComputed.isEmpty()) {
        descriptionComputed += " - ";
      }
      descriptionComputed += description;
    }
    return descriptionComputed;
  }

  /**
   * Procédure permettant d'impacter la case à cocher "Passage à l'huissier" sur la facture liée à
   * l'écriture
   *
   * @param moveLine Une ligne d'écriture
   */
  @Transactional
  public void usherProcess(MoveLine moveLine) {

    Invoice invoice = moveLine.getMove().getInvoice();
    if (invoice != null) {
      if (moveLine.getUsherPassageOk()) {
        invoice.setUsherPassageOk(true);
      } else {
        invoice.setUsherPassageOk(false);
      }
      Beans.get(InvoiceRepository.class).save(invoice);
    }
  }

  /**
   * Method used to recover all credit reconciliable move line from a move line list
   *
   * @param moveLineList
   * @return reconciliableCreditMoveLineList
   */
  public List<MoveLine> getReconciliableCreditMoveLines(List<MoveLine> moveLineList) {

    List<MoveLine> reconciliableCreditMoveLineList = new ArrayList<>();

    for (MoveLine moveLine : moveLineList) {
      if (moveLine.getAccount().getReconcileOk()
          && moveLine.getCredit().compareTo(BigDecimal.ZERO) > 0
          && moveLine.getDebit().compareTo(BigDecimal.ZERO) == 0) {
        reconciliableCreditMoveLineList.add(moveLine);
      }
    }

    return reconciliableCreditMoveLineList;
  }

  /**
   * Method used to recover all debit reconciliable move line from a move line list
   *
   * @param moveLineList
   * @return reconciliableDebitMoveLineList
   */
  public List<MoveLine> getReconciliableDebitMoveLines(List<MoveLine> moveLineList) {

    List<MoveLine> reconciliableDebitMoveLineList = new ArrayList<>();

    for (MoveLine moveLine : moveLineList) {
      if (moveLine.getAccount().getReconcileOk()
          && moveLine.getCredit().compareTo(BigDecimal.ZERO) == 0
          && moveLine.getDebit().compareTo(BigDecimal.ZERO) > 0) {
        reconciliableDebitMoveLineList.add(moveLine);
      }
    }

    return reconciliableDebitMoveLineList;
  }

  /**
   * Method used to reconcile the move line list passed as a parameter
   *
   * @param creditMoveLineList
   * @param debitMoveLineList
   * @throws AxelorException
   */
  public void reconcileMoveLines(List<MoveLine> moveLineList) {

    List<MoveLine> reconciliableCreditMoveLineList = getReconciliableCreditMoveLines(moveLineList);
    List<MoveLine> reconciliableDebitMoveLineList = getReconciliableDebitMoveLines(moveLineList);

    Map<List<Object>, Pair<List<MoveLine>, List<MoveLine>>> moveLineMap = new HashMap<>();

    populateCredit(moveLineMap, reconciliableCreditMoveLineList);

    populateDebit(moveLineMap, reconciliableDebitMoveLineList);

    Comparator<MoveLine> byDate = Comparator.comparing(MoveLine::getDate);

    PaymentService paymentService = Beans.get(PaymentService.class);

    for (Pair<List<MoveLine>, List<MoveLine>> moveLineLists : moveLineMap.values()) {
      List<MoveLine> companyPartnerCreditMoveLineList = moveLineLists.getLeft();
      List<MoveLine> companyPartnerDebitMoveLineList = moveLineLists.getRight();
      companyPartnerCreditMoveLineList.sort(byDate);
      companyPartnerDebitMoveLineList.sort(byDate);
      paymentService.useExcessPaymentOnMoveLinesDontThrow(
          companyPartnerDebitMoveLineList, companyPartnerCreditMoveLineList);
    }
  }

  private void populateCredit(
      Map<List<Object>, Pair<List<MoveLine>, List<MoveLine>>> moveLineMap,
      List<MoveLine> reconciliableMoveLineList) {
    populateMoveLineMap(moveLineMap, reconciliableMoveLineList, true);
  }

  private void populateDebit(
      Map<List<Object>, Pair<List<MoveLine>, List<MoveLine>>> moveLineMap,
      List<MoveLine> reconciliableMoveLineList) {
    populateMoveLineMap(moveLineMap, reconciliableMoveLineList, false);
  }

  private void populateMoveLineMap(
      Map<List<Object>, Pair<List<MoveLine>, List<MoveLine>>> moveLineMap,
      List<MoveLine> reconciliableMoveLineList,
      boolean isCredit) {
    for (MoveLine moveLine : reconciliableMoveLineList) {

      Move move = moveLine.getMove();

      List<Object> keys = new ArrayList<Object>();

      keys.add(move.getCompany());
      keys.add(moveLine.getAccount());
      keys.add(moveLine.getPartner());

      Pair<List<MoveLine>, List<MoveLine>> moveLineLists = moveLineMap.get(keys);

      if (moveLineLists == null) {
        moveLineLists = Pair.of(new ArrayList<>(), new ArrayList<>());
        moveLineMap.put(keys, moveLineLists);
      }

      List<MoveLine> moveLineList = isCredit ? moveLineLists.getLeft() : moveLineLists.getRight();
      moveLineList.add(moveLine);
    }
  }
}
