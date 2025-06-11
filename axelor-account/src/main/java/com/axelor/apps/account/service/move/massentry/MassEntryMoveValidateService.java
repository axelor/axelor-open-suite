package com.axelor.apps.account.service.move.massentry;

import com.axelor.apps.account.db.Move;
import java.util.List;
import java.util.Map;

public interface MassEntryMoveValidateService {

  Map<List<Long>, String> validateMassEntryMove(Move move);
}
