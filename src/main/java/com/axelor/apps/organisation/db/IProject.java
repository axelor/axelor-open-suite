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
package com.axelor.apps.organisation.db;

/**
 * Interface of Project object. Enum all static variable of object.
 * 
 */
public interface IProject {

	/**
     * Static select in Project
     */

    // STATUS SELECT

	static final int STATUS_DRAFT = 1;
	static final int STATUS_CONFIRMED = 2;
	static final int STATUS_STARTED = 3;
	static final int STATUS_COMPLETED = 4;
	static final int STATUS_CANCELED = 5;
	
	
	// REAL ESTIMATED METHOD SELECT
	static final int REAL_ESTIMATED_METHOD_NONE = 1;
	static final int REAL_ESTIMATED_METHOD_PROGRESS = 2;
	static final int REAL_ESTIMATED_METHOD_SUBSCRIPTION = 3;
	
	
	// REPORT TYPE SELECT
	static final String REPORT_TYPE_PDF = "pdf";
	static final String REPORT_TYPE_XLS = "xls";
	
}