package com.axelor.apps.purchase.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.purchase.db.CallTender;
import com.axelor.apps.purchase.db.CallTenderNeed;
import com.axelor.apps.purchase.service.SupplierCatalogService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.io.IOException;
import java.util.Optional;
import javax.mail.MessagingException;

public class CallTenderNeedController {

  public void setUnit(ActionRequest request, ActionResponse response)
      throws AxelorException, IOException, MessagingException, ClassNotFoundException {

    var callTenderNeed = request.getContext().asType(CallTenderNeed.class);
    var company =
        Optional.ofNullable(request.getContext().getParent())
            .map(c -> c.asType(CallTender.class))
            .map(CallTender::getCompany)
            .orElse(null);
    if (callTenderNeed != null && callTenderNeed.getProduct() != null) {
      response.setValue(
          "unit",
          Beans.get(SupplierCatalogService.class)
              .getUnit(callTenderNeed.getProduct(), null, company));
    }
  }
}
