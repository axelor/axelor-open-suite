/**
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public
 * Attribution License Version 1.0 (the "License"); you may not use
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
 * Software distributed under the License is distributed on an "AS IS"
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
 * Copyright (c) 2012-2014 Axelor. All Rights Reserved.
 */
package com.axelor.apps.base.db;

/**
 * Interface of Product package. Enum all static variable of packages.
 * 
 */
public interface IProduct {

	// APPLICATION TYPE SELECT VALUE
	static final int APPLICATION_TYPE_PRODUCT = 1;
	static final int APPLICATION_TYPE_PROFILE = 2;
	static final int APPLICATION_TYPE_EXPENSE = 3;

	// PRODUCT TYPE SELECT
	static final String PRODUCT_TYPE_SERVICE = "service";
	static final String PRODUCT_TYPE_STORABLE = "storable";
	
	// SALE TRACKING ORDER SELECT
	static final int SALE_TRACKING_ORDER_FIFO = 1;
	static final int SALE_TRACKING_ORDER_LIFO = 2;
	
	// SALE SUPPLY SELECT
	static final int SALE_SUPPLY_FROM_STOCK = 1;
	static final int SALE_SUPPLY_PURCHASE = 2;
	static final int SALE_SUPPLY_PRODUCE = 3;
	
}
