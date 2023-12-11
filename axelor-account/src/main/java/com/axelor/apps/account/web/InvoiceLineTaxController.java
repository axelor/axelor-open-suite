package com.axelor.apps.account.web;

import com.axelor.apps.account.db.InvoiceLineTax;
import com.axelor.apps.account.service.invoice.InvoiceLineTaxGroupService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.HashMap;
import java.util.Map;

public class InvoiceLineTaxController {

  public void setInvoiceLineTaxScale(ActionRequest request, ActionResponse response) {
    InvoiceLineTax invoiceLineTax = request.getContext().asType(InvoiceLineTax.class);
    try {
      if (invoiceLineTax == null || invoiceLineTax.getInvoice() == null) {
        return;
      }

      Map<String, Map<String, Object>> attrsMap = new HashMap<>();
      Beans.get(InvoiceLineTaxGroupService.class)
          .setInvoiceLineTaxScale(invoiceLineTax.getInvoice(), attrsMap, "");

      response.setAttrs(attrsMap);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
