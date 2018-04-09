package com.axelor.db;

import org.hibernate.event.spi.EventType;

/**
 * Interface for classes listening to one or several
 * entities lifecycle. This avoids to override repository methods
 * and ensure that the listener is called no matter what happens.
 * 
 * Beware that any exception thrown from {@link #onSave(Model)} or
 * {@link #onDelete(Model)} would prevent the operation to complete.
 * 
 * Also, listeners cannot access to EntityManager or perform queries
 * (this is a limitation from the JPA spec…).
 * 
 * FIXME should be moved to axelor-core
 *
 */
public interface EntityLifeCycleListener {
	/**
	 * Entity classes this class wants to listen on.
	 * Listeners are called from more specific class to
	 * more generic one.
	 * @return The array of listened classes.
	 */
	Class<? extends Model>[] targetClasses();
	
	/**
	 * Callback when an entity is about to be inserted.
	 * @param entity Entity to be inserted
	 * @see EventType#PRE_INSERT
	 */
	void onInsert(Model entity);
	
	/**
	 * Callback when an entity is about to be updated
	 * @param entity Entity to be updated.
	 * @see EventType#PRE_UPDATE
	 */
	void onUpdate(Model entity);
	
	/**
	 * Callback on entity delete
	 * @param entity Entity to be deleted.
	 * @see EventType#PRE_DELETE
	 */
	void onDelete(Model entity);
	
	/**
	 * Callback on entity load
	 * @param entity Freshly loaded entity
	 * @see EventType#POST_LOAD
	 */
	void onLoad(Model entity);
}
