package com.axelor.apps.sale.service.saleorderline.subline;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.math.BigDecimal;
import java.util.List;

public interface SubSaleOrderLineComputeService {

  BigDecimal computeSumSubLineList(List<SaleOrderLine> subSaleOrderLineList, SaleOrder saleOrder)
      throws AxelorException;
}
