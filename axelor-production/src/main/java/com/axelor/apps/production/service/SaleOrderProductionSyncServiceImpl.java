package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import com.google.inject.Inject;

import java.util.Objects;

public class SaleOrderProductionSyncServiceImpl implements SaleOrderProductionSyncService {

    protected final SaleOrderLineBomService saleOrderLineBomService;

    @Inject
    public SaleOrderProductionSyncServiceImpl(SaleOrderLineBomService saleOrderLineBomService) {
        this.saleOrderLineBomService = saleOrderLineBomService;
    }

    @Override
    public void syncSaleOrderLineList(SaleOrder saleOrder) throws AxelorException {
        Objects.requireNonNull(saleOrder);




    }
}
