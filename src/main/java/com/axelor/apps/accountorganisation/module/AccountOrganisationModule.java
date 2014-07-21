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
package com.axelor.apps.accountorganisation.module;

import com.axelor.app.AxelorModule;
import com.axelor.app.AxelorModuleInfo;
import com.axelor.apps.accountorganisation.service.PurchaseOrderInvoiceServiceAccountOrganisationImpl;
import com.axelor.apps.accountorganisation.service.PurchaseOrderLineServiceAccountOrganisationImpl;
import com.axelor.apps.accountorganisation.service.PurchaseOrderServiceAccountOrganisationImpl;
import com.axelor.apps.accountorganisation.service.SaleOrderInvoiceServiceAccountOrganisationImpl;
import com.axelor.apps.accountorganisation.service.SaleOrderPurchaseServiceAccountOrganisationImpl;
import com.axelor.apps.accountorganisation.service.SaleOrderServiceAccountOrganisationImpl;
import com.axelor.apps.accountorganisation.service.StockMoveLineServiceAccountOrganisationImpl;
import com.axelor.apps.stock.service.StockMoveLineServiceImpl;
import com.axelor.apps.supplychain.service.PurchaseOrderInvoiceServiceImpl;
import com.axelor.apps.supplychain.service.PurchaseOrderLineServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.PurchaseOrderServiceSupplychainImpl;
import com.axelor.apps.supplychain.service.SaleOrderInvoiceServiceImpl;
import com.axelor.apps.supplychain.service.SaleOrderPurchaseServiceImpl;
import com.axelor.apps.supplychain.service.SaleOrderServiceSupplychainImpl;

@AxelorModuleInfo(name = "axelor-account-organisation")
public class AccountOrganisationModule extends AxelorModule {

    @Override
    protected void configure() {
        bind(PurchaseOrderLineServiceSupplychainImpl.class).to(PurchaseOrderLineServiceAccountOrganisationImpl.class);
        bind(PurchaseOrderServiceSupplychainImpl.class).to(PurchaseOrderServiceAccountOrganisationImpl.class);
        bind(SaleOrderServiceSupplychainImpl.class).to(SaleOrderServiceAccountOrganisationImpl.class);
        bind(PurchaseOrderInvoiceServiceImpl.class).to(PurchaseOrderInvoiceServiceAccountOrganisationImpl.class);
        bind(SaleOrderInvoiceServiceImpl.class).to(SaleOrderInvoiceServiceAccountOrganisationImpl.class);
        bind(SaleOrderPurchaseServiceImpl.class).to(SaleOrderPurchaseServiceAccountOrganisationImpl.class);
        bind(StockMoveLineServiceImpl.class).to(StockMoveLineServiceAccountOrganisationImpl.class);
    }
}