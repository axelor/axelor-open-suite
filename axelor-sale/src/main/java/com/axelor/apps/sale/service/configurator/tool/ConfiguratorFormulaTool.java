package com.axelor.apps.sale.service.configurator.tool;

import com.axelor.apps.sale.db.ConfiguratorFormula;
import com.axelor.apps.sale.db.repo.ConfiguratorFormulaRepository;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaJsonField;

/** Class with static method to be used in ConfiguratorFormula domain */
public class ConfiguratorFormulaTool {

  private ConfiguratorFormulaTool() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * Compute {@link ConfiguratorFormula#fieldName} of a formula from its field depending on its
   * type.
   */
  public static String computeFieldName(ConfiguratorFormula configuratorFormula) {
    Integer typeSelect = configuratorFormula.getTypeSelect();
    String freeIndicatorName = configuratorFormula.getFreeIndicatorName();
    if (typeSelect == null) {
      return "";
    } else if (typeSelect == ConfiguratorFormulaRepository.TYPE_PRODUCT
        || typeSelect == ConfiguratorFormulaRepository.TYPE_SALE_ORDER_LINE) {
      return computeFieldNameFromMetaField(
          configuratorFormula.getMetaField(), configuratorFormula.getMetaJsonField());
    } else if (typeSelect == ConfiguratorFormulaRepository.TYPE_INFO) {
      return freeIndicatorName;
    } else {
      return "";
    }
  }

  protected static String computeFieldNameFromMetaField(
      MetaField metaField, MetaJsonField metaJsonField) {
    if (metaField == null) {
      return "";
    } else if (metaJsonField != null) {
      return metaField.getName() + "$" + metaJsonField.getName();
    } else {
      return metaField.getName();
    }
  }
}
