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
package com.axelor.apps.base.db;

/**
 * Interface of Pricing package. Enum all static variable of packages.
 * 
 * @author guerrier
 * 
 */
public interface IPricing {

	/**
	 * Static select in Pricing
	 */

	// ADMINISTERED ELECTED
	static final String ADMIN = "admin";
	static final String ELU = "elu";
	static final String ROUTING = "routing";
	static final String SERVICE = "service";

	/**
	 * Static select in Constituent
	 */

	// TYPE SELECT VALUE
	static final int VAR_TYPE = 1;
	static final int FIX_TYPE = 2;

	// COMMITMENT TYPE SELECT VALUE
	static final int NONE = 0;
	static final int COMMITMENT_TYPE = 1;
	static final int HTA_TYPE = 2;
	static final int DISC_BAND_TYPE = 3;
	
	// POWER INPUT MODE 
	static final int NO_POWER = 1;
	static final int MONO_POWER = 2;
	static final int MULTI_POWER = 3;
	

}
