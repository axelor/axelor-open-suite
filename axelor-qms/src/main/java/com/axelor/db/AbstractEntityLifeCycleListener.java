package com.axelor.db;

/**
 * Helper class for {@link EntityLifeCycleListener} allowing to only
 * implement needed events.
 * 
 */
public abstract class AbstractEntityLifeCycleListener implements EntityLifeCycleListener {

	@Override
	public void onInsert(Model entity) {
	}
	
	@Override
	public void onUpdate(Model entity) {
	}
	
	@Override
	public void onDelete(Model entity) {
	}

	@Override
	public void onLoad(Model entity) {
	}
}
