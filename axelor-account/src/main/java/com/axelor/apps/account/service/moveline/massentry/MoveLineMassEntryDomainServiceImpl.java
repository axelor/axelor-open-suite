package com.axelor.apps.account.service.moveline.massentry;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveLineMassEntry;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.Partner;
import com.axelor.utils.helpers.StringHelper;
import java.util.List;
import java.util.stream.Collectors;

public class MoveLineMassEntryDomainServiceImpl implements MoveLineMassEntryDomainService {

  @Override
  public String createDomainForMovePartnerBankDetails(
      Move move, MoveLineMassEntry moveLineMassEntry) {
    Partner partner = moveLineMassEntry.getPartner();
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
