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
package com.axelor.apps.account.service.payment.invoice.payment;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.Tax;
import com.axelor.apps.account.db.repo.FinancialDiscountRepository;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.db.repo.PaymentModeRepository;
import com.axelor.apps.account.service.AccountManagementAccountService;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.AccountingSituationService;
import com.axelor.apps.account.service.ReconcileService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceToolService;
import com.axelor.apps.account.service.move.MoveCreateService;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.account.service.payment.PaymentModeService;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;

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
  protected AccountManagementAccountService accountManagementService;

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
      AppAccountService appAccountService,
      AccountManagementAccountService accountManagementService) {

    this.paymentModeService = paymentModeService;
    this.moveLineCreateService = moveLineCreateService;
    this.moveCreateService = moveCreateService;
    this.moveValidateService = moveValidateService;
    this.moveToolService = moveToolService;
    this.accountConfigService = accountConfigService;
    this.invoicePaymentRepository = invoicePaymentRepository;
    this.reconcileService = reconcileService;
    this.invoicePaymentToolService = invoicePaymentToolService;
    this.appAccountService = appAccountService;
    this.accountManagementService = accountManagementService;
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

    Invoice invoice = invoicePayment.getInvoice();
    validatePartnerAccount(invoice);

    if (!force && invoicePayment.getStatusSelect() != InvoicePaymentRepository.STATUS_DRAFT) {
      return;
    }

    setInvoicePaymentStatus(invoicePayment);
    createInvoicePaymentMove(invoicePayment);

    invoicePaymentToolService.updateAmountPaid(invoicePayment.getInvoice());
    if (invoicePayment.getInvoice() != null
        && invoicePayment.getInvoice().getOperationSubTypeSelect()
            == InvoiceRepository.OPERATION_SUB_TYPE_ADVANCE) {
      invoicePayment.setTypeSelect(InvoicePaymentRepository.TYPE_ADVANCEPAYMENT);
    }
    invoicePaymentRepository.save(invoicePayment);
  }

  protected void validatePartnerAccount(Invoice invoice) throws AxelorException {
    Account partnerAccount = invoice.getPartnerAccount();
    if (!partnerAccount.getReconcileOk() || !partnerAccount.getUseForPartnerBalance()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(AccountExceptionMessage.ACCOUNT_RECONCILABLE_USE_FOR_PARTNER_BALANCE));
    }
  }

  protected void createInvoicePaymentMove(InvoicePayment invoicePayment)
      throws AxelorException, JAXBException, IOException, DatatypeConfigurationException {
    Invoice invoice = invoicePayment.getInvoice();
    if (accountConfigService
        .getAccountConfig(invoice.getCompany())
        .getGenerateMoveForInvoicePayment()) {
      this.createMoveForInvoicePayment(invoicePayment);
    } else {
      Beans.get(AccountingSituationService.class).updateCustomerCredit(invoice.getPartner());
      invoicePaymentRepository.save(invoicePayment);
    }
  }

  protected void setInvoicePaymentStatus(InvoicePayment invoicePayment) throws AxelorException {
    invoicePayment.setStatusSelect(InvoicePaymentRepository.STATUS_VALIDATED);
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

    MoveLine invoiceMoveLine = moveToolService.getInvoiceCustomerMoveLineByLoop(invoice);

    if (invoice.getOperationSubTypeSelect() == InvoiceRepository.OPERATION_SUB_TYPE_ADVANCE) {

      AccountConfig accountConfig = accountConfigService.getAccountConfig(company);

      if (InvoiceToolService.isPurchase(invoice)) {
        customerAccount = accountConfigService.getSupplierAdvancePaymentAccount(accountConfig);
      } else {
        customerAccount = accountConfigService.getAdvancePaymentAccount(accountConfig);
      }

    } else {
      if (invoiceMoveLine == null) {
        return null;
      }
      customerAccount = invoiceMoveLine.getAccount();
    }
    Move move =
        moveCreateService.createMove(
            journal,
            company,
            invoicePayment.getCurrency(),
            partner,
            paymentDate,
            paymentDate,
            paymentMode,
            invoice.getFiscalPosition(),
            MoveRepository.TECHNICAL_ORIGIN_AUTOMATIC,
            MoveRepository.FUNCTIONAL_ORIGIN_PAYMENT,
            getOriginFromInvoicePayment(invoicePayment),
            description);

    MoveLine customerMoveLine = null;
    move.setTradingName(invoice.getTradingName());

    move = this.fillMove(invoicePayment, move, customerAccount);
    moveValidateService.accounting(move);

    for (MoveLine moveline : move.getMoveLineList()) {
      if (moveline.getAccount() == customerAccount) {
        customerMoveLine = moveline;
      }
    }
    if (invoice.getOperationSubTypeSelect() != InvoiceRepository.OPERATION_SUB_TYPE_ADVANCE) {
      Reconcile reconcile =
          reconcileService.reconcile(invoiceMoveLine, customerMoveLine, true, false);

      invoicePayment.setReconcile(reconcile);
    }
    invoicePayment.setMove(move);

    return invoicePaymentRepository.save(invoicePayment);
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
  public Move fillMove(InvoicePayment invoicePayment, Move move, Account customerAccount)
      throws AxelorException {

    Invoice invoice = invoicePayment.getInvoice();
    Company company = invoice.getCompany();
    BigDecimal paymentAmount = invoicePayment.getAmount();
    PaymentMode paymentMode = invoicePayment.getPaymentMode();
    Partner partner = invoice.getPartner();
    LocalDate paymentDate = invoicePayment.getPaymentDate();
    BankDetails companyBankDetails = invoicePayment.getCompanyBankDetails();
    boolean isDebitInvoice = moveToolService.isDebitCustomer(invoice, true);
    String origin = getOriginFromInvoicePayment(invoicePayment);
    boolean isFinancialDiscount = this.isFinancialDiscount(invoicePayment);
    int counter = 1;

    move.addMoveLineListItem(
        moveLineCreateService.createMoveLine(
            move,
            partner,
            paymentModeService.getPaymentModeAccount(paymentMode, company, companyBankDetails),
            paymentAmount,
            isDebitInvoice,
            paymentDate,
            null,
            counter++,
            origin,
            move.getDescription()));

    if (isFinancialDiscount) {
      move.addMoveLineListItem(
          moveLineCreateService.createMoveLine(
              move,
              partner,
              this.getFinancialDiscountAccount(invoice, company),
              invoicePayment.getFinancialDiscountAmount(),
              isDebitInvoice,
              paymentDate,
              null,
              counter++,
              origin,
              invoicePayment.getDescription()));

      paymentAmount =
          invoicePayment
              .getAmount()
              .add(
                  invoicePayment
                      .getFinancialDiscountAmount()
                      .add(invoicePayment.getFinancialDiscountTaxAmount()));
    }

    move.addMoveLineListItem(
        moveLineCreateService.createMoveLine(
            move,
            partner,
            customerAccount,
            paymentAmount,
            !isDebitInvoice,
            paymentDate,
            null,
            counter++,
            origin,
            move.getDescription()));

    if (isFinancialDiscount
        && invoicePayment.getFinancialDiscount().getDiscountBaseSelect()
            == FinancialDiscountRepository.DISCOUNT_BASE_VAT) {
      Account financialDiscountVATAccount = this.getFinancialDiscountVATAccount(invoice, company);

      if (financialDiscountVATAccount != null) {
        move.addMoveLineListItem(
            moveLineCreateService.createMoveLine(
                move,
                partner,
                financialDiscountVATAccount,
                invoicePayment.getFinancialDiscountTaxAmount(),
                isDebitInvoice,
                paymentDate,
                null,
                counter,
                origin,
                invoicePayment.getDescription()));
      }
    }

    return move;
  }

  protected boolean isFinancialDiscount(InvoicePayment invoicePayment) {
    return invoicePayment.getApplyFinancialDiscount()
        && invoicePayment.getFinancialDiscount() != null
        && appAccountService.getAppAccount().getManageFinancialDiscount();
  }

  protected Account getFinancialDiscountAccount(Invoice invoice, Company company)
      throws AxelorException {
    if (InvoiceToolService.isPurchase(invoice)) {
      return accountConfigService.getAccountConfig(company).getPurchFinancialDiscountAccount();
    } else {
      return accountConfigService.getAccountConfig(company).getSaleFinancialDiscountAccount();
    }
  }

  protected Account getFinancialDiscountVATAccount(Invoice invoice, Company company)
      throws AxelorException {
    AccountConfig accountConfig = accountConfigService.getAccountConfig(company);
    Tax tax =
        InvoiceToolService.isPurchase(invoice)
            ? accountConfig.getPurchFinancialDiscountTax()
            : accountConfig.getSaleFinancialDiscountTax();

    return tax.getAccountManagementList().stream()
        .filter(it -> it.getCompany().equals(company))
        .map(AccountManagement::getFinancialDiscountAccount)
        .findFirst()
        .orElse(null);
  }
}
