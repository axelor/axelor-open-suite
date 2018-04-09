package com.axelor.db;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.hibernate.boot.Metadata;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.event.spi.PostLoadEvent;
import org.hibernate.event.spi.PostLoadEventListener;
import org.hibernate.event.spi.PreDeleteEvent;
import org.hibernate.event.spi.PreDeleteEventListener;
import org.hibernate.event.spi.PreInsertEvent;
import org.hibernate.event.spi.PreInsertEventListener;
import org.hibernate.event.spi.PreUpdateEvent;
import org.hibernate.event.spi.PreUpdateEventListener;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelor.inject.Beans;
import com.axelor.meta.MetaScanner;

/**
 * Hibernate integrator scanning for all classes implementing
 * {@link EntityLifeCycleListener} and handling plumbing to have
 * them called on right event.
 *
 * FIXME move to axelor core
 */
public class EntityLifeCycleListenerIntegrator implements Integrator {
	private Logger log = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	@Override
	public void integrate(Metadata metadata, SessionFactoryImplementor sessionFactory,
			SessionFactoryServiceRegistry serviceRegistry) {
			EventListenerRegistry registry = serviceRegistry.getService(EventListenerRegistry.class);
			DispatcherEventListener listener = new DispatcherEventListener();

			for(Class<? extends EntityLifeCycleListener> clazz : MetaScanner.findSubTypesOf(EntityLifeCycleListener.class).find()) {
				if(Modifier.isAbstract(clazz.getModifiers())) {
					continue;
				}
				try {
					if(log.isDebugEnabled()) {
						log.debug("Found EntityLifeCycleListener " + clazz.getName());
					}
					EntityLifeCycleListener l = Beans.inject(clazz.newInstance());
					listener.addListener(l, l.targetClasses());
				} catch (InstantiationException | IllegalAccessException e) {
					log.error("An exception occured during EntityLifeCycleListener instantiation, skipping", e);
				}
			}

			if(listener.hasListeners()) {
				registry.appendListeners(EventType.POST_LOAD, listener);
				registry.appendListeners(EventType.PRE_DELETE, listener);
				registry.appendListeners(EventType.PRE_INSERT, listener);
				registry.appendListeners(EventType.PRE_UPDATE, listener);
			}
	}

	@Override
	public void disintegrate(SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {
		log.debug(getClass() + "::disintegrate");
	}

	/**
	 * Helper class which will be registered as an Hibernate event listener,
	 * handling dispatching to actual entity listeners.
	 */
	private static class DispatcherEventListener implements PreDeleteEventListener, PreUpdateEventListener, PreInsertEventListener, PostLoadEventListener {
		private static final long serialVersionUID = -1802549935130773605L;

		private Map<Class<? extends Model>, List<EntityLifeCycleListener>> registeredListeners = Collections.synchronizedMap(new HashMap<>());
		private Map<Class<? extends Model>, List<EntityLifeCycleListener>> cachedListeners = new HashMap<>();

		public void addListener(EntityLifeCycleListener listener, @SuppressWarnings("unchecked") Class<? extends Model>... targetClasses) {
			for(Class<? extends Model> clazz : targetClasses) {
				List<EntityLifeCycleListener> l = registeredListeners.get(clazz);
				if(l == null) {
					registeredListeners.put(clazz, l = new LinkedList<>());
				}
				l.add(listener);
			}
		}

		@Override
		public void onPostLoad(PostLoadEvent event) {
			for(EntityLifeCycleListener listener : getListeners(event.getEntity().getClass())) {
				listener.onLoad((Model)event.getEntity());
			}
		}

		@Override
		public boolean onPreInsert(PreInsertEvent event) {
			for(EntityLifeCycleListener listener : getListeners(event.getEntity().getClass())) {
				listener.onInsert((Model)event.getEntity());
			}
			return false;
		}

		@Override
		public boolean onPreUpdate(PreUpdateEvent event) {
			for(EntityLifeCycleListener listener : getListeners(event.getEntity().getClass())) {
				listener.onUpdate((Model)event.getEntity());
			}
			return false;
		}

		@Override
		public boolean onPreDelete(PreDeleteEvent event) {
			for(EntityLifeCycleListener listener : getListeners(event.getEntity().getClass())) {
				listener.onDelete((Model)event.getEntity());
			}
			return false;
		}

		public boolean hasListeners() {
			return registeredListeners.size() > 0;
		}

		private List<EntityLifeCycleListener> getListeners(Class<?> clazz) {
			List<EntityLifeCycleListener> listeners = cachedListeners.get(clazz);
			if(listeners != null) {
				return listeners;
			}

			listeners = new LinkedList<>();
			do {
				List<EntityLifeCycleListener> l = registeredListeners.get(clazz);
				if(l != null) {
					listeners.addAll(l);
				}
				clazz = clazz.getSuperclass();
			} while(clazz != null);
			return listeners;
		}

	}
}
