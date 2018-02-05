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
	
	@Inject
	private MetaFiles metaFiles;
	
	@Inject
	private ImportCurrencyService currencyService;
	
	@Inject
	private ImportCountryService countryService;
	
	@Inject
	private ImportCustomerService customerService;
	
	@Inject
	private ImportAddressService addressService;
	
	@Inject
	private ImportCategoryService categoryService;
	
	@Inject
	private ImportProductService productService;
	
	@Inject
	private ImportOrderService orderService;
	
	@Inject
	private ImportOrderDetailService orderDetailService;
	
	File importFile = File.createTempFile("Import Log", ".txt");
	FileWriter fwImport = null;
	BufferedWriter bwImport = null;
	
    Integer totalDone = 0;
    Integer totalAnomaly = 0;
	
    /**
	 * Initialize constructor.  
	 * 
	 * @throws IOException
	 */
    public PrestaShopServiceImportImpl() throws IOException {
    	
    	fwImport = new FileWriter(importFile);
    	bwImport = new BufferedWriter(fwImport);
	}
    
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
    	
    	this.importAxelorBase();
    	bwImport = orderService.importOrder(bwImport);
    	bwImport = orderDetailService.importOrderDetail(bwImport);
    	
		File importFile = closeLog();
		MetaFile importMetaFile = metaFiles.upload(importFile);
		batch.setPrestaShopBatchLog(importMetaFile);
		return batch;
	}
    
    /**
	 * Close import log file
	 * 
	 * @return
	 * @throws IOException
	 */
	public File closeLog() throws IOException {
		bwImport.newLine();
		bwImport.write("-----------------------------------------------");
		bwImport.close();
		fwImport.close();
		return importFile;
	}
	
}
