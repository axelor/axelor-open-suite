package com.axelor.apps.bankpayment.service.invoiceterm;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLine;
import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.InvoiceTermRepository;
import com.axelor.apps.account.service.JournalService;
import com.axelor.apps.account.service.PfpService;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.invoice.InvoiceTermDateComputeService;
import com.axelor.apps.account.service.invoice.InvoiceTermFinancialDiscountService;
import com.axelor.apps.account.service.invoice.InvoiceTermPfpToolService;
import com.axelor.apps.account.service.invoice.InvoiceTermPfpUpdateService;
import com.axelor.apps.account.service.invoice.InvoiceTermServiceImpl;
import com.axelor.apps.account.service.invoice.InvoiceTermToolService;
import com.axelor.apps.account.service.payment.invoice.payment.InvoiceTermPaymentService;
import com.axelor.apps.bankpayment.service.bankdetails.BankDetailsBankPaymentService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.service.CurrencyScaleService;
import com.axelor.apps.base.service.CurrencyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.auth.db.User;
import com.axelor.auth.db.repo.UserRepository;
import com.axelor.dms.db.repo.DMSFileRepository;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;

public class InvoiceTermBankPaymentServiceImpl extends InvoiceTermServiceImpl {

    protected final BankDetailsBankPaymentService bankDetailsBankPaymentService;

    @Inject
    public InvoiceTermBankPaymentServiceImpl(InvoiceTermRepository invoiceTermRepo, InvoiceRepository invoiceRepo, AppAccountService appAccountService, JournalService journalService, InvoiceTermFinancialDiscountService invoiceTermFinancialDiscountService, UserRepository userRepo, PfpService pfpService, CurrencyScaleService currencyScaleService, DMSFileRepository DMSFileRepo, InvoiceTermPaymentService invoiceTermPaymentService, CurrencyService currencyService, AppBaseService appBaseService, InvoiceTermPfpUpdateService invoiceTermPfpUpdateService, InvoiceTermToolService invoiceTermToolService, InvoiceTermPfpToolService invoiceTermPfpToolService, InvoiceTermDateComputeService invoiceTermDateComputeService, BankDetailsBankPaymentService bankDetailsBankPaymentService) {
        super(invoiceTermRepo, invoiceRepo, appAccountService, journalService, invoiceTermFinancialDiscountService, userRepo, pfpService, currencyScaleService, DMSFileRepo, invoiceTermPaymentService, currencyService, appBaseService, invoiceTermPfpUpdateService, invoiceTermToolService, invoiceTermPfpToolService, invoiceTermDateComputeService);
        this.bankDetailsBankPaymentService = bankDetailsBankPaymentService;
    }

    @Override
    public InvoiceTerm createInvoiceTerm(
            Invoice invoice,
            Move move,
            MoveLine moveLine,
            BankDetails bankDetails,
            User pfpUser,
            PaymentMode paymentMode,
            LocalDate date,
            LocalDate estimatedPaymentDate,
            BigDecimal amount,
            BigDecimal percentage,
            int sequence,
            boolean isHoldBack)
            throws AxelorException {
        InvoiceTerm newInvoiceTerm = super.createInvoiceTerm(invoice,move,moveLine,bankDetails,pfpUser,paymentMode,date,estimatedPaymentDate,amount,percentage,sequence,isHoldBack);

        bankDetailsBankPaymentService.getBankDetailsLinkedToActiveUmr(paymentMode, newInvoiceTerm.getPartner(), newInvoiceTerm.getCompany())
                .stream()
                .findAny()
                .ifPresent(newInvoiceTerm::setBankDetails);

        return newInvoiceTerm;
    }
}
