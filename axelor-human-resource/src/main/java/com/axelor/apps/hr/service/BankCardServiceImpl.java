package com.axelor.apps.hr.service;

import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.utils.helpers.StringHelper;
import java.util.Collection;
import java.util.stream.Collectors;

public class BankCardServiceImpl implements BankCardService {
  @Override
  public String createDomainForBankCard(BankDetails bankDetails, Company company) {
    if (bankDetails != null) {
      return "self.id IN (" + StringHelper.getIdListString(bankDetails.getBankCardList()) + ")";
    }
    return "self.id IN ("
        + StringHelper.getIdListString(
            company.getBankDetailsList().stream()
                .map(BankDetails::getBankCardList)
                .flatMap(Collection::stream)
                .collect(Collectors.toList()))
        + ")";
  }
}
