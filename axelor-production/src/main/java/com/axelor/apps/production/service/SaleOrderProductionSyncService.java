package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;

public interface SaleOrderProductionSyncService {


    /**
     * This method will synchronize the sale order lines and sub sale order lines.
     * @param saleOrder
     * @throws AxelorException
     */
    void syncSaleOrderLineList(SaleOrder saleOrder) throws AxelorException;
}
