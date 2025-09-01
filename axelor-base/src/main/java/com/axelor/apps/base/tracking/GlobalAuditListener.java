package com.axelor.apps.base.tracking;

import com.axelor.db.Model;
import com.axelor.inject.Beans;
import com.google.inject.Singleton;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.hibernate.Transaction;
import org.hibernate.action.spi.AfterTransactionCompletionProcess;
import org.hibernate.action.spi.BeforeTransactionCompletionProcess;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.event.spi.EventSource;
import org.hibernate.event.spi.PreCollectionUpdateEvent;
import org.hibernate.event.spi.PreCollectionUpdateEventListener;

@Singleton
public class GlobalAuditListener implements PreCollectionUpdateEventListener {

  private final Map<Transaction, GlobalAuditTracker> trackers = new ConcurrentHashMap<>();

  private GlobalAuditTracker get(EventSource sourceSession) {
    return trackers.computeIfAbsent(
        sourceSession.accessTransaction(),
        transaction -> {
          final GlobalAuditTracker tracker = Beans.get(GlobalAuditTracker.class);
          sourceSession
              .getActionQueue()
              .registerProcess(
                  (BeforeTransactionCompletionProcess)
                      session ->
                          Optional.ofNullable(trackers.get(transaction))
                              .ifPresent(x -> x.doBeforeTransactionCompletion(session)));
          sourceSession
              .getActionQueue()
              .registerProcess(
                  (AfterTransactionCompletionProcess)
                      (success, session) -> trackers.remove(transaction));
          return tracker;
        });
  }

  @Override
  public void onPreUpdateCollection(PreCollectionUpdateEvent event) {
    final EventSource session = event.getSession();
    final GlobalAuditTracker tracker = get(session);

    if (event.getAffectedOwnerOrNull() instanceof Model owner) {
      @SuppressWarnings("unchecked")
      final PersistentCollection<? extends Model> collection =
          (PersistentCollection<? extends Model>) event.getCollection();
      tracker.addUpdatedCollection(owner, collection);
    }
  }

  public Map<Transaction, GlobalAuditTracker> getTrackers() {
    return trackers;
  }
}
