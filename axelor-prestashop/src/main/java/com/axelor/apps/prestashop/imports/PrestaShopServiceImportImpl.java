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
package com.axelor.apps.prestashop.imports;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;

import com.axelor.apps.base.db.Batch;
import com.axelor.apps.prestashop.imports.service.ImportAddressService;
import com.axelor.apps.prestashop.imports.service.ImportCategoryService;
import com.axelor.apps.prestashop.imports.service.ImportCountryService;
import com.axelor.apps.prestashop.imports.service.ImportCurrencyService;
import com.axelor.apps.prestashop.imports.service.ImportCustomerService;
import com.axelor.apps.prestashop.imports.service.ImportOrderDetailService;
import com.axelor.apps.prestashop.imports.service.ImportOrderService;
import com.axelor.apps.prestashop.imports.service.ImportProductService;
import com.axelor.apps.prestashop.service.library.PrestaShopWebserviceException;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.inject.Inject;

import wslite.json.JSONException;

public class PrestaShopServiceImportImpl implements PrestaShopServiceImport {
	private MetaFiles metaFiles;
	private ImportCurrencyService currencyService;
	private ImportCountryService countryService;
	private ImportCustomerService customerService;
	private ImportAddressService addressService;
	private ImportCategoryService categoryService;
	private ImportProductService productService;
	private ImportOrderService orderService;
	private ImportOrderDetailService orderDetailService;

	private File importFile; // FIXME

	@Inject
	public PrestaShopServiceImportImpl(MetaFiles metaFiles, ImportCurrencyService currencyService, ImportCountryService countryService,
			ImportCustomerService customerService, ImportAddressService addressService,
			ImportCategoryService categoryService, ImportProductService productService, ImportOrderService orderService,
			ImportOrderDetailService orderDetailService) throws IOException {
		super();
		this.metaFiles = metaFiles;
		this.currencyService = currencyService;
		this.countryService = countryService;
		this.customerService = customerService;
		this.addressService = addressService;
		this.categoryService = categoryService;
		this.productService = productService;
		this.orderService = orderService;
		this.orderDetailService = orderDetailService;

		importFile = File.createTempFile("Import Log", ".txt");
		fwImport = new FileWriter(importFile);
		bwImport = new BufferedWriter(fwImport);
	}

	FileWriter fwImport = null;
	BufferedWriter bwImport = null;

	Integer totalDone = 0;
	Integer totalAnomaly = 0;

	/**
	 * Import base module from prestashop
	 *
	 * @throws IOException
	 * @throws PrestaShopWebserviceException
	 * @throws TransformerException
	 * @throws JAXBException
	 * @throws JSONException
	 */
	public void importAxelorBase() throws IOException, PrestaShopWebserviceException, TransformerException, JAXBException, JSONException {
		bwImport = currencyService.importCurrency(bwImport);
		bwImport = countryService.importCountry(bwImport);
		bwImport = customerService.importCustomer(bwImport);
		bwImport = addressService.importAddress(bwImport);
		bwImport = categoryService.importCategory(bwImport);
		bwImport = productService.importProduct(bwImport);
	}

	/**
	 * Import Axelor modules (Base, SaleOrder)
	 */
	@Override
	public Batch importPrestShop(Batch batch) throws IOException, PrestaShopWebserviceException, TransformerException, JAXBException, JSONException {
		try {
			importAxelorBase();
			bwImport = orderService.importOrder(bwImport);
			bwImport = orderDetailService.importOrderDetail(bwImport);
		} finally {
			closeLog();
			MetaFile importMetaFile = metaFiles.upload(importFile);
			batch.setPrestaShopBatchLog(importMetaFile);
		}
		return batch;
	}

	/**
	 * Close import log file
	 *
	 * @throws IOException
	 */
	public void closeLog() throws IOException {
		bwImport.newLine();
		bwImport.write("-----------------------------------------------");
		bwImport.close();
		fwImport.close();
	}
}
