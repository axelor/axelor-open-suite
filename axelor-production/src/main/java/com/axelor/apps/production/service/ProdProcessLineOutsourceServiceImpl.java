package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.production.db.ProdProcess;
import com.axelor.apps.production.db.ProdProcessLine;
import java.util.Objects;
import java.util.Optional;

public class ProdProcessLineOutsourceServiceImpl implements ProdProcessLineOutsourceService {
  @Override
  public Optional<Partner> getOutsourcePartner(ProdProcessLine prodProcessLine)
      throws AxelorException {
    Objects.requireNonNull(prodProcessLine);
    Objects.requireNonNull(prodProcessLine.getProdProcess());

    Optional<ProdProcess> optionalProdProcess =
        Optional.ofNullable(prodProcessLine.getProdProcess());
    if (optionalProdProcess.map(ProdProcess::getOutsourcing).orElse(false)) {
      return optionalProdProcess.map(ProdProcess::getSubcontractor);
    } else if (prodProcessLine.getOutsourcing()) {
      return Optional.ofNullable(prodProcessLine.getOutsourcingPartner());
    }

    return Optional.empty();
  }
}
