package com.axelor.apps.base.tracking;

import com.axelor.db.audit.HibernateListenerConfigurator;
import jakarta.inject.Inject;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;

public class AosHibernateConfigurator implements HibernateListenerConfigurator {

  private final GlobalAuditListener globalAuditListener;

  @Inject
  public AosHibernateConfigurator(GlobalAuditListener globalAuditListener) {
    this.globalAuditListener = globalAuditListener;
  }

  @Override
  public void registerListeners(EventListenerRegistry eventListenerRegistry) {
    eventListenerRegistry.appendListeners(EventType.PRE_UPDATE, globalAuditListener);
    eventListenerRegistry.appendListeners(EventType.PRE_INSERT, globalAuditListener);
    eventListenerRegistry.appendListeners(EventType.PRE_COLLECTION_UPDATE, globalAuditListener);
    eventListenerRegistry.appendListeners(EventType.PRE_DELETE, globalAuditListener);
  }
}
