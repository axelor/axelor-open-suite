package com.axelor.apps.base.tracking;

import java.util.List;

public interface GlobalAuditCollectionUpdateService {

  void createLogForCollectionUpdate(List<GlobalAuditTracker.CollectionState> collectionStateList);
}
