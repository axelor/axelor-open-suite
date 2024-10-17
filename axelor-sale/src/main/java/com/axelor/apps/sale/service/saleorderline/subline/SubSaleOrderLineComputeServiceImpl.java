package com.axelor.apps.sale.service.saleorderline.subline;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineComputeService;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class SubSaleOrderLineComputeServiceImpl implements SubSaleOrderLineComputeService {

  protected final SaleOrderLineComputeService saleOrderLineComputeService;

  @Inject
  public SubSaleOrderLineComputeServiceImpl(
      SaleOrderLineComputeService saleOrderLineComputeService) {
    this.saleOrderLineComputeService = saleOrderLineComputeService;
  }

  @Override
  public BigDecimal computeSumSubLineList(
      List<SaleOrderLine> subSaleOrderLineList, SaleOrder saleOrder) throws AxelorException {
    for (SaleOrderLine subSaleOrderLine : subSaleOrderLineList) {
      List<SaleOrderLine> subSubSaleOrderLineList = subSaleOrderLine.getSubSaleOrderLineList();
      if (CollectionUtils.isNotEmpty(subSubSaleOrderLineList)) {
        subSaleOrderLine.setPrice(computeSumSubLineList(subSubSaleOrderLineList, saleOrder));
        saleOrderLineComputeService.computeValues(saleOrder, subSaleOrderLine);
      }
    }
    return subSaleOrderLineList.stream()
        .map(SaleOrderLine::getExTaxTotal)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }
}
