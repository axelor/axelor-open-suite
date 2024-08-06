/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.supplierportal.module;

import com.axelor.app.AxelorModule;
import com.axelor.apps.supplierportal.service.ProductSupplierService;
import com.axelor.apps.supplierportal.service.ProductSupplierServiceImpl;
import com.axelor.apps.supplierportal.service.SupplierViewService;
import com.axelor.apps.supplierportal.service.SupplierViewServiceImpl;
import com.axelor.apps.supplierportal.service.app.AppSupplierPortalService;
import com.axelor.apps.supplierportal.service.app.AppSupplierPortalServiceImpl;

public class SupplierPortalModule extends AxelorModule {

  @Override
  protected void configure() {
    bind(AppSupplierPortalService.class).to(AppSupplierPortalServiceImpl.class);
    bind(SupplierViewService.class).to(SupplierViewServiceImpl.class);
    bind(ProductSupplierService.class).to(ProductSupplierServiceImpl.class);
  }
}
