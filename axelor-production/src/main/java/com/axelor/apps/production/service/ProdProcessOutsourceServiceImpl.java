package com.axelor.apps.production.service;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.production.db.ProdProcess;
import java.util.Objects;
import java.util.Optional;

public class ProdProcessOutsourceServiceImpl implements ProdProcessOutsourceService {

  @Override
  public Optional<Partner> getOutsourcePartner(ProdProcess prodProcess) {
    Objects.requireNonNull(prodProcess);

    if (prodProcess.getOutsourcing()) {
      return Optional.ofNullable(prodProcess.getSubcontractor());
    }

    return Optional.empty();
  }
}
