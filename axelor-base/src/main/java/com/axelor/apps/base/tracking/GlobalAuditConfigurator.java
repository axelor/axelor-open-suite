package com.axelor.apps.base.tracking;

import com.axelor.db.audit.HibernateListenerConfigurator;
import com.google.inject.Inject;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;

public class GlobalAuditConfigurator implements HibernateListenerConfigurator {
  private final GlobalAuditListener globalAuditListener;

  @Inject
  public GlobalAuditConfigurator(GlobalAuditListener globalAuditListener) {
    this.globalAuditListener = globalAuditListener;
  }

  @Override
  public void registerListeners(EventListenerRegistry registry) {
    registry.appendListeners(EventType.PRE_COLLECTION_UPDATE, globalAuditListener);
  }
}
