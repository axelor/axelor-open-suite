/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.service.ReconcileService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceService;
import com.axelor.apps.account.service.invoice.InvoiceToolService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.account.service.payment.PaymentService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.PriceListRepository;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
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

  protected AccountConfigService accountConfigService;

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
      AccountConfigService accountConfigService) {
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
    if (invoice == null) {
      return null;
    }
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
      }

      log.debug(
          "Creation of a move specific to the invoice {} (Company : {}, Journal : {})",
          invoice.getInvoiceId(),
          company.getName(),
          journal != null ? journal.getCode() : "");

      int functionalOrigin = Beans.get(InvoiceService.class).getPurchaseTypeOrSaleType(invoice);
      if (functionalOrigin == PriceListRepository.TYPE_PURCHASE) {
        functionalOrigin = MoveRepository.FUNCTIONAL_ORIGIN_PURCHASE;
      } else if (functionalOrigin == PriceListRepository.TYPE_SALE) {
        functionalOrigin = MoveRepository.FUNCTIONAL_ORIGIN_SALE;
      } else {
        functionalOrigin = 0;
      }
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
              MoveRepository.TECHNICAL_ORIGIN_AUTOMATIC,
              functionalOrigin,
              origin,
              description);

      if (move != null) {

        move.setInvoice(invoice);

        move.setTradingName(invoice.getTradingName());

        boolean isDebitCustomer = moveToolService.isDebitCustomer(invoice, false);

        move.getMoveLineList()
            .addAll(
                moveLineCreateService.createMoveLines(
                    invoice,
                    move,
                    company,
                    partner,
                    account,
                    journal != null && journal.getIsInvoiceMoveConsolidated(),
                    isPurchase,
                    isDebitCustomer));

        moveToolService.setOriginAndDescriptionOnMoveLineList(move);

        moveRepository.save(move);

        invoice.setMove(move);

        invoice.setCompanyInTaxTotalRemaining(moveToolService.getInTaxTotalRemaining(invoice));
        moveValidateService.validate(move);
      }
    }

    return move;
  }

  /**
   * Method for using excess payments.<br>
   * <br>
   * 2 cases : <br>
   * - the account of the overpayments is the same as the one of the invoice: then we letter
   * directly <br>
   * - the account is not the same: we create an O.D. on the right account
   *
   * @param invoice
   * @return Generated move if it is not the same account on the excess payment and on the invoice,
   *     else null.
   * @throws AxelorException
   */
  @Override
  public Move createMoveUseExcessPaymentOrDue(Invoice invoice) throws AxelorException {
    if (invoice != null) {
      if (moveToolService.isDebitCustomer(invoice, true)) {
        // Use excess payments
        return this.createMoveUseExcessPayment(invoice);

      } else {
        // Use invoice dues
        return this.createMoveUseInvoiceDue(invoice);
      }
    }
    return null;
  }

  /**
   * Method to use the balances due on the credit note. Firstly, the selected invoices are recovered
   * from the credit note, then the other invoices of the third party.<br>
   * <br>
   * 2 cases :<br>
   * - the account of the due is the same as the account of the credit: then we letter directly<br>
   * - the account is not the same: we create a pass-through O.D. on the right account
   *
   * @param invoice
   * @return Generated move if it is not the same account on the excess payment and on the invoice,
   *     else null.
   * @throws AxelorException
   */
  @Override
  public Move createMoveUseInvoiceDue(Invoice invoice) throws AxelorException {

    Company company = invoice.getCompany();
    Move move = null;

    AccountConfig accountConfig = accountConfigService.getAccountConfig(company);

    // Get invoice dues
    List<MoveLine> debitMoveLines =
        moveDueService.getInvoiceDue(invoice, accountConfig.getAutoReconcileOnInvoice());

    if (!debitMoveLines.isEmpty()) {
      MoveLine invoiceCustomerMoveLine = moveToolService.getCustomerMoveLineByLoop(invoice);

      // If it is the same account on the excess payment and on the invoice, then we letter directly
      if (moveToolService.isSameAccount(debitMoveLines, invoiceCustomerMoveLine.getAccount())) {
        List<MoveLine> creditMoveLineList = new ArrayList<>();
        creditMoveLineList.add(invoiceCustomerMoveLine);
        paymentService.useExcessPaymentOnMoveLines(debitMoveLines, creditMoveLineList);
      }
      // Otherwise we create an O.D. to pass from the account of the invoice to another account on
      // the overpayments
      else {
        move = this.createMoveUseDebit(invoice, debitMoveLines, invoiceCustomerMoveLine);
      }

      // Management of the switch to 580
      reconcileService.balanceCredit(invoiceCustomerMoveLine);

      invoice.setCompanyInTaxTotalRemaining(moveToolService.getInTaxTotalRemaining(invoice));
    }

    return move;
  }

  @Override
  public Move createMoveUseExcessPayment(Invoice invoice) throws AxelorException {

    Move move = null;
    Company company = invoice.getCompany();
    String origin = invoice.getInvoiceId();

    // Get advance payment moves from the invoice
    List<MoveLine> creditMoveLineList = moveExcessPaymentService.getAdvancePaymentMoveList(invoice);

    AccountConfig accountConfig = accountConfigService.getAccountConfig(company);

    // Get excess payments
    creditMoveLineList.addAll(moveExcessPaymentService.getExcessPayment(invoice));
    if (CollectionUtils.isNotEmpty(creditMoveLineList)) {

      Partner partner = invoice.getPartner();
      Account account = invoice.getPartnerAccount();
      MoveLine invoiceCustomerMoveLine = moveToolService.getCustomerMoveLineByLoop(invoice);

      Journal journal = accountConfigService.getAutoMiscOpeJournal(accountConfig);

      // If it is the same account on the excess payment and on the invoice, then we letter directly
      if (moveToolService.isSameAccount(creditMoveLineList, account)) {
        List<MoveLine> debitMoveLineList = new ArrayList<>();
        debitMoveLineList.add(invoiceCustomerMoveLine);
        paymentService.useExcessPaymentOnMoveLines(debitMoveLineList, creditMoveLineList);
      }
      // Otherwise we create an O.D. to pass from the account of the invoice to another account on
      // the overpayments
      else {

        log.debug(
            "Creation of an O.D. move specific to the use of the excess payment {} (Company : {}, Journal : {})",
            invoice.getInvoiceId(),
            company.getName(),
            journal.getCode());

        move =
            moveCreateService.createMove(
                journal,
                company,
                null,
                partner,
                invoice.getInvoiceDate(),
                invoice.getInvoiceDate(),
                null,
                invoice.getFiscalPosition(),
                MoveRepository.TECHNICAL_ORIGIN_AUTOMATIC,
                MoveRepository.FUNCTIONAL_ORIGIN_PAYMENT,
                origin,
                null);

        if (move != null) {
          BigDecimal totalCreditAmount = moveToolService.getTotalCreditAmount(creditMoveLineList);
          BigDecimal amount = totalCreditAmount.min(invoiceCustomerMoveLine.getDebit());

          // Création de la ligne au crédit
          MoveLine creditMoveLine =
              moveLineCreateService.createMoveLine(
                  move,
                  partner,
                  account,
                  amount,
                  false,
                  appAccountService.getTodayDate(company),
                  1,
                  origin,
                  null);
          move.getMoveLineList().add(creditMoveLine);

          // Emploie des trop-perçus sur les lignes de debit qui seront créées au fil de
          // l'eau
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
    return move;
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
        "Creation of an O.D. move specific to the use of the excess payment {} (Company : {}, Journal : {})",
        invoice.getInvoiceId(),
        company.getName(),
        journal.getCode());

    BigDecimal remainingAmount = invoice.getInTaxTotal().abs();

    log.debug("Amount to be paid with the recovered debit : {}", remainingAmount);

    Move oDmove =
        moveCreateService.createMove(
            journal,
            company,
            null,
            partner,
            invoice.getInvoiceDate(),
            invoice.getInvoiceDate(),
            null,
            invoice.getFiscalPosition(),
            MoveRepository.TECHNICAL_ORIGIN_AUTOMATIC,
            MoveRepository.FUNCTIONAL_ORIGIN_PAYMENT,
            origin,
            null);

    if (oDmove != null) {
      BigDecimal totalDebitAmount = moveToolService.getTotalDebitAmount(debitMoveLines);
      BigDecimal amount = totalDebitAmount.min(invoiceCustomerMoveLine.getCredit());

      // Création de la ligne au débit
      MoveLine debitMoveLine =
          moveLineCreateService.createMoveLine(
              oDmove,
              partner,
              account,
              amount,
              true,
              appAccountService.getTodayDate(company),
              1,
              origin,
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
}
