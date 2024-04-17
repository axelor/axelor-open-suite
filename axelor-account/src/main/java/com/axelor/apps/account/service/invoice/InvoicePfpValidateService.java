package com.axelor.apps.account.service.invoice;

import com.axelor.apps.base.AxelorException;

public interface InvoicePfpValidateService {

  void validatePfp(Long invoiceId) throws AxelorException;
}
