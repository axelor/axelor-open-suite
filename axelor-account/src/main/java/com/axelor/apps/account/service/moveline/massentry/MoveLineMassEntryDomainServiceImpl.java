package com.axelor.apps.account.service.moveline.massentry;

import com.axelor.apps.account.db.MoveLineMassEntry;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Partner;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

public class MoveLineMassEntryDomainServiceImpl implements MoveLineMassEntryDomainService {

  @Override
  public String createDomainForMovePartnerBankDetails(MoveLineMassEntry moveLineMassEntry) {
    Partner partner = moveLineMassEntry.getPartner();
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
