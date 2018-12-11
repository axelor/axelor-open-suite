/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2018 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.studio.service.importer;

import com.axelor.common.Inflector;
import com.axelor.exception.AxelorException;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.MetaModule;
import com.axelor.meta.db.repo.MetaFieldRepository;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.studio.service.CommonService;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.lang.invoke.MethodHandles;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModelImporterService {

  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Inject private MetaFieldRepository metaFieldRepo;

  @Inject private FieldImporterService fieldImporter;

  @Inject private MetaModelRepository metaModelRepo;

  @Inject private CommonService commonService;

  private ImporterService importerService;

  public void importModel(
      ImporterService importerService,
      Map<String, String> valMap,
      int rowNum,
      MetaModule metaModule)
      throws AxelorException {

    String name = valMap.get(CommonService.MODEL);
    if (name == null) {
      return;
    }

    this.importerService = importerService;

    String[] names = name.split("\\(");
    MetaModel model = getModel(names[0], metaModule);

    MetaModel nestedModel = null;
    String parentField = null;
    if (names.length > 1) {
      parentField = names[1].replace(")", "");
      nestedModel = importerService.getNestedModels(name);
      if (nestedModel == null) {
        nestedModel = createNestedModel(metaModule, model, parentField);
        importerService.addNestedModel(name, nestedModel);
      }
    }

    String[] basic = getBasic(valMap, parentField);

    MetaField metaField = null;
    if (CommonService.FIELD_TYPES.containsKey(basic[0]) && !basic[2].startsWith("$")) {
      if (nestedModel != null) {
        metaField = addField(basic, valMap, rowNum, nestedModel, metaModule);
      } else {
        metaField = addField(basic, valMap, rowNum, model, metaModule);
      }
    }

    if (!Strings.isNullOrEmpty(basic[0])
        && (!CommonService.IGNORE_TYPES.contains(basic[0]) || basic[0].equals("empty"))) {
      importerService.addView(model, basic, valMap, rowNum, metaField);
    }
  }

  private MetaField addField(
      String[] basic,
      Map<String, String> valMap,
      int rowNum,
      MetaModel model,
      MetaModule metaModule)
      throws AxelorException {

    Integer sequence = importerService.getFieldSeq(model.getId());
    MetaField metaField =
        fieldImporter.importField(valMap, rowNum, basic, model, metaModule, sequence);
    //    if (metaField.getCustomised()) {
    //      importerService.updateModuleMap(metaModule.getName(), model.getName(), basic[2]);
    //    }

    String addGrid = valMap.get(CommonService.GRID);
    if (addGrid != null && addGrid.equalsIgnoreCase("x")) {
      importerService.addGridField(metaModule.getName(), model.getName(), metaField);
    }

    return metaField;
  }

  private String[] getBasic(Map<String, String> valMap, String parentField) {

    String fieldType = valMap.get(CommonService.TYPE);
    String ref = null;
    String form = null;
    String grid = null;
    if (fieldType != null) {
      String[] fieldTypes = fieldType.split(",");
      String[] type = fieldTypes[0].split("\\(");
      fieldType = type[0];
      if (type.length > 1) {
        ref = type[1].replace(")", "");
      }
      if (fieldTypes.length > 1) {
        form = fieldTypes[1];
      }
      if (fieldTypes.length > 2) {
        grid = fieldTypes[2];
      }
    }

    if (CommonService.FR_MAP.containsKey(fieldType)) {
      fieldType = CommonService.FR_MAP.get(fieldType);
    }

    String name = valMap.get(CommonService.NAME);
    String title = valMap.get(CommonService.TITLE);
    String titleFr = valMap.get(CommonService.TITLE_FR);
    if (Strings.isNullOrEmpty(title)) {
      title = titleFr;
    }

    if (Strings.isNullOrEmpty(name)
        && !Strings.isNullOrEmpty(title)
        && !fieldType.equals("label")) {
      name = commonService.getFieldName(title);
    }

    return new String[] {fieldType, ref, name, title, parentField, form, grid};
  }

  private MetaModel createNestedModel(
      MetaModule module, MetaModel parentModel, String nestedField) {

    log.debug("Search for nested field: {}, model: {}", nestedField, parentModel.getName());

    MetaField metaField =
        metaFieldRepo
            .all()
            .filter("self.name = ?1 and self.metaModel = ?2", nestedField, parentModel)
            .fetchOne();

    if (metaField != null && metaField.getRelationship() != null) {
      log.debug("Field found with type: {}", metaField.getTypeName());
      return getModel(metaField.getTypeName(), module);
    }

    return parentModel;
  }

  /**
   * Set package name and fullName of model and save it.
   *
   * @param models List of MetaModels to save.
   * @return List of saved MetaModels.
   */
  @Transactional(rollbackOn = {Exception.class})
  public MetaModel getModel(String name, MetaModule module) {

    name = Inflector.getInstance().camelize(name);

    MetaModel model = metaModelRepo.findByName(name);

    importerService.updateModuleMap(module.getName(), name, null);

    if (model == null) {
      model = new MetaModel(name);
      //      model.setMetaModule(module);
    }

    if (model.getPackageName() == null) {
      String[] modules = module.getName().replace("axelor-", "").split("-");
      model.setPackageName("com.axelor.apps." + modules[0] + ".db");
      model.setFullName("com.axelor.apps." + modules[0] + ".db." + model.getName());
    }
    //    model.setCustomised(true);
    //    model.setEdited(true);

    return metaModelRepo.save(model);
  }
}
