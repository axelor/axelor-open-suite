package com.axelor.apps.base.service.tax;

import com.axelor.apps.account.db.TaxEquiv;
import com.axelor.meta.CallMethod;

public interface TaxEquivService {
  @CallMethod
  String getTaxDomain(TaxEquiv taxEquiv, boolean isFromTax, boolean isToTax);
}
