package com.axelor.apps.account.service;

import com.axelor.apps.account.db.MoveLineQuery;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.exception.AxelorException;
import java.util.List;

public interface MoveLineQueryService {

  public String getMoveLineQuery(MoveLineQuery moveLineQuery) throws AxelorException;

  public void ureconcileMoveLinesWithCacheManagement(List<Reconcile> reconcileList)
      throws AxelorException;
}
