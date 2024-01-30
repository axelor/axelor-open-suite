package com.axelor.apps.production.service.manuforder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.ManufOrder;
import java.time.LocalDateTime;
import java.util.List;

public interface ManufOrderPlanService {

  ManufOrder plan(ManufOrder manufOrder) throws AxelorException;

  String planManufOrders(List<ManufOrder> manufOrderList) throws AxelorException;

  LocalDateTime computePlannedStartDateT(ManufOrder manufOrder);

  LocalDateTime computePlannedEndDateT(ManufOrder manufOrder);

  void updatePlannedDates(ManufOrder manufOrder, LocalDateTime plannedStartDateT)
      throws AxelorException;
}
