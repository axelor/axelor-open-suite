/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.exceptions;

public interface IAppExceptionMessages {
	
	static final public String NO_CONFIG_REQUIRED = /*$$(*/ "No configuration required" /*)*/;
	
	static final public String APP_IN_USE = /*$$(*/ "This app is used by %s. Please deactivate them before continue." /*)*/;
	
	static final public String BULK_INSTALL_SUCCESS = /*$$(*/ "Apps installed successfully" /*)*/;
	
	static final public String REFRESH_APP_SUCCESS = /*$$(*/ "Apps refreshed successfully" /*)*/;
	
	static final public String REFRESH_APP_ERROR = /*$$(*/ "Error in refreshing app" /*)*/;
	
	static final public String NO_LANGAUAGE_SELECTED = /*$$(*/ "No application language set. Please set 'application.locale' property." /*)*/;
	
	static final public String DEMO_DATA_SUCCESS = /*$$(*/ "Demo data loaded successfully" /*)*/;
	
}
