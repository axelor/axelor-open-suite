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
package com.axelor.apps.prestashop.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.prestashop.app.AppPrestaShopService;
import com.axelor.apps.prestashop.app.AppPrestaShopServiceImpl;
import com.axelor.apps.prestashop.service.exports.PrestaShopServiceExport;
import com.axelor.apps.prestashop.service.exports.PrestaShopServiceExportImpl;
import com.axelor.apps.prestashop.service.imports.PrestaShopServiceImport;
import com.axelor.apps.prestashop.service.imports.PrestaShopServiceImportImpl;

public class PrestaShopModule extends AxelorModule {

    @Override
    protected void configure() {
    	bind(AppPrestaShopService.class).to(AppPrestaShopServiceImpl.class);
        bind(PrestaShopServiceImport.class).to(PrestaShopServiceImportImpl.class);
        bind(PrestaShopServiceExport.class).to(PrestaShopServiceExportImpl.class);
    }
}