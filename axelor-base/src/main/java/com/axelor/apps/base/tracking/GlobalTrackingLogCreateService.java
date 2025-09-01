package com.axelor.apps.base.tracking;

import com.axelor.events.internal.BeforeTransactionComplete;

public interface GlobalTrackingLogCreateService {
  void createLogForStoredEntities(BeforeTransactionComplete event);

  void createLogForDeletedEntities(BeforeTransactionComplete event);
}
