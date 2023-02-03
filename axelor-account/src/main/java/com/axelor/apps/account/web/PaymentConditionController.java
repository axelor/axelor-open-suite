package com.axelor.apps.account.web;

import com.axelor.apps.account.db.PaymentCondition;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.db.repo.MoveRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class PaymentConditionController {

  public void alertModification(ActionRequest request, ActionResponse response) {
    PaymentCondition paymentCondition = request.getContext().asType(PaymentCondition.class);
    try {
      if (paymentCondition.getId() != null) {
        long linkedObjetsCount =
            Beans.get(InvoiceRepository.class)
                    .all()
                    .filter("self.paymentCondition.id = ?1", paymentCondition.getId())
                    .count()
                + Beans.get(MoveRepository.class)
                    .all()
                    .filter("self.paymentCondition.id = ?1", paymentCondition.getId())
                    .count();
        if (linkedObjetsCount > 0) {
          response.setAlert(
              String.format(
                  I18n.get(
                      "%d objects are linked to this payment condition, the modifications will be applied these objects."),
                  linkedObjetsCount));
        }
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
