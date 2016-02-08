/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2016 Axelor (<http://axelor.com>).
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
package com.axelor.app.production.db;

/**
 * Interface of Event package. Enum all static variable of packages.
 * 
 * @author dubaux
 * 
 */
public interface IManufOrder {


	/**
	 * Static status select
	 */

	static final int STATUS_DRAFT = 1;
	static final int STATUS_CANCELED = 2;
	static final int STATUS_PLANNED = 3;
	static final int STATUS_IN_PROGRESS = 4;
	static final int STATUS_STANDBY = 5;
	static final int STATUS_FINISHED = 6;

	
}
