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
package com.axelor.apps.account.db;

/**
 * Interface of Payment Mode model.
 */
public interface IPaymentMode {

	// TYPE
	static final int TYPE_OTHER = 1;
	static final int TYPE_DD = 2;
	static final int TYPE_IPO = 3;
	static final int TYPE_IPO_CHEQUE = 4;
	static final int TYPE_CASH = 5;
	static final int TYPE_BANK_CARD = 6;
	static final int TYPE_CHEQUE = 7;
	static final int TYPE_WEB = 8;
	static final int TYPE_TRANSFER = 9;

	// Sales or purchases
	static final int SALES = 1;
	static final int PURCHASES = 2;

}
