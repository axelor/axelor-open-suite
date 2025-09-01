package com.axelor.apps.base.tracking;

import com.axelor.event.Observes;
import com.axelor.events.internal.BeforeTransactionComplete;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.util.List;

public class GlobalAuditObserver {

  private final GlobalAuditListener globalAuditListener;

  @Inject
  public GlobalAuditObserver(GlobalAuditListener globalAuditListener) {
    this.globalAuditListener = globalAuditListener;
  }

  public void process(@Observes BeforeTransactionComplete event) {
    GlobalTrackingLogCreateService globalTrackingLogCreateService =
        Beans.get(GlobalTrackingLogCreateService.class);
    globalTrackingLogCreateService.createLogForStoredEntities(event);
    globalTrackingLogCreateService.createLogForDeletedEntities(event);

    List<GlobalAuditTracker.CollectionState> collectionStateList =
        globalAuditListener.getTrackers().values().stream()
            .flatMap(tracker -> tracker.getUpdatedCollections().stream())
            .toList();
    Beans.get(GlobalAuditCollectionUpdateService.class)
        .createLogForCollectionUpdate(collectionStateList);
  }
}
