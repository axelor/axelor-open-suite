package com.axelor.apps.crm.db;

/**
 * Interface of Event object. Enum all static variable of object.
 * 
 * @author dubaux
 * 
 */
public interface ICalendar {


	/**
	 * Static calendar type select
	 */
	static final int ICAL_SERVER = 1;
	static final int CALENDAR_SERVER = 2;
	static final int GCAL = 3;
	static final int ZIMBRA = 4;
	static final int KMS = 5;
	static final int CGP = 6;
	static final int CHANDLER = 7;
	
}
