package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.repo.InvoiceTermRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.move.MovePfpValidateService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;

public class InvoiceTermPfpUpdateServiceImpl implements InvoiceTermPfpUpdateService {

  protected InvoiceTermPfpToolService invoiceTermPfpToolService;
  protected InvoiceTermPfpValidateService invoiceTermPfpValidateService;
  protected InvoiceTermPfpService invoiceTermPfpService;
  protected MovePfpValidateService movePfpValidateService;
  protected InvoicePfpValidateService invoicePfpValidateService;

  @Inject
  public InvoiceTermPfpUpdateServiceImpl(
      InvoiceTermPfpToolService invoiceTermPfpToolService,
      InvoiceTermPfpValidateService invoiceTermPfpValidateService,
      InvoiceTermPfpService invoiceTermPfpService,
      MovePfpValidateService movePfpValidateService,
      InvoicePfpValidateService invoicePfpValidateService) {
    this.invoiceTermPfpToolService = invoiceTermPfpToolService;
    this.invoiceTermPfpValidateService = invoiceTermPfpValidateService;
    this.invoiceTermPfpService = invoiceTermPfpService;
    this.movePfpValidateService = movePfpValidateService;
    this.invoicePfpValidateService = invoicePfpValidateService;
  }

  @Override
  public void updatePfp(
      InvoiceTerm invoiceTerm, Map<InvoiceTerm, Integer> invoiceTermPfpValidateStatusSelectMap)
      throws AxelorException {
    int pfpValidateStatusSelect;
    if (MapUtils.isEmpty(invoiceTermPfpValidateStatusSelectMap)) {
      pfpValidateStatusSelect = invoiceTerm.getPfpValidateStatusSelect();
    } else {
      pfpValidateStatusSelect = invoiceTermPfpValidateStatusSelectMap.get(invoiceTerm);
    }

    if (invoiceTermPfpToolService
        .getAlreadyValidatedStatusList()
        .contains(pfpValidateStatusSelect)) {
      return;
    } else if (pfpValidateStatusSelect == InvoiceTermRepository.PFP_STATUS_LITIGATION) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(AccountExceptionMessage.INVOICE_TERM_PFP_REFUSED));
    }

    invoiceTermPfpValidateStatusSelectMap.remove(invoiceTerm);

    if (invoiceTerm.getAmountRemaining().signum() > 0) {
      BigDecimal grantedAmount = invoiceTerm.getAmount().subtract(invoiceTerm.getAmountRemaining());

      invoiceTermPfpValidateService.initPftPartialValidation(invoiceTerm, grantedAmount, null);
      invoiceTermPfpService.createPfpInvoiceTerm(
          invoiceTerm, invoiceTerm.getInvoice(), grantedAmount);
    } else {
      invoiceTermPfpValidateService.validatePfp(invoiceTerm, AuthUtils.getUser());
    }

    Invoice invoice = invoiceTerm.getInvoice();
    if (invoice != null
        && invoice.getInvoiceTermList().stream()
            .allMatch(
                it ->
                    invoiceTermPfpToolService.getPfpValidateStatusSelect(it)
                        == InvoiceTermRepository.PFP_STATUS_VALIDATED)) {
      // Beans.get to prevent circular injection
      invoicePfpValidateService.validatePfp(invoice.getId());
    }

    Move move = invoiceTerm.getMoveLine().getMove();
    if (move.getMoveLineList().stream()
        .allMatch(
            ml ->
                CollectionUtils.isEmpty(ml.getInvoiceTermList())
                    || ml.getInvoiceTermList().stream()
                        .allMatch(
                            it ->
                                invoiceTermPfpToolService.getPfpValidateStatusSelect(it)
                                    == InvoiceTermRepository.PFP_STATUS_VALIDATED))) {
      movePfpValidateService.validatePfp(move.getId());
    }
  }
}
