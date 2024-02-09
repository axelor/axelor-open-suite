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
package com.axelor.apps.bankpayment.service.bankorder;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountingSituation;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.repo.AccountRepository;
import com.axelor.apps.account.db.repo.InvoiceTermRepository;
import com.axelor.apps.account.db.repo.JournalRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.db.repo.PaymentModeRepository;
import com.axelor.apps.account.service.AccountingSituationService;
import com.axelor.apps.account.service.move.MoveCreateService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.account.service.payment.PaymentModeService;
import com.axelor.apps.bankpayment.db.BankOrder;
import com.axelor.apps.bankpayment.db.BankOrderLine;
import com.axelor.apps.bankpayment.db.BankOrderLineOrigin;
import com.axelor.apps.bankpayment.db.repo.BankOrderLineOriginRepository;
import com.axelor.apps.bankpayment.db.repo.BankOrderLineRepository;
import com.axelor.apps.bankpayment.db.repo.BankOrderRepository;
import com.axelor.apps.bankpayment.exception.BankPaymentExceptionMessage;
import com.axelor.apps.bankpayment.service.config.BankPaymentConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Currency;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.BankDetailsRepository;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BankOrderMoveServiceImpl implements BankOrderMoveService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public static final int FETCH_LIMIT = 20;

  protected MoveCreateService moveCreateService;
  protected MoveValidateService moveValidateService;
  protected PaymentModeService paymentModeService;
  protected AccountingSituationService accountingSituationService;
  protected BankPaymentConfigService bankPaymentConfigService;
  protected MoveLineCreateService moveLineCreateService;
  protected BankOrderLineRepository bankOrderLineRepository;
  protected PaymentModeRepository paymentModeRepository;
  protected CompanyRepository companyRepository;
  protected BankDetailsRepository bankDetailsRepository;
  protected JournalRepository journalRepository;
  protected AccountRepository accountRepository;
  protected InvoiceTermRepository invoiceTermRepository;

  protected PaymentMode paymentMode;
  protected Company senderCompany;
  protected int orderTypeSelect;
  protected int partnerTypeSelect;
  protected Journal journal;
  protected Account senderBankAccount;
  protected BankDetails senderBankDetails;
  protected boolean isMultiDate;
  protected boolean isMultiCurrency;
  protected boolean isDebit;

  @Inject
  public BankOrderMoveServiceImpl(
      MoveCreateService moveCreateService,
      MoveValidateService moveValidateService,
      PaymentModeService paymentModeService,
      AccountingSituationService accountingSituationService,
      BankPaymentConfigService bankPaymentConfigService,
      MoveLineCreateService moveLineCreateService,
      BankOrderLineRepository bankOrderLineRepository,
      PaymentModeRepository paymentModeRepository,
      CompanyRepository companyRepository,
      BankDetailsRepository bankDetailsRepository,
      JournalRepository journalRepository,
      AccountRepository accountRepository,
      InvoiceTermRepository invoiceTermRepository) {

    this.moveCreateService = moveCreateService;
    this.moveValidateService = moveValidateService;
    this.paymentModeService = paymentModeService;
    this.accountingSituationService = accountingSituationService;
    this.bankPaymentConfigService = bankPaymentConfigService;
    this.moveLineCreateService = moveLineCreateService;
    this.bankOrderLineRepository = bankOrderLineRepository;
    this.paymentModeRepository = paymentModeRepository;
    this.companyRepository = companyRepository;
    this.bankDetailsRepository = bankDetailsRepository;
    this.journalRepository = journalRepository;
    this.accountRepository = accountRepository;
    this.invoiceTermRepository = invoiceTermRepository;
  }

  @Override
  public void generateMoves(BankOrder bankOrder) throws AxelorException {

    if (bankOrder.getBankOrderLineList() == null || bankOrder.getBankOrderLineList().isEmpty()) {
      return;
    }

    paymentMode = bankOrder.getPaymentMode();

    if (bankOrder.getAccountingTriggerSelect() == PaymentModeRepository.ACCOUNTING_TRIGGER_NONE
        || bankOrder.getAccountingTriggerSelect()
            == PaymentModeRepository.ACCOUNTING_TRIGGER_IMMEDIATE) {
      return;
    }

    orderTypeSelect = bankOrder.getOrderTypeSelect();
    senderCompany = bankOrder.getSenderCompany();
    senderBankDetails = bankOrder.getSenderBankDetails();
    partnerTypeSelect = bankOrder.getPartnerTypeSelect();

    journal =
        paymentModeService.getPaymentModeJournal(paymentMode, senderCompany, senderBankDetails);
    senderBankAccount =
        paymentModeService.getPaymentModeAccount(paymentMode, senderCompany, senderBankDetails);

    isMultiDate = bankOrder.getIsMultiDate();
    isMultiCurrency = BankOrderToolService.isMultiCurrency(bankOrder);

    isDebit =
        orderTypeSelect == BankOrderRepository.ORDER_TYPE_INTERNATIONAL_CREDIT_TRANSFER
            || orderTypeSelect == BankOrderRepository.ORDER_TYPE_SEPA_CREDIT_TRANSFER;

    generateMovesBankOrderLines(bankOrder);
  }

  @Transactional(rollbackOn = Exception.class)
  protected void generateMovesBankOrderLines(BankOrder bankOrder) throws AxelorException {

    Query<BankOrderLine> query =
        bankOrderLineRepository
            .all()
            .filter("self.bankOrder = :bankOrder")
            .bind("bankOrder", bankOrder)
            .order("id");

    List<BankOrderLine> bankOrderLines = query.fetch(FETCH_LIMIT, 0);
    if (bankOrderLines.size() == 1) {
      generateMoves(bankOrderLines.get(0));
    } else {
      int offSet = FETCH_LIMIT;

      while (!bankOrderLines.isEmpty()) {
        for (BankOrderLine bankOrderLine : bankOrderLines) {
          generateMoves(bankOrderLine);
        }

        JPA.clear();
        fetchDetachedEntities();
        bankOrderLines = query.fetch(FETCH_LIMIT, offSet);
        offSet += FETCH_LIMIT;
      }
    }
  }

  protected void fetchDetachedEntities() {

    this.paymentMode = paymentModeRepository.find(this.paymentMode.getId());
    this.senderCompany = companyRepository.find(this.senderCompany.getId());
    this.senderBankDetails = bankDetailsRepository.find(this.senderBankDetails.getId());
    this.journal = journalRepository.find(this.journal.getId());
    this.senderBankAccount = accountRepository.find(this.senderBankAccount.getId());
  }

  protected void generateMoves(BankOrderLine bankOrderLine) throws AxelorException {

    if (bankOrderLine.getSenderMove() == null) {
      bankOrderLine.setSenderMove(generateSenderMove(bankOrderLine));
    }
    if (partnerTypeSelect == BankOrderRepository.PARTNER_TYPE_COMPANY
        && bankOrderLine.getReceiverMove() == null) {
      bankOrderLine.setReceiverMove(generateReceiverMove(bankOrderLine));
    }

    bankOrderLineRepository.save(bankOrderLine);
  }

  protected Move generateSenderMove(BankOrderLine bankOrderLine) throws AxelorException {

    Partner partner = bankOrderLine.getPartner();

    Move senderMove =
        moveCreateService.createMove(
            journal,
            senderCompany,
            this.getCurrency(bankOrderLine),
            partner,
            this.getDate(bankOrderLine),
            null,
            paymentMode,
            partner != null ? partner.getFiscalPosition() : null,
            MoveRepository.TECHNICAL_ORIGIN_AUTOMATIC,
            MoveRepository.FUNCTIONAL_ORIGIN_PAYMENT,
            bankOrderLine.getReceiverReference(),
            bankOrderLine.getReceiverLabel(),
            bankOrderLine.getBankOrder().getSenderBankDetails());

    senderMove.setPartnerBankDetails(getPartnerBankDetails(bankOrderLine));

    MoveLine bankMoveLine =
        moveLineCreateService.createMoveLine(
            senderMove,
            partner,
            senderBankAccount,
            bankOrderLine.getBankOrderAmount(),
            !isDebit,
            senderMove.getDate(),
            1,
            bankOrderLine.getReceiverReference(),
            bankOrderLine.getReceiverLabel());
    senderMove.addMoveLineListItem(bankMoveLine);

    MoveLine partnerMoveLine =
        moveLineCreateService.createMoveLine(
            senderMove,
            partner,
            getPartnerAccount(partner, senderCompany, senderCompany),
            bankOrderLine.getBankOrderAmount(),
            isDebit,
            senderMove.getDate(),
            2,
            bankOrderLine.getReceiverReference(),
            bankOrderLine.getReceiverLabel());
    senderMove.addMoveLineListItem(partnerMoveLine);

    moveValidateService.accounting(senderMove);

    return senderMove;
  }

  protected Move generateReceiverMove(BankOrderLine bankOrderLine) throws AxelorException {

    Partner partner = bankOrderLine.getPartner();
    Company receiverCompany = bankOrderLine.getReceiverCompany();

    BankDetails receiverBankDetails = bankOrderLine.getReceiverBankDetails();

    Journal receiverJournal =
        paymentModeService.getPaymentModeJournal(paymentMode, receiverCompany, receiverBankDetails);
    Account receiverBankAccount =
        paymentModeService.getPaymentModeAccount(paymentMode, receiverCompany, receiverBankDetails);

    Move receiverMove =
        moveCreateService.createMove(
            receiverJournal,
            receiverCompany,
            this.getCurrency(bankOrderLine),
            partner,
            this.getDate(bankOrderLine),
            null,
            paymentMode,
            partner != null ? partner.getFiscalPosition() : null,
            MoveRepository.TECHNICAL_ORIGIN_AUTOMATIC,
            MoveRepository.FUNCTIONAL_ORIGIN_PAYMENT,
            bankOrderLine.getReceiverReference(),
            bankOrderLine.getReceiverLabel(),
            bankOrderLine.getBankOrder().getSenderBankDetails());

    receiverMove.setPartnerBankDetails(getPartnerBankDetails(bankOrderLine));

    MoveLine bankMoveLine =
        moveLineCreateService.createMoveLine(
            receiverMove,
            partner,
            receiverBankAccount,
            bankOrderLine.getBankOrderAmount(),
            isDebit,
            receiverMove.getDate(),
            1,
            bankOrderLine.getReceiverReference(),
            bankOrderLine.getReceiverLabel());
    receiverMove.addMoveLineListItem(bankMoveLine);

    MoveLine partnerMoveLine =
        moveLineCreateService.createMoveLine(
            receiverMove,
            partner,
            getPartnerAccount(partner, receiverCompany, receiverMove.getCompany()),
            bankOrderLine.getBankOrderAmount(),
            !isDebit,
            receiverMove.getDate(),
            2,
            bankOrderLine.getReceiverReference(),
            bankOrderLine.getReceiverLabel());
    receiverMove.addMoveLineListItem(partnerMoveLine);

    moveValidateService.accounting(receiverMove);

    return receiverMove;
  }

  protected Account getPartnerAccount(Partner partner, Company receiverCompany, Company moveCompany)
      throws AxelorException {

    AccountingSituation accountingSituation =
        accountingSituationService.getAccountingSituation(partner, receiverCompany);

    switch (partnerTypeSelect) {
      case BankOrderRepository.PARTNER_TYPE_CUSTOMER:
        return accountingSituationService.getCustomerAccount(partner, receiverCompany);

      case BankOrderRepository.PARTNER_TYPE_EMPLOYEE:
        return accountingSituationService.getEmployeeAccount(partner, receiverCompany);

      case BankOrderRepository.PARTNER_TYPE_SUPPLIER:
        return accountingSituationService.getSupplierAccount(partner, receiverCompany);

      case BankOrderRepository.PARTNER_TYPE_COMPANY:
        if (receiverCompany.equals(senderCompany)) {
          return bankPaymentConfigService.getInternalBankToBankAccount(
              bankPaymentConfigService.getBankPaymentConfig(moveCompany));
        } else {
          return bankPaymentConfigService.getExternalBankToBankAccount(
              bankPaymentConfigService.getBankPaymentConfig(moveCompany));
        }

      default:
        throw new AxelorException(
            accountingSituation,
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(BankPaymentExceptionMessage.BANK_ORDER_PARTNER_TYPE_MISSING),
            I18n.get(BaseExceptionMessage.EXCEPTION));
    }
  }

  protected LocalDate getDate(BankOrderLine bankOrderLine) {

    if (isMultiDate) {
      return bankOrderLine.getBankOrderDate();
    } else {
      return bankOrderLine.getBankOrder().getBankOrderDate();
    }
  }

  protected Currency getCurrency(BankOrderLine bankOrderLine) {

    if (isMultiCurrency) {
      return bankOrderLine.getBankOrderCurrency();
    } else {
      return bankOrderLine.getBankOrder().getBankOrderCurrency();
    }
  }

  protected BankDetails getPartnerBankDetails(BankOrderLine bankOrderLine) {
    if (bankOrderLine.getBankOrderLineOriginList().size() == 1
        && BankOrderLineOriginRepository.RELATED_TO_INVOICE_TERM.equals(
            bankOrderLine.getBankOrderLineOriginList().get(0).getRelatedToSelect())) {
      BankOrderLineOrigin origin = bankOrderLine.getBankOrderLineOriginList().get(0);
      if (origin.getRelatedToSelectId() != null) {
        InvoiceTerm invoiceTerm = invoiceTermRepository.find(origin.getRelatedToSelectId());

        if (invoiceTerm != null && invoiceTerm.getBankDetails() != null) {
          return invoiceTerm.getBankDetails();
        } else if (Optional.ofNullable(invoiceTerm)
            .map(InvoiceTerm::getMoveLine)
            .map(MoveLine::getMove)
            .map(Move::getPartnerBankDetails)
            .isPresent()) {
          return invoiceTerm.getMoveLine().getMove().getPartnerBankDetails();
        } else if (Optional.ofNullable(invoiceTerm)
            .map(InvoiceTerm::getInvoice)
            .map(Invoice::getBankDetails)
            .isPresent()) {
          return invoiceTerm.getInvoice().getBankDetails();
        }
      }
    }
    return null;
  }
}
