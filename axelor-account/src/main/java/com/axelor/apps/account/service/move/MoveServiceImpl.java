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
package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.AnalyticMoveLine;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.repo.AnalyticMoveLineRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.db.repo.ReconcileRepository;
import com.axelor.apps.account.service.AnalyticMoveLineService;
import com.axelor.apps.account.service.ReconcileService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceService;
import com.axelor.apps.account.service.invoice.InvoiceToolService;
import com.axelor.apps.account.service.payment.PaymentService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PriceListRepository;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoveServiceImpl implements MoveService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected MoveLineService moveLineService;
  protected MoveCreateService moveCreateService;
  protected MoveValidateService moveValidateService;
  protected MoveRemoveService moveRemoveService;
  protected MoveToolService moveToolService;
  protected ReconcileService reconcileService;
  protected MoveDueService moveDueService;
  protected PaymentService paymentService;
  protected MoveExcessPaymentService moveExcessPaymentService;
  protected AccountConfigService accountConfigService;
  protected MoveRepository moveRepository;

  protected AppAccountService appAccountService;

  @Inject
  public MoveServiceImpl(
      AppAccountService appAccountService,
      MoveLineService moveLineService,
      MoveCreateService moveCreateService,
      MoveValidateService moveValidateService,
      MoveToolService moveToolService,
      MoveRemoveService moveRemoveService,
      ReconcileService reconcileService,
      MoveDueService moveDueService,
      PaymentService paymentService,
      MoveExcessPaymentService moveExcessPaymentService,
      MoveRepository moveRepository,
      AccountConfigService accountConfigService) {

    this.moveLineService = moveLineService;
    this.moveCreateService = moveCreateService;
    this.moveValidateService = moveValidateService;
    this.moveRemoveService = moveRemoveService;
    this.moveToolService = moveToolService;
    this.reconcileService = reconcileService;
    this.moveDueService = moveDueService;
    this.paymentService = paymentService;
    this.moveExcessPaymentService = moveExcessPaymentService;
    this.moveRepository = moveRepository;
    this.accountConfigService = accountConfigService;

    this.appAccountService = appAccountService;
  }

  @Override
  public MoveLineService getMoveLineService() {
    return moveLineService;
  }

  @Override
  public MoveCreateService getMoveCreateService() {
    return moveCreateService;
  }

  @Override
  public MoveValidateService getMoveValidateService() {
    return moveValidateService;
  }

  @Override
  public MoveRemoveService getMoveRemoveService() {
    return moveRemoveService;
  }

  @Override
  public MoveToolService getMoveToolService() {
    return moveToolService;
  }

  @Override
  public ReconcileService getReconcileService() {
    return reconcileService;
  }

  /**
   * Créer une écriture comptable propre à la facture.
   *
   * @param invoice
   * @param consolidate
   * @return
   * @throws AxelorException
   */
  @Transactional(rollbackOn = {Exception.class})
  @Override
  public Move createMove(Invoice invoice) throws AxelorException {
    Move move = null;

    if (invoice != null && invoice.getInvoiceLineList() != null) {

      Journal journal = invoice.getJournal();
      Company company = invoice.getCompany();
      Partner partner = invoice.getPartner();
      Account account = invoice.getPartnerAccount();

      log.debug(
          "Création d'une écriture comptable spécifique à la facture {} (Société : {}, Journal : {})",
          new Object[] {invoice.getInvoiceId(), company.getName(), journal.getCode()});

      int functionalOrigin = Beans.get(InvoiceService.class).getPurchaseTypeOrSaleType(invoice);
      if (functionalOrigin == PriceListRepository.TYPE_PURCHASE) {
        functionalOrigin = MoveRepository.FUNCTIONAL_ORIGIN_PURCHASE;
      } else if (functionalOrigin == PriceListRepository.TYPE_SALE) {
        functionalOrigin = MoveRepository.FUNCTIONAL_ORIGIN_SALE;
      } else {
        functionalOrigin = 0;
      }
      move =
          moveCreateService.createMove(
              journal,
              company,
              invoice.getCurrency(),
              partner,
              invoice.getInvoiceDate(),
              invoice.getPaymentMode(),
              MoveRepository.TECHNICAL_ORIGIN_AUTOMATIC,
              functionalOrigin);

      if (move != null) {

        move.setInvoice(invoice);

        move.setTradingName(invoice.getTradingName());

        boolean isPurchase = InvoiceToolService.isPurchase(invoice);

        boolean isDebitCustomer = moveToolService.isDebitCustomer(invoice, false);

        move.getMoveLineList()
            .addAll(
                moveLineService.createMoveLines(
                    invoice,
                    move,
                    company,
                    partner,
                    account,
                    journal.getIsInvoiceMoveConsolidated(),
                    isPurchase,
                    isDebitCustomer));

        moveRepository.save(move);

        invoice.setMove(move);

        invoice.setCompanyInTaxTotalRemaining(moveToolService.getInTaxTotalRemaining(invoice));
        moveValidateService.validate(move);
      }
    }

    return move;
  }

  /**
   * Méthode permettant d'employer les trop-perçus 2 cas : - le compte des trop-perçus est le même
   * que celui de la facture : alors on lettre directement - le compte n'est pas le même : on créée
   * une O.D. de passage sur le bon compte
   *
   * @param invoice
   * @return
   * @throws AxelorException
   */
  @Override
  public Move createMoveUseExcessPaymentOrDue(Invoice invoice) throws AxelorException {

    Move move = null;

    if (invoice != null) {

      if (moveToolService.isDebitCustomer(invoice, true)) {

        // Emploie du trop perçu
        this.createMoveUseExcessPayment(invoice);

      } else {

        // Emploie des dûs
        this.createMoveUseInvoiceDue(invoice);
      }
    }
    return move;
  }

  /**
   * Méthode permettant d'employer les dûs sur l'avoir On récupère prioritairement les dûs
   * (factures) selectionné sur l'avoir, puis les autres dûs du tiers
   *
   * <p>2 cas : - le compte des dûs est le même que celui de l'avoir : alors on lettre directement -
   * le compte n'est pas le même : on créée une O.D. de passage sur le bon compte
   *
   * @param invoice
   * @return
   * @throws AxelorException
   */
  @Override
  public Move createMoveUseInvoiceDue(Invoice invoice) throws AxelorException {

    Company company = invoice.getCompany();
    Move move = null;

    AccountConfig accountConfig = accountConfigService.getAccountConfig(company);

    // Récupération des dûs
    List<MoveLine> debitMoveLines =
        moveDueService.getInvoiceDue(invoice, accountConfig.getAutoReconcileOnInvoice());

    if (!debitMoveLines.isEmpty()) {
      MoveLine invoiceCustomerMoveLine = moveToolService.getCustomerMoveLineByLoop(invoice);

      // Si c'est le même compte sur les trop-perçus et sur la facture, alors on lettre directement
      if (moveToolService.isSameAccount(debitMoveLines, invoiceCustomerMoveLine.getAccount())) {
        List<MoveLine> creditMoveLineList = new ArrayList<MoveLine>();
        creditMoveLineList.add(invoiceCustomerMoveLine);
        paymentService.useExcessPaymentOnMoveLines(debitMoveLines, creditMoveLineList);
      }
      // Sinon on créée une O.D. pour passer du compte de la facture à un autre compte sur les
      // trop-perçus
      else {
        this.createMoveUseDebit(invoice, debitMoveLines, invoiceCustomerMoveLine);
      }

      // Gestion du passage en 580
      reconcileService.balanceCredit(invoiceCustomerMoveLine);

      invoice.setCompanyInTaxTotalRemaining(moveToolService.getInTaxTotalRemaining(invoice));
    }

    return move;
  }

  @Override
  public void createMoveUseExcessPayment(Invoice invoice) throws AxelorException {

    Company company = invoice.getCompany();

    // Récupération des acomptes de la facture
    List<MoveLine> creditMoveLineList = moveExcessPaymentService.getAdvancePaymentMoveList(invoice);

    AccountConfig accountConfig = accountConfigService.getAccountConfig(company);

    // Récupération des trop-perçus
    creditMoveLineList.addAll(moveExcessPaymentService.getExcessPayment(invoice));
    if (creditMoveLineList != null && creditMoveLineList.size() != 0) {

      Partner partner = invoice.getPartner();
      Account account = invoice.getPartnerAccount();
      MoveLine invoiceCustomerMoveLine = moveToolService.getCustomerMoveLineByLoop(invoice);

      Journal journal = accountConfigService.getAutoMiscOpeJournal(accountConfig);

      // Si c'est le même compte sur les trop-perçus et sur la facture, alors on lettre directement
      if (moveToolService.isSameAccount(creditMoveLineList, account)) {
        List<MoveLine> debitMoveLineList = new ArrayList<MoveLine>();
        debitMoveLineList.add(invoiceCustomerMoveLine);
        paymentService.useExcessPaymentOnMoveLines(debitMoveLineList, creditMoveLineList);
      }
      // Sinon on créée une O.D. pour passer du compte de la facture à un autre compte sur les
      // trop-perçus
      else {

        log.debug(
            "Création d'une écriture comptable O.D. spécifique à l'emploie des trop-perçus {} (Société : {}, Journal : {})",
            new Object[] {invoice.getInvoiceId(), company.getName(), journal.getCode()});

        Move move =
            moveCreateService.createMove(
                journal,
                company,
                null,
                partner,
                invoice.getInvoiceDate(),
                null,
                MoveRepository.TECHNICAL_ORIGIN_AUTOMATIC,
                MoveRepository.FUNCTIONAL_ORIGIN_PAYMENT);

        if (move != null) {
          BigDecimal totalCreditAmount = moveToolService.getTotalCreditAmount(creditMoveLineList);
          BigDecimal amount = totalCreditAmount.min(invoiceCustomerMoveLine.getDebit());

          // Création de la ligne au crédit
          MoveLine creditMoveLine =
              moveLineService.createMoveLine(
                  move,
                  partner,
                  account,
                  amount,
                  false,
                  appAccountService.getTodayDate(company),
                  1,
                  invoice.getInvoiceId(),
                  null);
          move.getMoveLineList().add(creditMoveLine);

          // Emploie des trop-perçus sur les lignes de debit qui seront créées au fil de l'eau
          paymentService.useExcessPaymentWithAmountConsolidated(
              creditMoveLineList,
              amount,
              move,
              2,
              partner,
              company,
              account,
              invoice.getInvoiceDate(),
              invoice.getDueDate());

          moveValidateService.validate(move);

          // Création de la réconciliation
          Reconcile reconcile =
              reconcileService.createReconcile(
                  invoiceCustomerMoveLine, creditMoveLine, amount, false);
          if (reconcile != null) {
            reconcileService.confirmReconcile(reconcile, true);
          }
        }
      }

      invoice.setCompanyInTaxTotalRemaining(moveToolService.getInTaxTotalRemaining(invoice));
    }
  }

  @Override
  public Move createMoveUseDebit(
      Invoice invoice, List<MoveLine> debitMoveLines, MoveLine invoiceCustomerMoveLine)
      throws AxelorException {
    Company company = invoice.getCompany();
    Partner partner = invoice.getPartner();
    Account account = invoice.getPartnerAccount();

    Journal journal =
        accountConfigService.getAutoMiscOpeJournal(accountConfigService.getAccountConfig(company));

    log.debug(
        "Création d'une écriture comptable O.D. spécifique à l'emploie des trop-perçus {} (Société : {}, Journal : {})",
        new Object[] {invoice.getInvoiceId(), company.getName(), journal.getCode()});

    BigDecimal remainingAmount = invoice.getInTaxTotal().abs();

    log.debug("Montant à payer avec l'avoir récupéré : {}", remainingAmount);

    Move oDmove =
        moveCreateService.createMove(
            journal,
            company,
            null,
            partner,
            invoice.getInvoiceDate(),
            null,
            MoveRepository.TECHNICAL_ORIGIN_AUTOMATIC,
            MoveRepository.FUNCTIONAL_ORIGIN_PAYMENT);

    if (oDmove != null) {
      BigDecimal totalDebitAmount = moveToolService.getTotalDebitAmount(debitMoveLines);
      BigDecimal amount = totalDebitAmount.min(invoiceCustomerMoveLine.getCredit());

      // Création de la ligne au débit
      MoveLine debitMoveLine =
          moveLineService.createMoveLine(
              oDmove,
              partner,
              account,
              amount,
              true,
              appAccountService.getTodayDate(company),
              1,
              invoice.getInvoiceId(),
              null);
      oDmove.getMoveLineList().add(debitMoveLine);

      // Emploie des dûs sur les lignes de credit qui seront créées au fil de l'eau
      paymentService.createExcessPaymentWithAmount(
          debitMoveLines,
          amount,
          oDmove,
          2,
          partner,
          company,
          null,
          account,
          appAccountService.getTodayDate(company));

      moveValidateService.validate(oDmove);

      // Création de la réconciliation
      Reconcile reconcile =
          reconcileService.createReconcile(debitMoveLine, invoiceCustomerMoveLine, amount, false);
      if (reconcile != null) {
        reconcileService.confirmReconcile(reconcile, true);
      }
    }
    return oDmove;
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public Move generateReverse(
      Move move,
      boolean isAutomaticReconcile,
      boolean isAutomaticAccounting,
      boolean isUnreconcileOriginalMove,
      LocalDate dateOfReversion)
      throws AxelorException {

    Move newMove =
        moveCreateService.createMove(
            move.getJournal(),
            move.getCompany(),
            move.getCurrency(),
            move.getPartner(),
            dateOfReversion,
            move.getPaymentMode(),
            MoveRepository.TECHNICAL_ORIGIN_ENTRY,
            move.getFunctionalOriginSelect(),
            move.getIgnoreInDebtRecoveryOk(),
            move.getIgnoreInAccountingOk(),
            move.getAutoYearClosureMove());

    move.setInvoice(move.getInvoice());
    move.setPaymentVoucher(move.getPaymentVoucher());

    boolean validatedMove =
        move.getStatusSelect() == MoveRepository.STATUS_ACCOUNTED
            || move.getStatusSelect() == MoveRepository.STATUS_VALIDATED;

    for (MoveLine moveLine : move.getMoveLineList()) {
      log.debug("Moveline {}", moveLine);
      Boolean isDebit = moveLine.getDebit().compareTo(BigDecimal.ZERO) > 0;

      MoveLine newMoveLine = generateReverseMoveLine(newMove, moveLine, dateOfReversion, isDebit);

      if (moveLine.getAnalyticDistributionTemplate() != null) {
        newMoveLine.setAnalyticDistributionTemplate(moveLine.getAnalyticDistributionTemplate());

        List<AnalyticMoveLine> analyticMoveLineList =
            Beans.get(AnalyticMoveLineService.class)
                .generateLines(
                    newMoveLine.getAnalyticDistributionTemplate(),
                    newMoveLine.getDebit().add(newMoveLine.getCredit()),
                    AnalyticMoveLineRepository.STATUS_REAL_ACCOUNTING,
                    dateOfReversion);
        if (CollectionUtils.isNotEmpty(analyticMoveLineList)) {
          analyticMoveLineList.forEach(
              analyticMoveLine -> newMoveLine.addAnalyticMoveLineListItem(analyticMoveLine));
        }
      }

      newMove.addMoveLineListItem(newMoveLine);

      if (isUnreconcileOriginalMove) {
        List<Reconcile> reconcileList =
            Beans.get(ReconcileRepository.class)
                .all()
                .filter(
                    "self.statusSelect != ?1 AND (self.debitMoveLine = ?2 OR self.creditMoveLine = ?2)",
                    ReconcileRepository.STATUS_CANCELED,
                    moveLine)
                .fetch();
        for (Reconcile reconcile : reconcileList) {
          reconcileService.unreconcile(reconcile);
        }
      }

      if (validatedMove && isAutomaticReconcile) {
        if (isDebit) {
          reconcileService.reconcile(moveLine, newMoveLine, false, true);
        } else {
          reconcileService.reconcile(newMoveLine, moveLine, false, true);
        }
      }
    }

    if (validatedMove && isAutomaticAccounting) {
      moveValidateService.validate(newMove);
    }

    return moveRepository.save(newMove);
  }

  @Override
  public MoveLine findMoveLineByAccount(Move move, Account account) throws AxelorException {
    return move.getMoveLineList().stream()
        .filter(moveLine -> moveLine.getAccount().equals(account))
        .findFirst()
        .orElseThrow(
            () ->
                new AxelorException(
                    move,
                    TraceBackRepository.CATEGORY_NO_VALUE,
                    I18n.get("%s account not found in move %s"),
                    account.getName(),
                    move.getReference()));
  }

  @Override
  public Map<String, Object> computeTotals(Move move) {

    Map<String, Object> values = new HashMap<>();
    if (move.getMoveLineList() == null || move.getMoveLineList().isEmpty()) {
      return values;
    }
    values.put("$totalLines", move.getMoveLineList().size());

    BigDecimal totalDebit =
        move.getMoveLineList().stream()
            .map(MoveLine::getDebit)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    values.put("$totalDebit", totalDebit);

    BigDecimal totalCredit =
        move.getMoveLineList().stream()
            .map(MoveLine::getCredit)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    values.put("$totalCredit", totalCredit);

    BigDecimal difference = totalDebit.subtract(totalCredit);
    values.put("$difference", difference);

    return values;
  }

  @Override
  public Move generateReverse(Move move, Map<String, Object> assistantMap) throws AxelorException {
    move =
        generateReverse(
            move,
            (boolean) assistantMap.get("isAutomaticReconcile"),
            (boolean) assistantMap.get("isAutomaticAccounting"),
            (boolean) assistantMap.get("isUnreconcileOriginalMove"),
            (LocalDate) assistantMap.get("dateOfReversion"));
    return move;
  }

  protected MoveLine generateReverseMoveLine(
      Move reverseMove, MoveLine orgineMoveLine, LocalDate dateOfReversion, boolean isDebit)
      throws AxelorException {
    MoveLine reverseMoveLine =
        moveLineService.createMoveLine(
            reverseMove,
            orgineMoveLine.getPartner(),
            orgineMoveLine.getAccount(),
            orgineMoveLine.getCurrencyAmount(),
            orgineMoveLine.getDebit().add(orgineMoveLine.getCredit()),
            orgineMoveLine.getCurrencyRate(),
            !isDebit,
            dateOfReversion,
            dateOfReversion,
            dateOfReversion,
            orgineMoveLine.getCounter(),
            orgineMoveLine.getName(),
            null);
    return reverseMoveLine;
  }

  @Override
  public String filterPartner(Move move) {
    Long companyId = move.getCompany().getId();
    String domain = "self.isContact = false AND " + companyId + " member of self.companySet";
    if (move.getJournal() != null
        && !Strings.isNullOrEmpty(move.getJournal().getCompatiblePartnerTypeSelect())) {
      domain += " AND (";
      String[] partnerSet = move.getJournal().getCompatiblePartnerTypeSelect().split(", ");
      String lastPartner = partnerSet[partnerSet.length - 1];
      for (String partner : partnerSet) {
        domain += "self." + partner + " = true";
        if (!partner.equals(lastPartner)) {
          domain += " OR ";
        }
      }
      domain += ")";
    }
    return domain;
  }

  @Override
  public Move updateMoveLinesDateExcludeFromPeriodOnlyWithoutSave(Move move)
      throws AxelorException {
    if (move.getPeriod() != null && move.getDate() != null) {
      for (MoveLine moveLine : ListUtils.emptyIfNull(move.getMoveLineList())) {
        if ((move.getPeriod().getFromDate().isAfter(moveLine.getDate())
            || move.getPeriod().getToDate().isBefore(moveLine.getDate()))) {
          moveLine.setDate(move.getDate());
        }
      }
    }
    return move;
  }
}
