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
package com.axelor.apps.prestashop.imports.service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.time.ZonedDateTime;

import com.axelor.apps.base.db.AppPrestashop;
import com.axelor.apps.prestashop.service.library.PrestaShopWebserviceException;

public interface ImportCurrencyService {

	/**
	 * Fetches currencies from prestashop and store them in ABS
	 * @param appConfig Prestashop module's configuration
	 * @param endDate End date of previous batch run or <code>null</code> if
	 * first run.
	 * @param logBuffer Buffer receiving log messages to be displayed in Axelor
	 * interface
	 * @throws IOException
	 * @throws PrestaShopWebserviceException
	 */
	public void importCurrency(AppPrestashop appConfig, ZonedDateTime endDate, BufferedWriter logBuffer) throws IOException, PrestaShopWebserviceException;
}
