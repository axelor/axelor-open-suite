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
package com.axelor.apps.account.service.payment.invoice.payment;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.AccountManagement;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.InvoiceTermPayment;
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
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.AccountManagementAccountService;
import com.axelor.apps.account.service.AccountingSituationService;
import com.axelor.apps.account.service.ReconcileService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.account.service.invoice.InvoiceToolService;
import com.axelor.apps.account.service.move.MoveCreateService;
import com.axelor.apps.account.service.move.MoveLineInvoiceTermService;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.account.service.payment.PaymentModeService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.DateService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import com.google.inject.servlet.RequestScoped;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import org.apache.commons.collections.CollectionUtils;

@RequestScoped
public class InvoicePaymentValidateServiceImpl implements InvoicePaymentValidateService {

  protected PaymentModeService paymentModeService;
  protected MoveCreateService moveCreateService;
  protected MoveValidateService moveValidateService;
  protected MoveToolService moveToolService;
  protected MoveLineCreateService moveLineCreateService;
  protected AccountConfigService accountConfigService;
  protected InvoicePaymentRepository invoicePaymentRepository;
  protected ReconcileService reconcileService;
  protected AppAccountService appAccountService;
  protected AccountManagementAccountService accountManagementAccountService;
  protected InvoicePaymentToolService invoicePaymentToolService;
  protected DateService dateService;
  protected MoveLineInvoiceTermService moveLineInvoiceTermService;
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
      AppAccountService appAccountService,
      AccountManagementAccountService accountManagementAccountService,
      InvoicePaymentToolService invoicePaymentToolService,
      DateService dateService,
      MoveLineInvoiceTermService moveLineInvoiceTermService,
      InvoiceTermService invoiceTermService) {

    this.paymentModeService = paymentModeService;
    this.moveLineCreateService = moveLineCreateService;
    this.moveCreateService = moveCreateService;
    this.moveValidateService = moveValidateService;
    this.moveToolService = moveToolService;
    this.accountConfigService = accountConfigService;
    this.invoicePaymentRepository = invoicePaymentRepository;
    this.reconcileService = reconcileService;
    this.appAccountService = appAccountService;
    this.accountManagementAccountService = accountManagementAccountService;
    this.invoicePaymentToolService = invoicePaymentToolService;
    this.dateService = dateService;
    this.moveLineInvoiceTermService = moveLineInvoiceTermService;
    this.invoiceTermService = invoiceTermService;
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
    invoice.setNextDueDate(Beans.get(InvoiceToolService.class).getNextDueDate(invoice));

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
              invoice.getDueDate().format(dateService.getDateFormat()));
    }

    Account customerAccount;

    Journal journal =
        paymentModeService.getPaymentModeJournal(paymentMode, company, companyBankDetails);

    List<MoveLine> invoiceMoveLines = moveToolService.getInvoiceCustomerMoveLines(invoicePayment);

    if (invoice.getOperationSubTypeSelect() == InvoiceRepository.OPERATION_SUB_TYPE_ADVANCE) {

      AccountConfig accountConfig = accountConfigService.getAccountConfig(company);

      if (InvoiceToolService.isPurchase(invoice)) {
        customerAccount = accountConfigService.getSupplierAdvancePaymentAccount(accountConfig);
      } else {
        customerAccount = accountConfigService.getAdvancePaymentAccount(accountConfig);
      }

    } else {
      if (CollectionUtils.isEmpty(invoiceMoveLines)) {
        return null;
      }
      customerAccount = invoiceMoveLines.get(0).getAccount();
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
            invoice.getBankDetails(),
            MoveRepository.TECHNICAL_ORIGIN_AUTOMATIC,
            MoveRepository.FUNCTIONAL_ORIGIN_PAYMENT,
            getOriginFromInvoicePayment(invoicePayment),
            description,
            invoice.getCompanyBankDetails());
    move.setPaymentCondition(null);

    MoveLine customerMoveLine = null;
    move.setTradingName(invoice.getTradingName());

    BigDecimal maxAmount;
    if (CollectionUtils.isEmpty(invoiceMoveLines)) {
      // using null value to indicate we do not use a maxAmount
      maxAmount = null;
    } else {
      maxAmount =
          invoiceMoveLines.stream()
              .map(MoveLine::getAmountRemaining)
              .reduce(BigDecimal::add)
              .orElse(BigDecimal.ZERO);
    }

    move = this.fillMove(invoicePayment, move, customerAccount, maxAmount);
    moveValidateService.accounting(move);

    for (MoveLine moveline : move.getMoveLineList()) {
      if (customerAccount.equals(moveline.getAccount())) {
        customerMoveLine = moveline;
      }
    }

    invoicePayment.setMove(move);
    if (customerMoveLine != null
        && invoice.getOperationSubTypeSelect() != InvoiceRepository.OPERATION_SUB_TYPE_ADVANCE) {
      for (MoveLine invoiceMoveLine : invoiceMoveLines) {
        Reconcile reconcile =
            reconcileService.reconcile(
                invoiceMoveLine, customerMoveLine, true, true, invoicePayment);

        if (reconcile == null) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_INCONSISTENCY,
              I18n.get(AccountExceptionMessage.INVOICE_PAYMENT_CANNOT_RECONCILE),
              invoiceMoveLine.getName(),
              invoiceMoveLine.getAccount().getCode(),
              customerMoveLine.getName(),
              customerMoveLine.getAccount().getCode());
        }

        invoicePayment.setReconcile(reconcile);
      }
    }

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
  public Move fillMove(
      InvoicePayment invoicePayment, Move move, Account customerAccount, BigDecimal maxAmount)
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
    boolean financialDiscountVat =
        invoicePayment.getFinancialDiscount() != null
            && invoicePayment.getFinancialDiscount().getDiscountBaseSelect()
                == FinancialDiscountRepository.DISCOUNT_BASE_VAT;
    boolean isPurchase = InvoiceToolService.isPurchase(invoice);

    AccountConfig accountConfig = accountConfigService.getAccountConfig(company);

    BigDecimal companyPaymentAmount =
        invoicePayment.getInvoiceTermPaymentList().stream()
            .map(InvoiceTermPayment::getCompanyPaidAmount)
            .reduce(BigDecimal::add)
            .orElse(BigDecimal.ZERO);
    if (maxAmount != null) {
      companyPaymentAmount = companyPaymentAmount.min(maxAmount);
    }
    BigDecimal currencyRate = companyPaymentAmount.divide(paymentAmount, 5, RoundingMode.HALF_UP);

    companyPaymentAmount =
        invoiceTermService.adjustAmountInCompanyCurrency(
            invoice.getInvoiceTermList(),
            invoice.getCompanyInTaxTotalRemaining(),
            companyPaymentAmount,
            paymentAmount,
            invoice.getMove() != null
                ? invoice.getMove().getMoveLineList().stream()
                    .map(MoveLine::getCurrencyRate)
                    .findAny()
                    .orElse(BigDecimal.ONE)
                : BigDecimal.ONE);

    move.addMoveLineListItem(
        moveLineCreateService.createMoveLine(
            move,
            partner,
            paymentModeService.getPaymentModeAccount(paymentMode, company, companyBankDetails),
            paymentAmount,
            companyPaymentAmount,
            currencyRate,
            isDebitInvoice,
            paymentDate,
            null,
            paymentDate,
            counter++,
            origin,
            move.getDescription()));
    MoveLine financialDiscountMoveLine = null;
    if (isFinancialDiscount) {
      financialDiscountMoveLine =
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
              invoicePayment.getDescription());

      Tax financialDiscountTax = null;
      if (financialDiscountVat) {
        financialDiscountTax =
            isPurchase
                ? accountConfigService.getPurchFinancialDiscountTax(accountConfig)
                : accountConfigService.getSaleFinancialDiscountTax(accountConfig);

        if (financialDiscountTax.getActiveTaxLine() != null) {
          financialDiscountMoveLine.setTaxLine(financialDiscountTax.getActiveTaxLine());
          financialDiscountMoveLine.setTaxRate(financialDiscountTax.getActiveTaxLine().getValue());
          financialDiscountMoveLine.setTaxCode(financialDiscountTax.getCode());
        }
      }

      move.addMoveLineListItem(financialDiscountMoveLine);

      paymentAmount =
          invoicePayment
              .getAmount()
              .add(
                  invoicePayment
                      .getFinancialDiscountAmount()
                      .add(invoicePayment.getFinancialDiscountTaxAmount()));
      companyPaymentAmount = paymentAmount;
    }

    move.addMoveLineListItem(
        moveLineCreateService.createMoveLine(
            move,
            partner,
            customerAccount,
            paymentAmount,
            companyPaymentAmount,
            currencyRate,
            !isDebitInvoice,
            paymentDate,
            null,
            paymentDate,
            counter++,
            origin,
            move.getDescription()));

    if (isFinancialDiscount
        && invoicePayment != null
        && invoicePayment.getFinancialDiscount() != null
        && financialDiscountVat
        && financialDiscountMoveLine != null
        && BigDecimal.ZERO.compareTo(invoicePayment.getFinancialDiscountTaxAmount()) != 0) {

      if (financialDiscountMoveLine.getAccount().getVatSystemSelect() == null
          || financialDiscountMoveLine.getAccount().getVatSystemSelect() == 0) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(AccountExceptionMessage.MISSING_VAT_SYSTEM_ON_ACCOUNT),
            financialDiscountMoveLine.getAccount().getCode());
      }

      int vatSystem = financialDiscountMoveLine.getAccount().getVatSystemSelect();

      Account financialDiscountVATAccount =
          this.getFinancialDiscountVATAccount(
              invoice, company, move.getJournal(), vatSystem, move.getFunctionalOriginSelect());

      if (financialDiscountVATAccount != null) {
        MoveLine financialDiscountVatMoveLine =
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
                invoicePayment.getDescription());
        financialDiscountVatMoveLine.setTaxLine(financialDiscountMoveLine.getTaxLine());
        financialDiscountVatMoveLine.setTaxRate(financialDiscountMoveLine.getTaxRate());
        financialDiscountVatMoveLine.setTaxCode(financialDiscountMoveLine.getTaxCode());
        financialDiscountVatMoveLine.setVatSystemSelect(vatSystem);
        move.addMoveLineListItem(financialDiscountVatMoveLine);
      }
    }

    for (MoveLine moveLine : move.getMoveLineList()) {
      moveLineInvoiceTermService.generateDefaultInvoiceTerm(move, moveLine, paymentDate, false);
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
    AccountConfig accountConfig = accountConfigService.getAccountConfig(company);
    if (InvoiceToolService.isPurchase(invoice)) {
      return accountConfigService.getPurchFinancialDiscountAccount(accountConfig);
    } else {
      return accountConfigService.getSaleFinancialDiscountAccount(accountConfig);
    }
  }

  protected Account getFinancialDiscountVATAccount(
      Invoice invoice, Company company, Journal journal, int vatSystemSelect, int functionalOrigin)
      throws AxelorException {
    AccountConfig accountConfig = accountConfigService.getAccountConfig(company);
    Tax tax =
        InvoiceToolService.isPurchase(invoice)
            ? accountConfigService.getPurchFinancialDiscountTax(accountConfig)
            : accountConfigService.getSaleFinancialDiscountTax(accountConfig);
    AccountManagement accountManagement =
        tax.getAccountManagementList().stream()
            .filter(it -> it.getCompany().equals(company))
            .findFirst()
            .orElse(null);
    return accountManagementAccountService.getTaxAccount(
        accountManagement, tax, company, journal, vatSystemSelect, functionalOrigin, false, true);
  }
}
