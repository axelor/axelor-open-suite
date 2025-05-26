package com.axelor.apps.account.service.invoiceterm;

import com.axelor.apps.account.db.InvoiceTerm;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Partner;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

public class InvoiceTermDomainServiceImpl implements InvoiceTermDomainService {

  @Override
  public String createDomainForBankDetails(InvoiceTerm invoiceTerm) {
    Partner partner = invoiceTerm.getPartner();
    String domain = "";

    if (partner != null && !partner.getBankDetailsList().isEmpty()) {
      List<Long> bankDetailsIdList =
          partner.getBankDetailsList().stream()
              .filter(BankDetails::getActive)
              .map(BankDetails::getId)
              .collect(Collectors.toList());

      domain = "self.id IN (" + StringUtils.join(bankDetailsIdList, ',') + ")";
    }
    return domain;
  }
}
