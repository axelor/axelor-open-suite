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

package com.axelor.apps.prestashop.service.exports;

import java.io.File;
import java.io.IOException;
import java.time.ZonedDateTime;
import javax.xml.transform.TransformerException;
import com.axelor.apps.prestashop.service.PrestaShopWebserviceException;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.inject.Inject;

public class PrestaShopServiceExport {
	
	@Inject
	private PrestaShopServiceImplExport psExport;
	
	@Inject
	private MetaFiles metaFiles;
	
	public void exportAxelorBase(ZonedDateTime endDate) throws PrestaShopWebserviceException, TransformerException, IOException {
		
		psExport.exportAxelorCurrencies(endDate);
		psExport.exportAxelorCountries(endDate);
		psExport.exportAxelorPartners(endDate);
		psExport.exportAxelorPartnerAddresses(endDate);
		psExport.exportAxelorProductCategories(endDate);
		psExport.exportAxelorProducts(endDate);
	}	
	
	public MetaFile exportPrestShop(ZonedDateTime endDate) throws PrestaShopWebserviceException, TransformerException, IOException {
		
		this.exportAxelorBase(endDate);
		psExport.exportAxelorSaleOrders(endDate);
		psExport.exportAxelorSaleOrderLines();
		
		File exportFile = psExport.closeLog();
		MetaFile exporMetatFile = metaFiles.upload(exportFile);
		return exporMetatFile;
	}
}
