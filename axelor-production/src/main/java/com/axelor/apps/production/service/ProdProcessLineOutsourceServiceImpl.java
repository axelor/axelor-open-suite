package com.axelor.apps.production.service;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.production.db.ProdProcessLine;
import com.google.inject.Inject;
import java.util.Objects;
import java.util.Optional;

public class ProdProcessLineOutsourceServiceImpl implements ProdProcessLineOutsourceService {

  protected ProdProcessOutsourceService prodProcessOutsourceService;

  @Inject
  public ProdProcessLineOutsourceServiceImpl(
      ProdProcessOutsourceService prodProcessOutsourceService) {
    this.prodProcessOutsourceService = prodProcessOutsourceService;
  }

  @Override
  public Optional<Partner> getOutsourcePartner(ProdProcessLine prodProcessLine) {
    Objects.requireNonNull(prodProcessLine);
    Objects.requireNonNull(prodProcessLine.getProdProcess());

    if (prodProcessLine.getOutsourcing() && !prodProcessLine.getProdProcess().getOutsourcing()) {
      return Optional.ofNullable(prodProcessLine.getOutsourcingPartner());
    } else {
      return prodProcessOutsourceService.getOutsourcePartner(prodProcessLine.getProdProcess());
    }
  }
}
