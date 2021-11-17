package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class InvoiceControlServiceImpl implements InvoiceControlService {

  protected InvoiceRepository invoiceRepository;

  @Inject
  public InvoiceControlServiceImpl(InvoiceRepository invoiceRepository) {
    this.invoiceRepository = invoiceRepository;
  }

  @Override
  public Boolean isDuplicate(Invoice invoice) {
    Objects.requireNonNull(invoice);
    StringBuilder query =
        new StringBuilder(
            "self.supplierInvoiceNb = :supplierInvoiceNb AND self.partner = :partnerId AND YEAR(self.originDate) = :yearOriginDate AND self.statusSelect != :statusSelect");
    Map<String, Object> params = new HashMap<String, Object>();

    if (invoice.getOriginDate() != null
        && invoice.getSupplierInvoiceNb() != null
        && invoice.getPartner() != null) {

      params.put("supplierInvoiceNb", invoice.getSupplierInvoiceNb());
      params.put("partnerId", invoice.getPartner().getId());
      params.put("yearOriginDate", invoice.getOriginDate().getYear());
      params.put("statusSelect", InvoiceRepository.STATUS_CANCELED);

      if (invoice.getId() != null) {
        query.append(" AND self.id != :invoiceId");
        params.put("invoiceId", invoice.getId());
      }

      return invoiceRepository.all().filter(query.toString()).bind(params).count() > 0;
    }

    return false;
  }
}
