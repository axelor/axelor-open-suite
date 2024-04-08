package com.axelor.apps.production.service.manuforder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.ProdProcess;
import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.service.operationorder.OperationOrderService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ManufOrderOperationServiceImpl implements ManufOrderOperationOrderService {

  protected final OperationOrderService operationOrderService;
  protected final ManufOrderRepository manufOrderRepo;

  @Inject
  public ManufOrderOperationServiceImpl(
      OperationOrderService operationOrderService, ManufOrderRepository manufOrderRepo) {
    this.operationOrderService = operationOrderService;
    this.manufOrderRepo = manufOrderRepo;
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void preFillOperations(ManufOrder manufOrder) throws AxelorException {

    BillOfMaterial billOfMaterial = manufOrder.getBillOfMaterial();

    if (manufOrder.getProdProcess() == null) {
      manufOrder.setProdProcess(billOfMaterial.getProdProcess());
    }
    ProdProcess prodProcess = manufOrder.getProdProcess();

    if (prodProcess != null && prodProcess.getProdProcessLineList() != null) {
      List<ProdProcessLine> sortedProdProcessLineList =
          prodProcess.getProdProcessLineList().stream()
              .sorted(Comparator.comparing(ProdProcessLine::getPriority))
              .collect(Collectors.toList());

      for (ProdProcessLine prodProcessLine : sortedProdProcessLineList) {
        manufOrder.addOperationOrderListItem(
            operationOrderService.createOperationOrder(manufOrder, prodProcessLine));
      }
    }

    manufOrderRepo.save(manufOrder);
  }

  @Override
  @Transactional
  public void updateOperationsName(ManufOrder manufOrder) {
    for (OperationOrder operationOrder : manufOrder.getOperationOrderList()) {
      operationOrder.setName(
          operationOrderService.computeName(
              manufOrder, operationOrder.getPriority(), operationOrder.getOperationName()));
    }
  }
}
