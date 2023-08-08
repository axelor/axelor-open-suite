package com.axelor.apps.account.service.invoiceterm;

import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.base.AxelorException;
import java.util.Map;

public interface InvoiceTermGroupService {
  Map<String, Object> getOnNewValuesMap(InvoiceTerm invoiceTerm) throws AxelorException;

  Map<String, Object> getOnLoadValuesMap(InvoiceTerm invoiceTerm);

  Map<String, Object> getAmountOnChangeValuesMap(InvoiceTerm invoiceTerm);

  Map<String, Object> checkPfpValidatorUser(InvoiceTerm invoiceTerm);

  Map<String, Map<String, Object>> getOnNewAttrsMap(InvoiceTerm invoiceTerm);

  Map<String, Map<String, Object>> getOnLoadAttrsMap(InvoiceTerm invoiceTerm);
}
