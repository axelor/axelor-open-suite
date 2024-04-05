package com.axelor.apps.account.service.payment.invoice.payment;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.InvoiceTermPayment;
import com.axelor.apps.account.db.Journal;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.apps.account.db.repo.PaymentModeRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.accountingsituation.AccountingSituationService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.account.service.invoice.InvoiceToolService;
import com.axelor.apps.account.service.move.MoveCreateService;
import com.axelor.apps.account.service.move.MoveLineInvoiceTermService;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.account.service.moveline.MoveLineFinancialDiscountService;
import com.axelor.apps.account.service.payment.PaymentModeService;
import com.axelor.apps.account.service.reconcile.ReconcileService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.DateService;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import jakarta.xml.bind.JAXBException;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import javax.xml.datatype.DatatypeConfigurationException;
import org.apache.commons.collections.CollectionUtils;

public class InvoicePaymentMoveCreateServiceImpl implements InvoicePaymentMoveCreateService {

  protected DateService dateService;
  protected PaymentModeService paymentModeService;
  protected MoveToolService moveToolService;
  protected AccountConfigService accountConfigService;
  protected MoveCreateService moveCreateService;
  protected MoveValidateService moveValidateService;
  protected ReconcileService reconcileService;
  protected AppAccountService appAccountService;
  protected InvoiceTermService invoiceTermService;
  protected MoveLineInvoiceTermService moveLineInvoiceTermService;
  protected MoveLineFinancialDiscountService moveLineFinancialDiscountService;
  protected MoveLineCreateService moveLineCreateService;
  protected AccountingSituationService accountingSituationService;
  protected InvoicePaymentRepository invoicePaymentRepository;

  @Inject
  public InvoicePaymentMoveCreateServiceImpl(
      DateService dateService,
      PaymentModeService paymentModeService,
      MoveToolService moveToolService,
      AccountConfigService accountConfigService,
      MoveCreateService moveCreateService,
      MoveValidateService moveValidateService,
      ReconcileService reconcileService,
      AppAccountService appAccountService,
      InvoiceTermService invoiceTermService,
      MoveLineInvoiceTermService moveLineInvoiceTermService,
      MoveLineFinancialDiscountService moveLineFinancialDiscountService,
      MoveLineCreateService moveLineCreateService,
      AccountingSituationService accountingSituationService,
      InvoicePaymentRepository invoicePaymentRepository) {
    this.dateService = dateService;
    this.paymentModeService = paymentModeService;
    this.moveToolService = moveToolService;
    this.accountConfigService = accountConfigService;
    this.moveCreateService = moveCreateService;
    this.moveValidateService = moveValidateService;
    this.reconcileService = reconcileService;
    this.appAccountService = appAccountService;
    this.invoiceTermService = invoiceTermService;
    this.moveLineInvoiceTermService = moveLineInvoiceTermService;
    this.moveLineFinancialDiscountService = moveLineFinancialDiscountService;
    this.moveLineCreateService = moveLineCreateService;
    this.accountingSituationService = accountingSituationService;
    this.invoicePaymentRepository = invoicePaymentRepository;
  }

  /**
   * Method to create a payment move for an invoice Payment
   *
   * <p>Create a move and reconcile it with the invoice move
   *
   * @param invoicePayment An invoice payment
   * @throws AxelorException
   */
  @Override
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
              .map(BigDecimal::abs)
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

  protected String getOriginFromInvoicePayment(InvoicePayment invoicePayment) {
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

  protected Move fillMove(
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
            && invoicePayment.getFinancialDiscountTaxAmount().signum() > 0;

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
        companyPaymentAmount.subtract(
            invoicePayment.getFinancialDiscountAmount().multiply(currencyRate));

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
                : BigDecimal.ONE,
            company);

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

    if (isFinancialDiscount) {
      counter =
          moveLineFinancialDiscountService.createFinancialDiscountMoveLine(
              invoice, move, invoicePayment, origin, counter, isDebitInvoice, financialDiscountVat);

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
            counter,
            origin,
            move.getDescription()));

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

  @Override
  public void createInvoicePaymentMove(InvoicePayment invoicePayment)
      throws AxelorException, JAXBException, IOException, DatatypeConfigurationException {
    Invoice invoice = invoicePayment.getInvoice();
    if (accountConfigService
        .getAccountConfig(invoice.getCompany())
        .getGenerateMoveForInvoicePayment()) {
      this.createMoveForInvoicePayment(invoicePayment);
    } else {
      accountingSituationService.updateCustomerCredit(invoice.getPartner());
      invoicePaymentRepository.save(invoicePayment);
    }
  }
}
