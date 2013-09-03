package com.axelor.apps.crm.db;

/**
 * Interface of Event object. Enum all static variable of object.
 * 
 * @author dubaux
 * 
 */
public interface IEvent {


	/**
	 * Static event type select
	 */
	static final int CALL = 1;
	static final int MEETING = 2;
	static final int TASK = 3;
	static final int HOLIDAY = 4;
	static final int TICKET = 5;
	
	/**
	 * Static event call status select
	 */
	static final int CALL_STATUS_INCOMING = 1;
	static final int CALL_STATUS_OUTGOING = 2;
	
	/**
	 * Static event status select
	 */
	static final int STATUS_PLANIFIED = 1;
	static final int STATUS_REALIZED = 2;
	static final int STATUS_CANCELED = 3;
	
	/**
	 * Static event related to select
	 */
	static final String RELATED_TO_PARTNER = "com.axelor.apps.base.db.Partner";
	static final String RELATED_TO_LEAD = "com.axelor.apps.crm.db.Lead";
}
