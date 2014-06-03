/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2012-2014 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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