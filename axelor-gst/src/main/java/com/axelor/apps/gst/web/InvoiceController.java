package com.axelor.apps.gst.web;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.CompanyRepository;
import com.axelor.apps.base.db.repo.PartnerRepository;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.gst.service.InvoiceLineServiceGST;
import com.axelor.apps.gst.service.InvoiceServiceGST;
import com.axelor.db.mapper.Mapper;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InvoiceController {

  @Inject private InvoiceLineServiceGST invoiceLineService;
  @Inject private InvoiceServiceGST invoiceService;
  @Inject private ProductRepository productRepository;
  @Inject private InvoiceRepository invoiceRepository;
  @Inject private PartnerService partnerService;

  // use when create invoice with wizard

  @SuppressWarnings("unchecked")
  public void setInvoiceLineForWizard(ActionRequest req, ActionResponse res) {

    List<Long> productIds = (List<Long>) req.getContext().get("productIds");
    if (productIds != null && !productIds.isEmpty()) {
      List<Product> productList =
          productRepository.all().filter("self.id in (?1)", productIds).fetch();
      List<InvoiceLine> invoiceLineList = invoiceLineService.getInvoiceLineFromProduct(productList);
      res.setValue("$invoiceLines", invoiceLineList);
    }
  }

  // use when create invoice with popup
  public void setInvoiceLine(ActionRequest request, ActionResponse response) {
    try {
      Invoice invoice = request.getContext().asType(Invoice.class);
      List<Long> productIds = (List<Long>) request.getContext().get("productIds");
      Integer partnerId = (Integer) request.getContext().get("partnerId");

      if (productIds != null && !productIds.isEmpty() && partnerId != null) {
        invoice = invoiceService.setInvoiceDetails(invoice, productIds, partnerId);
        response.setValues(invoice);
      }
    } catch (Exception e) {
      e.printStackTrace();
      TraceBackService.trace(response, e);
    }
  }

  public void calculate(ActionRequest request, ActionResponse response) {

    Invoice invoice = request.getContext().asType(Invoice.class);

    try {
      invoice = invoiceService.calculate(invoice);
      response.setValues(invoice);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public void calculateFoWizard(ActionRequest request, ActionResponse response) {
    System.out.println(" Change partner call invoice line calculation.");
  }

  public void saveInvoice(ActionRequest request, ActionResponse response) {
    try {
      Invoice invoice = new Invoice();
      invoice.setOperationTypeSelect(invoiceRepository.OPERATION_TYPE_CLIENT_SALE);
      List<InvoiceLine> invoiceLineList = new ArrayList<InvoiceLine>();
      if (request.getContext().get("invoicePartner") == null) {
        response.setError("Please select the partner.");
      } else if (request.getContext().get("invoiceLines") == null) {
        response.setError("Please select at least one product.");
      }

      Map<String, Object> mapPartner =
          (Map<String, Object>) request.getContext().get("invoicePartner");
      Partner partner =
          Beans.get(PartnerRepository.class)
              .all()
              .filter("self.id = :id")
              .bind("id", mapPartner.get("id"))
              .fetchOne();

      List<Map> invoiceLinesMap = (List<Map>) request.getContext().get("invoiceLines");
      for (Map map : invoiceLinesMap) {

        InvoiceLine invoiceLine = Mapper.toBean(InvoiceLine.class, map);
        Product product = Mapper.toBean(Product.class, map);

        Map<String, Object> mapProduct = (Map<String, Object>) map.get("product");

        invoiceLine.setProduct(
            Beans.get(ProductRepository.class)
                .all()
                .filter("self.id = ?", mapProduct.get("id"))
                .fetchOne());
        invoiceLineList.add(invoiceLine);
      }

      invoice.setInvoiceLineList(invoiceLineList);
      invoice.setPartner(partner);
      invoice.setAddress(partnerService.getInvoicingAddress(partner));
      Company company = Beans.get(CompanyRepository.class).all().fetchOne();
      invoice.setCompany(company);
      invoice.setCurrency(company.getCurrency());

      Long invoiceId = invoiceService.saveInvoice(invoice);

      // Open the generated invoice in a new tab
      response.setView(
          ActionView.define("Invoice")
              .model(Invoice.class.getName())
              .add("grid", "invoice-grid")
              .add("form", "invoice-form")
              .param("forceEdit", "true")
              .context("_showRecord", String.valueOf(invoiceId))
              .map());
      response.setCanClose(true);

    } catch (Exception e) {
      e.printStackTrace();
      TraceBackService.trace(response, e);
    }
  }
}
