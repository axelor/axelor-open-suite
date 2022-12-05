/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.service.app;

import com.axelor.apps.base.db.App;
import com.axelor.apps.base.db.repo.AppRepository;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaAction;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.MetaJsonRecord;
import com.axelor.meta.db.MetaMenu;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.repo.MetaMenuRepository;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.google.inject.Inject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class AccessTemplateServiceImpl implements AccessTemplateService {

  private Map<String, String> menuApp;

  private Map<String, String> objMenu;

  private List<String> configMenus;

  private List<String> appMenus;

  private static final String[] objectHeaders = new String[] {"Object", "User", "Manager"};
  private static final String[] menuHeaders = new String[] {"Menu", "User", "Manager"};

  private String defaultApp = null;

  protected MetaMenuRepository metaMenuRepo;
  protected MetaFiles metaFiles;
  protected MetaModelRepository metaModelRepo;

  @Inject
  public AccessTemplateServiceImpl(
      MetaMenuRepository metaMenuRepo, MetaFiles metaFiles, MetaModelRepository metaModelRepo) {
    this.metaMenuRepo = metaMenuRepo;
    this.metaFiles = metaFiles;
    this.metaModelRepo = metaModelRepo;
  }

  @Override
  public MetaFile generateTemplate() throws AxelorException {

    try {

      menuApp = new HashMap<>();
      objMenu = new HashMap<>();
      configMenus = new ArrayList<>();
      appMenus = new ArrayList<>();
      defaultApp = null;

      App app = Beans.get(AppRepository.class).findByCode("base");
      if (app == null) {
        return null;
      }
      defaultApp = app.getCode();

      getMenusPerApp();

      updateNoMenuObjects();

      return createExcel();
    } catch (Exception e) {
      TraceBackService.trace(e);
      throw new AxelorException(TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, e.getMessage());
    }
  }

  private void getMenusPerApp() {

    List<MetaMenu> menus = metaMenuRepo.all().filter("self.parent is null").order("id").fetch();

    processMenu(menus.iterator());
  }

  private void processMenu(Iterator<MetaMenu> menuIter) {

    if (!menuIter.hasNext()) {
      return;
    }

    MetaMenu menu = menuIter.next();

    String app = getApp(menu);
    menuApp.put(menu.getName(), app);
    MetaAction action = menu.getAction();
    if (action != null && action.getType().equals("action-view")) {
      String model = action.getModel();
      if (validModel(model)) {
        String menuName = getObjMenu(menu);
        if (menuName != null) {
          objMenu.put(model, menuName);
        }
      }
    }
    List<MetaMenu> menus = metaMenuRepo.all().filter("self.parent = ?1", menu).order("id").fetch();
    processMenu(menus.iterator());
    processMenu(menuIter);
  }

  private boolean validModel(String model) {

    if (model == null) {
      return false;
    }

    if (objMenu.containsKey(model)) {
      return false;
    }

    return !model.equals(MetaJsonRecord.class.getName());
  }

  private String getObjMenu(MetaMenu menu) {

    if (appMenus.contains(menu.getName())) {
      return menu.getName();
    }
    if (configMenus.contains(menu.getName())) {
      return menu.getName();
    }

    MetaMenu parent = menu.getParent();
    while (parent != null) {
      if (configMenus.contains(parent.getName())) {
        break;
      }
      if (appMenus.contains(parent.getName())) {
        break;
      }
      parent = parent.getParent();
    }

    if (parent != null) {
      return parent.getName();
    }

    return null;
  }

  private String getApp(MetaMenu menu) {

    String appCode = null;
    String condition = menu.getConditionToCheck();

    if (condition != null) {
      String[] cond = condition.split("__config__\\.app\\.isApp\\('");
      if (cond.length > 1) {
        App app = Beans.get(AppRepository.class).findByCode(cond[1].split("'")[0]);
        if (app != null) {
          if (condition.trim().equals("__config__.app.isApp('" + app.getCode() + "')")
              && menu.getAction() == null) {
            appMenus.add(menu.getName());
          }
          appCode = app.getCode();
        }
      }
    }

    MetaMenu parent = menu.getParent();
    if (appCode == null && parent != null && menuApp.containsKey(parent.getName())) {
      appCode = menuApp.get(parent.getName());
    }

    if (parent == null && appCode == null) {
      configMenus.add(menu.getName());
    }

    if (menu.getTitle().equals("Configuration") || menu.getTitle().equals("Configurations")) {
      configMenus.add(menu.getName());
      appMenus.remove(menu.getName());
    }

    if (appCode == null) {
      return defaultApp;
    }

    return appCode;
  }

  private MetaFile createExcel() throws IOException {

    XSSFWorkbook workBook = new XSSFWorkbook();

    List<String> keys = new ArrayList<>();
    keys.addAll(objMenu.keySet());
    Collections.sort(keys);

    Set<String> menuProcessed = new HashSet<>();
    for (String obj : keys) {
      String menu = objMenu.get(obj);
      String app = menuApp.get(menu);
      if (app == null) {
        continue;
      }
      writeObjectSheet(workBook, obj, menu, app);
      if (!menuProcessed.contains(menu)) {
        writeMenuSheet(workBook, menu, app);
        menuProcessed.add(menu);
      }
    }

    List<String> menusRemaining = new ArrayList<>();
    menusRemaining.addAll(appMenus);
    for (String menu : menusRemaining) {
      writeMenuSheet(workBook, menu, menuApp.get(menu));
    }

    return createMetaFile(workBook);
  }

  public MetaFile createMetaFile(XSSFWorkbook workBook) throws IOException {

    Path path = MetaFiles.createTempFile("AccessConfigTemplate", ".xlsx");

    File file = path.toFile();
    FileOutputStream fout = new FileOutputStream(file);
    workBook.write(fout);
    fout.close();

    return metaFiles.upload(file);
  }

  public void writeObjectSheet(XSSFWorkbook workBook, String obj, String menu, String app) {

    XSSFSheet sheet = workBook.getSheet(app);
    if (sheet == null) {
      sheet = workBook.createSheet(app);
      writeRow(sheet, objectHeaders);
    }
    String usersRights = appMenus.contains(menu) ? "rwcde" : "r";
    writeRow(sheet, new String[] {obj, usersRights, "rwcde"});
  }

  private void writeMenuSheet(XSSFWorkbook workBook, String menu, String app) {
    XSSFSheet sheet = workBook.getSheet(app + "-menu");
    if (sheet == null) {
      sheet = workBook.createSheet(app + "-menu");
      writeRow(sheet, menuHeaders);
    }
    String usersRights = configMenus.contains(menu) ? "" : "x";
    writeRow(sheet, new String[] {menu, usersRights, "x"});
    appMenus.remove(menu);
  }

  private void writeRow(XSSFSheet sheet, String[] values) {

    XSSFRow row = sheet.createRow(sheet.getPhysicalNumberOfRows());

    for (int i = 0; i < values.length; i++) {
      XSSFCell cell = row.createCell(i);
      cell.setCellValue(values[i]);
    }
  }

  private void updateNoMenuObjects() {

    List<MetaModel> metaModels =
        metaModelRepo.all().filter("self.fullName not in ?1", objMenu.keySet()).fetch();

    Iterator<MetaModel> modelIter = metaModels.iterator();
    String appMenu = objMenu.get(App.class.getName());
    while (modelIter.hasNext()) {
      MetaModel model = modelIter.next();
      try {
        Class klass = Class.forName(model.getFullName());
        if (App.class.isAssignableFrom(klass)) {
          objMenu.put(model.getFullName(), appMenu);
          modelIter.remove();
        } else if (addObject(model)) {
          modelIter.remove();
        } else {
          objMenu.put(model.getFullName(), appMenu);
        }
      } catch (ClassNotFoundException e) {
        continue;
      }
    }
  }

  private boolean addObject(MetaModel model) {

    for (Entry<String, String> entry : objMenu.entrySet()) {
      if (model.getFullName().contains(entry.getKey())) {
        objMenu.put(model.getFullName(), entry.getValue());
        return true;
      }
    }

    String pkgName = model.getPackageName();
    for (Entry<String, String> entry : objMenu.entrySet()) {
      String key = entry.getKey();
      String objPkg = key.substring(0, key.lastIndexOf("."));
      if (pkgName.equals(objPkg)) {
        objMenu.put(model.getFullName(), entry.getValue());
        return true;
      }
    }

    return false;
  }
}
