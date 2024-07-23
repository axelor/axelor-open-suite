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
package com.axelor.apps.account.service.reconcile;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.repo.JournalTypeRepository;
import com.axelor.apps.account.db.repo.MoveLineRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.db.repo.ReconcileRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.AdvancePaymentMoveLineCreateService;
import com.axelor.apps.account.service.invoice.InvoiceTermToolService;
import com.axelor.apps.account.service.move.MoveAdjustementService;
import com.axelor.apps.account.service.move.MoveCreateService;
import com.axelor.apps.account.service.move.MoveLineControlService;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.move.PaymentMoveLineDistributionService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.account.service.moveline.MoveLineTaxService;
import com.axelor.apps.account.service.reconcile.reconcilegroup.ReconcileGroupService;
import com.axelor.apps.account.util.TaxConfiguration;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.db.Query;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import com.google.inject.servlet.RequestScoped;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequestScoped
public class ReconcileServiceImpl implements ReconcileService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected MoveToolService moveToolService;
  protected AccountConfigService accountConfigService;
  protected ReconcileRepository reconcileRepository;
  protected MoveAdjustementService moveAdjustementService;
  protected ReconcileSequenceService reconcileSequenceService;
  protected MoveLineTaxService moveLineTaxService;
  protected AppBaseService appBaseService;
  protected PaymentMoveLineDistributionService paymentMoveLineDistributionService;
  protected MoveLineControlService moveLineControlService;
  protected MoveLineRepository moveLineRepo;
  protected MoveCreateService moveCreateService;
  protected MoveLineCreateService moveLineCreateService;
  protected MoveValidateService moveValidateService;
  protected CurrencyScaleService currencyScaleService;
  protected ReconcileToolService reconcileToolService;
  protected ReconcileCheckService reconcileCheckService;
  protected ReconcileInvoiceTermComputationService reconcileInvoiceTermComputationService;
  protected InvoiceTermToolService invoiceTermToolService;
  protected AdvancePaymentMoveLineCreateService advancePaymentMoveLineCreateService;

  @Inject
  public ReconcileServiceImpl(
      MoveToolService moveToolService,
      AccountConfigService accountConfigService,
      ReconcileRepository reconcileRepository,
      MoveAdjustementService moveAdjustementService,
      ReconcileSequenceService reconcileSequenceService,
      MoveLineTaxService moveLineTaxService,
      AppBaseService appBaseService,
      PaymentMoveLineDistributionService paymentMoveLineDistributionService,
      MoveLineControlService moveLineControlService,
      MoveLineRepository moveLineRepo,
      MoveCreateService moveCreateService,
      MoveLineCreateService moveLineCreateService,
      MoveValidateService moveValidateService,
      CurrencyScaleService currencyScaleService,
      ReconcileToolService reconcileToolService,
      ReconcileCheckService reconcileCheckService,
      ReconcileInvoiceTermComputationService reconcileInvoiceTermComputationService,
      InvoiceTermToolService invoiceTermToolService,
      AdvancePaymentMoveLineCreateService advancePaymentMoveLineCreateService) {

    this.moveToolService = moveToolService;
    this.accountConfigService = accountConfigService;
    this.reconcileRepository = reconcileRepository;
    this.moveAdjustementService = moveAdjustementService;
    this.reconcileSequenceService = reconcileSequenceService;
    this.moveLineTaxService = moveLineTaxService;
    this.appBaseService = appBaseService;
    this.paymentMoveLineDistributionService = paymentMoveLineDistributionService;
    this.moveLineControlService = moveLineControlService;
    this.moveLineRepo = moveLineRepo;
    this.moveCreateService = moveCreateService;
    this.moveLineCreateService = moveLineCreateService;
    this.moveValidateService = moveValidateService;
    this.currencyScaleService = currencyScaleService;
    this.reconcileToolService = reconcileToolService;
    this.reconcileCheckService = reconcileCheckService;
    this.reconcileInvoiceTermComputationService = reconcileInvoiceTermComputationService;
    this.invoiceTermToolService = invoiceTermToolService;
    this.advancePaymentMoveLineCreateService = advancePaymentMoveLineCreateService;
  }

  /**
   * Permet de créer une réconciliation en passant les paramètres qu'il faut
   *
   * @param debitMoveLine Une ligne d'écriture au débit
   * @param creditMoveLine Une ligne d'écriture au crédit
   * @param amount Le montant à reconciler
   * @param canBeZeroBalanceOk Peut être soldé?
   * @return Une reconciliation
   */
  @Override
  @Transactional
  public Reconcile createReconcile(
      MoveLine debitMoveLine,
      MoveLine creditMoveLine,
      BigDecimal amount,
      boolean canBeZeroBalanceOk)
      throws AxelorException {

    if (ReconcileService.isReconcilable(debitMoveLine, creditMoveLine)
        && amount.compareTo(BigDecimal.ZERO) > 0) {

      log.debug(
          "Create Reconcile (Company : {}, Debit MoveLine : {}, Credit MoveLine : {}, Amount : {}, Can be zero balance ? {} )",
          debitMoveLine.getMove().getCompany(),
          debitMoveLine.getName(),
          creditMoveLine.getName(),
          amount,
          canBeZeroBalanceOk);

      Reconcile reconcile =
          new Reconcile(
              debitMoveLine.getMove().getCompany(),
              currencyScaleService.getCompanyScaledValue(debitMoveLine, amount),
              debitMoveLine,
              creditMoveLine,
              ReconcileRepository.STATUS_DRAFT,
              canBeZeroBalanceOk);

      if (!moveToolService.isDebitMoveLine(debitMoveLine)) {

        reconcile.setDebitMoveLine(creditMoveLine);
        reconcile.setCreditMoveLine(debitMoveLine);
      }

      return reconcileRepository.save(reconcile);
    }
    return null;
  }

  /**
   * Permet de confirmer une réconciliation On ne peut réconcilier que des moveLine ayant le même
   * compte
   *
   * @param reconcile Une reconciliation
   * @return L'etat de la reconciliation
   * @throws AxelorException
   */
  @Override
  @Transactional(rollbackOn = {Exception.class})
  public Reconcile confirmReconcile(
      Reconcile reconcile, boolean updateInvoicePayments, boolean updateInvoiceTerms)
      throws AxelorException {

    checkDifferentAccounts(reconcile, updateInvoicePayments, updateInvoiceTerms);

    reconcile = initReconcileConfirmation(reconcile, updateInvoicePayments, updateInvoiceTerms);

    if (updateInvoicePayments) {
      reconcileInvoiceTermComputationService.updatePayments(reconcile, updateInvoiceTerms);
    }
    Beans.get(ReconcileGroupService.class).addAndValidateReconcileGroup(reconcile);

    return reconcileRepository.save(reconcile);
  }

  public void checkDifferentAccounts(
      Reconcile reconcile, boolean updateInvoicePayments, boolean updateInvoiceTerms)
      throws AxelorException {

    // Check if move lines accounts are the same (debit and credit)
    if (!reconcile
        .getCreditMoveLine()
        .getAccount()
        .equals(reconcile.getDebitMoveLine().getAccount())) {
      log.debug(
          "Credit move line account : {} , Debit move line account : {}",
          reconcile.getCreditMoveLine().getAccount(),
          reconcile.getDebitMoveLine().getAccount());

      // Check if move lines accounts are compatible
      if (!reconcile
          .getDebitMoveLine()
          .getAccount()
          .getCompatibleAccountSet()
          .contains(reconcile.getCreditMoveLine().getAccount())) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(AccountExceptionMessage.RECONCILE_2),
            I18n.get(BaseExceptionMessage.EXCEPTION));
      } else {
        Reconcile newReconcile = createReconcileForDifferentAccounts(reconcile);
        confirmReconcile(newReconcile, updateInvoicePayments, updateInvoiceTerms);
      }
    }
  }

  @Transactional(rollbackOn = {Exception.class})
  public Reconcile initReconcileConfirmation(
      Reconcile reconcile, boolean updateInvoicePayments, boolean updateInvoiceTerm)
      throws AxelorException {

    reconcileCheckService.reconcilePreconditions(
        reconcile, updateInvoicePayments, updateInvoiceTerm);

    MoveLine debitMoveLine = reconcile.getDebitMoveLine();
    MoveLine creditMoveLine = reconcile.getCreditMoveLine();

    // Add the reconciled amount to the reconciled amount in the move line
    creditMoveLine.setAmountPaid(creditMoveLine.getAmountPaid().add(reconcile.getAmount()));
    debitMoveLine.setAmountPaid(debitMoveLine.getAmountPaid().add(reconcile.getAmount()));

    reconcile = reconcileRepository.save(reconcile);

    reconcile.setStatusSelect(ReconcileRepository.STATUS_CONFIRMED);

    if (reconcile.getCanBeZeroBalanceOk()) {
      // Alors nous utilisons la règle de gestion consitant à imputer l'écart sur un compte
      // transitoire si le seuil est respecté
      canBeZeroBalance(reconcile.getDebitMoveLine(), reconcile.getCreditMoveLine());
      reconcile.setCanBeZeroBalanceOk(false);
    }

    reconcile.setReconciliationDateTime(
        appBaseService.getTodayDateTime(reconcile.getCompany()).toLocalDateTime());

    reconcileSequenceService.setSequence(reconcile);

    reconcileToolService.updatePartnerAccountingSituation(reconcile);
    reconcileToolService.updateInvoiceCompanyInTaxTotalRemaining(reconcile);
    this.setEffectiveDate(reconcile);
    this.updatePaymentTax(reconcile);
    this.updatePaymentMoveLineDistribution(reconcile);

    return reconcile;
  }

  protected void updatePaymentTax(Reconcile reconcile) throws AxelorException {
    Move debitMove = reconcile.getDebitMoveLine().getMove();
    Move creditMove = reconcile.getCreditMoveLine().getMove();

    if (debitMove.getFunctionalOriginSelect() == MoveRepository.FUNCTIONAL_ORIGIN_PAYMENT) {
      moveLineTaxService.generateTaxPaymentMoveLineList(
          reconcile.getDebitMoveLine(), reconcile.getCreditMoveLine(), reconcile);
    }
    if (creditMove.getFunctionalOriginSelect() == MoveRepository.FUNCTIONAL_ORIGIN_PAYMENT) {
      moveLineTaxService.generateTaxPaymentMoveLineList(
          reconcile.getCreditMoveLine(), reconcile.getDebitMoveLine(), reconcile);
    }
  }

  /**
   * Méthode permettant de lettrer une écriture au débit avec une écriture au crédit
   *
   * @param debitMoveLine
   * @param creditMoveLine
   * @throws AxelorException
   */
  @Override
  public Reconcile reconcile(
      MoveLine debitMoveLine,
      MoveLine creditMoveLine,
      boolean canBeZeroBalanceOk,
      boolean updateInvoicePayments,
      InvoicePayment invoicePayment)
      throws AxelorException {
    return this.reconcile(
        debitMoveLine, creditMoveLine, null, canBeZeroBalanceOk, updateInvoicePayments);
  }

  public Reconcile reconcile(
      MoveLine debitMoveLine,
      MoveLine creditMoveLine,
      InvoicePayment invoicePayment,
      boolean canBeZeroBalanceOk,
      boolean updateInvoicePayments)
      throws AxelorException {
    BigDecimal amount =
        debitMoveLine.getAmountRemaining().abs().min(creditMoveLine.getAmountRemaining().abs());
    Reconcile reconcile =
        this.createReconcile(debitMoveLine, creditMoveLine, amount, canBeZeroBalanceOk);

    if (reconcile != null) {
      if (invoicePayment != null) {
        invoicePayment.setReconcile(reconcile);
      }

      this.confirmReconcile(reconcile, updateInvoicePayments, true);
      return reconcile;
    }

    return null;
  }

  /**
   * Méthode permettant de lettrer une écriture au débit avec une écriture au crédit
   *
   * @param debitMoveLine
   * @param creditMoveLine
   * @throws AxelorException
   */
  public Reconcile reconcile(
      MoveLine debitMoveLine,
      MoveLine creditMoveLine,
      boolean canBeZeroBalanceOk,
      boolean updateInvoicePayments)
      throws AxelorException {
    return this.reconcile(
        debitMoveLine, creditMoveLine, canBeZeroBalanceOk, updateInvoicePayments, null);
  }

  /** @param reconcile */
  protected void updatePaymentMoveLineDistribution(Reconcile reconcile) {
    // FIXME This feature will manage at a first step only reconcile of purchase (journal type of
    // type purchase)
    Move purchaseMove = reconcile.getCreditMoveLine().getMove();
    if (!purchaseMove.getJournal().getJournalType().getCode().equals("ACH")
        || purchaseMove.getPartner() == null) {
      return;
    }
    paymentMoveLineDistributionService.generatePaymentMoveLineDistributionList(
        purchaseMove, reconcile);
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void setEffectiveDate(Reconcile reconcile) {
    MoveLine creditMoveLine = reconcile.getCreditMoveLine();
    MoveLine debitMoveLine = reconcile.getDebitMoveLine();
    LocalDate creditMoveLineDate, debitMoveLineDate;

    if (creditMoveLine.getMove().getJournal().getJournalType().getTechnicalTypeSelect()
        == JournalTypeRepository.TECHNICAL_TYPE_SELECT_TREASURY) {
      creditMoveLineDate = creditMoveLine.getDueDate();
      debitMoveLineDate = debitMoveLine.getOriginDate();
    } else if (debitMoveLine.getMove().getJournal().getJournalType().getTechnicalTypeSelect()
        == JournalTypeRepository.TECHNICAL_TYPE_SELECT_TREASURY) {
      creditMoveLineDate = creditMoveLine.getOriginDate();
      debitMoveLineDate = debitMoveLine.getDueDate();
    } else if (creditMoveLine.getOriginDate() != null && debitMoveLine.getOriginDate() != null) {
      creditMoveLineDate = creditMoveLine.getOriginDate();
      debitMoveLineDate = debitMoveLine.getOriginDate();
    } else {
      creditMoveLineDate = creditMoveLine.getDate();
      debitMoveLineDate = debitMoveLine.getDate();
    }

    reconcile.setEffectiveDate(max(creditMoveLineDate, debitMoveLineDate));

    reconcileRepository.save(reconcile);
  }

  private LocalDate max(LocalDate d1, LocalDate d2) {
    if (d1 == null && d2 == null) return null;
    if (d1 == null) return d2;
    if (d2 == null) return d1;
    return (d1.isAfter(d2)) ? d1 : d2;
  }

  /**
   * Procédure permettant de gérer les écarts de règlement, check sur la case à cocher 'Peut être
   * soldé' Alors nous utilisons la règle de gestion consitant à imputer l'écart sur un compte
   * transitoire si le seuil est respecté
   *
   * @param reconcile Une reconciliation
   * @throws AxelorException
   */
  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void canBeZeroBalance(MoveLine debitMoveLine, MoveLine creditMoveLine)
      throws AxelorException {

    if (debitMoveLine != null
        && debitMoveLine.getAmountRemaining().compareTo(BigDecimal.ZERO) > 0) {
      balanceDebit(debitMoveLine);
    } else if (creditMoveLine != null
        && creditMoveLine.getAmountRemaining().compareTo(BigDecimal.ZERO) < 0) {
      balanceCredit(creditMoveLine);
    }

    log.debug("End of payment difference management");
  }

  /**
   * Solder le trop-perçu si il respecte les règles de seuil
   *
   * @param creditMoveLine
   * @throws AxelorException
   */
  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void balanceCredit(MoveLine creditMoveLine) throws AxelorException {
    if (creditMoveLine != null) {
      BigDecimal creditAmountRemaining = creditMoveLine.getAmountRemaining().abs();
      log.debug("Credit amount to pay / to reconcile: {}", creditAmountRemaining);

      if (creditAmountRemaining.compareTo(BigDecimal.ZERO) > 0) {

        AccountConfig accountConfig =
            accountConfigService.getAccountConfig(creditMoveLine.getMove().getCompany());

        if (creditAmountRemaining
                .plus()
                .compareTo(accountConfig.getThresholdDistanceFromRegulation())
            <= 0) {

          Account creditAccount =
              accountConfigService.getCashPositionVariationCreditAccountDontThrow(accountConfig);

          if (invoiceTermToolService.isThresholdNotOnLastUnpaidInvoiceTerm(
                  creditMoveLine, accountConfig.getThresholdDistanceFromRegulation())
              || creditAccount == null) {
            return;
          }

          log.debug("Threshold respected");

          MoveLine debitAdjustmentMoveLine =
              moveAdjustementService.createAdjustmentMove(creditMoveLine, creditAccount);

          // Création de la réconciliation
          Reconcile newReconcile =
              this.createReconcile(
                  debitAdjustmentMoveLine, creditMoveLine, creditAmountRemaining, false);
          if (newReconcile != null) {
            this.confirmReconcile(newReconcile, true, true);
            reconcileRepository.save(newReconcile);
          }
        }
      }
    }
  }

  /**
   * Solder le trop-perçu si il respecte les règles de seuil
   *
   * @param debitMoveLine
   * @throws AxelorException
   */
  @Transactional(rollbackOn = {Exception.class})
  protected void balanceDebit(MoveLine debitMoveLine) throws AxelorException {
    if (debitMoveLine != null) {
      BigDecimal debitAmountRemaining = debitMoveLine.getAmountRemaining();
      log.debug("Debit amount to pay / to reconcile: {}", debitAmountRemaining);

      if (debitAmountRemaining.compareTo(BigDecimal.ZERO) > 0) {

        Company company = debitMoveLine.getMove().getCompany();

        AccountConfig accountConfig = accountConfigService.getAccountConfig(company);

        if (debitAmountRemaining
                .plus()
                .compareTo(accountConfig.getThresholdDistanceFromRegulation())
            <= 0) {

          Account debitAccount =
              accountConfigService.getCashPositionVariationDebitAccountDontThrow(accountConfig);

          if (invoiceTermToolService.isThresholdNotOnLastUnpaidInvoiceTerm(
                  debitMoveLine, accountConfig.getThresholdDistanceFromRegulation())
              || debitAccount == null) {
            return;
          }

          log.debug("Threshold respected");

          MoveLine creditAdjustMoveLine =
              moveAdjustementService.createAdjustmentMove(debitMoveLine, debitAccount);

          // Création de la réconciliation
          Reconcile newReconcile =
              this.createReconcile(
                  debitMoveLine, creditAdjustMoveLine, debitAmountRemaining, false);
          if (newReconcile != null) {
            this.confirmReconcile(newReconcile, true, true);
            reconcileRepository.save(newReconcile);
          }
        }
      }
    }
  }

  @Override
  public List<Reconcile> getReconciles(MoveLine moveLine) {

    List<Reconcile> debitReconcileList = moveLine.getDebitReconcileList();
    List<Reconcile> creditReconcileList = moveLine.getCreditReconcileList();

    if (moveToolService.isDebitMoveLine(moveLine)) {
      return debitReconcileList;
    } else if (debitReconcileList != null && !creditReconcileList.isEmpty()) {
      return creditReconcileList;
    }
    return Lists.newArrayList();
  }

  @Override
  public String getStringAllowedCreditMoveLines(Reconcile reconcile) {
    return getAllowedCreditMoveLines(reconcile).stream()
        .map(Objects::toString)
        .collect(Collectors.joining(","));
  }

  @Override
  public String getStringAllowedDebitMoveLines(Reconcile reconcile) {
    return getAllowedDebitMoveLines(reconcile).stream()
        .map(Objects::toString)
        .collect(Collectors.joining(","));
  }

  @Override
  public List<Long> getAllowedCreditMoveLines(Reconcile reconcile) {
    StringBuilder moveLineQueryStr = new StringBuilder("self.credit > 0");
    return getMoveLineIds(reconcile, moveLineQueryStr, reconcile.getDebitMoveLine());
  }

  @Override
  public List<Long> getAllowedDebitMoveLines(Reconcile reconcile) {

    StringBuilder moveLineQueryStr = new StringBuilder("self.debit > 0");
    return getMoveLineIds(reconcile, moveLineQueryStr, reconcile.getCreditMoveLine());
  }

  protected List<Long> getMoveLineIds(
      Reconcile reconcile, StringBuilder moveLineQueryStr, MoveLine otherMoveLine) {
    computeMoveLineDomain(moveLineQueryStr, otherMoveLine);

    Query<MoveLine> moveLineQuery = getMoveLineQuery(reconcile, moveLineQueryStr);

    moveLineQuery = setQueryBindings(moveLineQuery, otherMoveLine);

    return moveLineQuery.fetch().stream()
        .filter(moveLineControlService::canReconcile)
        .map(MoveLine::getId)
        .collect(Collectors.toList());
  }

  protected Query<MoveLine> getMoveLineQuery(Reconcile reconcile, StringBuilder moveLineQueryStr) {
    return moveLineRepo
        .all()
        .filter(moveLineQueryStr.toString())
        .bind("company", reconcile.getCompany());
  }

  protected void computeMoveLineDomain(StringBuilder moveLineQueryStr, MoveLine otherMoveLine) {

    moveLineQueryStr.append(
        " AND self.move.statusSelect IN (2,3) AND self.move.company = :company AND self.account.reconcileOk IS TRUE");

    if (otherMoveLine == null) {
      return;
    }

    moveLineQueryStr.append(" AND self.account = :account");

    if (otherMoveLine.getAccount().getUseForPartnerBalance()
        && otherMoveLine.getPartner() != null) {
      moveLineQueryStr.append(" AND self.partner = :partner");
    }
  }

  protected Query<MoveLine> setQueryBindings(
      Query<MoveLine> moveLineQuery, MoveLine otherMoveLine) {

    Account account = null;
    Partner partner = null;

    if (otherMoveLine != null) {
      account = otherMoveLine.getAccount();
      partner = otherMoveLine.getPartner();
    }

    if (account != null) {
      moveLineQuery = moveLineQuery.bind("account", account);

      if (account.getUseForPartnerBalance() && partner != null) {
        moveLineQuery.bind("partner", partner);
      }
    }

    return moveLineQuery;
  }

  @Transactional(rollbackOn = {Exception.class})
  public Reconcile createReconcileForDifferentAccounts(Reconcile reconcile) throws AxelorException {
    MoveLine debitMoveLine = reconcile.getDebitMoveLine();
    Move debitMove = debitMoveLine.getMove();
    MoveLine creditMoveLine = reconcile.getCreditMoveLine();
    Move creditMove = creditMoveLine.getMove();

    BigDecimal reconciledAmount = reconcile.getAmount();

    Account debitAccount = debitMoveLine.getAccount();
    Account creditAccount = creditMoveLine.getAccount();

    AccountConfig accountConfig = accountConfigService.getAccountConfig(reconcile.getCompany());

    Journal miscOperationJournal = accountConfigService.getAutoMiscOpeJournal(accountConfig);

    boolean isDebit = false;

    Move originMove = null;
    Partner partner = null;
    Reconcile newReconcile = null;

    if (debitMove.getFunctionalOriginSelect() == MoveRepository.FUNCTIONAL_ORIGIN_PAYMENT) {
      originMove = debitMove;
    } else {
      originMove = creditMove;
      isDebit = true;
    }

    if (debitMove.getPartner() != null && debitMove.getPartner().equals(creditMove.getPartner())) {
      partner = debitMove.getPartner();
    }

    StringJoiner origin = new StringJoiner(" - ");
    Optional.of(debitMove).map(Move::getOrigin).ifPresent(origin::add);
    Optional.of(creditMove).map(Move::getOrigin).ifPresent(origin::add);
    StringJoiner description = new StringJoiner(" - ");
    Optional.of(debitMove).map(Move::getDescription).ifPresent(description::add);
    Optional.of(creditMove).map(Move::getDescription).ifPresent(description::add);

    Move move =
        moveCreateService.createMove(
            miscOperationJournal,
            reconcile.getCompany(),
            null,
            partner,
            null,
            partner != null ? partner.getFiscalPosition() : null,
            MoveRepository.TECHNICAL_ORIGIN_AUTOMATIC,
            0,
            origin.toString(),
            description.toString(),
            originMove != null ? originMove.getCompanyBankDetails() : null);

    MoveLine newCreditMoveLine =
        moveLineCreateService.createMoveLine(
            move,
            originMove.getPartner(),
            debitAccount,
            reconciledAmount,
            false,
            debitMoveLine.getDate(),
            1,
            debitMoveLine.getName(),
            null);

    // Création de la ligne au débit
    MoveLine newDebitMoveLine =
        moveLineCreateService.createMoveLine(
            move,
            originMove.getPartner(),
            creditAccount,
            reconciledAmount,
            true,
            creditMoveLine.getDate(),
            2,
            creditMoveLine.getName(),
            null);

    move.addMoveLineListItem(newDebitMoveLine);
    move.addMoveLineListItem(newCreditMoveLine);

    Map<TaxConfiguration, Pair<BigDecimal, BigDecimal>> taxConfigurationAmountMap = new HashMap<>();
    if (reconciledAmount.signum() > 0) {
      advancePaymentMoveLineCreateService.manageAdvancePaymentInvoiceTaxMoveLines(
          move,
          creditMoveLine,
          reconciledAmount.divide(
              creditMoveLine.getCredit(), AppBaseService.COMPUTATION_SCALING, RoundingMode.HALF_UP),
          creditMoveLine.getDate(),
          taxConfigurationAmountMap);
      advancePaymentMoveLineCreateService.manageAdvancePaymentInvoiceTaxMoveLines(
          move,
          debitMoveLine,
          reconciledAmount.divide(
              debitMoveLine.getDebit(), AppBaseService.COMPUTATION_SCALING, RoundingMode.HALF_UP),
          debitMoveLine.getDate(),
          taxConfigurationAmountMap);
    }

    advancePaymentMoveLineCreateService.fillMoveWithTaxMoveLines(move, taxConfigurationAmountMap);

    moveValidateService.accounting(move);

    if (isDebit) {
      newReconcile = createReconcile(newDebitMoveLine, creditMoveLine, reconciledAmount, true);
      reconcile.setCreditMoveLine(newCreditMoveLine);
    } else {
      newReconcile = createReconcile(debitMoveLine, newCreditMoveLine, reconciledAmount, true);
      reconcile.setDebitMoveLine(newDebitMoveLine);
    }

    return newReconcile;
  }
}
