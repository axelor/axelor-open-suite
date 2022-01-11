package com.axelor.apps.production.service.configurator;

import com.axelor.apps.production.db.ConfiguratorProdProduct;
import com.axelor.apps.production.db.ProdProduct;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.JsonContext;

public interface ConfiguratorProdProductService {

  /**
   * Generate a prod product from a configurator prod product and a JsonContext holding the custom
   * values
   *
   * @param confProdProcessLine
   * @param attributes
   * @return
   */
  ProdProduct generateProdProduct(ConfiguratorProdProduct confProdProduct, JsonContext attributes)
      throws AxelorException;
}
