package com.axelor.apps.account.db.repo;

import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.account.db.PaymentSession;
import com.axelor.apps.base.db.Partner;
import com.axelor.db.Query;
import java.util.List;

public class AccountInvoiceTermRepository extends InvoiceTermRepository {

  public List<InvoiceTerm> findByPaymentSessionAndPartner(
      PaymentSession paymentSession, Partner partner) {
    return Query.of(InvoiceTerm.class)
        .filter("self.paymentSession.id = :id AND self.invoice.partner.id = :partnerId")
        .bind("id", paymentSession.getId())
        .bind("partnerId", partner.getId())
        .fetch();
  }
}
