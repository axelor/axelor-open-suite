package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Address;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.util.List;

public interface SaleOrderDeliveryAddressService {

  List<SaleOrderLine> updateSaleOrderLinesDeliveryAddress(SaleOrder saleOrder);

  Address getDeliveryAddress(SaleOrder saleOrder, List<SaleOrderLine> saleOrderLineList);

  void checkSaleOrderLinesDeliveryAddress(List<SaleOrderLine> saleOrderLineList)
      throws AxelorException;
}
