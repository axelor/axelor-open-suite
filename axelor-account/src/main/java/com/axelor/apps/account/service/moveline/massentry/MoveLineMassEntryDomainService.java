package com.axelor.apps.account.service.moveline.massentry;

import com.axelor.apps.account.db.MoveLineMassEntry;

public interface MoveLineMassEntryDomainService {
  String createDomainForMovePartnerBankDetails(MoveLineMassEntry moveLineMassEntry);
}
