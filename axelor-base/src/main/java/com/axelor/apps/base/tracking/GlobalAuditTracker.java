package com.axelor.apps.base.tracking;

import com.axelor.db.Model;
import com.axelor.db.annotations.Track;
import com.axelor.db.audit.AuditTracker;
import com.axelor.db.tracking.FieldTracking;
import com.axelor.db.tracking.ModelTracking;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.hibernate.collection.spi.PersistentCollection;

public class GlobalAuditTracker extends AuditTracker {

  private final List<CollectionState> updatedCollections = new ArrayList<>();

  public record CollectionState(
      Model owner,
      Collection<? extends Model> collection,
      Collection<? extends Model> oldCollection) {}

  public void addUpdatedCollection(Model owner, PersistentCollection<? extends Model> collection) {
    @SuppressWarnings("unchecked")
    var value = (Collection<? extends Model>) collection.getValue();

    @SuppressWarnings("unchecked")
    var snapshot = (Collection<? extends Model>) collection.getStoredSnapshot();

    var oldValue =
        snapshot instanceof Set
            ? snapshot.stream().collect(Collectors.toSet())
            : snapshot.stream().toList();

    updatedCollections.add(new CollectionState(owner, value, oldValue));
  }

  public List<CollectionState> getUpdatedCollections() {
    return updatedCollections;
  }

  @Override
  protected ModelTracking getTrack(Model entity) {
    if (entity == null) {
      return null;
    }
    Track track = entity.getClass().getAnnotation(Track.class);
    List<FieldTracking> trackedCustomFields = getTrackedCustomFields(entity);
    return ModelTracking.create(track, trackedCustomFields);
  }
}
