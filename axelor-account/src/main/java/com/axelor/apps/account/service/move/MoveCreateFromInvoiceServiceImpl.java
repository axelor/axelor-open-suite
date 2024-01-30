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
package com.axelor.apps.account.service.move;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.repo.JournalRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.PaymentConditionService;
import com.axelor.apps.account.service.ReconcileService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceToolService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.account.service.payment.PaymentService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoveCreateFromInvoiceServiceImpl implements MoveCreateFromInvoiceService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  protected AppAccountService appAccountService;
  protected MoveCreateService moveCreateService;
  protected MoveLineCreateService moveLineCreateService;
  protected MoveToolService moveToolService;
  protected MoveRepository moveRepository;
  protected MoveValidateService moveValidateService;
  protected MoveDueService moveDueService;
  protected PaymentService paymentService;
  protected ReconcileService reconcileService;
  protected MoveExcessPaymentService moveExcessPaymentService;
  protected JournalRepository journalRepository;
  protected AccountConfigService accountConfigService;
  protected PaymentConditionService paymentConditionService;
  protected CurrencyService currencyService;

  @Inject
  public MoveCreateFromInvoiceServiceImpl(
      AppAccountService appAccountService,
      MoveCreateService moveCreateService,
      MoveLineCreateService moveLineCreateService,
      MoveToolService moveToolService,
      MoveRepository moveRepository,
      MoveValidateService moveValidateService,
      MoveDueService moveDueService,
      PaymentService paymentService,
      ReconcileService reconcileService,
      MoveExcessPaymentService moveExcessPaymentService,
      AccountConfigService accountConfigService,
      JournalRepository journalRepository,
      PaymentConditionService paymentConditionService,
      CurrencyService currencyService) {
    this.appAccountService = appAccountService;
    this.moveCreateService = moveCreateService;
    this.moveLineCreateService = moveLineCreateService;
    this.moveToolService = moveToolService;
    this.moveRepository = moveRepository;
    this.moveValidateService = moveValidateService;
    this.moveDueService = moveDueService;
    this.paymentService = paymentService;
    this.reconcileService = reconcileService;
    this.moveExcessPaymentService = moveExcessPaymentService;
    this.accountConfigService = accountConfigService;
    this.journalRepository = journalRepository;
    this.paymentConditionService = paymentConditionService;
    this.currencyService = currencyService;
  }

  /**
   * Create a Move from an Invoice
   *
   * @param invoice
   * @return
   * @throws AxelorException
   */
  @Transactional(rollbackOn = {Exception.class})
  @Override
  public Move createMove(Invoice invoice) throws AxelorException {
    Move move = null;
    String origin = invoice.getInvoiceId();

    if (InvoiceToolService.isPurchase(invoice)) {
      origin = invoice.getSupplierInvoiceNb();
    }

    if (invoice.getInvoiceLineList() != null) {
      Journal journal = invoice.getJournal();
      Company company = invoice.getCompany();
      Partner partner = invoice.getPartner();
      Account account = invoice.getPartnerAccount();

      String description = null;

      if (journal != null) {
        description = journal.getDescriptionModel();

        if (journal.getDescriptionIdentificationOk() && origin != null) {
          if (ObjectUtils.isEmpty(description)) {
            description = origin;
          } else {
            description = String.format("%s %s", description, origin);
          }
        }
      }

      if (accountConfigService.getAccountConfig(company).getIsDescriptionRequired()
          && StringUtils.isEmpty(description)) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(AccountExceptionMessage.MOVE_INVOICE_DESCRIPTION_REQUIRED),
            company.getName());
      }

      log.debug(
          "Creation of a move specific to the invoice {} (Company : {}, Journal : {})",
          new Object[] {
            invoice.getInvoiceId(),
            company.getName(),
            Optional.ofNullable(journal).map(Journal::getCode).orElse("")
          });

      int functionalOrigin = InvoiceToolService.getFunctionalOrigin(invoice);
      boolean isPurchase = InvoiceToolService.isPurchase(invoice);

      move =
          moveCreateService.createMove(
              journal,
              company,
              invoice.getCurrency(),
              partner,
              invoice.getInvoiceDate(),
              isPurchase ? invoice.getOriginDate() : invoice.getInvoiceDate(),
              invoice.getPaymentMode(),
              invoice.getFiscalPosition(),
              invoice.getBankDetails(),
              MoveRepository.TECHNICAL_ORIGIN_AUTOMATIC,
              functionalOrigin,
              origin,
              description,
              invoice.getCompanyBankDetails());

      if (move != null) {

        move.setInvoice(invoice);

        move.setTradingName(invoice.getTradingName());
        paymentConditionService.checkPaymentCondition(invoice.getPaymentCondition());
        move.setPaymentCondition(invoice.getPaymentCondition());

        move.setDueDate(invoice.getDueDate());

        boolean isDebitCustomer = moveToolService.isDebitCustomer(invoice, false);

        move.getMoveLineList()
            .addAll(
                moveLineCreateService.createMoveLines(
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
        moveValidateService.accounting(move);
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

    // Recuperation of due
    List<MoveLine> debitMoveLines =
        moveDueService.getInvoiceDue(invoice, accountConfig.getAutoReconcileOnInvoice());

    if (!debitMoveLines.isEmpty()) {
      MoveLine invoiceCustomerMoveLine = moveToolService.getCustomerMoveLineByLoop(invoice);

      // We directly use excess if invoice and excessPayment share the same account
      if (moveToolService.isSameAccount(debitMoveLines, invoiceCustomerMoveLine.getAccount())) {
        List<MoveLine> creditMoveLineList = new ArrayList<MoveLine>();
        creditMoveLineList.add(invoiceCustomerMoveLine);
        paymentService.useExcessPaymentOnMoveLines(debitMoveLines, creditMoveLineList);
      }
      // Else we create a O.D
      else {
        this.createMoveUseDebit(invoice, debitMoveLines, invoiceCustomerMoveLine);
      }

      // Management of the switch to 580
      reconcileService.balanceCredit(invoiceCustomerMoveLine);

      invoice.setCompanyInTaxTotalRemaining(moveToolService.getInTaxTotalRemaining(invoice));
    }

    return move;
  }

  @Override
  public void createMoveUseExcessPayment(Invoice invoice) throws AxelorException {

    Company company = invoice.getCompany();
    String origin = invoice.getInvoiceId();

    // Recuperation of advance payment
    List<MoveLine> creditMoveLineList = moveExcessPaymentService.getAdvancePaymentMoveList(invoice);

    AccountConfig accountConfig = accountConfigService.getAccountConfig(company);

    // recuperation of excess payment
    creditMoveLineList.addAll(moveExcessPaymentService.getExcessPayment(invoice));
    if (creditMoveLineList != null && creditMoveLineList.size() != 0) {

      Partner partner = invoice.getPartner();
      Account account = invoice.getPartnerAccount();
      MoveLine invoiceCustomerMoveLine = moveToolService.getCustomerMoveLineByLoop(invoice);

      Journal journal = accountConfigService.getAutoMiscOpeJournal(accountConfig);

      // We directly use excess if invoice and excessPayment share the same account
      if (moveToolService.isSameAccount(creditMoveLineList, account)) {
        List<MoveLine> debitMoveLineList = new ArrayList<>();
        debitMoveLineList.add(invoiceCustomerMoveLine);
        paymentService.useExcessPaymentOnMoveLines(debitMoveLineList, creditMoveLineList);
      }
      // Else we create a O.D
      else {

        log.debug(
            "Creation of a O.D. move specific to the use of overpayment {} (Company : {}, Journal : {})",
            invoice.getInvoiceId(),
            company.getName(),
            journal.getCode());

        Move move =
            moveCreateService.createMove(
                journal,
                company,
                invoice.getCurrency(),
                partner,
                invoice.getInvoiceDate(),
                invoice.getInvoiceDate(),
                null,
                invoice.getFiscalPosition(),
                MoveRepository.TECHNICAL_ORIGIN_AUTOMATIC,
                MoveRepository.FUNCTIONAL_ORIGIN_PAYMENT,
                origin,
                null,
                invoice.getCompanyBankDetails());

        if (move != null) {
          BigDecimal totalCreditAmount = moveToolService.getTotalCreditAmount(creditMoveLineList);
          BigDecimal amount = totalCreditAmount.min(invoiceCustomerMoveLine.getDebit());

          BigDecimal moveLineAmount =
              moveToolService
                  .getTotalCurrencyAmount(creditMoveLineList)
                  .min(invoiceCustomerMoveLine.getCurrencyAmount());
          LocalDate date = invoice.getInvoiceDate();

          // credit move line creation
          MoveLine creditMoveLine =
              moveLineCreateService.createMoveLine(
                  move,
                  partner,
                  account,
                  moveLineAmount,
                  amount,
                  amount.divide(
                      moveLineAmount, AppBaseService.COMPUTATION_SCALING, RoundingMode.HALF_UP),
                  false,
                  date,
                  date,
                  date,
                  1,
                  origin,
                  null);
          move.getMoveLineList().add(creditMoveLine);

          // Use of excess payment
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

          moveValidateService.accounting(move);

          // Reconciliation creation
          Reconcile reconcile =
              reconcileService.createReconcile(
                  invoiceCustomerMoveLine, creditMoveLine, amount, false);
          if (reconcile != null) {
            reconcileService.confirmReconcile(reconcile, true, true);
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
    String origin = invoice.getInvoiceId();

    Journal journal =
        accountConfigService.getAutoMiscOpeJournal(accountConfigService.getAccountConfig(company));

    log.debug(
        "Creation of a O.D. move specific to the use of overpayment {} (Company : {}, Journal : {})",
        invoice.getInvoiceId(),
        company.getName(),
        journal.getCode());

    BigDecimal remainingAmount = invoice.getInTaxTotal().abs();

    log.debug("Amount to pay with the credit note : {}", remainingAmount);

    Move oDmove =
        moveCreateService.createMove(
            journal,
            company,
            invoice.getCurrency(),
            partner,
            invoice.getInvoiceDate(),
            invoice.getInvoiceDate(),
            null,
            invoice.getFiscalPosition(),
            MoveRepository.TECHNICAL_ORIGIN_AUTOMATIC,
            MoveRepository.FUNCTIONAL_ORIGIN_PAYMENT,
            origin,
            invoiceCustomerMoveLine.getDescription(),
            invoice.getCompanyBankDetails());

    if (oDmove != null) {
      BigDecimal totalDebitAmount = moveToolService.getTotalDebitAmount(debitMoveLines);
      BigDecimal amount = totalDebitAmount.min(invoiceCustomerMoveLine.getCredit());

      BigDecimal moveLineAmount =
          moveToolService
              .getTotalCurrencyAmount(debitMoveLines)
              .min(invoiceCustomerMoveLine.getCurrencyAmount().abs());
      LocalDate date = invoice.getInvoiceDate();

      // debit move line creation
      MoveLine debitMoveLine =
          moveLineCreateService.createMoveLine(
              oDmove,
              partner,
              account,
              moveLineAmount,
              amount,
              amount.divide(
                  moveLineAmount, AppBaseService.COMPUTATION_SCALING, RoundingMode.HALF_UP),
              true,
              date,
              date,
              date,
              1,
              origin,
              null);
      oDmove.getMoveLineList().add(debitMoveLine);

      // Use of excess payment
      paymentService.createExcessPaymentWithAmount(
          debitMoveLines,
          amount,
          oDmove,
          2,
          partner,
          company,
          null,
          account,
          invoice.getInvoiceDate());

      moveValidateService.accounting(oDmove);

      // Reconciliation creation
      Reconcile reconcile =
          reconcileService.createReconcile(debitMoveLine, invoiceCustomerMoveLine, amount, false);
      if (reconcile != null) {
        reconcileService.confirmReconcile(reconcile, true, true);
      }
    }
    return oDmove;
  }
}
