package com.axelor.apps.gst.web;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.base.db.Partner;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.Optional;

public class PartnerController {

  public void setContact(ActionRequest req, ActionResponse res) {
    Invoice invoice = req.getContext().asType(Invoice.class);
    Optional<Partner> partnerTypePrimary =
        invoice
            .getPartner()
            .getContactPartnerSet()
            .stream()
            .filter(it -> it.getType() != null && it.getType().equals("Primary"))
            .findFirst();
    if (partnerTypePrimary.isPresent() && partnerTypePrimary.get() != null) {
      res.setValue("contactPartner", partnerTypePrimary.get());
    }
  }
}
