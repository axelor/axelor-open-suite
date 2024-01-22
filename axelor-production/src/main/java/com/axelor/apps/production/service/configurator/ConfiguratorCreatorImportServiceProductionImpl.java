/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2024 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.production.service.configurator;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.production.db.ConfiguratorBOM;
import com.axelor.apps.production.db.ConfiguratorProdProcess;
import com.axelor.apps.production.db.ConfiguratorProdProcessLine;
import com.axelor.apps.production.db.ConfiguratorProdProduct;
import com.axelor.apps.production.db.repo.ConfiguratorBOMRepository;
import com.axelor.apps.production.exceptions.ProductionExceptionMessage;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.sale.db.ConfiguratorCreator;
import com.axelor.apps.sale.service.configurator.ConfiguratorCreatorImportServiceImpl;
import com.axelor.apps.sale.service.configurator.ConfiguratorCreatorService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class ConfiguratorCreatorImportServiceProductionImpl
    extends ConfiguratorCreatorImportServiceImpl {

  private static int MAX_DEPTH = 50;

  @Inject
  public ConfiguratorCreatorImportServiceProductionImpl(
      ConfiguratorCreatorService configuratorCreatorService) {
    super(configuratorCreatorService);
  }

  /**
   * Update the changed attribute in all formula O2M. This implementation also update formulas in
   * configurator BOM and configurator prod process.
   *
   * @param creator
   * @param oldName
   * @param newName
   * @throws AxelorException
   */
  @Override
  protected void updateAttributeNameInFormulas(
      ConfiguratorCreator creator, String oldName, String newName) throws AxelorException {
    super.updateAttributeNameInFormulas(creator, oldName, newName);

    if (!Beans.get(AppProductionService.class).isApp("production")) {
      return;
    }
    ConfiguratorBOM configuratorBom = creator.getConfiguratorBom();
    if (configuratorBom != null) {
      updateAttributeNameInFormulas(configuratorBom, oldName, newName, 0);
    }
  }

  /**
   * Update attribute name in formulas for a configurator bom.
   *
   * @param configuratorBom
   * @param oldName
   * @param newName
   * @param counter used to count the recursive call.
   * @throws AxelorException if we got too many recursive call.
   */
  protected void updateAttributeNameInFormulas(
      ConfiguratorBOM configuratorBom, String oldName, String newName, int counter)
      throws AxelorException {
    if (counter > MAX_DEPTH) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(ProductionExceptionMessage.CONFIGURATOR_BOM_IMPORT_TOO_MANY_CALLS));
    }
    updateAllFormulaFields(configuratorBom, oldName, newName);
    ConfiguratorProdProcess configuratorProdProcess = configuratorBom.getConfiguratorProdProcess();
    if (configuratorProdProcess != null) {
      updateAttributeNameInFormulas(configuratorBom.getConfiguratorProdProcess(), oldName, newName);
    }

    // recursive call for child BOMs
    List<ConfiguratorBOM> childConfiguratorBomList =
        Beans.get(ConfiguratorBOMRepository.class)
            .all()
            .filter("self.parentConfiguratorBOM.id = :parentId")
            .bind("parentId", configuratorBom.getId())
            .fetch();
    if (childConfiguratorBomList != null) {
      for (ConfiguratorBOM childConfiguratorBom : childConfiguratorBomList) {
        updateAttributeNameInFormulas(childConfiguratorBom, oldName, newName, counter + 1);
      }
    }
  }

  /**
   * Update attribute name in formulas for a configurator prod process.
   *
   * @param configuratorBom
   * @param oldName
   * @param newName
   * @throws AxelorException
   */
  protected void updateAttributeNameInFormulas(
      ConfiguratorProdProcess configuratorProdProcess, String oldName, String newName)
      throws AxelorException {
    List<ConfiguratorProdProcessLine> configuratorProdProcessLines =
        configuratorProdProcess.getConfiguratorProdProcessLineList();
    if (configuratorProdProcessLines == null) {
      return;
    }
    for (ConfiguratorProdProcessLine configuratorProdProcessLine : configuratorProdProcessLines) {
      updateAllFormulaFields(configuratorProdProcessLine, oldName, newName);
      List<ConfiguratorProdProduct> confProdProductList =
          configuratorProdProcessLine.getConfiguratorProdProductList();
      if (CollectionUtils.isNotEmpty(confProdProductList)) {
        for (ConfiguratorProdProduct confProdProduct : confProdProductList) {
          updateAllFormulaFields(confProdProduct, oldName, newName);
        }
      }
    }
  }

  /**
   * Replace oldName by newName in all string fields of the given object.
   *
   * @param obj
   * @param oldName
   * @param newName
   * @throws AxelorException
   */
  protected void updateAllFormulaFields(Object obj, String oldName, String newName)
      throws AxelorException {
    List<Field> formulaFields =
        Arrays.stream(obj.getClass().getDeclaredFields())
            .filter(field -> field.getType().equals(String.class))
            .collect(Collectors.toList());
    for (Field field : formulaFields) {
      try {
        // call getter of the string field
        Object strFormula =
            new PropertyDescriptor(field.getName(), obj.getClass()).getReadMethod().invoke(obj);
        if (strFormula != null) {
          new PropertyDescriptor(field.getName(), obj.getClass())
              .getWriteMethod()
              .invoke(obj, ((String) strFormula).replace(oldName, newName));
        }
      } catch (IntrospectionException
          | IllegalAccessException
          | IllegalArgumentException
          | InvocationTargetException e) {
        // should not happen since we fetched fields from the class
        throw new AxelorException(e, TraceBackRepository.CATEGORY_INCONSISTENCY);
      }
    }
  }
}
