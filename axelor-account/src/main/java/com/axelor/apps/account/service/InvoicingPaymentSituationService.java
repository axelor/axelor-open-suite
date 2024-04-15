package com.axelor.apps.account.service;

import com.axelor.apps.account.db.InvoicingPaymentSituation;
import com.axelor.apps.base.db.Partner;

public interface InvoicingPaymentSituationService {
  String getCompanyDomain(InvoicingPaymentSituation invoicingPaymentSituation, Partner partner);
}
