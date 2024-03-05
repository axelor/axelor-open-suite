package com.axelor.apps.hr.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.hr.db.BankCard;
import java.util.List;
import java.util.stream.Collectors;

public class InvoiceHRServiceImpl implements InvoiceHRService {
  @Override
  public String createDomainForBankCard(Invoice invoice) {
    BankDetails bankDetails = invoice.getBankDetails();
    if (bankDetails != null) {
      List<Long> ids =
          bankDetails.getBankCardList().stream().map(BankCard::getId).collect(Collectors.toList());
      return "self.id IN " + ids.toString().replace("[", "(").replace("]", ")");
    }

    List<BankDetails> bankDetailsList = invoice.getCompany().getBankDetailsList();
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
