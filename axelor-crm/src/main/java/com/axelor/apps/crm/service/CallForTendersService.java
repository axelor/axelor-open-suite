package com.axelor.apps.crm.service;

import com.axelor.apps.crm.db.CallForTenders;

public interface CallForTendersService {

  public void win(CallForTenders call);

  public void loose(CallForTenders call);

  public void setBackInProgress(CallForTenders call);
}
