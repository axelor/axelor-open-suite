package com.axelor.apps.hr.service;

import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.hr.db.BankCard;
import java.util.List;
import java.util.stream.Collectors;

public class BankCardServiceImpl implements BankCardService {
  @Override
  public String createDomainForBankCard(BankDetails bankDetails, Company company) {
    if (bankDetails != null) {
      List<Long> ids =
          bankDetails.getBankCardList().stream().map(BankCard::getId).collect(Collectors.toList());
      return "self.id IN " + ids.toString().replace("[", "(").replace("]", ")");
    }

    List<BankDetails> bankDetailsList = company.getBankDetailsList();
    if (bankDetailsList != null) {
      List<Long> ids =
          bankDetailsList.stream()
              .map(BankDetails::getBankCardList)
              .flatMapToLong(x -> x.stream().mapToLong(BankCard::getId))
              .boxed()
              .collect(Collectors.toList());
      return "self.id IN " + ids.toString().replace("[", "(").replace("]", ")");
    }
    return "self.id IN (0)";
  }
}
