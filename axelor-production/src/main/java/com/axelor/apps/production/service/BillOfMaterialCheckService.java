package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.BillOfMaterial;

public interface BillOfMaterialCheckService {

  void checkUsedBom(BillOfMaterial billOfMaterial) throws AxelorException;
}
