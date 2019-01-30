package com.axelor.apps.account.service;

import com.axelor.apps.account.db.FiscalPosition;
import com.axelor.apps.base.db.Partner;

public class PartnerAccountService {

  public String getDefaultSpecificTaxNote(Partner partner) {
    FiscalPosition fiscalPosition = partner.getFiscalPosition();

    if (fiscalPosition == null || !fiscalPosition.getCustomerSpecificNote()) {
      return "";
    }

    return fiscalPosition.getCustomerSpecificNoteText();
  }
}
