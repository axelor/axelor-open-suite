package com.axelor.apps.account.service.invoiceterm;

import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Partner;
import com.axelor.utils.helpers.StringHelper;
import java.util.List;
import java.util.stream.Collectors;

public class InvoiceTermDomainServiceImpl implements InvoiceTermDomainService {

  @Override
  public String createDomainForBankDetails(InvoiceTerm invoiceTerm) {
    Partner partner = invoiceTerm.getPartner();
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
