package com.axelor.apps.base.utils;

import com.axelor.apps.base.db.Product;
import java.util.Map;

public interface ConfiguratorGeneratorLibraryService {
  Product createProductFromConfigurator(Map<String, Object> params);
}
