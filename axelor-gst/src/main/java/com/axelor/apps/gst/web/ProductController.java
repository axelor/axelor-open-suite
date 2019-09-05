package com.axelor.apps.gst.web;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.base.db.Partner;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class ProductController {

  @SuppressWarnings("unchecked")
  public void createInvoice(ActionRequest req, ActionResponse res) {
    try {
      String productIds = req.getContext().get("_ids").toString();
      ActionViewBuilder actionViewBuilder =
          ActionView.define(String.format("Create New Invoice"))
              .model(Invoice.class.getName())
              .add("form", "invoice-form")
              .context("productIds", productIds)
              .context("_operationTypeSelect", InvoiceRepository.OPERATION_TYPE_CLIENT_SALE);

      res.setView(actionViewBuilder.map());

    } catch (Exception e) {
      e.printStackTrace();
      System.out.println(
          "Error in InvoiceLineController  setInvoiceLineFromProduct:: " + e.getMessage());
    }
  }

  public void setPartnerForInvoice(ActionRequest request, ActionResponse response) {

    if (request.getContext().get("productIds") == null) {
      response.setError("Please select product.");
    } else if (request.getContext().get("partner") == null) {
      response.setError("Please select partner.");
    }
    Partner partner = (Partner) request.getContext().get("partner");

    request.getContext().put("productIds", request.getContext().get("productIds"));
    request.getContext().put("partnerId", partner.getId());
    createInvoiceWithPopup(request, response);
  }

  public void createInvoiceWithPopup(ActionRequest req, ActionResponse res) {
    try {

      ActionViewBuilder actionViewBuilder =
          ActionView.define(String.format("Create New Invoice"))
              .model(Invoice.class.getName())
              .add("form", "invoice-form")
              .add("grid", "invoice-grid")
              .param("forceEdit", "true")
              .context("productIds", req.getContext().get("productIds"))
              .context("partnerId", req.getContext().get("partnerId"))
              .context("_operationTypeSelect", InvoiceRepository.OPERATION_TYPE_CLIENT_SALE);

      res.setView(actionViewBuilder.map());

    } catch (Exception e) {
      e.printStackTrace();
      System.out.println(
          "Error in InvoiceLineController  setInvoiceLineFromProduct:: " + e.getMessage());
    }
  }
}
