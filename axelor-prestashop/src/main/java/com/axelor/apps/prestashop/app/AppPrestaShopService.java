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
package com.axelor.apps.prestashop.app;

import com.axelor.apps.base.db.AppPrestashop;

public interface AppPrestaShopService {

	public boolean checkAccess(AppPrestashop ps);

	/**
	 * Check prestashop url is valid or not
	 *
	 * @param ps  object of AppPrestashop contains configuration details
	 * @return <code>true</code> if specified URL is valid (not null and
	 * not ending with a /), <code>false</code> otherwise.
	 */
	public boolean validateUrl(AppPrestashop ps);
}
