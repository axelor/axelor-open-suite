package com.axelor.apps.base.utils;

import com.axelor.apps.base.db.Product;
import com.axelor.inject.Beans;
import com.axelor.studio.ls.annotation.LinkScriptFunction;
import java.util.Map;

public class ConfiguratorLinkScriptLibrary {

  @LinkScriptFunction("createProduct")
  public static Product createProduct(Map<String, Object> params) {
    return Beans.get(ConfiguratorGeneratorLibraryService.class)
        .createProductFromConfigurator(params);
  }
}
