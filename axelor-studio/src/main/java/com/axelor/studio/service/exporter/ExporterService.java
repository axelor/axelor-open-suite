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
package com.axelor.studio.service.exporter;

import com.axelor.i18n.I18n;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaAction;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.MetaMenu;
import com.axelor.meta.db.MetaModule;
import com.axelor.meta.db.repo.MetaModuleRepository;
import com.axelor.meta.schema.views.AbstractWidget;
import com.axelor.studio.service.CommonService;
import com.axelor.studio.service.TranslationService;
import com.axelor.studio.service.ViewLoaderService;
import com.axelor.studio.service.importer.DataReaderService;
import com.axelor.studio.service.validator.ValidatorService;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExporterService {

  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private String menuPath = null;

  private String menuPathFR = null;

  private Map<String, String> processedMenus = new HashMap<String, String>();

  private Set<String> viewProcessed = new HashSet<String>();

  private Map<String, String[]> docMap = new HashMap<String, String[]>();

  private Map<String, List<String[]>> commentMap = new HashMap<String, List<String[]>>();

  private DataWriter writer;

  private String writerKey;

  private String[] headers;

  private boolean helpOnly = false;

  private List<String> exportModules = new ArrayList<String>();

  @Inject private CommonService common;

  @Inject private ModelExporter modelExporter;

  @Inject private MetaModuleRepository metaModuleRepo;

  @Inject private MenuExporter menuExporter;

  @Inject private ActionExporter actionExporter;

  @Inject private CustomExporter customExporter;

  @Inject private TranslationService translationService;

  @Inject private ValidatorService validatorService;

  @Inject private MetaFiles metaFiles;

  public MetaFile export(
      MetaFile oldFile, DataWriter writer, DataReaderService reader, boolean helpOnly)
      throws IOException, ClassNotFoundException {

    setExportModules();
    this.helpOnly = helpOnly;

    if (helpOnly) {
      headers = CommonService.HELP_HEADERS;
    } else {
      headers = CommonService.HEADERS;
    }
    this.writer = writer;
    this.writer.initialize();

    if (oldFile != null) {
      if (reader.initialize(oldFile)) {
        File logFile = validatorService.validate(reader, headers);
        if (logFile != null) {
          return metaFiles.upload(logFile);
        }
        updateDocMap(reader);
      }
    }

    if (!helpOnly) {
      addModules(reader);
    }

    menuExporter.export(writer, exportModules);

    if (!helpOnly) {
      actionExporter.export(writer);
    }

    processMenu();

    processCustom();

    addRemainingDoc();

    return this.writer.export(oldFile);
  }

  public static String getModuleToCheck(AbstractWidget item, String module) {

    String moduleName = item.getModuleToCheck();

    if (Strings.isNullOrEmpty(moduleName)) {
      moduleName = module;
    }

    return moduleName;
  }

  private void setExportModules() {

    List<MetaModule> modules =
        metaModuleRepo.all().filter("self.installed = true and self.name != 'axelor-core'").fetch();

    for (MetaModule module : modules) {
      exportModules.add(module.getName());
    }
  }

  public boolean isExportModule(String name) {

    return exportModules.contains(name);
  }

  public boolean isViewProcessed(String name) {

    return viewProcessed.contains(name);
  }

  public void addViewProcessed(String name) {

    viewProcessed.add(name);
  }

  private void addModules(DataReaderService reader) {

    String[] keys = reader.getKeys();

    if (keys != null) {
      for (int count = 0; count < reader.getTotalLines(keys[0]); count++) {
        String[] row = reader.read(keys[0], count);
        if (row == null) {
          continue;
        }

        writer.write(keys[0], count, row);
      }
    } else {
      writer.write("Modules", 0, CommonService.MODULE_HEADERS);
    }
  }

  protected void writeRow(Map<String, String> valMap, boolean newForm) {

    if (valMap.get(CommonService.TITLE) != null && valMap.get(CommonService.TITLE).equals("X")) {
      valMap.put(CommonService.TITLE, "Multiply(" + valMap.get(CommonService.TITLE) + ")");
      valMap.put(
          CommonService.TITLE_FR,
          translationService.getTranslation(
              "Multiply(" + valMap.get(CommonService.TITLE) + ")", "fr"));
    }

    if (newForm && !helpOnly) {
      addGeneralRow(writerKey, valMap);
    }

    valMap.put(CommonService.MENU, menuPath);
    valMap.put(CommonService.MENU_FR, menuPathFR);

    valMap = addHelp(null, valMap);

    writer.write(writerKey, null, valMap, headers);

    addComments(writerKey, null, valMap, false);
  }

  private void processMenu() throws ClassNotFoundException {

    List<MetaMenu> menus = menuExporter.getMenus(exportModules);

    for (MetaMenu menu : menus) {
      String name = menu.getName();
      if (processedMenus.containsKey(name)) {
        continue;
      }

      updateMenuPath(menu);

      if (menu.getParent() == null) {
        String title = menu.getTitle();
        writerKey = I18n.get(title);
        if (processedMenus.containsValue(title)) {
          writerKey += "(" + menu.getId() + ")";
        }
        if (helpOnly) {
          writer.write(writerKey, null, CommonService.HELP_HEADERS);
        } else {
          writer.write(writerKey, null, CommonService.HEADERS);
        }
      }

      MetaAction action = menu.getAction();
      if (action != null && action.getType().equals("action-view")) {
        if (action.getModule() != null && action.getModule().equals("axelor-core")) {
          continue;
        }
        modelExporter.export(this, action);
      }

      processedMenus.put(name, menu.getTitle());
    }

    modelExporter.exportAppConfigs(this);
  }

  private Map<String, String> addHelp(String docKey, Map<String, String> valMap) {

    if (!docMap.isEmpty()) {

      if (docKey == null) {
        docKey = getDocKey(valMap);
      }
      if (docMap.containsKey(docKey)) {
        String[] doc = docMap.get(docKey);
        if (doc[0] != null) {
          valMap.put(CommonService.DOC, doc[0]);
        }
        if (doc[1] != null) {
          valMap.put(CommonService.DOC_FR, doc[1]);
        }
        if (doc[2] != null) {
          valMap.put(CommonService.HELP, doc[2]);
        }
        docMap.remove(docKey);
      }
    }

    return valMap;
  }

  private String getDocKey(Map<String, String> valMap) {

    String name = getFieldName(valMap);

    String model = valMap.get(CommonService.MODEL);
    if (model != null) {
      String[] modelSplit = model.split("\\.");
      model = modelSplit[modelSplit.length - 1];
    }

    String key =
        model
            + ","
            + valMap.get(CommonService.VIEW)
            + ","
            + getFieldType(valMap.get(CommonService.TYPE))
            + ","
            + name;

    return key;
  }

  private String getFieldName(Map<String, String> valMap) {

    String name = valMap.get(CommonService.NAME);

    if (Strings.isNullOrEmpty(name)) {
      name = valMap.get(CommonService.TITLE);
      if (!Strings.isNullOrEmpty(name)) {
        name = common.getFieldName(name);
      }
    }

    return name;
  }

  protected void setMenuPath(String menuPath, String menuPathFR) {
    this.menuPath = menuPath;
    this.menuPathFR = menuPathFR;
  }

  private void addGeneralRow(String key, Map<String, String> valuesMap) {

    Map<String, String> valsMap = new HashMap<>();
    valsMap.put(CommonService.MODULE, valuesMap.get(CommonService.MODULE));
    valsMap.put(CommonService.MODEL, valuesMap.get(CommonService.MODEL));
    valsMap.put(CommonService.VIEW, valuesMap.get(CommonService.VIEW));
    valsMap.put(CommonService.TYPE, "general");

    if (menuPath != null) {
      valsMap.put(CommonService.MENU, menuPath);
      valsMap.put(CommonService.MENU_FR, menuPathFR);
      menuPath = null;
      menuPathFR = null;
    }

    valsMap = addHelp(null, valsMap);
    writer.write(key, null, valsMap, headers);

    addComments(key, null, valuesMap, false);
  }

  private void updateMenuPath(MetaMenu metaMenu) {

    List<String> menus = new ArrayList<String>();
    menus.add(metaMenu.getTitle());

    addParentMenus(menus, metaMenu);

    Collections.reverse(menus);

    boolean first = true;
    for (String mn : menus) {
      String mnFR = translationService.getTranslation(mn, "fr");
      if (Strings.isNullOrEmpty(mnFR)) {
        mnFR = mn;
      }
      if (first) {
        menuPath = mn;
        menuPathFR = mnFR;
      } else {
        menuPath += "/" + mn;
        menuPathFR += "/" + mnFR;
      }
      first = false;
    }
  }

  private void addParentMenus(List<String> menus, MetaMenu metaMenu) {

    MetaMenu parentMenu = metaMenu.getParent();

    if (parentMenu != null) {
      menus.add(parentMenu.getTitle());
      addParentMenus(menus, parentMenu);
    }
  }

  private void updateDocMap(DataReaderService reader) {

    String[] keys = reader.getKeys();

    if (keys == null || keys.length == 1) {
      return;
    }

    keys = Arrays.copyOfRange(keys, 1, keys.length);

    for (String key : keys) {

      log.debug("Loading key: {}", key);
      String lastKey = key;

      for (int count = 1; count < reader.getTotalLines(key); count++) {

        String[] row = reader.read(key, count);
        if (row == null || row.length < headers.length) {
          continue;
        }

        Map<String, String> valMap = createValMap(row, headers);

        String name = getFieldName(valMap);

        String type = valMap.get(CommonService.TYPE);
        if (type == null) {
          continue;
        }

        String model = valMap.get(CommonService.MODEL);
        if (model != null) {
          model = common.inflector.camelize(model);
        }

        String view = valMap.get(CommonService.VIEW);
        if (model != null && view == null) {
          view = ViewLoaderService.getDefaultViewName(model, "form");
        }

        if (updateComment(lastKey, type, row)) {
          continue;
        }

        lastKey = model + "," + view + "," + getFieldType(type) + "," + name;
        if (valMap.get(CommonService.DOC) != null || valMap.get(CommonService.DOC_FR) != null) {
          docMap.put(
              lastKey,
              new String[] {
                valMap.get(CommonService.DOC),
                valMap.get(CommonService.DOC_FR),
                valMap.get(CommonService.HELP)
              });
        }
      }
    }
  }

  public static Map<String, String> createValMap(String[] row, String[] headers) {

    Map<String, String> valMap = new HashMap<>();
    if (headers != null) {
      for (int i = 0; i < row.length; i++) {
        if (headers.length <= i) {
          break;
        }
        valMap.put(headers[i], row[i]);
      }
    }

    return valMap;
  }

  private boolean updateComment(String lastKey, String type, String[] row) {

    if (type.contains("(")) {
      type = type.substring(0, type.indexOf("("));
    }

    if (!CommonService.FIELD_TYPES.containsKey(type)
        && !CommonService.VIEW_ELEMENTS.containsKey(type)) {

      List<String[]> rows = new ArrayList<String[]>();
      if (commentMap.containsKey(lastKey)) {
        rows = commentMap.get(lastKey);
      }

      rows.add(row);

      commentMap.put(lastKey, rows);

      return true;
    }

    return false;
  }

  private Integer addComments(
      String writeKey, Integer index, Map<String, String> valMap, boolean header) {

    if (commentMap.isEmpty()) {
      return index;
    }

    String key = null;
    if (header) {
      key = writeKey;
    } else {
      key = getDocKey(valMap);
    }

    if (commentMap.containsKey(key)) {
      for (String[] row : commentMap.get(key)) {
        if (index != null) {
          index++;
        }
        writer.write(writeKey, index, row);
      }
    }

    return index;
  }

  private String getFieldType(String type) {

    if (type == null) {
      return type;
    }
    type = type.trim();

    if (type.contains("(")) {
      type = type.substring(0, type.indexOf("("));
    }

    if (CommonService.FR_MAP.containsKey(type)) {
      type = CommonService.FR_MAP.get(type);
    }

    if (CommonService.FIELD_TYPES.containsKey(type)) {
      type = CommonService.FIELD_TYPES.get(type);
    } else if (CommonService.VIEW_ELEMENTS.containsKey(type)) {
      type = CommonService.VIEW_ELEMENTS.get(type);
    }

    type = type.toUpperCase();

    if (type.startsWith("PANEL")) {
      return "PANEL";
    }

    if (type.startsWith("WIZARD")) {
      return "BUTTON";
    }

    return type.replace("-", "_");
  }

  private void processCustom() {

    List<MetaMenu> customMenus = menuExporter.getCustomMenu();

    for (MetaMenu customMenu : customMenus) {
      String name = customMenu.getName();
      if (processedMenus.containsKey(name)) {
        continue;
      }

      updateMenuPath(customMenu);

      if (customMenu.getParent() == null) {
        String title = "Custom";
        writerKey = I18n.get(title);
        writer.write(writerKey, null, CommonService.HEADERS);
      }

      MetaAction action = customMenu.getAction();
      if (action != null && action.getType().equals("action-view")) {
        if (action.getModule() != null && action.getModule().equals("axelor-core")) {
          continue;
        }
        customExporter.customExport(this, action);
      }

      processedMenus.put(name, customMenu.getTitle());
    }
  }

  private void addRemainingDoc() {

    writer.write("MissingFields", 0, new String[] {"KEY", "HELP", "HELP FR"});
    int count = 1;
    for (Entry<String, String[]> entry : docMap.entrySet()) {
      String[] val = entry.getValue();
      writer.write("MissingFields", count, new String[] {entry.getKey(), val[0], val[1]});
      count++;
    }
  }
}
