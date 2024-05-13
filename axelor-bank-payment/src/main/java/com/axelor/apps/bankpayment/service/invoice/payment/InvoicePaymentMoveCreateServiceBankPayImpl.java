package com.axelor.apps.bankpayment.service.invoice.payment;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.PaymentSession;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.PaymentModeRepository;
import com.axelor.apps.account.service.accountingsituation.AccountingSituationService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceLineTaxToolService;
import com.axelor.apps.account.service.invoice.InvoiceTermService;
import com.axelor.apps.account.service.move.MoveCreateService;
import com.axelor.apps.account.service.move.MoveLineInvoiceTermService;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.account.service.move.MoveValidateService;
import com.axelor.apps.account.service.moveline.MoveLineCreateService;
import com.axelor.apps.account.service.moveline.MoveLineFinancialDiscountService;
import com.axelor.apps.account.service.payment.PaymentModeService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoicePaymentMoveCreateServiceImpl;
import com.axelor.apps.account.service.reconcile.ReconcileService;
import com.axelor.apps.bankpayment.db.BankOrder;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderCreateService;
import com.axelor.apps.bankpayment.service.bankorder.BankOrderValidationService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.DateService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import jakarta.xml.bind.JAXBException;
import java.io.IOException;
import javax.xml.datatype.DatatypeConfigurationException;

public class InvoicePaymentMoveCreateServiceBankPayImpl
    extends InvoicePaymentMoveCreateServiceImpl {

  protected BankOrderCreateService bankOrderCreateService;
  protected BankOrderValidationService bankOrderValidationService;

  @Inject
  public InvoicePaymentMoveCreateServiceBankPayImpl(
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
      CurrencyService currencyService,
      InvoiceLineTaxToolService invoiceLineTaxToolService,
      BankOrderCreateService bankOrderCreateService,
      BankOrderValidationService bankOrderValidationService,
      InvoicePaymentRepository invoicePaymentRepository) {
    super(
        dateService,
        paymentModeService,
        moveToolService,
        accountConfigService,
        moveCreateService,
        moveValidateService,
        reconcileService,
        appAccountService,
        invoiceTermService,
        moveLineInvoiceTermService,
        moveLineFinancialDiscountService,
        moveLineCreateService,
        accountingSituationService,
        currencyService,
        invoicePaymentRepository,
        invoiceLineTaxToolService);
    this.bankOrderCreateService = bankOrderCreateService;
    this.bankOrderValidationService = bankOrderValidationService;
  }

  @Override
  public void createInvoicePaymentMove(InvoicePayment invoicePayment)
      throws AxelorException, DatatypeConfigurationException, JAXBException, IOException {
    Invoice invoice = invoicePayment.getInvoice();
    Company company = invoice.getCompany();
    PaymentSession paymentSession = invoicePayment.getPaymentSession();

    PaymentMode paymentMode = invoicePayment.getPaymentMode();
    if (accountConfigService.getAccountConfig(company).getGenerateMoveForInvoicePayment()
        && (!paymentMode.getGenerateBankOrder()
            || (paymentSession != null
                && paymentSession.getAccountingTriggerSelect()
                    == PaymentModeRepository.ACCOUNTING_TRIGGER_IMMEDIATE)
            || (paymentSession == null
                && paymentMode.getAccountingTriggerSelect()
                    == PaymentModeRepository.ACCOUNTING_TRIGGER_IMMEDIATE))) {
      invoicePayment = this.createMoveForInvoicePayment(invoicePayment);
    } else {
      accountingSituationService.updateCustomerCredit(invoicePayment.getInvoice().getPartner());
      invoicePayment = invoicePaymentRepository.save(invoicePayment);
    }
    if (paymentMode.getGenerateBankOrder()
        && invoicePayment.getBankOrder() == null
        && paymentSession == null) {
      this.createBankOrder(invoicePayment);
    }
  }

  /**
   * Method to create a bank order for an invoice Payment
   *
   * @param invoicePayment An invoice payment
   * @throws AxelorException
   * @throws DatatypeConfigurationException
   * @throws IOException
   * @throws JAXBException
   */
  @Transactional(rollbackOn = {Exception.class})
  public void createBankOrder(InvoicePayment invoicePayment)
      throws AxelorException, JAXBException, IOException, DatatypeConfigurationException {

    BankOrder bankOrder = bankOrderCreateService.createBankOrder(invoicePayment);

    if (invoicePayment.getPaymentMode().getAutoConfirmBankOrder()) {
      bankOrderValidationService.confirm(bankOrder);
    }
  }
}
