/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.base.web;

import com.axelor.apps.app.db.App;
import com.axelor.apps.app.db.repo.AppRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.app.AccessTemplateService;
import com.axelor.apps.base.service.app.AppService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.common.Inflector;
import com.axelor.db.Model;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.MetaView;
import com.axelor.meta.db.repo.MetaViewRepository;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import jakarta.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Singleton
public class AppController {

  public void importDataDemo(ActionRequest request, ActionResponse response) {
    try {
      App app = request.getContext().asType(App.class);
      app = Beans.get(AppRepository.class).find(app.getId());
      Beans.get(AppService.class).importDataDemo(app);

      response.setInfo(I18n.get(BaseExceptionMessage.APP_DEMO_DATA_SUCCESS));

      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void installApp(ActionRequest request, ActionResponse response) {
    try {
      App app = request.getContext().asType(App.class);
      app = Beans.get(AppRepository.class).find(app.getId());

      Beans.get(AppService.class).installApp(app, null);

      response.setSignal("refresh-app", true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void configure(ActionRequest request, ActionResponse response) {
    try {
      Context context = request.getContext();

      MetaView formView = null;
      String code = (String) context.get("code");
      String appName = Inflector.getInstance().camelize(code);
      Model config = (Model) context.get("app" + appName);
      String model = "com.axelor.apps.app.db.App" + appName;

      if (config != null) {
        formView =
            Beans.get(MetaViewRepository.class)
                .all()
                .filter(
                    "self.type = 'form' AND self.model = ? AND self.name like '%-config-form'",
                    model)
                .fetchOne();
      }

      if (formView == null) {
        response.setInfo(I18n.get(BaseExceptionMessage.APP_NO_CONFIG_REQUIRED));
      } else {
        response.setView(
            ActionView.define(I18n.get("Configure") + ": " + context.get("name"))
                .add("form", formView.getName())
                .model(model)
                .context("_showRecord", config.getId())
                .param("forceEdit", "true")
                .map());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void uninstallApp(ActionRequest request, ActionResponse response) {
    try {
      App app = request.getContext().asType(App.class);
      app = Beans.get(AppRepository.class).find(app.getId());

      Beans.get(AppService.class).unInstallApp(app);

      response.setSignal("refresh-app", true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  @SuppressWarnings("unchecked")
  public void bulkInstall(ActionRequest request, ActionResponse response) {
    try {
      Context context = request.getContext();

      Set<Map<String, Object>> apps = new HashSet<>();
      Collection<Map<String, Object>> appsSet =
          (Collection<Map<String, Object>>) context.get("appsSet");
      if (appsSet != null) {
        apps.addAll(appsSet);
      }

      boolean importDemo = Boolean.TRUE.equals(context.get("importDemoData"));
      String language = (String) context.get("languageSelect");
      AppRepository appRepository = Beans.get(AppRepository.class);

      List<App> appList = new ArrayList<>();
      apps.forEach(
          appData -> appList.add(appRepository.find(Long.parseLong(appData.get("id").toString()))));

      Beans.get(AppService.class).bulkInstall(appList, importDemo, language);

      response.setInfo(I18n.get(BaseExceptionMessage.APP_BULK_INSTALL_SUCCESS));
      response.setSignal("refresh-app", true);

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void generateAccessTemplate(ActionRequest request, ActionResponse response) {
    try {
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
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void importRoles(ActionRequest request, ActionResponse response) {
    try {
      App app = request.getContext().asType(App.class);

      app = Beans.get(AppRepository.class).find(app.getId());

      Beans.get(AppService.class).importRoles(app);
      response.setReload(true);
      response.setInfo(I18n.get(BaseExceptionMessage.APP_ROLE_IMPORT_SUCCESS));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void importAllRoles(ActionRequest request, ActionResponse response) {
    try {
      Beans.get(AppService.class).importRoles();

      response.setInfo(I18n.get(BaseExceptionMessage.APP_ROLE_IMPORT_SUCCESS));
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
