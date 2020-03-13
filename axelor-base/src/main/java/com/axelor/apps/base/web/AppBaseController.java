/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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

import com.axelor.app.AppSettings;
import com.axelor.apps.base.db.AppBase;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.apps.base.service.CurrencyConversionService;
import com.axelor.apps.base.service.MapService;
import com.axelor.apps.base.service.administration.ExportDbObjectService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class AppBaseController {

  @Inject private ExportDbObjectService eos;

  @Inject private MapService mapService;

  @Inject private CurrencyConversionService currencyConversionService;

  public void exportObjects(ActionRequest request, ActionResponse response) {
    MetaFile metaFile = eos.exportObject();
    if (metaFile == null) {
      response.setFlash(I18n.get(IExceptionMessage.GENERAL_4));
    } else {
      response.setView(
          ActionView.define(I18n.get(IExceptionMessage.GENERAL_5))
              .model("com.axelor.meta.db.MetaFile")
              .add("form", "meta-files-form")
              .add("grid", "meta-files-grid")
              .param("forceEdit", "true")
              .context("_showRecord", metaFile.getId().toString())
              .map());
    }
  }

  public void checkMapApi(ActionRequest request, ActionResponse response) {
    try {
      AppBase appBase = request.getContext().asType(AppBase.class);
      Integer apiType = appBase.getMapApiSelect();

      if (apiType == 1) {
        mapService.testGMapService();
        response.setFlash(IExceptionMessage.GENERAL_6);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void updateCurrencyConversion(ActionRequest request, ActionResponse response)
      throws AxelorException {
    currencyConversionService.updateCurrencyConverion();
    response.setReload(true);
  }

  public void applyApplicationMode(ActionRequest request, ActionResponse response) {
    String applicationMode = AppSettings.get().get("application.mode", "prod");
    if ("dev".equals(applicationMode)) {
      response.setAttr("mainPanel", "hidden", false);
    }
  }

  public void showCustomersOnMap(ActionRequest request, ActionResponse response) {
    try {
      response.setView(
          ActionView.define(I18n.get("Customers"))
              .add("html", mapService.getMapURI("customer"))
              .map());
    } catch (Exception e) {
      TraceBackService.trace(e);
    }
  }

  public void showProspectsOnMap(ActionRequest request, ActionResponse response) {
    try {
      response.setView(
          ActionView.define(I18n.get("Prospects"))
              .add("html", mapService.getMapURI("prospect"))
              .map());
    } catch (Exception e) {
      TraceBackService.trace(e);
    }
  }

  public void showSuppliersOnMap(ActionRequest request, ActionResponse response) {
    try {
      response.setView(
          ActionView.define(I18n.get("Suppliers"))
              .add("html", mapService.getMapURI("supplier"))
              .map());
    } catch (Exception e) {
      TraceBackService.trace(e);
    }
  }
}
