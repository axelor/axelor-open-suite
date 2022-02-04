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
package com.axelor.studio.web;

import com.axelor.apps.base.db.App;
import com.axelor.apps.base.service.app.AppService;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.studio.db.AppBuilder;
import com.axelor.studio.db.repo.AppBuilderRepository;

public class AppBuilderController {

  public void installApp(ActionRequest request, ActionResponse response) throws AxelorException {

    AppBuilder appBuilder = request.getContext().asType(AppBuilder.class);
    appBuilder = Beans.get(AppBuilderRepository.class).find(appBuilder.getId());

    App app = appBuilder.getGeneratedApp();
    Beans.get(AppService.class).installApp(app, null);

    response.setSignal("refresh-app", true);
  }

  public void uninstallApp(ActionRequest request, ActionResponse response) throws AxelorException {

    AppBuilder appBuilder = request.getContext().asType(AppBuilder.class);
    appBuilder = Beans.get(AppBuilderRepository.class).find(appBuilder.getId());

    App app = appBuilder.getGeneratedApp();
    Beans.get(AppService.class).unInstallApp(app);

    response.setSignal("refresh-app", true);
  }
}
