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
package com.axelor.apps.account.service.moveline;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AnalyticAccount;
import com.axelor.apps.account.db.AnalyticAxis;
import com.axelor.apps.account.db.AnalyticAxisByCompany;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLineTax;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.account.db.TaxPaymentMoveLine;
import com.axelor.apps.account.db.repo.AccountAnalyticRulesRepository;
import com.axelor.apps.account.db.repo.AccountConfigRepository;
import com.axelor.apps.account.db.repo.AccountTypeRepository;
import com.axelor.apps.account.db.repo.AnalyticAccountRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.IExceptionMessage;
import com.axelor.apps.account.service.AnalyticMoveLineService;
import com.axelor.apps.account.service.FiscalPositionAccountService;
import com.axelor.apps.account.service.TaxAccountService;
import com.axelor.apps.account.service.TaxPaymentMoveLineService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.payment.PaymentService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.tool.service.ListToolService;
import com.axelor.common.ObjectUtils;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoveLineServiceImpl implements MoveLineService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected MoveLineComputeAnalyticService moveLineComputeAnalyticService;
  protected MoveLineToolService moveLineToolService;
  protected TaxAccountService taxAccountService;
  protected FiscalPositionAccountService fiscalPositionAccountService;
  protected AnalyticMoveLineService analyticMoveLineService;
  protected CurrencyService currencyService;
  protected MoveLineRepository moveLineRepository;
  protected TaxPaymentMoveLineService taxPaymentMoveLineService;
  protected AnalyticAccountRepository analyticAccountRepository;
  protected AccountConfigService accountConfigService;
  protected AccountAnalyticRulesRepository accountAnalyticRulesRepository;
  protected ListToolService listToolService;
  protected InvoiceRepository invoiceRepository;
  protected PaymentService paymentService;
  protected MoveRepository moveRepository;
  protected AppBaseService appBaseService;

  @Inject
  public MoveLineServiceImpl(
      TaxAccountService taxAccountService,
      FiscalPositionAccountService fiscalPositionAccountService,
      AnalyticMoveLineService analyticMoveLineService,
      CurrencyService currencyService,
      MoveLineRepository moveLineRepository,
      TaxPaymentMoveLineService taxPaymentMoveLineService,
      AnalyticAccountRepository analyticAccountRepository,
      AccountConfigService accountConfigService,
      AccountAnalyticRulesRepository accountAnalyticRulesRepository,
      ListToolService listToolService,
      InvoiceRepository invoiceRepository,
      PaymentService paymentService,
      MoveRepository moveRepository,
      AppBaseService appBaseService,
      MoveLineComputeAnalyticService moveLineComputeAnalyticService,
      MoveLineToolService moveLineToolService) {
    this.taxAccountService = taxAccountService;
    this.fiscalPositionAccountService = fiscalPositionAccountService;
    this.analyticMoveLineService = analyticMoveLineService;
    this.currencyService = currencyService;
    this.moveLineRepository = moveLineRepository;
    this.taxPaymentMoveLineService = taxPaymentMoveLineService;
    this.analyticAccountRepository = analyticAccountRepository;
    this.accountConfigService = accountConfigService;
    this.accountAnalyticRulesRepository = accountAnalyticRulesRepository;
    this.listToolService = listToolService;
    this.invoiceRepository = invoiceRepository;
    this.paymentService = paymentService;
    this.moveRepository = moveRepository;
    this.appBaseService = appBaseService;
    this.moveLineComputeAnalyticService = moveLineComputeAnalyticService;
    this.moveLineToolService = moveLineToolService;
  }

  @Override
  public MoveLine balanceCreditDebit(MoveLine moveLine, Move move) {
    if (move.getMoveLineList() != null) {
      BigDecimal totalCredit =
          move.getMoveLineList().stream()
              .map(it -> it.getCredit())
              .reduce((a, b) -> a.add(b))
              .orElse(BigDecimal.ZERO);
      BigDecimal totalDebit =
          move.getMoveLineList().stream()
              .map(it -> it.getDebit())
              .reduce((a, b) -> a.add(b))
              .orElse(BigDecimal.ZERO);
      if (totalCredit.compareTo(totalDebit) < 0) {
        moveLine.setCredit(totalDebit.subtract(totalCredit));
      } else if (totalCredit.compareTo(totalDebit) > 0) {
        moveLine.setDebit(totalCredit.subtract(totalDebit));
      }
    }
    return moveLine;
  }

  // TODO: Deplacer cette methode dans un service Invoice
  /**
   * Procédure permettant d'impacter la case à cocher "Passage à l'huissier" sur la facture liée à
   * l'écriture
   *
   * @param moveLine Une ligne d'écriture
   */
  @Override
  @Transactional
  public void usherProcess(MoveLine moveLine) {

    Invoice invoice = moveLine.getMove().getInvoice();
    if (invoice != null) {
      if (moveLine.getUsherPassageOk()) {
        invoice.setUsherPassageOk(true);
      } else {
        invoice.setUsherPassageOk(false);
      }
      invoiceRepository.save(invoice);
    }
  }

  /**
   * Method used to reconcile the move line list passed as a parameter
   *
   * @param moveLineList
   */
  @Override
  public void reconcileMoveLinesWithCacheManagement(List<MoveLine> moveLineList) {

    List<MoveLine> reconciliableCreditMoveLineList =
        moveLineToolService.getReconciliableCreditMoveLines(moveLineList);
    List<MoveLine> reconciliableDebitMoveLineList =
        moveLineToolService.getReconciliableDebitMoveLines(moveLineList);

    Map<List<Object>, Pair<List<MoveLine>, List<MoveLine>>> moveLineMap = new HashMap<>();

    populateCredit(moveLineMap, reconciliableCreditMoveLineList);

    populateDebit(moveLineMap, reconciliableDebitMoveLineList);

    Comparator<MoveLine> byDate = Comparator.comparing(MoveLine::getDate);

    for (Pair<List<MoveLine>, List<MoveLine>> moveLineLists : moveLineMap.values()) {
      try {
        moveLineLists = this.findMoveLineLists(moveLineLists);
        this.useExcessPaymentOnMoveLinesDontThrow(byDate, paymentService, moveLineLists);
      } catch (Exception e) {
        TraceBackService.trace(e);
        log.debug(e.getMessage());
      } finally {
        JPA.clear();
      }
    }
  }

  protected Pair<List<MoveLine>, List<MoveLine>> findMoveLineLists(
      Pair<List<MoveLine>, List<MoveLine>> moveLineLists) {
    List<MoveLine> fetchedDebitMoveLineList =
        moveLineLists.getLeft().stream()
            .map(moveLine -> moveLineRepository.find(moveLine.getId()))
            .collect(Collectors.toList());
    List<MoveLine> fetchedCreditMoveLineList =
        moveLineLists.getRight().stream()
            .map(moveLine -> moveLineRepository.find(moveLine.getId()))
            .collect(Collectors.toList());
    return Pair.of(fetchedDebitMoveLineList, fetchedCreditMoveLineList);
  }

  @Override
  @Transactional
  public void reconcileMoveLines(List<MoveLine> moveLineList) {
    List<MoveLine> reconciliableCreditMoveLineList =
        moveLineToolService.getReconciliableCreditMoveLines(moveLineList);
    List<MoveLine> reconciliableDebitMoveLineList =
        moveLineToolService.getReconciliableDebitMoveLines(moveLineList);

    Map<List<Object>, Pair<List<MoveLine>, List<MoveLine>>> moveLineMap = new HashMap<>();

    populateCredit(moveLineMap, reconciliableCreditMoveLineList);

    populateDebit(moveLineMap, reconciliableDebitMoveLineList);

    Comparator<MoveLine> byDate = Comparator.comparing(MoveLine::getDate);

    for (Pair<List<MoveLine>, List<MoveLine>> moveLineLists : moveLineMap.values()) {
      List<MoveLine> companyPartnerCreditMoveLineList = moveLineLists.getLeft();
      List<MoveLine> companyPartnerDebitMoveLineList = moveLineLists.getRight();
      companyPartnerCreditMoveLineList.sort(byDate);
      companyPartnerDebitMoveLineList.sort(byDate);
      paymentService.useExcessPaymentOnMoveLinesDontThrow(
          companyPartnerDebitMoveLineList, companyPartnerCreditMoveLineList);
    }
  }

  @Transactional
  protected void useExcessPaymentOnMoveLinesDontThrow(
      Comparator<MoveLine> byDate,
      PaymentService paymentService,
      Pair<List<MoveLine>, List<MoveLine>> moveLineLists) {
    List<MoveLine> companyPartnerCreditMoveLineList = moveLineLists.getLeft();
    List<MoveLine> companyPartnerDebitMoveLineList = moveLineLists.getRight();
    companyPartnerCreditMoveLineList.sort(byDate);
    companyPartnerDebitMoveLineList.sort(byDate);
    paymentService.useExcessPaymentOnMoveLinesDontThrow(
        companyPartnerDebitMoveLineList, companyPartnerCreditMoveLineList);
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

  // TODO : DEPLACER UNE PARTIE DE CA DANS MOVESERVICE.
  // ON DOIT SEULEMENT GERER LA PARTIE CREATION DE MOVE LINE.
  // PAS DE moveRepository.save(move).
  @Override
  @Transactional
  public void autoTaxLineGenerate(Move move) throws AxelorException {

    List<MoveLine> moveLineList = move.getMoveLineList();

    moveLineList.sort(
        new Comparator<MoveLine>() {
          @Override
          public int compare(MoveLine o1, MoveLine o2) {
            if (o2.getSourceTaxLine() != null) {
              return 0;
            }
            return -1;
          }
        });

    Iterator<MoveLine> moveLineItr = moveLineList.iterator();

    Map<String, MoveLine> map = new HashMap<>();
    Map<String, MoveLine> newMap = new HashMap<>();

    while (moveLineItr.hasNext()) {

      MoveLine moveLine = moveLineItr.next();

      TaxLine taxLine = moveLine.getTaxLine();
      TaxLine sourceTaxLine = moveLine.getSourceTaxLine();

      if (sourceTaxLine != null) {

        String sourceTaxLineKey = moveLine.getAccount().getCode() + sourceTaxLine.getId();

        map.put(sourceTaxLineKey, moveLine);
        moveLineItr.remove();
        continue;
      }

      if (taxLine != null) {

        String accountType = moveLine.getAccount().getAccountType().getTechnicalTypeSelect();

        if (accountType.equals(AccountTypeRepository.TYPE_DEBT)
            || accountType.equals(AccountTypeRepository.TYPE_CHARGE)
            || accountType.equals(AccountTypeRepository.TYPE_INCOME)
            || accountType.equals(AccountTypeRepository.TYPE_ASSET)) {

          BigDecimal debit = moveLine.getDebit();
          BigDecimal credit = moveLine.getCredit();
          BigDecimal currencyRate = moveLine.getCurrencyRate();
          BigDecimal currencyAmount;
          LocalDate date = moveLine.getDate();
          Company company = move.getCompany();

          MoveLine newOrUpdatedMoveLine = new MoveLine();

          if (accountType.equals(AccountTypeRepository.TYPE_DEBT)
              || accountType.equals(AccountTypeRepository.TYPE_CHARGE)) {
            newOrUpdatedMoveLine.setAccount(
                taxAccountService.getAccount(taxLine.getTax(), company, true, false));
          } else if (accountType.equals(AccountTypeRepository.TYPE_INCOME)) {
            newOrUpdatedMoveLine.setAccount(
                taxAccountService.getAccount(taxLine.getTax(), company, false, false));
          } else if (accountType.equals(AccountTypeRepository.TYPE_ASSET)) {
            newOrUpdatedMoveLine.setAccount(
                taxAccountService.getAccount(taxLine.getTax(), company, true, true));
          }

          Account newAccount = newOrUpdatedMoveLine.getAccount();
          if (newAccount == null) {
            throw new AxelorException(
                move,
                TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
                I18n.get(IExceptionMessage.MOVE_LINE_6),
                taxLine.getName(),
                company.getName());
          }
          if (move.getPartner().getFiscalPosition() != null) {
            newAccount =
                fiscalPositionAccountService.getAccount(
                    move.getPartner().getFiscalPosition(), newAccount);
          }

          String newSourceTaxLineKey = newAccount.getCode() + taxLine.getId();

          if (!map.containsKey(newSourceTaxLineKey) && !newMap.containsKey(newSourceTaxLineKey)) {

            newOrUpdatedMoveLine =
                this.createNewMoveLine(
                    debit, credit, date, accountType, taxLine, newOrUpdatedMoveLine);
          } else {

            if (newMap.containsKey(newSourceTaxLineKey)) {
              newOrUpdatedMoveLine = newMap.get(newSourceTaxLineKey);
            } else if (!newMap.containsKey(newSourceTaxLineKey)
                && map.containsKey(newSourceTaxLineKey)) {
              newOrUpdatedMoveLine = map.get(newSourceTaxLineKey);
            }
            newOrUpdatedMoveLine.setDebit(debit.multiply(taxLine.getValue()));
            newOrUpdatedMoveLine.setCredit(credit.multiply(taxLine.getValue()));
          }
          newOrUpdatedMoveLine.setMove(move);
          newOrUpdatedMoveLine = setCurrencyAmount(newOrUpdatedMoveLine);
          newOrUpdatedMoveLine.setOrigin(move.getOrigin());
          newOrUpdatedMoveLine.setDescription(move.getDescription());
          newOrUpdatedMoveLine.setOriginDate(move.getOriginDate());
          newMap.put(newSourceTaxLineKey, newOrUpdatedMoveLine);
        }
      }
    }

    moveLineList.addAll(newMap.values());
    moveRepository.save(move);
  }

  @Override
  public MoveLine createNewMoveLine(
      BigDecimal debit,
      BigDecimal credit,
      LocalDate date,
      String accountType,
      TaxLine taxLine,
      MoveLine newOrUpdatedMoveLine) {

    newOrUpdatedMoveLine.setSourceTaxLine(taxLine);
    newOrUpdatedMoveLine.setDebit(debit.multiply(taxLine.getValue()));
    newOrUpdatedMoveLine.setCredit(credit.multiply(taxLine.getValue()));
    newOrUpdatedMoveLine.setDescription(taxLine.getTax().getName());
    newOrUpdatedMoveLine.setDate(date);

    return newOrUpdatedMoveLine;
  }

  @Override
  public void validateMoveLine(MoveLine moveLine) throws AxelorException {
    if (moveLine.getDebit().compareTo(BigDecimal.ZERO) == 0
        && moveLine.getCredit().compareTo(BigDecimal.ZERO) == 0
        && moveLine.getCurrencyAmount().compareTo(BigDecimal.ZERO) == 0) {
      throw new AxelorException(
          moveLine,
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(IExceptionMessage.MOVE_LINE_7),
          moveLine.getAccount().getCode());
    }
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public MoveLine generateTaxPaymentMoveLineList(
      MoveLine customerMoveLine, Invoice invoice, Reconcile reconcile) throws AxelorException {
    BigDecimal paymentAmount = reconcile.getAmount();
    BigDecimal invoiceTotalAmount = invoice.getCompanyInTaxTotal();
    for (InvoiceLineTax invoiceLineTax : invoice.getInvoiceLineTaxList()) {

      TaxLine taxLine = invoiceLineTax.getTaxLine();
      BigDecimal vatRate = taxLine.getValue();
      BigDecimal baseAmount = invoiceLineTax.getCompanyExTaxBase();
      BigDecimal detailPaymentAmount =
          baseAmount
              .multiply(paymentAmount)
              .divide(invoiceTotalAmount, 6, RoundingMode.HALF_UP)
              .setScale(2, RoundingMode.HALF_UP);

      TaxPaymentMoveLine taxPaymentMoveLine =
          new TaxPaymentMoveLine(
              customerMoveLine,
              taxLine,
              reconcile,
              vatRate,
              detailPaymentAmount,
              appBaseService.getTodayDate(reconcile.getCompany()));

      taxPaymentMoveLine = taxPaymentMoveLineService.computeTaxAmount(taxPaymentMoveLine);

      customerMoveLine.addTaxPaymentMoveLineListItem(taxPaymentMoveLine);
    }
    this.computeTaxAmount(customerMoveLine);
    return moveLineRepository.save(customerMoveLine);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public MoveLine reverseTaxPaymentMoveLines(MoveLine customerMoveLine, Reconcile reconcile)
      throws AxelorException {
    List<TaxPaymentMoveLine> reverseTaxPaymentMoveLines = new ArrayList<TaxPaymentMoveLine>();
    for (TaxPaymentMoveLine taxPaymentMoveLine : customerMoveLine.getTaxPaymentMoveLineList()) {
      if (!taxPaymentMoveLine.getIsAlreadyReverse()
          && taxPaymentMoveLine.getReconcile().equals(reconcile)) {
        TaxPaymentMoveLine reverseTaxPaymentMoveLine =
            taxPaymentMoveLineService.getReverseTaxPaymentMoveLine(taxPaymentMoveLine);

        reverseTaxPaymentMoveLines.add(reverseTaxPaymentMoveLine);
      }
    }
    for (TaxPaymentMoveLine reverseTaxPaymentMoveLine : reverseTaxPaymentMoveLines) {
      customerMoveLine.addTaxPaymentMoveLineListItem(reverseTaxPaymentMoveLine);
    }
    this.computeTaxAmount(customerMoveLine);
    return moveLineRepository.save(customerMoveLine);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public MoveLine computeTaxAmount(MoveLine moveLine) throws AxelorException {
    moveLine.setTaxAmount(BigDecimal.ZERO);
    if (!ObjectUtils.isEmpty(moveLine.getTaxPaymentMoveLineList())) {
      for (TaxPaymentMoveLine taxPaymentMoveLine : moveLine.getTaxPaymentMoveLineList()) {
        moveLine.setTaxAmount(moveLine.getTaxAmount().add(taxPaymentMoveLine.getTaxAmount()));
      }
    }
    return moveLine;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public MoveLine setIsSelectedBankReconciliation(MoveLine moveLine) {
    moveLine.setIsSelectedBankReconciliation(!moveLine.getIsSelectedBankReconciliation());
    return moveLineRepository.save(moveLine);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public MoveLine removePostedNbr(MoveLine moveLine, String postedNbr) {
    String posted = moveLine.getPostedNbr();
    List<String> postedNbrs = new ArrayList<String>(Arrays.asList(posted.split(",")));
    postedNbrs.remove(postedNbr);
    posted = String.join(",", postedNbrs);
    moveLine.setPostedNbr(posted);
    return moveLine;
  }

  public MoveLine setCurrencyAmount(MoveLine moveLine) {
    Move move = moveLine.getMove();
    if (move.getMoveLineList().size() == 0) {
      try {
        moveLine.setCurrencyRate(
            currencyService.getCurrencyConversionRate(
                move.getCurrency(), move.getCompany().getCurrency()));
      } catch (AxelorException e1) {
        TraceBackService.trace(e1);
      }
    } else {
      moveLine.setCurrencyRate(move.getMoveLineList().get(0).getCurrencyRate());
    }
    if (!move.getCurrency().equals(move.getCompany().getCurrency())) {
      BigDecimal unratedAmount = moveLine.getDebit().add(moveLine.getCredit());
      moveLine.setCurrencyAmount(
          unratedAmount.divide(moveLine.getCurrencyRate(), MathContext.DECIMAL128));
    }
    return moveLine;
  }

  @Override
  public MoveLine analyzeMoveLine(MoveLine moveLine) throws AxelorException {
    if (moveLine != null) {

      if (moveLine.getAnalyticMoveLineList() == null) {
        moveLine.setAnalyticMoveLineList(new ArrayList<>());
      } else {
        moveLine.getAnalyticMoveLineList().clear();
      }

      AnalyticMoveLine analyticMoveLine = null;

      if (moveLine.getAxis1AnalyticAccount() != null) {
        analyticMoveLine =
            analyticMoveLineService.computeAnalyticMoveLine(
                moveLine, moveLine.getAxis1AnalyticAccount());
        moveLine.addAnalyticMoveLineListItem(analyticMoveLine);
      }
      if (moveLine.getAxis2AnalyticAccount() != null) {
        analyticMoveLine =
            analyticMoveLineService.computeAnalyticMoveLine(
                moveLine, moveLine.getAxis2AnalyticAccount());
        moveLine.addAnalyticMoveLineListItem(analyticMoveLine);
      }
      if (moveLine.getAxis3AnalyticAccount() != null) {
        analyticMoveLine =
            analyticMoveLineService.computeAnalyticMoveLine(
                moveLine, moveLine.getAxis3AnalyticAccount());
        moveLine.addAnalyticMoveLineListItem(analyticMoveLine);
      }
      if (moveLine.getAxis4AnalyticAccount() != null) {
        analyticMoveLine =
            analyticMoveLineService.computeAnalyticMoveLine(
                moveLine, moveLine.getAxis4AnalyticAccount());
        moveLine.addAnalyticMoveLineListItem(analyticMoveLine);
      }
      if (moveLine.getAxis5AnalyticAccount() != null) {
        analyticMoveLine =
            analyticMoveLineService.computeAnalyticMoveLine(
                moveLine, moveLine.getAxis5AnalyticAccount());
        moveLine.addAnalyticMoveLineListItem(analyticMoveLine);
      }
    }
    return moveLine;
  }

  @Override
  public boolean compareNbrOfAnalyticAxisSelect(int position, MoveLine moveLine)
      throws AxelorException {
    return moveLine != null
        && moveLine.getMove() != null
        && moveLine.getMove().getCompany() != null
        && position
            <= accountConfigService
                .getAccountConfig(moveLine.getMove().getCompany())
                .getNbrOfAnalyticAxisSelect();
  }

  public List<Long> setAxisDomains(MoveLine moveLine, int position) throws AxelorException {
    List<Long> analyticAccountListByAxis = new ArrayList<Long>();
    List<Long> analyticAccountListByRules = new ArrayList<Long>();

    AnalyticAxis analyticAxis = new AnalyticAxis();

    if (compareNbrOfAnalyticAxisSelect(position, moveLine)) {

      for (AnalyticAxisByCompany axis :
          accountConfigService
              .getAccountConfig(moveLine.getMove().getCompany())
              .getAnalyticAxisByCompanyList()) {
        if (axis.getOrderSelect() == position) {
          analyticAxis = axis.getAnalyticAxis();
        }
      }

      for (AnalyticAccount analyticAccount :
          analyticAccountRepository.findByAnalyticAxis(analyticAxis).fetch()) {
        analyticAccountListByAxis.add(analyticAccount.getId());
      }
      if (moveLine.getAccount() != null) {
        List<AnalyticAccount> analyticAccountList =
            accountAnalyticRulesRepository.findAnalyticAccountByAccounts(moveLine.getAccount());
        if (!analyticAccountList.isEmpty()) {
          for (AnalyticAccount analyticAccount : analyticAccountList) {
            analyticAccountListByRules.add(analyticAccount.getId());
          }
          analyticAccountListByAxis =
              listToolService.intersection(analyticAccountListByAxis, analyticAccountListByRules);
        }
      }
    }
    return analyticAccountListByAxis;
  }

  @Override
  public MoveLine selectDefaultDistributionTemplate(MoveLine moveLine) throws AxelorException {
    if (moveLine != null && moveLine.getAccount() != null) {
      if (moveLine.getAccount().getAnalyticDistributionAuthorized()
          && moveLine.getAccount().getAnalyticDistributionTemplate() != null
          && accountConfigService
                  .getAccountConfig(moveLine.getAccount().getCompany())
                  .getAnalyticDistributionTypeSelect()
              == AccountConfigRepository.DISTRIBUTION_TYPE_PRODUCT) {
        moveLine.setAnalyticDistributionTemplate(
            moveLine.getAccount().getAnalyticDistributionTemplate());
      }
    } else {
      moveLine.setAnalyticDistributionTemplate(null);
    }
    moveLine.getAnalyticMoveLineList().clear();
    moveLine = moveLineComputeAnalyticService.computeAnalyticDistribution(moveLine);
    return moveLine;
  }
}
