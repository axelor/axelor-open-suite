package com.axelor.apps.gst.web;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.List;

public class ProductController {

  @SuppressWarnings("unchecked")
  public void createInvoice(ActionRequest req, ActionResponse res) {
    try {
      List<Long> productIds = (List<Long>) req.getContext().get("_ids");
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
}
