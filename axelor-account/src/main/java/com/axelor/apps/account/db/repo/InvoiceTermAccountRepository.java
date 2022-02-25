package com.axelor.apps.account.db.repo;

import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.PaymentSession;
import com.axelor.apps.base.db.Partner;
import com.axelor.db.Query;
import java.util.List;
import java.util.Map;

public class InvoiceTermAccountRepository extends InvoiceTermRepository {
  @Override
  public void remove(InvoiceTerm entity) {
    if (!entity.getIsPaid()) {
      super.remove(entity);
    }
  }

  @Override
  public Map<String, Object> populate(Map<String, Object> json, Map<String, Object> context) {
    json.put("$statusSelectPaymentSession", context.get("statusSelect"));
    return super.populate(json, context);
  }

  public List<InvoiceTerm> findByPaymentSessionAndPartner(
      PaymentSession paymentSession, Partner partner) {
    return Query.of(InvoiceTerm.class)
        .filter("self.paymentSession.id = :id AND self.moveLine.partner.id = :partnerId")
        .bind("id", paymentSession.getId())
        .bind("partnerId", partner.getId())
        .fetch();
  }
}
