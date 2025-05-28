package com.axelor.apps.account.service;

import com.axelor.apps.account.db.PaymentSchedule;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Partner;
import com.axelor.utils.helpers.StringHelper;
import java.util.List;
import java.util.stream.Collectors;

public class PaymentScheduleDomainServiceImpl implements PaymentScheduleDomainService {

  @Override
  public String createDomainForBankDetails(PaymentSchedule paymentSchedule) {
    Partner partner = paymentSchedule.getPartner();
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
