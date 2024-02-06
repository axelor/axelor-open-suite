package com.axelor.apps.production.service.manufacturingoperation;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.production.db.ManufacturingOperation;
import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.service.ProdProcessLineOutsourceService;
import com.axelor.apps.production.service.manuforder.ManufOrderOutsourceService;
import com.google.inject.Inject;
import java.util.Objects;
import java.util.Optional;

public class ManufacturingOperationOutsourceServiceImpl
    implements ManufacturingOperationOutsourceService {

  protected ProdProcessLineOutsourceService prodProcessLineOutsourceService;
  protected ManufOrderOutsourceService manufOrderOutsourceService;

  @Inject
  public ManufacturingOperationOutsourceServiceImpl(
      ProdProcessLineOutsourceService prodProcessLineOutsourceService,
      ManufOrderOutsourceService manufOrderOutsourceService) {
    this.prodProcessLineOutsourceService = prodProcessLineOutsourceService;
    this.manufOrderOutsourceService = manufOrderOutsourceService;
  }

  @Override
  public Optional<Partner> getOutsourcePartner(ManufacturingOperation manufacturingOperation) {
    Objects.requireNonNull(manufacturingOperation);
    Objects.requireNonNull(manufacturingOperation.getManufOrder());

    // Fetching from manufOrder
    if (manufacturingOperation.getOutsourcing()
        && manufacturingOperation.getManufOrder().getOutsourcing()) {
      return manufOrderOutsourceService.getOutsourcePartner(manufacturingOperation.getManufOrder());
      // Fetching from prodProcessLine or itself
    } else if (manufacturingOperation.getOutsourcing()
        && !manufacturingOperation.getManufOrder().getOutsourcing()) {
      ProdProcessLine prodProcessLine = manufacturingOperation.getProdProcessLine();
      if ((prodProcessLine.getOutsourcing() || prodProcessLine.getOutsourcable())
          && manufacturingOperation.getOutsourcingPartner() == null) {
        return prodProcessLineOutsourceService.getOutsourcePartner(prodProcessLine);
      } else {
        return Optional.ofNullable(manufacturingOperation.getOutsourcingPartner());
      }
    }
    return Optional.empty();
  }

  @Override
  public boolean getUseLineInGeneratedPO(ManufacturingOperation manufacturingOperation) {
    Objects.requireNonNull(manufacturingOperation);
    Objects.requireNonNull(manufacturingOperation.getProdProcessLine());

    ProdProcessLine prodProcessLine = manufacturingOperation.getProdProcessLine();

    if (manufacturingOperation.getManufOrder().getOutsourcing()
        || prodProcessLine.getOutsourcing()
        || manufacturingOperation.getOutsourcing()
        || (prodProcessLine.getOutsourcable() && manufacturingOperation.getOutsourcing())) {
      return prodProcessLine.getUseLineInGeneratedPurchaseOrder();
    }
    return false;
  }
}
