package com.axelor.apps.gst.web;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.gst.service.InvoiceLineServiceGST;
import com.axelor.apps.gst.service.InvoiceServiceGST;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class InvoiceController {

  @Inject private ProductRepository productRepository;
  @Inject private InvoiceLineServiceGST invoiceLineService;
  @Inject private InvoiceServiceGST invoiceService;

  @SuppressWarnings("unchecked")
  public void setInvoiceLine(ActionRequest req, ActionResponse res) {
    List<Long> productIds = (List<Long>) req.getContext().get("productIds");

    List<TaxLine> taxLineList = new ArrayList<TaxLine>();
    if (productIds != null && !productIds.isEmpty()) {
      List<Product> productList =
          productRepository.all().filter("self.id in (?1)", productIds).fetch();
      List<InvoiceLine> invoiceLineList = invoiceLineService.getInvoiceLineFromProduct(productList);
      res.setValue("invoiceLineList", invoiceLineList);
    }
  }

  public void compute(ActionRequest request, ActionResponse response) {

    Invoice invoice = request.getContext().asType(Invoice.class);

    try {
      invoice = invoiceService.calculate(invoice);
      // response.setValue("invoiceLineTaxList", value);
      //      response.setValue("invoiceLineList", invoice.getInvoiceLineList());
      response.setValues(invoice);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
