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

import com.axelor.apps.base.db.AppPrestashop;
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
	public void exportAxelorBase(AppPrestashop appConfig, ZonedDateTime endDate) throws PrestaShopWebserviceException, TransformerException, IOException, ParserConfigurationException, SAXException, JAXBException, TransformerFactoryConfigurationError {
		currencyService.exportCurrency(appConfig, endDate, bwExport);
		countryService.exportCountry(appConfig, endDate, bwExport);
		customerService.exportCustomer(appConfig, endDate, bwExport);
		addressService.exportAddress(appConfig, endDate, bwExport);
		categoryService.exportCategory(appConfig, endDate, bwExport);
		productService.exportProduct(appConfig, endDate, bwExport);
	}

	/**
	 * Export Axelor modules (Base, SaleOrder)
	 */
	@Override
	public void export(AppPrestashop appConfig, ZonedDateTime endDate, Batch batch) throws PrestaShopWebserviceException, TransformerException, IOException, ParserConfigurationException, SAXException, JAXBException, TransformerFactoryConfigurationError {
		exportAxelorBase(appConfig, endDate);

		orderService.exportOrder(appConfig, endDate, bwExport);
		detailService.exportOrderDetail(appConfig, endDate, bwExport);
		File exportFile = closeLog();
		MetaFile exporMetatFile = metaFiles.upload(exportFile);
		batch.setPrestaShopBatchLog(exporMetatFile);
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
