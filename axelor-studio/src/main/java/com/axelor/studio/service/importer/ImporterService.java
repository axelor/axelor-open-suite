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

import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.MetaModule;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.studio.db.ActionBuilder;
import com.axelor.studio.db.ActionBuilderView;
import com.axelor.studio.db.ViewBuilder;
import com.axelor.studio.db.ViewItem;
import com.axelor.studio.db.repo.ActionBuilderRepository;
import com.axelor.studio.db.repo.ViewItemRepository;
import com.axelor.studio.service.CommonService;
import com.axelor.studio.service.ConfigurationService;
import com.axelor.studio.service.exporter.ExporterService;
import com.axelor.studio.service.validator.ValidatorService;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImporterService {

  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private Map<String, Map<String, List<String>>> moduleMap;

  private Map<String, Map<String, List<MetaField>>> gridViewMap;

  private Map<Long, Integer> fieldSeqMap;

  private Map<String, MetaModel> nestedModels;

  private boolean replace = false;

  @Inject private MetaModelRepository metaModelRepo;

  @Inject private ValidatorService validatorService;

  @Inject private ConfigurationService configService;

  @Inject private ViewItemRepository viewItemRepo;

  @Inject private ActionBuilderRepository actionBuilderRepo;

  @Inject private ModuleImporterService moduleImporter;

  @Inject private FormImporterService formImporter;

  @Inject private MenuImporterService menuImporter;

  @Inject private GridImporterService gridImporter;

  @Inject private ModelImporterService modelImporter;

  @Inject private ActionImporterService actionImporter;

  /**
   * Root method to access the service. It will call other methods required to import model.
   *
   * @param modelImporter ModelImporter class that store all import meta file.
   * @return Return true if import done successfully else false.
   * @throws AxelorException
   */
  public File importData(DataReaderService reader, MetaFile metaFile, Boolean helpOnly)
      throws AxelorException {

    if (metaFile == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, I18n.get("Input file not exist"));
    }

    moduleMap = new HashMap<String, Map<String, List<String>>>();
    fieldSeqMap = new HashMap<Long, Integer>();
    gridViewMap = new HashMap<String, Map<String, List<MetaField>>>();
    nestedModels = new HashMap<String, MetaModel>();

    try {

      reader.initialize(metaFile);

      String[] headers = CommonService.HEADERS;
      File logFile = validatorService.validate(reader, headers);
      if (logFile != null) {
        return logFile;
      }
      formImporter.clear();

      moduleImporter.createModules(reader, "Modules");

      processSheets(reader);

      Map<String, MetaModel> modelMap = clearModels();

      generateGrid(modelMap);

      actionImporter.importActions(reader, "Actions");
      menuImporter.importMenus(reader, "Menu");

    } catch (IOException e) {
      throw new AxelorException(e, 5);
    }

    return null;
  }

  /**
   * Method create list of MetaModels by reading given input file.
   *
   * @param inputFile Data input file.
   * @return List of MetaModels created.
   * @throws IOException Exception in file handling.
   * @throws AxelorException
   */
  private void processSheets(DataReaderService reader) throws IOException, AxelorException {

    String[] keys = reader.getKeys();
    if (keys == null) {
      return;
    }

    for (String key : keys) {

      if (!key.equals("Modules") && !key.equals("Menu") && !key.equals("Actions")) {
        log.debug("Importing sheet: {}", key);
        int totalLines = reader.getTotalLines(key);

        for (int rowNum = 0; rowNum < totalLines; rowNum++) {

          String[] row = reader.read(key, rowNum);
          if (row == null) {
            continue;
          }

          processRow(row, key, rowNum);
        }
      }
    }
  }

  /**
   * Create MetaModel from data sheet data.
   *
   * @param sheet Excel sheet to process
   * @return MetaModel created
   * @throws AxelorException
   */
  private void processRow(String[] row, String key, int rowNum) throws AxelorException {

    replace = true;

    Map<String, String> valMap = ExporterService.createValMap(row, CommonService.HEADERS);
    String module = valMap.get(CommonService.MODULE);
    if (module == null) {
      return;
    }

    if (module.startsWith("*")) {
      replace = false;
      module = module.replace("*", "");
    }

    MetaModule metaModule = getModule(module, valMap.get(CommonService.IF_MODULE));
    if (metaModule == null) {
      return;
    }

    modelImporter.importModel(this, valMap, rowNum, metaModule);
  }

  public MetaModule getModule(String module, String checkModule) {

    List<String> installedModules = configService.getInstalledModules();

    MetaModule metaModule = null;

    if (checkModule != null && !installedModules.contains(checkModule)) {
      metaModule = configService.getModule(checkModule);
      if (metaModule != null) {
        List<String> depends =
            metaModule.getDepends().stream().map(it -> it.getName()).collect(Collectors.toList());
        if (depends != null && depends.contains(checkModule)) {
          return metaModule;
        }
      }
    } else if (!installedModules.contains(module)) {
      metaModule = configService.getModule(module);
    }

    return metaModule;
  }

  private void generateGrid(Map<String, MetaModel> clearedModels) throws AxelorException {

    Map<String, List<ActionBuilder>> actionViewMap = formImporter.getViewActionMap();

    for (String module : moduleMap.keySet()) {

      for (String modelName : moduleMap.get(module).keySet()) {
        if (!clearedModels.containsKey(modelName)) {
          continue;
        }
        MetaModel model = clearedModels.get(modelName);
        List<MetaField> fields = null;
        if (gridViewMap.containsKey(module)) {
          fields = gridViewMap.get(module).get(model);
        }

        //        if (model.getMetaModule() != null || fields != null) {
        if (fields != null) {
          //          if ((fields == null || fields.isEmpty()) && model.getMetaModule() != null) {
          //            module = model.getMetaModule().getName();
          //          }
          ViewBuilder viewBuilder =
              gridImporter.createGridView(getModule(module, null), model, fields);
          if (actionViewMap.containsKey(viewBuilder.getName())) {
            updateActionView(actionViewMap, viewBuilder);
          }
        } else {
          gridImporter.clearGrid(module, modelName);
        }
      }
    }

    if (!actionViewMap.isEmpty()) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get("Views not found: %s"),
          formImporter.getViewActionMap().keySet());
    }
  }

  @Transactional
  public void updateActionView(
      Map<String, List<ActionBuilder>> actionViewMap, ViewBuilder viewBuilder) {

    for (ActionBuilder builder : actionViewMap.get(viewBuilder.getName())) {
      ActionBuilderView abView = new ActionBuilderView();
      abView.setViewName(viewBuilder.getName());
      abView.setViewType(viewBuilder.getViewType());
      builder.addActionBuilderView(abView);
      builder.setModel(metaModelRepo.findByName(viewBuilder.getModel()).getFullName());
      actionBuilderRepo.save(builder);
    }

    actionViewMap.remove(viewBuilder.getName());
  }

  private Map<String, Set<String>> getModels() {

    Map<String, Set<String>> models = new HashMap<String, Set<String>>();

    for (String module : moduleMap.keySet()) {
      for (String modelName : moduleMap.get(module).keySet()) {
        if (!models.containsKey(modelName)) {
          models.put(modelName, new HashSet<String>());
        }
        if (moduleMap.get(module).get(modelName) != null) {
          models.get(modelName).addAll(moduleMap.get(module).get(modelName));
        }
      }
    }

    return models;
  }

  @Transactional
  Map<String, MetaModel> clearModels() {

    Map<String, MetaModel> modelMap = new HashMap<String, MetaModel>();

    Map<String, Set<String>> models = getModels();

    for (String modelName : models.keySet()) {
      MetaModel model = metaModelRepo.findByName(modelName);
      if (model == null || model.getMetaFields() == null) {
        continue;
      }

      Set<String> fields = models.get(modelName);

      Iterator<MetaField> fieldIter = model.getMetaFields().iterator();

      while (fieldIter.hasNext()) {
        MetaField field = fieldIter.next();
        //				if (field.getCustomised() && !fields.contains(field.getName())) {
        if (!fields.contains(field.getName())) {
          log.debug("Removing field : {}", field.getName());
          List<ViewItem> viewItems =
              viewItemRepo.all().filter("self.metaField = ?1", field).fetch();
          for (ViewItem viewItem : viewItems) {
            viewItemRepo.remove(viewItem);
          }
          fieldIter.remove();
        }
      }

      modelMap.put(modelName, metaModelRepo.save(model));
    }

    return modelMap;
  }

  public void addNestedModel(String name, MetaModel metaModel) {

    if (!nestedModels.containsKey(name)) {
      nestedModels.put(name, metaModel);
    }
  }

  public MetaModel getNestedModels(String name) {

    return nestedModels.get(name);
  }

  public void updateModuleMap(String module, String model, String field) {

    if (!moduleMap.containsKey(module)) {
      moduleMap.put(module, new HashMap<String, List<String>>());
    }

    if (!moduleMap.get(module).containsKey(model)) {
      moduleMap.get(module).put(model, new ArrayList<String>());
    }

    if (field != null) {
      moduleMap.get(module).get(model).add(field);
    }
  }

  public void addView(
      MetaModel model, String[] basic, Map<String, String> valMap, int rowNum, MetaField field)
      throws AxelorException {

    formImporter.importForm(model, basic, valMap, rowNum, field, replace);
  }

  public Integer getFieldSeq(Long modelId) {

    Integer seq = 1;
    if (fieldSeqMap.containsKey(modelId)) {
      seq = fieldSeqMap.get(modelId) + 1;
    }

    fieldSeqMap.put(modelId, seq);

    return seq;
  }

  public void addGridField(String module, String model, MetaField metaField) {

    Map<String, List<MetaField>> gridMap = null;
    if (!gridViewMap.containsKey(module)) {
      gridViewMap.put(module, new HashMap<String, List<MetaField>>());
    }
    gridMap = gridViewMap.get(module);
    if (!gridMap.containsKey(model)) {
      gridMap.put(model, new ArrayList<MetaField>());
    }

    gridMap.get(model).add(metaField);

    gridViewMap.put(module, gridMap);
  }
}
