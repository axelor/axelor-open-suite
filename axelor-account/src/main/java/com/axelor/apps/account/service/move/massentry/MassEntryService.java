package com.axelor.apps.account.service.move.massentry;

import com.axelor.apps.account.db.MoveLineMassEntry;
import com.axelor.apps.base.db.Company;
import java.util.List;

public interface MassEntryService {

  MoveLineMassEntry getFirstMoveLineMassEntryInformations(
      List<MoveLineMassEntry> moveLineMassEntryList,
      MoveLineMassEntry moveLineMassEntry,
      Company company);
}
