package com.axelor.apps.account.web;

import com.axelor.apps.account.db.InvoicingPaymentSituation;
import com.axelor.apps.account.service.umr.UmrService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.exception.ErrorException;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class UmrController {

  @ErrorException
  public void onNew(ActionRequest request, ActionResponse response) throws AxelorException {
    if (request.getContext().getParent() != null
        && InvoicingPaymentSituation.class.equals(
            request.getContext().getParent().getContextClass())) {
      InvoicingPaymentSituation invoicingPaymentSituation =
          request.getContext().getParent().asType(InvoicingPaymentSituation.class);
      if (invoicingPaymentSituation == null) {
        return;
      }

      response.setValues(Beans.get(UmrService.class).getOnNewValuesMap(invoicingPaymentSituation));
    }
  }
}
