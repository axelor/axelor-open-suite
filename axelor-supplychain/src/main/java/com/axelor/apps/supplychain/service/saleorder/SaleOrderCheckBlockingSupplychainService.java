package com.axelor.apps.supplychain.service.saleorder;

import com.axelor.apps.sale.db.SaleOrder;
import java.util.List;

public interface SaleOrderCheckBlockingSupplychainService {

  List<String> checkBlocking(SaleOrder saleOrder);
}
