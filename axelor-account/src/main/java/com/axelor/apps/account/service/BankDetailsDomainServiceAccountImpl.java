package com.axelor.apps.account.service;

import com.axelor.apps.account.db.PaymentMode;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Partner;
import com.axelor.utils.helpers.StringHelper;
import java.util.List;
import java.util.stream.Collectors;

public class BankDetailsDomainServiceAccountImpl implements BankDetailsDomainServiceAccount {

  @Override
  public String createDomainForBankDetails(
      Partner partner, PaymentMode paymentMode, Company company) {
    String domain = "self.id IN (0)";
    if (partner != null && !partner.getBankDetailsList().isEmpty()) {
      List<BankDetails> bankDetailsList =
          partner.getBankDetailsList().stream()
              .filter(BankDetails::getActive)
              .collect(Collectors.toList());

      domain = "self.id IN (" + StringHelper.getIdListString(bankDetailsList) + ")";
    }
    return domain;
  }
}
