package com.axelor.apps.bankpayment.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.service.invoice.generator.InvoiceGenerator;
import com.axelor.apps.bankpayment.service.bankdetails.BankDetailsBankPaymentService;
import com.axelor.apps.base.AxelorException;
import com.google.inject.Inject;

public class InvoiceGeneratorBankPayment extends InvoiceGenerator {

    protected final BankDetailsBankPaymentService bankDetailsBankPaymentService;

    @Inject
    public InvoiceGeneratorBankPayment(BankDetailsBankPaymentService bankDetailsBankPaymentService) {
        this.bankDetailsBankPaymentService = bankDetailsBankPaymentService;
    }

    @Override
    protected Invoice createInvoiceHeader() throws AxelorException {
        Invoice invoice = super.createInvoiceHeader();

        bankDetailsBankPaymentService.getBankDetailsLinkedToActiveUmr(paymentMode, partner, company)
                .stream()
                .findAny()
                .ifPresent(invoice::setBankDetails);

        return invoice;
    }

    @Override
    public Invoice generate() throws AxelorException {
        return createInvoiceHeader();
    }
}
