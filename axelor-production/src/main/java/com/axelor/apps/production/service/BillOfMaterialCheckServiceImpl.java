package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.util.Objects;

public class BillOfMaterialCheckServiceImpl implements BillOfMaterialCheckService {

  protected final ManufOrderRepository manufOrderRepository;

  @Inject
  public BillOfMaterialCheckServiceImpl(ManufOrderRepository manufOrderRepository) {
    this.manufOrderRepository = manufOrderRepository;
  }

  @Override
  public void checkUsedBom(BillOfMaterial billOfMaterial) throws AxelorException {
    Objects.requireNonNull(billOfMaterial);

    // Check usage in ManufOrder
    var anyManufOrder =
        manufOrderRepository
            .all()
            .filter("self.billOfMaterial = :billOfMaterial")
            .bind("billOfMaterial", billOfMaterial)
            .fetchOne();

    if (anyManufOrder != null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(ProductionExceptionMessage.CAN_NOT_REGENERATE_BOM_AS_ALREADY_IN_PRODUCTION));
    }
  }
}
