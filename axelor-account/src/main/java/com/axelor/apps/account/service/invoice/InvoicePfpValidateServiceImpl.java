package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.User;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class InvoicePfpValidateServiceImpl implements InvoicePfpValidateService {

  protected InvoiceTermPfpValidateService invoiceTermPfpValidateService;
  protected AppBaseService appBaseService;
  protected InvoiceRepository invoiceRepository;

  @Inject
  public InvoicePfpValidateServiceImpl(
      InvoiceTermPfpValidateService invoiceTermPfpValidateService,
      AppBaseService appBaseService,
      InvoiceRepository invoiceRepository) {
    this.invoiceTermPfpValidateService = invoiceTermPfpValidateService;
    this.appBaseService = appBaseService;
    this.invoiceRepository = invoiceRepository;
  }

  @Override
  @Transactional
  public void validatePfp(Long invoiceId) {
    Invoice invoice = invoiceRepository.find(invoiceId);
    User pfpValidatorUser =
        invoice.getPfpValidatorUser() != null ? invoice.getPfpValidatorUser() : AuthUtils.getUser();

    for (InvoiceTerm invoiceTerm : invoice.getInvoiceTermList()) {
      invoiceTermPfpValidateService.validatePfp(invoiceTerm, pfpValidatorUser);
    }

    invoice.setPfpValidatorUser(pfpValidatorUser);
    invoice.setPfpValidateStatusSelect(InvoiceRepository.PFP_STATUS_VALIDATED);
    invoice.setDecisionPfpTakenDateTime(
        appBaseService.getTodayDateTime(invoice.getCompany()).toLocalDateTime());
  }
}
