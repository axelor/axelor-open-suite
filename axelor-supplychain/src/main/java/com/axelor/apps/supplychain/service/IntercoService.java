/**
 * Axelor Business Solutions
 * <p>
 * Copyright (C) 2017 Axelor (<http://axelor.com>).
 * <p>
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.db.Company;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.exception.AxelorException;

public interface IntercoService {

    /**
     * Given a purchase order, generate the sale order counterpart for
     * the other company
     * @param purchaseOrder
     * @return the generated sale order
     */
    SaleOrder generateIntercoSaleFromPurchase(PurchaseOrder purchaseOrder);

    /**
     * Given a sale order, generate the sale order counterpart for
     * the other company
     * @param saleOrder
     * @return the generated purchase order
     */
    PurchaseOrder generateIntercoPurchaseFromSale(SaleOrder saleOrder) throws AxelorException;

    /**
     * Find the interco company from the sale order
     * @param saleOrder
     * @return
     */
    Company findIntercoCompany(SaleOrder saleOrder);

    /**
     * Find the interco company from the purchase order
     * @param purchaseOrder
     * @return
     */
    Company findIntercoCompany(PurchaseOrder purchaseOrder);
}
