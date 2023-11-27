package com.axelor.apps.account.web;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.service.invoice.InvoiceLineTaxGroupService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;

public class InvoiceLineTaxController {

  public void setInvoiceLineTaxScale(ActionRequest request, ActionResponse response) {
    Invoice invoice = request.getContext().asType(Invoice.class);
    try {
      if (invoice == null || CollectionUtils.isEmpty(invoice.getInvoiceTermList())) {
        return;
      }

      Map<String, Map<String, Object>> attrsMap = new HashMap<>();
      Beans.get(InvoiceLineTaxGroupService.class).setInvoiceLineTaxScale(invoice, attrsMap, null);

      response.setAttrs(attrsMap);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
