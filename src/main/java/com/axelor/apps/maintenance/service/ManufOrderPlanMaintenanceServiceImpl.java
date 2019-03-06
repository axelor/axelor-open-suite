package com.axelor.apps.maintenance.service;

import com.axelor.apps.production.service.ManufOrderPlanServiceImpl;
import com.axelor.apps.production.service.operationorder.OperationOrderService;
import com.google.inject.persist.Transactional;
import com.axelor.apps.maintenance.db.MaintenanceRequest;
import com.axelor.apps.maintenance.db.repo.MaintenanceRequestRepository;
import com.axelor.apps.production.db.Machine;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.db.WorkCenter;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.List;
import org.optaplanner.examples.projectjobscheduling.domain.resource.GlobalResource;
import org.optaplanner.examples.projectjobscheduling.domain.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManufOrderPlanMaintenanceServiceImpl extends ManufOrderPlanServiceImpl {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Override
  @Transactional(rollbackOn = {AxelorException.class, Exception.class})
  public void optaPlan(List<ManufOrder> manufOrderListToPlan, boolean quickSolve)
      throws AxelorException {
    manufOrderListToPlan.removeIf(manufOrder -> manufOrder.getType().equals(2));
    super.optaPlan(manufOrderListToPlan, quickSolve);
  }

}
