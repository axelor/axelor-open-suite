package com.axelor.apps.production.service.operationorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.service.ProdProcessLineOutsourceService;
import com.google.inject.Inject;
import java.util.Objects;
import java.util.Optional;

public class OperationOrderOutsourceServiceImpl implements OperationOrderOutsourceService {

  protected ProdProcessLineOutsourceService prodProcessLineOutsourceService;

  @Inject
  public OperationOrderOutsourceServiceImpl(
      ProdProcessLineOutsourceService prodProcessLineOutsourceService) {
    this.prodProcessLineOutsourceService = prodProcessLineOutsourceService;
  }

  @Override
  public Optional<Partner> getOutsourcePartner(OperationOrder operationOrder)
      throws AxelorException {
    Objects.requireNonNull(operationOrder);

    Optional<Partner> optProdProcessLinePartner =
        prodProcessLineOutsourceService.getOutsourcePartner(operationOrder.getProdProcessLine());

    if (optProdProcessLinePartner.isPresent()) {
      return optProdProcessLinePartner;
    } else if (operationOrder.getOutsourcing()) {
      return Optional.ofNullable(operationOrder.getOutsourcingPartner());
    }
    return Optional.empty();
  }
}
