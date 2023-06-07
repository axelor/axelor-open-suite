package com.axelor.apps.production.db.repo;

import com.axelor.apps.production.db.BillOfMaterialLine;

public class BillOfMaterialLineManagementRepository extends BillOfMaterialLineRepository {

  @Override
  public BillOfMaterialLine copy(BillOfMaterialLine entity, boolean deep) {

    BillOfMaterialLine copy = super.copy(entity, deep);

    return copy;
  }
}
