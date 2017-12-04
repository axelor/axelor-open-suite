/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2017 Axelor (<http://axelor.com>).
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

import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;

import com.axelor.apps.prestashop.service.library.PrestaShopWebserviceException;

import wslite.json.JSONException;

public interface ImportProductService {

	/**
	 * Import products from prestashop.
	 * 
	 * @param bwImport object of import logfile
	 * @return object import log file
	 * @throws IOException
	 * @throws PrestaShopWebserviceException
	 * @throws TransformerException
	 * @throws JAXBException
	 * @throws JSONException
	 */
	public BufferedWriter importProduct(BufferedWriter bwImport) throws IOException, PrestaShopWebserviceException, TransformerException, JAXBException, JSONException;
}
