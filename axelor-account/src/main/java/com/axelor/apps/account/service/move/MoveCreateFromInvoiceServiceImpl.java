/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.repo.InvoiceTermRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.PaymentConditionService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceToolService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.account.service.payment.PaymentService;
import com.axelor.apps.account.service.reconcile.ReconcileService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.common.ObjectUtils;
import com.axelor.common.StringUtils;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoveCreateFromInvoiceServiceImpl implements MoveCreateFromInvoiceService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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
  protected PaymentConditionService paymentConditionService;
  protected CurrencyService currencyService;

  @Inject
  public MoveCreateFromInvoiceServiceImpl(
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
      PaymentConditionService paymentConditionService,
      CurrencyService currencyService) {
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
        move.setThirdPartyPayerPartner(invoice.getThirdPartyPayerPartner());

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
                    journal != null ? journal.getIsInvoiceMoveConsolidated() : false,
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
   * Method to use excess payments. 2 cases: - the excess payment account is the same as the invoice
   * account: then we reconcile directly - the account is different: we create a miscellaneous
   * operation move to transfer to the correct account
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

        // Use excess payment
        this.createMoveUseExcessPayment(invoice);

      } else {

        // Use due amounts
        this.createMoveUseInvoiceDue(invoice);
      }
    }
    return move;
  }

  /**
   * Method to use due amounts on the credit note. We first retrieve the due amounts (invoices)
   * selected on the credit note, then the other due amounts of the partner
   *
   * <p>2 cases: - the due account is the same as the credit note account: then we reconcile
   * directly - the account is different: we create a miscellaneous operation move to transfer to
   * the correct account
   *
   * @param invoice
   * @return
   * @throws AxelorException
   */
  @Override
  public Move createMoveUseInvoiceDue(Invoice invoice) throws AxelorException {
    if (invoice.getInvoiceTermList().stream()
        .allMatch(
            it -> it.getPfpValidateStatusSelect() == InvoiceTermRepository.PFP_STATUS_LITIGATION)) {
      return null;
    }

    Company company = invoice.getCompany();
    Move move = null;

    AccountConfig accountConfig = accountConfigService.getAccountConfig(company);

    // Recuperation of due
    List<MoveLine> debitMoveLines =
        moveDueService.getInvoiceDue(invoice, accountConfig.getAutoReconcileOnInvoice());

    if (!debitMoveLines.isEmpty()) {
      List<MoveLine> invoiceCustomerMoveLines =
          moveToolService.getInvoiceCustomerMoveLines(invoice);

      // Sort move lines: non-holdback first, then holdback
      // This is required because holdback invoice terms cannot be paid before other terms
      List<MoveLine> sortedInvoiceCustomerMoveLines =
          invoiceCustomerMoveLines.stream()
              .sorted(Comparator.comparing(this::isHoldbackMoveLine))
              .collect(Collectors.toList());

      // Group debit move lines by account for matching with credit note move lines
      Map<Account, List<MoveLine>> debitMoveLinesByAccount =
          debitMoveLines.stream().collect(Collectors.groupingBy(MoveLine::getAccount));

      // Process each customer move line from the credit note (non-holdback first, then holdback)
      for (MoveLine invoiceCustomerMoveLine : sortedInvoiceCustomerMoveLines) {
        Account account = invoiceCustomerMoveLine.getAccount();
        List<MoveLine> matchingDebitLines = debitMoveLinesByAccount.get(account);

        if (matchingDebitLines != null && !matchingDebitLines.isEmpty()) {
          // Same account: direct reconciliation
          List<MoveLine> creditMoveLineList = new ArrayList<>();
          creditMoveLineList.add(invoiceCustomerMoveLine);
          paymentService.useExcessPaymentOnMoveLines(matchingDebitLines, creditMoveLineList);
        } else {
          // Different account: create O.D.
          this.createMoveUseDebit(invoice, debitMoveLines, invoiceCustomerMoveLine);
        }

        // Management of the switch to 580
        reconcileService.canBeZeroBalance(null, invoiceCustomerMoveLine);
      }

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
      List<MoveLine> invoiceCustomerMoveLines =
          moveToolService.getInvoiceCustomerMoveLines(invoice);

      // Sort move lines: non-holdback first, then holdback
      // This is required because holdback invoice terms cannot be paid before other terms
      List<MoveLine> sortedInvoiceCustomerMoveLines =
          invoiceCustomerMoveLines.stream()
              .sorted(Comparator.comparing(this::isHoldbackMoveLine))
              .collect(Collectors.toList());

      Journal journal = accountConfigService.getAutoMiscOpeJournal(accountConfig);

      // Group credit move lines by account for matching with invoice move lines
      Map<Account, List<MoveLine>> creditMoveLinesByAccount =
          creditMoveLineList.stream().collect(Collectors.groupingBy(MoveLine::getAccount));

      // Process each customer move line from the invoice (non-holdback first, then holdback)
      for (MoveLine invoiceCustomerMoveLine : sortedInvoiceCustomerMoveLines) {
        Account account = invoiceCustomerMoveLine.getAccount();
        List<MoveLine> matchingCreditLines = creditMoveLinesByAccount.get(account);

        if (matchingCreditLines != null && !matchingCreditLines.isEmpty()) {
          // Same account: direct reconciliation
          List<MoveLine> debitMoveLineList = new ArrayList<>();
          debitMoveLineList.add(invoiceCustomerMoveLine);
          paymentService.useExcessPaymentOnMoveLines(debitMoveLineList, matchingCreditLines);
        } else {
          // Different account: create O.D.
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
                    currencyService.computeScaledExchangeRate(amount, moveLineAmount),
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
      BigDecimal invoiceAmount =
          invoiceCustomerMoveLine.getInvoiceTermList().stream()
              .filter(
                  it ->
                      it.getPfpValidateStatusSelect()
                          != InvoiceTermRepository.PFP_STATUS_LITIGATION)
              .map(InvoiceTerm::getCompanyAmount)
              .reduce(BigDecimal::add)
              .orElse(invoiceCustomerMoveLine.getCredit());

      BigDecimal amount = totalDebitAmount.min(invoiceAmount);

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
              currencyService.computeScaledExchangeRate(amount, moveLineAmount),
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

  /**
   * Check if a move line is a holdback move line. A move line is considered a holdback if all its
   * invoice terms have isHoldBack = true.
   *
   * @param moveLine the move line to check
   * @return true if the move line is a holdback, false otherwise
   */
  protected boolean isHoldbackMoveLine(MoveLine moveLine) {
    if (moveLine.getInvoiceTermList() == null || moveLine.getInvoiceTermList().isEmpty()) {
      return false;
    }
    return moveLine.getInvoiceTermList().stream().allMatch(InvoiceTerm::getIsHoldBack);
  }
}
