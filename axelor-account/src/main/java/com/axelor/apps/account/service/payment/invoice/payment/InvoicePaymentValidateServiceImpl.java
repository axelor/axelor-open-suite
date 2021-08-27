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
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.repo.AccountConfigRepository;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.db.repo.PaymentModeRepository;
import com.axelor.apps.account.service.AccountingSituationService;
import com.axelor.apps.account.service.ReconcileService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.move.MoveLineService;
import com.axelor.apps.account.service.move.MoveService;
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
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;

public class InvoicePaymentValidateServiceImpl implements InvoicePaymentValidateService {

  protected PaymentModeService paymentModeService;
  protected MoveService moveService;
  protected MoveLineService moveLineService;
  protected AccountConfigService accountConfigService;
  protected InvoicePaymentRepository invoicePaymentRepository;
  protected ReconcileService reconcileService;
  protected InvoicePaymentToolService invoicePaymentToolService;
  protected AccountConfigRepository accountConfigRepo;

  @Inject
  public InvoicePaymentValidateServiceImpl(
      PaymentModeService paymentModeService,
      MoveService moveService,
      MoveLineService moveLineService,
      AccountConfigService accountConfigService,
      InvoicePaymentRepository invoicePaymentRepository,
      ReconcileService reconcileService,
      InvoicePaymentToolService invoicePaymentToolService,
      AccountConfigRepository accountConfigRepo) {

    this.paymentModeService = paymentModeService;
    this.moveService = moveService;
    this.moveLineService = moveLineService;
    this.accountConfigService = accountConfigService;
    this.invoicePaymentRepository = invoicePaymentRepository;
    this.reconcileService = reconcileService;
    this.invoicePaymentToolService = invoicePaymentToolService;
    this.accountConfigRepo = accountConfigRepo;
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

    Account customerAccount;

    Journal journal =
        paymentModeService.getPaymentModeJournal(paymentMode, company, companyBankDetails);

    boolean isDebitInvoice = moveService.getMoveToolService().isDebitCustomer(invoice, true);

    MoveLine invoiceMoveLine =
        moveService.getMoveToolService().getInvoiceCustomerMoveLineByLoop(invoice);

    if (invoice.getOperationSubTypeSelect() == InvoiceRepository.OPERATION_SUB_TYPE_ADVANCE) {

      AccountConfig accountConfig = accountConfigService.getAccountConfig(company);

      customerAccount = accountConfigService.getAdvancePaymentAccount(accountConfig);
    } else {
      if (invoiceMoveLine == null) {
        return null;
      }
      customerAccount = invoiceMoveLine.getAccount();
    }

    String origin = getOriginFromInvoicePayment(invoicePayment);

    Move move =
        moveService
            .getMoveCreateService()
            .createMove(
                journal,
                company,
                invoicePayment.getCurrency(),
                partner,
                paymentDate,
                paymentMode,
                MoveRepository.TECHNICAL_ORIGIN_AUTOMATIC,
                MoveRepository.FUNCTIONAL_ORIGIN_PAYMENT);

    MoveLine customerMoveLine = null;
    move.setTradingName(invoice.getTradingName());

    if (!invoicePayment.getApplyFinancialDiscount()) {

      BigDecimal paymentAmount = invoicePayment.getAmount();

      move.addMoveLineListItem(
          moveLineService.createMoveLine(
              move,
              partner,
              paymentModeService.getPaymentModeAccount(paymentMode, company, companyBankDetails),
              paymentAmount,
              isDebitInvoice,
              paymentDate,
              null,
              1,
              origin,
              invoicePayment.getDescription()));

      customerMoveLine =
          moveLineService.createMoveLine(
              move,
              partner,
              customerAccount,
              paymentAmount,
              !isDebitInvoice,
              paymentDate,
              null,
              2,
              origin,
              invoicePayment.getDescription());

      move.addMoveLineListItem(customerMoveLine);

    } else if (invoicePayment.getApplyFinancialDiscount()
        && invoicePayment.getFinancialDiscount() != null
        && Beans.get(AppAccountService.class).getAppAccount().getManageFinancialDiscount()) {
    	
    	List<Object> list = getMoveWithFinancialDiscount(invoicePayment, move, customerAccount);
    	move = (Move) list.get(0);
    	customerMoveLine = (MoveLine) list.get(1);
    }

    moveService.getMoveValidateService().validate(move);

    if (invoice.getOperationSubTypeSelect() != InvoiceRepository.OPERATION_SUB_TYPE_ADVANCE) {
      Reconcile reconcile =
          reconcileService.reconcile(invoiceMoveLine, customerMoveLine, true, false);

      invoicePayment.setReconcile(reconcile);
    }
    invoicePayment.setMove(move);

    invoicePaymentRepository.save(invoicePayment);

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
  public List<Object> getMoveWithFinancialDiscount(
      InvoicePayment invoicePayment, Move move, Account customerAccount) throws AxelorException {

    Invoice invoice = invoicePayment.getInvoice();
    Company company = invoice.getCompany();
    PaymentMode paymentMode = invoicePayment.getPaymentMode();
    Partner partner = invoice.getPartner();
    LocalDate paymentDate = invoicePayment.getPaymentDate();
    BankDetails companyBankDetails = invoicePayment.getCompanyBankDetails();
    boolean isDebitInvoice = moveService.getMoveToolService().isDebitCustomer(invoice, true);
    String origin = getOriginFromInvoicePayment(invoicePayment);
    MoveLine customerMoveLine = null;
    List<Object> list= new ArrayList<Object>();

    BigDecimal paymentAmount =
        invoicePayment
            .getAmount()
            .add(
                invoicePayment
                    .getFinancialDiscountAmount()
                    .add(invoicePayment.getFinancialDiscountTaxAmount()));

    BigDecimal paymentAmountWithoutDiscount = invoicePayment.getAmount();

    if (invoice.getOperationTypeSelect() == 1) {

      Account purchAccount = new Account();

      for (AccountManagement accountManagement :
          accountConfigRepo
              .findByCompany(company)
              .getPurchFinancialDiscountTax()
              .getAccountManagementList()) {
        if (accountManagement.getCompany().equals(company)) {
          purchAccount = accountManagement.getFinancialDiscountAccount();
        }
      }

      if (invoicePayment.getFinancialDiscount().getDiscountBaseSelect() == 0
          && purchAccount != null) {
        move.addMoveLineListItem(
            moveLineService.createMoveLine(
                move,
                partner,
                paymentModeService.getPaymentModeAccount(paymentMode, company, companyBankDetails),
                paymentAmountWithoutDiscount,
                isDebitInvoice,
                paymentDate,
                null,
                1,
                origin,
                invoicePayment.getDescription()));

        move.addMoveLineListItem(
            moveLineService.createMoveLine(
                move,
                partner,
                accountConfigRepo.findByCompany(company).getPurchFinancialDiscountAccount(),
                invoicePayment.getFinancialDiscountAmount(),
                isDebitInvoice,
                paymentDate,
                null,
                2,
                origin,
                invoicePayment.getDescription()));

        customerMoveLine =
            moveLineService.createMoveLine(
                move,
                partner,
                customerAccount,
                paymentAmount,
                !isDebitInvoice,
                paymentDate,
                null,
                3,
                origin,
                invoicePayment.getDescription());

      } else if (invoicePayment.getFinancialDiscount().getDiscountBaseSelect() == 1
          && purchAccount != null) {

        move.addMoveLineListItem(
            moveLineService.createMoveLine(
                move,
                partner,
                paymentModeService.getPaymentModeAccount(paymentMode, company, companyBankDetails),
                paymentAmountWithoutDiscount,
                isDebitInvoice,
                paymentDate,
                null,
                1,
                origin,
                invoicePayment.getDescription()));

        move.addMoveLineListItem(
            moveLineService.createMoveLine(
                move,
                partner,
                accountConfigRepo.findByCompany(company).getPurchFinancialDiscountAccount(),
                invoicePayment.getFinancialDiscountAmount(),
                isDebitInvoice,
                paymentDate,
                null,
                2,
                origin,
                invoicePayment.getDescription()));

        move.addMoveLineListItem(
            moveLineService.createMoveLine(
                move,
                partner,
                purchAccount,
                invoicePayment.getFinancialDiscountTaxAmount(),
                isDebitInvoice,
                paymentDate,
                null,
                3,
                origin,
                invoicePayment.getDescription()));

        customerMoveLine =
            moveLineService.createMoveLine(
                move,
                partner,
                customerAccount,
                paymentAmount,
                !isDebitInvoice,
                paymentDate,
                null,
                4,
                origin,
                invoicePayment.getDescription());
      }

    } else if (invoice.getOperationTypeSelect() == 3) {

      Account saleAccount = new Account();
      for (AccountManagement accountManagement :
          accountConfigRepo
              .findByCompany(company)
              .getSaleFinancialDiscountTax()
              .getAccountManagementList()) {
        if (accountManagement.getCompany().equals(company)) {
          saleAccount = accountManagement.getFinancialDiscountAccount();
        }
      }

      if (invoicePayment.getFinancialDiscount().getDiscountBaseSelect() == 0
          && saleAccount != null) {
        move.addMoveLineListItem(
            moveLineService.createMoveLine(
                move,
                partner,
                paymentModeService.getPaymentModeAccount(paymentMode, company, companyBankDetails),
                paymentAmountWithoutDiscount,
                isDebitInvoice,
                paymentDate,
                null,
                1,
                origin,
                invoicePayment.getDescription()));

        move.addMoveLineListItem(
            moveLineService.createMoveLine(
                move,
                partner,
                accountConfigRepo.findByCompany(company).getSaleFinancialDiscountAccount(),
                invoicePayment.getFinancialDiscountAmount(),
                isDebitInvoice,
                paymentDate,
                null,
                2,
                origin,
                invoicePayment.getDescription()));

        customerMoveLine =
            moveLineService.createMoveLine(
                move,
                partner,
                customerAccount,
                paymentAmount,
                !isDebitInvoice,
                paymentDate,
                null,
                3,
                origin,
                invoicePayment.getDescription());

      } else if (invoicePayment.getFinancialDiscount().getDiscountBaseSelect() == 1
          && saleAccount != null) {

        move.addMoveLineListItem(
            moveLineService.createMoveLine(
                move,
                partner,
                paymentModeService.getPaymentModeAccount(paymentMode, company, companyBankDetails),
                paymentAmountWithoutDiscount,
                isDebitInvoice,
                paymentDate,
                null,
                1,
                origin,
                invoicePayment.getDescription()));

        move.addMoveLineListItem(
            moveLineService.createMoveLine(
                move,
                partner,
                accountConfigRepo.findByCompany(company).getSaleFinancialDiscountAccount(),
                invoicePayment.getFinancialDiscountAmount(),
                isDebitInvoice,
                paymentDate,
                null,
                2,
                origin,
                invoicePayment.getDescription()));

        move.addMoveLineListItem(
            moveLineService.createMoveLine(
                move,
                partner,
                saleAccount,
                invoicePayment.getFinancialDiscountTaxAmount(),
                isDebitInvoice,
                paymentDate,
                null,
                3,
                origin,
                invoicePayment.getDescription()));

        customerMoveLine =
            moveLineService.createMoveLine(
                move,
                partner,
                customerAccount,
                paymentAmount,
                !isDebitInvoice,
                paymentDate,
                null,
                4,
                origin,
                invoicePayment.getDescription());
      }
    }
    move.addMoveLineListItem(customerMoveLine);
    list.add(move);
    list.add(customerMoveLine);
    return list;
  }
}
