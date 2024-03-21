package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoicePayment;
import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.InvoiceTermPayment;
import com.axelor.apps.account.db.repo.InvoicePaymentRepository;
import com.axelor.apps.account.db.repo.InvoiceTermRepository;
import com.axelor.apps.account.service.PfpService;
import com.axelor.apps.base.AxelorException;
import com.axelor.db.Query;
import com.google.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class InvoiceTermFilterServiceImpl implements InvoiceTermFilterService {

  protected InvoiceTermRepository invoiceTermRepository;
  protected PfpService pfpService;

  @Inject
  public InvoiceTermFilterServiceImpl(
      InvoiceTermRepository invoiceTermRepository, PfpService pfpService) {
    this.invoiceTermRepository = invoiceTermRepository;
    this.pfpService = pfpService;
  }

  @Override
  public List<InvoiceTerm> getUnpaidInvoiceTermsFiltered(Invoice invoice) throws AxelorException {

    return filterInvoiceTermsByHoldBack(getUnpaidInvoiceTerms(invoice));
  }

  @Override
  public List<InvoiceTerm> getUnpaidInvoiceTermsFilteredWithoutPfpCheck(Invoice invoice)
      throws AxelorException {

    return filterInvoiceTermsByHoldBack(getUnpaidInvoiceTermsWithoutPfpCheck(invoice));
  }

  @Override
  public List<InvoiceTerm> filterInvoiceTermsByHoldBack(List<InvoiceTerm> invoiceTerms) {

    if (CollectionUtils.isEmpty(invoiceTerms)) {
      return invoiceTerms;
    }

    boolean isFirstHoldBack = invoiceTerms.get(0).getIsHoldBack();
    invoiceTerms.removeIf(it -> it.getIsHoldBack() != isFirstHoldBack);

    return invoiceTerms;
  }

  @Override
  public List<InvoiceTerm> getUnpaidInvoiceTermsWithoutPfpCheck(Invoice invoice)
      throws AxelorException {

    return buildUnpaidInvoiceTermsQuery(invoice, false);
  }

  @Override
  public List<InvoiceTerm> getUnpaidInvoiceTerms(Invoice invoice) throws AxelorException {
    boolean pfpCondition = pfpService.getPfpCondition(invoice);

    return buildUnpaidInvoiceTermsQuery(invoice, pfpCondition);
  }

  protected List<InvoiceTerm> buildUnpaidInvoiceTermsQuery(Invoice invoice, boolean pfpCondition) {
    String queryStr =
        "self.invoice = :invoice AND (self.isPaid IS NOT TRUE OR self.amountRemaining > 0)";

    if (pfpCondition) {
      queryStr =
          queryStr
              + " AND self.pfpValidateStatusSelect IN (:noPfp, :validated, :partiallyValidated)";
    }

    Query<InvoiceTerm> invoiceTermQuery =
        invoiceTermRepository.all().filter(queryStr).bind("invoice", invoice);

    if (pfpCondition) {
      invoiceTermQuery
          .bind("noPfp", InvoiceTermRepository.PFP_STATUS_NO_PFP)
          .bind("validated", InvoiceTermRepository.PFP_STATUS_VALIDATED)
          .bind("partiallyValidated", InvoiceTermRepository.PFP_STATUS_PARTIALLY_VALIDATED);
    }

    return this.filterNotAwaitingPayment(invoiceTermQuery.order("dueDate").fetch());
  }

  public List<InvoiceTerm> filterNotAwaitingPayment(List<InvoiceTerm> invoiceTermList) {
    return invoiceTermList.stream().filter(this::isNotAwaitingPayment).collect(Collectors.toList());
  }

  public boolean isNotAwaitingPayment(InvoiceTerm invoiceTerm) {
    if (invoiceTerm == null) {
      return false;
    } else if (invoiceTerm.getInvoice() != null) {
      Invoice invoice = invoiceTerm.getInvoice();

      if (CollectionUtils.isNotEmpty(invoice.getInvoicePaymentList())) {
        return invoice.getInvoicePaymentList().stream()
            .filter(it -> it.getStatusSelect() == InvoicePaymentRepository.STATUS_PENDING)
            .map(InvoicePayment::getInvoiceTermPaymentList)
            .flatMap(Collection::stream)
            .map(InvoiceTermPayment::getInvoiceTerm)
            .noneMatch(it -> it.getId().equals(invoiceTerm.getId()));
      }
    }

    return true;
  }
}
