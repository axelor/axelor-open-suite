package com.axelor.apps.account.service.reconcile;

import com.axelor.apps.account.db.MoveLine;
import java.util.List;

public interface ForeignExchangeGapToolsService {

  List<Integer> getForeignExchangeTypes();

  boolean isGain(MoveLine creditMoveLine, MoveLine debitMoveLine, boolean isDebit);

  boolean isDebit(MoveLine creditMoveLine, MoveLine debitMoveLine);
}
