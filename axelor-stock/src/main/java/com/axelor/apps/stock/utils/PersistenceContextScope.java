package com.axelor.apps.stock.utils;

import com.axelor.db.EntityHelper;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javax.persistence.EntityManager;
import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.internal.StatefulPersistenceContext;
import org.hibernate.engine.spi.PersistenceContext;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.proxy.HibernateProxy;

public final class PersistenceContextScope implements AutoCloseable {

  private static final ThreadLocal<PersistenceContextScope> CURRENT = new ThreadLocal<>();

  private final EntityManager em;
  // Identity set so we pin exact instances (not equals-by-id)
  private final Set<Object> pins = Collections.newSetFromMap(new IdentityHashMap<>());
  private boolean autoReattach = true;

  private PersistenceContextScope(EntityManager em) {
    this.em = em;
    this.pins.addAll(snapshotManaged(em));
    CURRENT.set(this);
  }

  /** Begin a scope that pins whatever is currently managed. */
  public static PersistenceContextScope begin() {
    return new PersistenceContextScope(JPA.em());
  }

  /**
   * Flush then detach everything except the originally pinned instances. Also re-attach any pinned
   * instance that somehow became detached.
   */
  public void flushAndPartialClear() {
    em.flush();
    partialClear();
  }

  /**
   * Preload and pin currently managed entities and their associations. Allows callers to avoid
   * re-association on every partial clear.
   */
  public void prefetchAssociations() {
    Session session = em.unwrap(Session.class);
    reattachAssociations(session, snapshotManaged(em));
  }

  public void reattachAssociationsForPins() {
    Session session = em.unwrap(Session.class);
    reattachAssociations(session, new LinkedHashSet<>(pins));
  }

  /** Enable or disable association reattachment after each partial clear. */
  public PersistenceContextScope autoReattach(boolean enabled) {
    this.autoReattach = enabled;
    return this;
  }

  public PersistenceContextScope pinGraph(Object... roots) {
    if (roots == null || roots.length == 0) {
      return this;
    }
    Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<>());
    for (Object root : roots) {
      pinGraphInternal(root, visited);
    }
    return this;
  }

  public PersistenceContextScope pin(Object... entities) {
    if (entities == null || entities.length == 0) {
      return this;
    }
    Session session = em.unwrap(Session.class);
    for (Object entity : entities) {
      if (entity == null) {
        continue;
      }
      pins.add(entity);
      if (entity instanceof Model) {
        Model model = (Model) entity;
        if (model.getId() == null) {
          continue;
        }
        if (!em.contains(entity)) {
          session.buildLockRequest(LockOptions.NONE).lock(entity);
        }
      }
    }
    return this;
  }

  private void pinGraphInternal(Object candidate, Set<Object> visited) {
    if (candidate == null) {
      return;
    }
    Object entity = EntityHelper.getEntity(candidate);
    if (entity == null || !visited.add(entity)) {
      return;
    }
    pin(entity);
    if (!(entity instanceof Model)) {
      return;
    }
    Mapper mapper = Mapper.of(entity.getClass());
    for (Property property : mapper.getProperties()) {
      if (property.getTarget() == null) {
        continue;
      }
      Object value;
      try {
        value = property.get(entity);
      } catch (RuntimeException e) {
        continue;
      }
      if (value == null) {
        continue;
      }
      if (property.isCollection()) {
        pinCollectionGraph(value, visited);
      } else {
        pinGraphInternal(value, visited);
      }
    }
  }

  private void pinCollectionGraph(Object association, Set<Object> visited) {
    Object value = association;
    if (value instanceof PersistentCollection) {
      PersistentCollection collection = (PersistentCollection) value;
      if (!collection.wasInitialized()) {
        return;
      }
      value = collection.getValue();
    }

    if (value instanceof Iterable) {
      for (Object element : (Iterable<?>) value) {
        pinGraphInternal(element, visited);
      }
      return;
    }

    if (value instanceof Map) {
      ((Map<?, ?>) value).values().forEach(element -> pinGraphInternal(element, visited));
    }
  }

  public static PersistenceContextScope current() {
    return CURRENT.get();
  }

  public boolean isAutoReattachEnabled() {
    return autoReattach;
  }

  /** Detach all non-pinned; ensure all pins are managed (reattach w/o SELECT). */
  public void partialClear() {
    Set<Object> managedBeforeClear = snapshotManaged(em);
    for (Object entity : managedBeforeClear) {
      if (!pins.contains(entity)) {
        em.detach(entity);
      }
    }

    Session session = em.unwrap(Session.class);
    reattachPins(session);
    if (autoReattach) {
      reattachAssociations(session, managedBeforeClear);
    }
  }

  private void reattachPins(Session session) {
    for (Object pin : pins) {
      if (pin instanceof Model) {
        Long id = ((Model) pin).getId();
        if (id == null) {
          continue;
        }
      }
      if (!em.contains(pin)) {
        session.buildLockRequest(LockOptions.NONE).lock(pin);
      }
    }
  }

  private void reattachAssociations(Session session, Set<Object> managedEntities) {
    Set<Object> __ownersSnapshot = new LinkedHashSet<>(managedEntities);
    for (Object owner : __ownersSnapshot) {
      if (owner == null) {
        continue;
      }
      Mapper mapper = Mapper.of(owner.getClass());
      for (Property property : mapper.getProperties()) {
        if (property.getTarget() == null) {
          continue;
        }
        Object value;
        try {
          value = property.get(owner);
        } catch (RuntimeException e) {
          continue;
        }
        if (value == null) {
          continue;
        }
        if (property.isCollection()) {
          reattachCollection(session, value);
        } else {
          reattachReference(session, value);
        }
      }
    }
  }

  private void reattachCollection(Session session, Object value) {
    if (value instanceof PersistentCollection) {
      PersistentCollection collection = (PersistentCollection) value;
      if (!collection.wasInitialized()) {
        return;
      }
      value = collection.getValue();
    }

    if (value instanceof Iterable) {
      for (Object element : (Iterable<?>) value) {
        reattachReference(session, element);
      }
      return;
    }

    if (value instanceof java.util.Map) {
      ((java.util.Map<?, ?>) value)
          .values()
          .forEach(element -> reattachReference(session, element));
    }
  }

  private void reattachReference(Session session, Object reference) {
    Object entity = EntityHelper.getEntity(reference);
    if (entity == null) {
      return;
    }
    if (entity instanceof HibernateProxy) {
      entity = ((HibernateProxy) entity).getHibernateLazyInitializer().getImplementation();
    }
    if (!(entity instanceof Model)) {
      return;
    }
    Model modelRef = (Model) entity;
    if (modelRef.getId() == null || em.contains(entity)) {
      return;
    }
    session.buildLockRequest(LockOptions.NONE).lock(entity);
    pins.add(entity);
  }

  @Override
  public void close() {
    CURRENT.remove();
    // nothing to clean; scope ends when method exits
  }

  private static Set<Object> snapshotManaged(EntityManager em) {
    SharedSessionContractImplementor ss = em.unwrap(SharedSessionContractImplementor.class);
    PersistenceContext pc = ss.getPersistenceContext();
    if (pc instanceof StatefulPersistenceContext) {
      Map<?, ?> byKey = ((StatefulPersistenceContext) pc).getEntitiesByKey();
      return new LinkedHashSet<>(byKey.values()); // actual managed instances
    }
    return Collections.emptySet();
  }
}
