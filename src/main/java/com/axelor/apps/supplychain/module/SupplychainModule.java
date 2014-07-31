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
import com.axelor.apps.purchase.service.PurchaseOrderLineService;
import com.axelor.apps.purchase.service.PurchaseOrderLineServiceImpl;
import com.axelor.apps.purchase.service.PurchaseOrderServiceImpl;
import com.axelor.apps.sale.service.SaleOrderService;
import com.axelor.apps.sale.service.SaleOrderServiceImpl;
import com.axelor.apps.stock.service.LocationLineService;
import com.axelor.apps.stock.service.LocationLineServiceImpl;
import com.axelor.apps.stock.service.MinStockRulesService;
import com.axelor.apps.stock.service.MinStockRulesServiceImpl;
import com.axelor.apps.stock.service.StockMoveLineService;
import com.axelor.apps.stock.service.StockMoveLineServiceImpl;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.apps.stock.service.StockMoveServiceImpl;
import com.axelor.apps.supplychain.service.MinStockRulesServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.PurchaseOrderInvoiceService;
import com.axelor.apps.supplychain.service.PurchaseOrderInvoiceServiceImpl;
import com.axelor.apps.supplychain.service.PurchaseOrderLineServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.PurchaseOrderServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.SaleOrderInvoiceService;
import com.axelor.apps.supplychain.service.SaleOrderInvoiceServiceImpl;
import com.axelor.apps.supplychain.service.SaleOrderPurchaseService;
import com.axelor.apps.supplychain.service.SaleOrderPurchaseServiceImpl;
import com.axelor.apps.supplychain.service.SaleOrderServiceStockImpl;
import com.axelor.apps.supplychain.service.SaleOrderServiceSupplychainImpl;

@AxelorModuleInfo(name = "axelor-supplychain")
public class SupplychainModule extends AxelorModule {

    @Override
    protected void configure() {
    	bind(MinStockRulesService.class).to(MinStockRulesServiceImpl.class);
        bind(MinStockRulesServiceImpl.class).to(MinStockRulesServiceSupplychainImpl.class);
        bind(StockMoveService.class).to(StockMoveServiceImpl.class);
        bind(PurchaseOrderServiceImpl.class).to(PurchaseOrderServiceSupplychainImpl.class);
        bind(PurchaseOrderLineService.class).to(PurchaseOrderLineServiceImpl.class);
        bind(LocationLineService.class).to(LocationLineServiceImpl.class);
        bind(SaleOrderService.class).to(SaleOrderServiceStockImpl.class);
        bind(SaleOrderServiceImpl.class).to(SaleOrderServiceSupplychainImpl.class);
        bind(PurchaseOrderInvoiceService.class).to(PurchaseOrderInvoiceServiceImpl.class);
        bind(SaleOrderInvoiceService.class).to(SaleOrderInvoiceServiceImpl.class);
        bind(SaleOrderPurchaseService.class).to(SaleOrderPurchaseServiceImpl.class);
        bind(StockMoveLineService.class).to(StockMoveLineServiceImpl.class);
    }
}