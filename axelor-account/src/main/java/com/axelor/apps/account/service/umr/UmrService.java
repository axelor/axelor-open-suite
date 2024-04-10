package com.axelor.apps.account.service.umr;

import com.axelor.apps.account.db.InvoicingPaymentSituation;
import com.axelor.apps.account.db.Umr;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import java.util.Map;

public interface UmrService {
  Map<String, Object> getOnNewValuesMap(InvoicingPaymentSituation invoicingPaymentSituation)
      throws AxelorException;

  Umr getActiveUmr(Company company, Partner partner);
}
