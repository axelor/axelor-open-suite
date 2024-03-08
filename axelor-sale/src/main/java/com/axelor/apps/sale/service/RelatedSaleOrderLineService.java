package com.axelor.apps.sale.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.rpc.Context;

public interface RelatedSaleOrderLineService {

  void updateRelatedSOLinesOnPriceChange(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException;

  void updateRelatedSOLinesOnQtyChange(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException;

  void updateRelatedOrderLines(SaleOrder saleOrder) throws AxelorException;

  SaleOrderLine setLineIndex(SaleOrderLine saleOrderLine, Context context);

  SaleOrderLine updateOnSaleOrderLineListChange(SaleOrderLine saleOrderLine);

  void populateSOLines(SaleOrder saleOrder) throws AxelorException;
}
