package com.axelor.apps.account.einvoice;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.base.db.Partner;
import com.axelor.i18n.I18n;
import com.axelor.meta.CallMethod;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import jakarta.xml.ws.soap.SOAPFaultException;


import java.io.IOException;
import java.time.LocalDate;

public class Controller {

    @Inject
    private InvoiceRepository invoiceRepository;
    private final EInvoiceService eIvoiceService = EInvoiceService.getInstance();

    private final static String COMPANY_STATUS_YES = "einvoice.company.status.accept";
    private final static String COMPANY_STATUS_NO = "einvoice.company.status.not.accept";
    private final static String INVOICE_SENT_SUCCESSFULLY = "einvoice.sent.successfully";

    @CallMethod
    public void check(ActionRequest request, ActionResponse response) throws IOException {
        Partner partner = (Partner) request.getContext().get("partner");
        String registrationCode = partner.getRegistrationCode();

        boolean result = eIvoiceService.isPartnerAcceptEinvoice(registrationCode);
        response.setNotify(I18n.get(result ? COMPANY_STATUS_YES : COMPANY_STATUS_NO));
        response.setReload(false);
    }

    @CallMethod
    @Transactional
    public void send(ActionRequest request, ActionResponse response) {
        Invoice invoice = request.getContext().asType(Invoice.class);
        try {
            eIvoiceService.sendInvoice(invoice);
            response.setNotify(I18n.get(INVOICE_SENT_SUCCESSFULLY));
            response.setReload(true);

            Invoice invoiceInDb = invoiceRepository.find(invoice.getId());
            invoiceInDb.setSentEinvoiceDate(LocalDate.now());
            invoiceRepository.save(invoiceInDb);

        } catch (SOAPFaultException e) {
            String faultMessage = e.getFault().getFaultString();
            response.setError(faultMessage);
        }
    }

    @CallMethod
    @Transactional
    public void markAsSent(ActionRequest request, ActionResponse response) {
        Long invoiceId;
        String contextClass = request.getContext().getContextClass().getSimpleName();

        if (contextClass.equals("Message")) { // Invoice sent via email
            invoiceId = (Long) request.getContext().get("relatedTo1SelectId");

        } else if (contextClass.equals("Invoice")) { // Manually marking invoice as sent
            invoiceId = (Long) request.getContext().get("id");

        } else {
            throw new RuntimeException("I don't know what to do with this request. Sorry");
        }

        response.setReload(true);
        Invoice invoiceInDb = invoiceRepository.find(invoiceId);
        invoiceInDb.setSentEmailDate(LocalDate.now());
        invoiceRepository.save(invoiceInDb);
    }

}
