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
package com.axelor.apps.account.service.payment.invoice.payment;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.AccountManagement;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.repo.FinancialDiscountRepository;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.db.repo.PaymentModeRepository;
import com.axelor.apps.account.service.AccountingSituationService;
import com.axelor.apps.account.service.ReconcileService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.account.service.move.MoveCreateService;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.account.service.payment.PaymentModeService;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import org.apache.commons.collections.CollectionUtils;

public class InvoicePaymentValidateServiceImpl implements InvoicePaymentValidateService {

  protected PaymentModeService paymentModeService;
  protected MoveCreateService moveCreateService;
  protected MoveValidateService moveValidateService;
  protected MoveToolService moveToolService;
  protected MoveLineCreateService moveLineCreateService;
  protected AccountConfigService accountConfigService;
  protected InvoicePaymentRepository invoicePaymentRepository;
  protected ReconcileService reconcileService;
  protected InvoicePaymentToolService invoicePaymentToolService;
  protected AppAccountService appAccountService;
  protected InvoiceTermService invoiceTermService;

  @Inject
  public InvoicePaymentValidateServiceImpl(
      PaymentModeService paymentModeService,
      MoveCreateService moveCreateService,
      MoveValidateService moveValidateService,
      MoveToolService moveToolService,
      MoveLineCreateService moveLineCreateService,
      AccountConfigService accountConfigService,
      InvoicePaymentRepository invoicePaymentRepository,
      ReconcileService reconcileService,
      InvoicePaymentToolService invoicePaymentToolService,
      InvoiceTermService invoiceTermService,
      AppAccountService appAccountService) {

    this.paymentModeService = paymentModeService;
    this.moveLineCreateService = moveLineCreateService;
    this.moveCreateService = moveCreateService;
    this.moveValidateService = moveValidateService;
    this.moveToolService = moveToolService;
    this.accountConfigService = accountConfigService;
    this.invoicePaymentRepository = invoicePaymentRepository;
    this.reconcileService = reconcileService;
    this.invoicePaymentToolService = invoicePaymentToolService;
    this.invoiceTermService = invoiceTermService;
    this.appAccountService = appAccountService;
  }

  /**
   * Method to validate an invoice Payment
   *
   * <p>Create the eventual move (depending general configuration) and reconcile it with the invoice
   * move Compute the amount paid on invoice Change the status to validated
   *
   * @param invoicePayment An invoice payment
   * @throws AxelorException
   * @throws DatatypeConfigurationException
   * @throws IOException
   * @throws JAXBException
   */
  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void validate(InvoicePayment invoicePayment, boolean force)
      throws AxelorException, JAXBException, IOException, DatatypeConfigurationException {

    if (!force && invoicePayment.getStatusSelect() != InvoicePaymentRepository.STATUS_DRAFT) {
      return;
    }

    invoicePayment.setStatusSelect(InvoicePaymentRepository.STATUS_VALIDATED);

    // TODO assign an automatic reference

    Company company = invoicePayment.getInvoice().getCompany();

    if (accountConfigService.getAccountConfig(company).getGenerateMoveForInvoicePayment()) {
      invoicePayment = this.createMoveForInvoicePayment(invoicePayment);
    } else {
      Beans.get(AccountingSituationService.class)
          .updateCustomerCredit(invoicePayment.getInvoice().getPartner());
      invoicePayment = invoicePaymentRepository.save(invoicePayment);
    }

    invoiceTermService.updateInvoiceTermsPaidAmount(invoicePayment);

    invoicePaymentToolService.updateAmountPaid(invoicePayment.getInvoice());
    if (invoicePayment.getInvoice() != null
        && invoicePayment.getInvoice().getOperationSubTypeSelect()
            == InvoiceRepository.OPERATION_SUB_TYPE_ADVANCE) {
      invoicePayment.setTypeSelect(InvoicePaymentRepository.TYPE_ADVANCEPAYMENT);
    }
    invoicePaymentRepository.save(invoicePayment);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void validate(InvoicePayment invoicePayment)
      throws AxelorException, JAXBException, IOException, DatatypeConfigurationException {
    validate(invoicePayment, false);
  }

  /**
   * Method to create a payment move for an invoice Payment
   *
   * <p>Create a move and reconcile it with the invoice move
   *
   * @param invoicePayment An invoice payment
   * @throws AxelorException
   */
  @Transactional(rollbackOn = {Exception.class})
  public InvoicePayment createMoveForInvoicePayment(InvoicePayment invoicePayment)
      throws AxelorException {

    Invoice invoice = invoicePayment.getInvoice();
    Company company = invoice.getCompany();
    PaymentMode paymentMode = invoicePayment.getPaymentMode();
    Partner partner = invoice.getPartner();
    LocalDate paymentDate = invoicePayment.getPaymentDate();
    BankDetails companyBankDetails = invoicePayment.getCompanyBankDetails();
    String description = invoicePayment.getDescription();
    if (description == null || description.isEmpty()) {
      description =
          String.format(
              "%s-%s-%s",
              invoicePayment.getPaymentMode().getName(),
              invoice.getPartner().getName(),
              invoice.getDueDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
    }

    Account customerAccount;

    Journal journal =
        paymentModeService.getPaymentModeJournal(paymentMode, company, companyBankDetails);

    List<MoveLine> invoiceMoveLines = moveToolService.getInvoiceCustomerMoveLines(invoicePayment);

    if (invoice.getOperationSubTypeSelect() == InvoiceRepository.OPERATION_SUB_TYPE_ADVANCE) {

      AccountConfig accountConfig = accountConfigService.getAccountConfig(company);

      customerAccount = accountConfigService.getAdvancePaymentAccount(accountConfig);
    } else {
      if (CollectionUtils.isEmpty(invoiceMoveLines)) {
        return null;
      }
      customerAccount = invoiceMoveLines.get(0).getAccount();

      Move move =
          moveCreateService.createMove(
              journal,
              company,
              invoicePayment.getCurrency(),
              partner,
              paymentDate,
              paymentMode,
              MoveRepository.TECHNICAL_ORIGIN_AUTOMATIC,
              MoveRepository.FUNCTIONAL_ORIGIN_PAYMENT,
              getOriginFromInvoicePayment(invoicePayment),
              description);

      MoveLine customerMoveLine = null;
      move.setTradingName(invoice.getTradingName());

      if (invoicePayment.getApplyFinancialDiscount()
          && invoicePayment.getFinancialDiscount() != null
          && appAccountService.getAppAccount().getManageFinancialDiscount()) {

        move = getMoveWithFinancialDiscount(invoicePayment, move, customerAccount);
      } else {
        move = getMoveWithoutFinancialDiscount(invoicePayment, move, customerAccount);
      }

      moveValidateService.validate(move);

      for (MoveLine moveline : move.getMoveLineList()) {
        if (moveline.getAccount() == customerAccount) {
          customerMoveLine = moveline;
        }
      }

      if (invoice.getOperationSubTypeSelect() != InvoiceRepository.OPERATION_SUB_TYPE_ADVANCE) {
        for (MoveLine invoiceMoveLine : invoiceMoveLines) {
          invoicePayment.addReconcileListItem(
              reconcileService.reconcile(invoiceMoveLine, customerMoveLine, true, false));
        }
      }
      invoicePayment.setMove(move);

      invoicePaymentRepository.save(invoicePayment);
    }
    return invoicePayment;
  }

  public String getOriginFromInvoicePayment(InvoicePayment invoicePayment) {
    String origin = invoicePayment.getInvoice().getInvoiceId();
    if (invoicePayment.getPaymentMode().getTypeSelect() == PaymentModeRepository.TYPE_CHEQUE
        || invoicePayment.getPaymentMode().getTypeSelect()
            == PaymentModeRepository.TYPE_IPO_CHEQUE) {
      origin = invoicePayment.getChequeNumber() != null ? invoicePayment.getChequeNumber() : origin;
    } else if (invoicePayment.getPaymentMode().getTypeSelect()
        == PaymentModeRepository.TYPE_BANK_CARD) {
      origin =
          invoicePayment.getInvoicePaymentRef() != null
              ? invoicePayment.getInvoicePaymentRef()
              : origin;
    }
    if (invoicePayment.getInvoice().getOperationTypeSelect()
            == InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE
        || invoicePayment.getInvoice().getOperationTypeSelect()
            == InvoiceRepository.OPERATION_TYPE_SUPPLIER_REFUND) {
      origin = invoicePayment.getInvoice().getSupplierInvoiceNb();
    }
    return origin;
  }

  @Transactional(rollbackOn = {Exception.class})
  public Move getMoveWithoutFinancialDiscount(
      InvoicePayment invoicePayment, Move move, Account customerAccount) throws AxelorException {

    Invoice invoice = invoicePayment.getInvoice();
    Company company = invoice.getCompany();
    BigDecimal paymentAmount = invoicePayment.getAmount();
    PaymentMode paymentMode = invoicePayment.getPaymentMode();
    Partner partner = invoice.getPartner();
    LocalDate paymentDate = invoicePayment.getPaymentDate();
    BankDetails companyBankDetails = invoicePayment.getCompanyBankDetails();
    boolean isDebitInvoice = moveToolService.isDebitCustomer(invoice, true);
    String origin = getOriginFromInvoicePayment(invoicePayment);

    move.addMoveLineListItem(
        moveLineCreateService.createMoveLine(
            move,
            partner,
            paymentModeService.getPaymentModeAccount(paymentMode, company, companyBankDetails),
            paymentAmount,
            isDebitInvoice,
            paymentDate,
            null,
            1,
            origin,
            move.getDescription()));

    move.addMoveLineListItem(
        moveLineCreateService.createMoveLine(
            move,
            partner,
            customerAccount,
            paymentAmount,
            !isDebitInvoice,
            paymentDate,
            null,
            2,
            origin,
            move.getDescription()));

    return move;
  }

  @Transactional(rollbackOn = {Exception.class})
  public Move getMoveWithFinancialDiscount(
      InvoicePayment invoicePayment, Move move, Account customerAccount) throws AxelorException {

    Invoice invoice = invoicePayment.getInvoice();
    Company company = invoice.getCompany();
    AccountConfig accountConfig = accountConfigService.getAccountConfig(company);

    if (invoice.getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_SUPPLIER_PURCHASE) {

      Account purchAccount = new Account();

      for (AccountManagement accountManagement :
          accountConfig.getPurchFinancialDiscountTax().getAccountManagementList()) {
        if (accountManagement.getCompany().equals(company)) {
          purchAccount = accountManagement.getFinancialDiscountAccount();
        }
      }

      move =
          getMoveLineWithFinancialDiscount(
              invoicePayment,
              move,
              customerAccount,
              purchAccount,
              accountConfigService.getAccountConfig(company).getPurchFinancialDiscountAccount());

    } else if (invoice.getOperationTypeSelect() == InvoiceRepository.OPERATION_TYPE_CLIENT_SALE) {

      Account saleAccount = new Account();
      for (AccountManagement accountManagement :
          accountConfig.getSaleFinancialDiscountTax().getAccountManagementList()) {
        if (accountManagement.getCompany().equals(company)) {
          saleAccount = accountManagement.getFinancialDiscountAccount();
        }
      }

      move =
          getMoveLineWithFinancialDiscount(
              invoicePayment,
              move,
              customerAccount,
              saleAccount,
              accountConfigService.getAccountConfig(company).getSaleFinancialDiscountAccount());
    }

    return move;
  }

  @Transactional(rollbackOn = {Exception.class})
  public Move getMoveLineWithFinancialDiscount(
      InvoicePayment invoicePayment,
      Move move,
      Account customerAccount,
      Account account,
      Account configAccount)
      throws AxelorException {
    Invoice invoice = invoicePayment.getInvoice();
    Company company = invoice.getCompany();
    PaymentMode paymentMode = invoicePayment.getPaymentMode();
    Partner partner = invoice.getPartner();
    LocalDate paymentDate = invoicePayment.getPaymentDate();
    BankDetails companyBankDetails = invoicePayment.getCompanyBankDetails();
    boolean isDebitInvoice = moveToolService.isDebitCustomer(invoice, true);
    String origin = getOriginFromInvoicePayment(invoicePayment);
    BigDecimal paymentAmountWithoutDiscount = invoicePayment.getAmount();

    BigDecimal paymentAmount =
        invoicePayment
            .getAmount()
            .add(
                invoicePayment
                    .getFinancialDiscountAmount()
                    .add(invoicePayment.getFinancialDiscountTaxAmount()));

    move.addMoveLineListItem(
        moveLineCreateService.createMoveLine(
            move,
            partner,
            paymentModeService.getPaymentModeAccount(paymentMode, company, companyBankDetails),
            paymentAmountWithoutDiscount,
            isDebitInvoice,
            paymentDate,
            null,
            1,
            move.getOrigin(),
            move.getDescription()));

    move.addMoveLineListItem(
        moveLineCreateService.createMoveLine(
            move,
            partner,
            configAccount,
            invoicePayment.getFinancialDiscountAmount(),
            isDebitInvoice,
            paymentDate,
            null,
            2,
            origin,
            invoicePayment.getDescription()));

    move.addMoveLineListItem(
        moveLineCreateService.createMoveLine(
            move,
            partner,
            customerAccount,
            paymentAmount,
            !isDebitInvoice,
            paymentDate,
            null,
            3,
            move.getOrigin(),
            move.getDescription()));

    if (invoicePayment.getFinancialDiscount().getDiscountBaseSelect()
            == FinancialDiscountRepository.DISCOUNT_BASE_VAT
        && account != null) {

      move.addMoveLineListItem(
          moveLineCreateService.createMoveLine(
              move,
              partner,
              account,
              invoicePayment.getFinancialDiscountTaxAmount(),
              isDebitInvoice,
              paymentDate,
              null,
              4,
              origin,
              invoicePayment.getDescription()));
    }

    return move;
  }
}
