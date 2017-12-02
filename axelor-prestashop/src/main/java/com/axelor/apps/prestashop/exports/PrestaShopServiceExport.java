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

package com.axelor.apps.prestashop.exports;

import java.io.IOException;
import java.time.ZonedDateTime;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.xml.sax.SAXException;

import com.axelor.apps.base.db.Batch;
import com.axelor.apps.prestashop.service.library.PrestaShopWebserviceException;

public interface PrestaShopServiceExport {
	
	/**
	 * Export ABS object's details to prestashop
	 * 
	 * @param endDate get as par last batch executed
	 * @return exported log file object
	 * @throws PrestaShopWebserviceException
	 * @throws TransformerException
	 * @throws IOException
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws TransformerFactoryConfigurationError 
	 * @throws JAXBException 
	 */
	public Batch exportPrestShop(ZonedDateTime endDate, Batch batch) throws PrestaShopWebserviceException, TransformerException, IOException, ParserConfigurationException, SAXException, JAXBException, TransformerFactoryConfigurationError;
}
