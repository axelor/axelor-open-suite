package com.axelor.apps.crm.db;

/**
 * Interface of Lead object. Enum all static variable of object.
 * 
 * @author dubaux
 * 
 */
public interface ILead {


	/**
	 * Static status select
	 */

	static final int STATUS_NEW = 1;
	static final int STATUS_ASSIGNED = 2;
	static final int STATUS_IN_PROCESS = 3;
	static final int STATUS_CONVERTED = 4;
	static final int STATUS_RECYCLED = 5;
	static final int STATUS_DEAD = 6;
	
}
