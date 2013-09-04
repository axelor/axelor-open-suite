/**
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the “License”); you may not use
 * this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://license.axelor.com/.
 *
 * The License is based on the Mozilla Public License Version 1.1 but
 * Sections 14 and 15 have been added to cover use of software over a
 * computer network and provide for limited attribution for the
 * Original Developer. In addition, Exhibit A has been modified to be
 * consistent with Exhibit B.
 *
 * Software distributed under the License is distributed on an “AS IS”
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is part of "Axelor Business Suite", developed by
 * Axelor exclusively.
 *
 * The Original Developer is the Initial Developer. The Initial Developer of
 * the Original Code is Axelor.
 *
 * All portions of the code written by Axelor are
 * Copyright (c) 2012-2013 Axelor. All Rights Reserved.
 */
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
