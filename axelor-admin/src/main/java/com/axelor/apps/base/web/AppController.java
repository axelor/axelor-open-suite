/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.web;

import com.axelor.apps.base.db.App;
import com.axelor.apps.base.db.repo.AppRepository;
import com.axelor.apps.base.exceptions.IExceptionMessages;
import com.axelor.apps.base.service.app.AccessConfigImportService;
import com.axelor.apps.base.service.app.AccessTemplateService;
import com.axelor.apps.base.service.app.AppService;
import com.axelor.common.Inflector;
import com.axelor.exception.AxelorException;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.repo.MetaFileRepository;
import com.axelor.meta.db.repo.MetaViewRepository;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.inject.Singleton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Singleton
public class AppController {

  public void importDataDemo(ActionRequest request, ActionResponse response)
      throws AxelorException {

    App app = request.getContext().asType(App.class);
    app = Beans.get(AppRepository.class).find(app.getId());
    Beans.get(AppService.class).importDataDemo(app);

    response.setFlash(I18n.get(IExceptionMessages.DEMO_DATA_SUCCESS));

    response.setReload(true);
  }

  public void installApp(ActionRequest request, ActionResponse response) throws AxelorException {

    App app = request.getContext().asType(App.class);
    app = Beans.get(AppRepository.class).find(app.getId());

    Beans.get(AppService.class).installApp(app, null);

    response.setSignal("refresh-app", true);
  }

  public void configure(ActionRequest request, ActionResponse response) {

    App app = request.getContext().asType(App.class);

    String code = app.getCode();
    String appName = Inflector.getInstance().camelize(code);
    String viewName = "app-" + code + "-config-form";

    if (Beans.get(MetaViewRepository.class).findByName(viewName) == null) {
      response.setFlash(I18n.get(IExceptionMessages.NO_CONFIG_REQUIRED));
    } else {
      response.setView(
          ActionView.define(I18n.get("Configure") + ": " + app.getName())
              .add("form", viewName)
              .model("com.axelor.apps.base.db.App" + appName)
              .context("_showRecord", app.getId())
              .param("forceEdit", "true")
              .map());
    }
  }

  public void uninstallApp(ActionRequest request, ActionResponse response) throws AxelorException {

    App app = request.getContext().asType(App.class);
    app = Beans.get(AppRepository.class).find(app.getId());

    Beans.get(AppService.class).unInstallApp(app);

    response.setSignal("refresh-app", true);
  }

  public void bulkInstall(ActionRequest request, ActionResponse response) throws AxelorException {

    Context context = request.getContext();

    Set<Map<String, Object>> apps = new HashSet<Map<String, Object>>();
    Collection<Map<String, Object>> appsSet =
        (Collection<Map<String, Object>>) context.get("appsSet");
    if (appsSet != null) {
      apps.addAll(appsSet);
    }

    Boolean importDemo = (Boolean) context.get("importDemoData");
    String language = (String) context.get("languageSelect");
    AppRepository appRepository = Beans.get(AppRepository.class);

    List<App> appList = new ArrayList<App>();
    for (Map<String, Object> appData : apps) {
      App app = appRepository.find(Long.parseLong(appData.get("id").toString()));
      appList.add(app);
    }

    Beans.get(AppService.class).bulkInstall(appList, importDemo, language);

    response.setFlash(I18n.get(IExceptionMessages.BULK_INSTALL_SUCCESS));
    response.setSignal("refresh-app", true);
  }

  public void refreshApp(ActionRequest request, ActionResponse response) {

    try {
      Beans.get(AppService.class).refreshApp();
      response.setNotify(I18n.get(IExceptionMessages.REFRESH_APP_SUCCESS));
      response.setReload(true);
    } catch (IOException | ClassNotFoundException e) {
      e.printStackTrace();
      response.setNotify(I18n.get(IExceptionMessages.REFRESH_APP_ERROR));
    }
  }

  public void generateAccessTemplate(ActionRequest request, ActionResponse response)
      throws AxelorException {

    MetaFile accesssFile = Beans.get(AccessTemplateService.class).generateTemplate();

    if (accesssFile == null) {
      return;
    }

    response.setView(
        ActionView.define(I18n.get("Export file"))
            .model(App.class.getName())
            .add(
                "html",
                "ws/rest/com.axelor.meta.db.MetaFile/"
                    + accesssFile.getId()
                    + "/content/download?v="
                    + accesssFile.getVersion())
            .param("download", "true")
            .map());
  }

  public void importRoles(ActionRequest request, ActionResponse response) throws AxelorException {

    App app = request.getContext().asType(App.class);

    app = Beans.get(AppRepository.class).find(app.getId());

    Beans.get(AppService.class).importRoles(app);
    response.setReload(true);
    response.setFlash(I18n.get(IExceptionMessages.ROLE_IMPORT_SUCCESS));
  }

  public void importAllRoles(ActionRequest request, ActionResponse response)
      throws AxelorException {

    Beans.get(AppService.class).importRoles();

    response.setFlash(I18n.get(IExceptionMessages.ROLE_IMPORT_SUCCESS));
    response.setReload(true);
  }

  public void importAccessConfig(ActionRequest request, ActionResponse response)
      throws AxelorException {

    Map<String, Object> metaFileMap = (Map<String, Object>) request.getContext().get("metaFile");

    if (metaFileMap != null) {
      Long fileId = Long.parseLong(metaFileMap.get("id").toString());
      Beans.get(AccessConfigImportService.class)
          .importAccessConfig(Beans.get(MetaFileRepository.class).find(fileId));
      response.setFlash(I18n.get(IExceptionMessages.ACCESS_CONFIG_IMPORTED));
      response.setCanClose(true);
    }
  }
}
