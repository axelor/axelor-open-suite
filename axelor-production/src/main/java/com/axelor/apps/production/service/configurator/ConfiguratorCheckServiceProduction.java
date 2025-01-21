package com.axelor.apps.production.service.configurator;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.BillOfMaterial;

public interface ConfiguratorCheckServiceProduction {
  void checkUsedBom(BillOfMaterial billOfMaterial) throws AxelorException;
}
