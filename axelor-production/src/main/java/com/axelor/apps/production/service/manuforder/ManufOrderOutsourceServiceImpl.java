package com.axelor.apps.production.service.manuforder;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.service.ProdProcessOutsourceService;
import com.google.inject.Inject;
import java.util.Objects;
import java.util.Optional;

public class ManufOrderOutsourceServiceImpl implements ManufOrderOutsourceService {

  protected ProdProcessOutsourceService prodProcessOutsourceService;

  @Inject
  public ManufOrderOutsourceServiceImpl(ProdProcessOutsourceService prodProcessOutsourceService) {
    this.prodProcessOutsourceService = prodProcessOutsourceService;
  }

  @Override
  public Optional<Partner> getOutsourcePartner(ManufOrder manufOrder) {
    Objects.requireNonNull(manufOrder);
    Objects.requireNonNull(manufOrder.getProdProcess());

    if (manufOrder.getOutsourcing() && manufOrder.getOutsourcingPartner() != null) {
      return Optional.of(manufOrder.getOutsourcingPartner());
    } else if (manufOrder.getOutsourcing() && manufOrder.getOutsourcingPartner() == null) {
      return prodProcessOutsourceService.getOutsourcePartner(manufOrder.getProdProcess());
    } else {
      return Optional.empty();
    }
  }

  @Override
  public boolean isOutsource(ManufOrder manufOrder) {
    return manufOrder.getOutsourcing();
  }
}
