package com.axelor.apps.supplychain.service.saleorder;

import static com.axelor.apps.sale.db.repo.SaleOrderRepository.DELIVERY_STATE_NOT_DELIVERED;
import static com.axelor.apps.sale.db.repo.SaleOrderRepository.INVOICING_STATE_NOT_INVOICED;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.studio.app.service.AppService;
import com.google.inject.Inject;
import java.math.BigDecimal;

public class SaleOrderCopySupplychainServiceImpl implements SaleOrderCopySupplychainService {

  protected final AppService appService;

  @Inject
  public SaleOrderCopySupplychainServiceImpl(AppService appService) {
    this.appService = appService;
  }

  @Override
  public void copySaleOrderSupplychainProcess(SaleOrder copy) {
    if (!appService.isApp("supplychain")) {
      return;
    }

    copy.setShipmentDate(null);
    copy.setDeliveryState(DELIVERY_STATE_NOT_DELIVERED);
    copy.setAmountInvoiced(null);
    copy.setInvoicingState(INVOICING_STATE_NOT_INVOICED);
    copy.setStockMoveList(null);

    if (copy.getSaleOrderLineList() != null) {
      for (SaleOrderLine saleOrderLine : copy.getSaleOrderLineList()) {
        saleOrderLine.setDeliveryState(null);
        saleOrderLine.setInvoicingState(null);
        saleOrderLine.setDeliveredQty(null);
        saleOrderLine.setAmountInvoiced(null);
        saleOrderLine.setInvoiced(null);
        saleOrderLine.setIsInvoiceControlled(null);
        saleOrderLine.setReservedQty(BigDecimal.ZERO);
      }
    }
  }
}
