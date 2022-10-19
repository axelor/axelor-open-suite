package com.axelor.apps.account.service;

import com.axelor.apps.account.db.MoveLineQuery;
import com.axelor.apps.account.db.Reconcile;
import com.axelor.exception.AxelorException;
import com.google.inject.persist.Transactional;
import java.util.List;

public interface MoveLineQueryService {

  public String getMoveLineQuery(MoveLineQuery moveLineQuery) throws AxelorException;

  @Transactional(rollbackOn = {Exception.class})
  public void ureconcileMoveLinesWithCacheManagement(List<Reconcile> reconcileList)
      throws AxelorException;
}
