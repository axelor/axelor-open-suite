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
package com.axelor.apps.sale.db;

public interface ISale {

	/**
	 * Static select in Product Category
	 */
	// TypeSelect
	static final String DEFAULT = "default";
	static final String SUBSCRIPTION = "subscription";
	static final String CONSUMPTION = "consumption";
	static final String OVERLOAD = "overload";
	static final String ROUTING = "routing";
	static final String FEE = "fee";
	static final String SERVICE = "service";
	static final String PROVISION = "provision";
	
	/**
	 * Static select in SalesRuleAction
	 */
   static final int MEETING = 1;
   static final int SALES_OFFER_PERSO = 2;
   static final int EMAIL = 3;
   static final int MAIL = 4;
   static final int ALARM = 5;

   
   static final int ANNIVERSARY_DATE = 0;
   static final int NOTIFICATION = 1;
   static final int INVOICING_BLOCKING = 3;

}
