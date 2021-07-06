package com.axelor.apps.sale.service.configurator;

import com.axelor.apps.sale.db.ConfiguratorFormula;
import com.axelor.db.Model;
import com.axelor.rpc.JsonContext;
import java.util.List;
import java.util.Map;

public interface ConfiguratorMapService {

  /**
   * Method that generate a Map for attrs fields (wich are storing customs fields). A map entry's
   * form is <attrNameField, mapOfCustomsFields>, with attrNameField the name of the attr field (for
   * example 'attr') and mapOfCustomsFields are entries with the form of <customFieldName, value>.
   * Only indicators that have name in form of "attrFieldName$fieldName_*" , with "_*" being
   * optional, will be treated Note : This method will consume indicators that are attr fields (i.e.
   * : will be removed from jsonIndicators)
   *
   * @param configurator
   * @param jsonIndicators
   * @return
   */
  Map<String, Map<String, Object>> generateAttrMap(
      List<? extends ConfiguratorFormula> formulas, JsonContext jsonIndicators);

  /**
   * This method map a model in json that need to be used in a OneToMany type. Map will be in the
   * form of <"id", model.id>
   *
   * @param model
   * @return
   */
  Map<String, Object> modelToJson(Model model);
}
