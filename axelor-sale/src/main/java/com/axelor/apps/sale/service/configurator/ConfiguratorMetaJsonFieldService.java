package com.axelor.apps.sale.service.configurator;

import com.axelor.apps.sale.db.ConfiguratorFormula;
import com.axelor.db.Model;
import com.axelor.rpc.JsonContext;
import java.util.List;

public interface ConfiguratorMetaJsonFieldService {

  /**
   * Method that fill attrs type fields of the targetObject of type Class with a json string wich
   * contains attr customs fields. It needs the list of formulas used to create the jsonIndicators
   * and the jsonIndicators themselves.
   *
   * @param list
   * @param jsonIndicators
   * @param type
   * @param targetObject
   */
  <T extends Model> void fillAttrs(
      List<? extends ConfiguratorFormula> formulas,
      JsonContext jsonIndicators,
      Class<T> type,
      T targetObject);
}
