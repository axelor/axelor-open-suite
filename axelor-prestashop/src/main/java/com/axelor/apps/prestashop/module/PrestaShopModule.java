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
package com.axelor.apps.prestashop.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.prestashop.app.AppPrestaShopService;
import com.axelor.apps.prestashop.app.AppPrestaShopServiceImpl;
import com.axelor.apps.prestashop.exports.PrestaShopServiceExport;
import com.axelor.apps.prestashop.exports.PrestaShopServiceExportImpl;
import com.axelor.apps.prestashop.exports.service.ExportAddressService;
import com.axelor.apps.prestashop.exports.service.ExportAddressServiceImpl;
import com.axelor.apps.prestashop.exports.service.ExportCategoryService;
import com.axelor.apps.prestashop.exports.service.ExportCategoryServiceImpl;
import com.axelor.apps.prestashop.exports.service.ExportCountryService;
import com.axelor.apps.prestashop.exports.service.ExportCountryServiceImpl;
import com.axelor.apps.prestashop.exports.service.ExportCurrencyService;
import com.axelor.apps.prestashop.exports.service.ExportCurrencyServiceImpl;
import com.axelor.apps.prestashop.exports.service.ExportCustomerService;
import com.axelor.apps.prestashop.exports.service.ExportCustomerServiceImpl;
import com.axelor.apps.prestashop.exports.service.ExportOrderService;
import com.axelor.apps.prestashop.exports.service.ExportOrderServiceImpl;
import com.axelor.apps.prestashop.exports.service.ExportProductService;
import com.axelor.apps.prestashop.exports.service.ExportProductServiceImpl;
import com.axelor.apps.prestashop.imports.PrestaShopServiceImport;
import com.axelor.apps.prestashop.imports.PrestaShopServiceImportImpl;
import com.axelor.apps.prestashop.imports.service.ImportAddressService;
import com.axelor.apps.prestashop.imports.service.ImportAddressServiceImpl;
import com.axelor.apps.prestashop.imports.service.ImportCategoryService;
import com.axelor.apps.prestashop.imports.service.ImportCategoryServiceImpl;
import com.axelor.apps.prestashop.imports.service.ImportCountryService;
import com.axelor.apps.prestashop.imports.service.ImportCountryServiceImpl;
import com.axelor.apps.prestashop.imports.service.ImportCurrencyService;
import com.axelor.apps.prestashop.imports.service.ImportCurrencyServiceImpl;
import com.axelor.apps.prestashop.imports.service.ImportCustomerService;
import com.axelor.apps.prestashop.imports.service.ImportCustomerServiceImpl;
import com.axelor.apps.prestashop.imports.service.ImportOrderDetailService;
import com.axelor.apps.prestashop.imports.service.ImportOrderDetailServiceImpl;
import com.axelor.apps.prestashop.imports.service.ImportOrderService;
import com.axelor.apps.prestashop.imports.service.ImportOrderServiceImpl;
import com.axelor.apps.prestashop.imports.service.ImportProductService;
import com.axelor.apps.prestashop.imports.service.ImportProductServiceImpl;

public class PrestaShopModule extends AxelorModule {

    @Override
    protected void configure() {
    	bind(AppPrestaShopService.class).to(AppPrestaShopServiceImpl.class);
        bind(PrestaShopServiceImport.class).to(PrestaShopServiceImportImpl.class);
        bind(PrestaShopServiceExport.class).to(PrestaShopServiceExportImpl.class);
        bind(ExportCurrencyService.class).to(ExportCurrencyServiceImpl.class);
        bind(ExportCountryService.class).to(ExportCountryServiceImpl.class);
        bind(ExportCustomerService.class).to(ExportCustomerServiceImpl.class);
        bind(ExportAddressService.class).to(ExportAddressServiceImpl.class);
        bind(ExportCategoryService.class).to(ExportCategoryServiceImpl.class);
        bind(ExportProductService.class).to(ExportProductServiceImpl.class);
        bind(ExportOrderService.class).to(ExportOrderServiceImpl.class);

        bind(ImportCurrencyService.class).to(ImportCurrencyServiceImpl.class);
        bind(ImportCountryService.class).to(ImportCountryServiceImpl.class);
        bind(ImportCustomerService.class).to(ImportCustomerServiceImpl.class);
        bind(ImportAddressService.class).to(ImportAddressServiceImpl.class);
        bind(ImportCategoryService.class).to(ImportCategoryServiceImpl.class);
        bind(ImportProductService.class).to(ImportProductServiceImpl.class);
        bind(ImportOrderService.class).to(ImportOrderServiceImpl.class);
        bind(ImportOrderDetailService.class).to(ImportOrderDetailServiceImpl.class);
    }
}