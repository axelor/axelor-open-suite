package com.axelor.apps.sale.utils;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.inject.Beans;
import com.axelor.studio.ls.annotation.LinkScriptFunction;
import java.util.Map;

public class ConfiguratorLinkScriptSaleLibrary {

  @LinkScriptFunction("generateSaleOrderLine")
  public static SaleOrderLine generateSaleOrderLine(Map<String, Object> params)
      throws AxelorException {
    return Beans.get(ConfiguratorGeneratorLibrarySaleService.class)
        .createSaleOrderLineFromConfigurator(params);
  }
}
