package com.axelor.apps.prestashop.entities;

/**
 * Specific entities that have an ids.
 * All entities are also container since they can be
 * returned by a fetch call.
 */
public abstract class PrestashopIdentifiableEntity extends PrestashopContainerEntity {
	private Integer id;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}
}
