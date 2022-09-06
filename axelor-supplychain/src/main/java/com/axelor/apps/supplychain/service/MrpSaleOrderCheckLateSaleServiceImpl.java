package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.supplychain.db.MrpLineType;
import com.axelor.apps.supplychain.db.repo.MrpLineTypeRepository;
import com.google.inject.Inject;
import java.time.LocalDate;

public class MrpSaleOrderCheckLateSaleServiceImpl implements MrpSaleOrderCheckLateSaleService {

  protected AppBaseService appBaseService;

  @Inject
  public MrpSaleOrderCheckLateSaleServiceImpl(AppBaseService appBaseService) {
    this.appBaseService = appBaseService;
  }

  @Override
  public boolean checkLateSalesParameter(SaleOrderLine saleOrderLine, MrpLineType mrpLineType) {
    // Determine deliveryDate
    LocalDate deliveryDate = saleOrderLine.getEstimatedDelivDate();
    if (deliveryDate == null) {
      deliveryDate = saleOrderLine.getSaleOrder().getDeliveryDate();
    }
    if (deliveryDate == null) {
      deliveryDate = saleOrderLine.getDesiredDelivDate();
    }

    // Determine if a line should be created
    if (deliveryDate == null && !mrpLineType.getIncludeElementWithoutDate()) {
      return false;
    }
    LocalDate todayDate = appBaseService.getTodayDate(saleOrderLine.getSaleOrder().getCompany());
    if (mrpLineType.getLateSalesSelect() == MrpLineTypeRepository.LATE_SALES_EXCLUDED) {
      if (deliveryDate != null && deliveryDate.isBefore(todayDate)) {
        return false;
      }
    } else if (mrpLineType.getLateSalesSelect() == MrpLineTypeRepository.LATE_SALES_ONLY) {
      if (deliveryDate == null || !deliveryDate.isBefore(todayDate)) {
        return false;
      }
    }

    return true;
  }
}
