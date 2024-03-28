package com.axelor.apps.hr.service;

import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;

public interface BankCardService {
  public String createDomainForBankCard(BankDetails bankDetails, Company company);
}
