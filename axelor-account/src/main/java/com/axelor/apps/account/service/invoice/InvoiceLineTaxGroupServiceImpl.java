package com.axelor.apps.account.service.invoice;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.service.invoice.attributes.InvoiceLineTaxAttrsService;
import com.axelor.common.ObjectUtils;
import com.google.inject.Inject;
import java.util.Map;

public class InvoiceLineTaxGroupServiceImpl implements InvoiceLineTaxGroupService {

  protected InvoiceLineTaxAttrsService invoiceLineTaxAttrsService;

  @Inject
  public InvoiceLineTaxGroupServiceImpl(InvoiceLineTaxAttrsService invoiceLineTaxAttrsService) {
    this.invoiceLineTaxAttrsService = invoiceLineTaxAttrsService;
  }

  @Override
  public void setInvoiceLineTaxScale(
      Invoice invoice, Map<String, Map<String, Object>> attrsMap, String prefix) {
    if (invoice != null && ObjectUtils.notEmpty(invoice.getInvoiceLineTaxList())) {
      invoiceLineTaxAttrsService.addExTaxBaseScale(invoice, attrsMap, prefix);
      invoiceLineTaxAttrsService.addTaxTotalScale(invoice, attrsMap, prefix);
      invoiceLineTaxAttrsService.addInTaxTotalScale(invoice, attrsMap, prefix);
    }
  }
}
