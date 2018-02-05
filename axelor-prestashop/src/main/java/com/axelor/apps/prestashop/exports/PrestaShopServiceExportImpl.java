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
package com.axelor.apps.prestashop.exports;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.ZonedDateTime;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import org.xml.sax.SAXException;
import com.axelor.apps.base.db.Batch;
import com.axelor.apps.prestashop.exports.service.ExportAddressService;
import com.axelor.apps.prestashop.exports.service.ExportCategoryService;
import com.axelor.apps.prestashop.exports.service.ExportCountryService;
import com.axelor.apps.prestashop.exports.service.ExportCurrencyService;
import com.axelor.apps.prestashop.exports.service.ExportCustomerService;
import com.axelor.apps.prestashop.exports.service.ExportOrderDetailService;
import com.axelor.apps.prestashop.exports.service.ExportOrderService;
import com.axelor.apps.prestashop.exports.service.ExportProductService;
import com.axelor.apps.prestashop.service.library.PrestaShopWebserviceException;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.inject.Inject;

public class PrestaShopServiceExportImpl implements PrestaShopServiceExport {
	
	@Inject
	private MetaFiles metaFiles;
	
	@Inject
	private ExportCurrencyService currencyService;
	
	@Inject
	private ExportCountryService countryService;
	
	@Inject
	private ExportCustomerService customerService;
	
	@Inject
	private ExportAddressService addressService;
	
	@Inject
	private ExportCategoryService categoryService; 
	
	@Inject
	private ExportProductService productService;
	
	@Inject
	private ExportOrderService orderService;
	
	@Inject
	private ExportOrderDetailService detailService;
	
	
	File exportFile = File.createTempFile("Export Log", ".txt");
	FileWriter fwExport = null;
	BufferedWriter bwExport = null;
	Integer totalDone = 0;
    Integer totalAnomaly = 0;
	
	/**
	 * Initialize constructor.  
	 * 
	 * @throws IOException
	 */
	public PrestaShopServiceExportImpl() throws IOException {

		fwExport = new FileWriter(exportFile);
		bwExport = new BufferedWriter(fwExport);
	}
	
	/**
	 * Export axelor base module
	 * 
	 * @param endDate
	 * @throws PrestaShopWebserviceException
	 * @throws TransformerException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws JAXBException
	 * @throws TransformerFactoryConfigurationError
	 */
	public void exportAxelorBase(ZonedDateTime endDate) throws PrestaShopWebserviceException, TransformerException, IOException, ParserConfigurationException, SAXException, JAXBException, TransformerFactoryConfigurationError {
		
		bwExport = currencyService.exportCurrency(endDate, bwExport);
		bwExport = countryService.exportCountry(endDate, bwExport);
		bwExport = customerService.exportCustomer(endDate, bwExport);
		bwExport = addressService.exportAddress(endDate, bwExport);
		bwExport = categoryService.exportCategory(endDate, bwExport);
		bwExport = productService.exportProduct(endDate, bwExport);
	}	
	
	/**
	 * Export Axelor modules (Base, SaleOrder)
	 */
	@Override
	public Batch exportPrestShop(ZonedDateTime endDate, Batch batch) throws PrestaShopWebserviceException, TransformerException, IOException, ParserConfigurationException, SAXException, JAXBException, TransformerFactoryConfigurationError {
		
		this.exportAxelorBase(endDate);
		
		bwExport = orderService.exportOrder(endDate, bwExport);
		bwExport = detailService.exportOrderDetail(endDate, bwExport);
		File exportFile = closeLog();
		MetaFile exporMetatFile = metaFiles.upload(exportFile);
		batch.setPrestaShopBatchLog(exporMetatFile);
		return batch;
	}
	
	/**
	 * Close export log file
	 * 
	 * @return
	 * @throws IOException
	 */
	public File closeLog() throws IOException {
		bwExport.newLine();
		bwExport.write("-----------------------------------------------");
		bwExport.close();
		fwExport.close();
		return exportFile;
	}
	
}
