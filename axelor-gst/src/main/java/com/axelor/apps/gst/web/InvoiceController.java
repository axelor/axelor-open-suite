package com.axelor.apps.gst.web;

import java.util.ArrayList;
import java.util.List;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.TaxLine;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.gst.service.InvoiceLineServiceGST;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;

public class InvoiceController {

  @Inject private ProductRepository productRepository;
  @Inject private InvoiceLineServiceGST invoiceLineService;

  @SuppressWarnings("unchecked")
  public void setInvoiceLine(ActionRequest req, ActionResponse res) {
    List<Long> productIds = (List<Long>) req.getContext().get("productIds");
    Invoice invoice = req.getContext().asType(Invoice.class);

    List<TaxLine> taxLineList = new ArrayList<TaxLine>();
    if (productIds != null && !productIds.isEmpty()) {
      List<Product> productList =
          productRepository.all().filter("self.id in (?1)", productIds).fetch();
      List<InvoiceLine> invoiceLineList = invoiceLineService.getInvoiceLineFromProduct(productList);

      //      generate taxLine
      //      List<InvoiceLineTax> invoiceTaxLines =
      //          (new TaxInvoiceLine(invoice, invoiceLineList)).creates();

      res.setValue("invoiceLineTaxList", taxLineList);
      res.setValue("invoiceLineList", invoiceLineList);
    }
  }
}
