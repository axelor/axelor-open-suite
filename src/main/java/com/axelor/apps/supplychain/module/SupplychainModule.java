/**
 * Axelor Business Solutions
 *
 * Copyright (C) 2012-2014 Axelor (<http://axelor.com>).
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
package com.axelor.apps.supplychain.module;

import com.axelor.app.AxelorModule;
import com.axelor.app.AxelorModuleInfo;
import com.axelor.apps.purchase.service.PurchaseOrderServiceImpl;
import com.axelor.apps.sale.service.SaleOrderServiceImpl;
import com.axelor.apps.stock.service.MinStockRulesServiceImpl;
import com.axelor.apps.supplychain.service.MinStockRulesServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.PurchaseOrderServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.SaleOrderServiceStockImpl;

@AxelorModuleInfo(name = "axelor-supplychain")
public class SupplychainModule extends AxelorModule {

    @Override
    protected void configure() {
        bind(MinStockRulesServiceImpl.class).to(MinStockRulesServiceSupplychainImpl.class);
        bind(PurchaseOrderServiceImpl.class).to(PurchaseOrderServiceSupplychainImpl.class);
        bind(SaleOrderServiceImpl.class).to(SaleOrderServiceStockImpl.class);
    }
}