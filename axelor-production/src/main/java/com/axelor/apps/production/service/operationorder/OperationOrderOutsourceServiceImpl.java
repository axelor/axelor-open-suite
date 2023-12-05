package com.axelor.apps.production.service.operationorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.service.ProdProcessLineOutsourceService;
import com.axelor.apps.production.service.manuforder.ManufOrderOutsourceService;
import com.google.inject.Inject;

import java.util.Objects;
import java.util.Optional;

public class OperationOrderOutsourceServiceImpl implements OperationOrderOutsourceService {

  protected ProdProcessLineOutsourceService prodProcessLineOutsourceService;
  protected ManufOrderOutsourceService manufOrderOutsourceService;

  @Inject
  public OperationOrderOutsourceServiceImpl(
      ProdProcessLineOutsourceService prodProcessLineOutsourceService,
      ManufOrderOutsourceService manufOrderOutsourceService) {
    this.prodProcessLineOutsourceService = prodProcessLineOutsourceService;
    this.manufOrderOutsourceService = manufOrderOutsourceService;
  }

  @Override
  public Optional<Partner> getOutsourcePartner(OperationOrder operationOrder)
      throws AxelorException {
    Objects.requireNonNull(operationOrder);

    Optional<Partner> optProdProcessLinePartner =
        prodProcessLineOutsourceService.getOutsourcePartner(operationOrder.getProdProcessLine());

    Optional<Partner> optManufOrderPartner =
            manufOrderOutsourceService.getOutsourcePartner(operationOrder.getManufOrder());


    if (optProdProcessLinePartner.isPresent()) {
      return optProdProcessLinePartner;
    } else if (optManufOrderPartner.isPresent()) {
      return optManufOrderPartner;
    } else if (operationOrder.getOutsourcing()) {
      return Optional.ofNullable(operationOrder.getOutsourcingPartner());
    }
    return Optional.empty();
  }
}
