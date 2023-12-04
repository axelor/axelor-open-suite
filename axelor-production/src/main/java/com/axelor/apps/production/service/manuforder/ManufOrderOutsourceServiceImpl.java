package com.axelor.apps.production.service.manuforder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.ProdProcess;
import java.util.Objects;
import java.util.Optional;

public class ManufOrderOutsourceServiceImpl implements ManufOrderOutsourceService {

  @Override
  public Optional<Partner> getOutsourcePartner(ManufOrder manufOrder) throws AxelorException {
    Objects.requireNonNull(manufOrder);
    Objects.requireNonNull(manufOrder.getProdProcess());

    Optional<ProdProcess> optionalProdProcess = Optional.ofNullable(manufOrder.getProdProcess());
    if (optionalProdProcess.map(ProdProcess::getOutsourcing).orElse(false)) {
      return optionalProdProcess.map(ProdProcess::getSubcontractor);
    } else if (manufOrder.getOutsourcing()) {
      return Optional.ofNullable(manufOrder.getOutsourcingPartner());
    }
    return Optional.empty();
  }

  @Override
  public boolean isOutsource(ManufOrder manufOrder) {
    return manufOrder.getOutsourcing();
  }
}
