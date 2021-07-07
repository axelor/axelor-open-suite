package com.axelor.apps.sale.service.configurator;

import com.axelor.apps.sale.db.Configurator;
import com.axelor.apps.sale.db.ConfiguratorFormula;
import com.axelor.db.Model;
import com.axelor.meta.db.MetaJsonField;
import com.axelor.rpc.JsonContext;
import java.util.List;
import java.util.Map;

public interface ConfiguratorMetaJsonFieldService {

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
   * This method map a Model in json that need to be used in a OneToMany type. Map will be in the
   * form of <"id", model.id>
   *
   * @param model
   * @return
   */
  Map<String, Object> modelToJson(Model model);

  /**
   * Method that fill attr type fields of the targetObject of type Class with json string equivalent
   * of attrValueMap
   *
   * @param <T>
   * @param attrValueMap
   * @param type
   * @param targetObject
   */
  <T extends Model> void fillAttrs(
      Map<String, Map<String, Object>> attrValueMap, Class<T> type, T targetObject);

  /**
   * Method that re-map a Map that is representating a Model object. Map will be in the form of
   * <"id", model.id>
   *
   * @param map
   * @return
   */
  Map<String, Object> objectMapToJson(Map<String, Object> map);

  /**
   * Filter from indicators list indicator that matches one the following: - The indicator is a
   * "one-to-many" type && it is not in one the formula's metaJsonField
   *
   * @param configurator
   * @param indicators
   * @return a filtered indicator list
   */
  List<MetaJsonField> filterIndicators(Configurator configurator, List<MetaJsonField> indicators);
}
