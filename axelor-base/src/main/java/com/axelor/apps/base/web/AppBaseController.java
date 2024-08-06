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
package com.axelor.apps.base.web;

import com.axelor.app.AppSettings;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.MapService;
import com.axelor.apps.base.service.administration.ExportDbObjectService;
import com.axelor.apps.base.service.currency.CurrencyConversionFactory;
import com.axelor.apps.base.service.currency.CurrencyConversionService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.quartz.JobRunner;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.studio.db.AppBase;
import com.google.inject.Singleton;

@Singleton
public class AppBaseController {

  public void exportObjects(ActionRequest request, ActionResponse response) {
    MetaFile metaFile = Beans.get(ExportDbObjectService.class).exportObject();
    if (metaFile == null) {
      response.setInfo(I18n.get(BaseExceptionMessage.GENERAL_4));
    } else {
      response.setView(
          ActionView.define(I18n.get(BaseExceptionMessage.GENERAL_5))
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
        Beans.get(MapService.class).testGMapService();
        response.setInfo(BaseExceptionMessage.GENERAL_6);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void updateCurrencyConversion(ActionRequest request, ActionResponse response) {
    try {
      CurrencyConversionService currencyConversionService =
          Beans.get(CurrencyConversionFactory.class).getCurrencyConversionService();
      currencyConversionService.updateCurrencyConverion();
      response.setReload(true);

    } catch (AxelorException e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
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
              .add("html", Beans.get(MapService.class).getMapURI("customer"))
              .map());
    } catch (Exception e) {
      TraceBackService.trace(e);
    }
  }

  public void showProspectsOnMap(ActionRequest request, ActionResponse response) {
    try {
      response.setView(
          ActionView.define(I18n.get("Prospects"))
              .add("html", Beans.get(MapService.class).getMapURI("prospect"))
              .map());
    } catch (Exception e) {
      TraceBackService.trace(e);
    }
  }

  public void showSuppliersOnMap(ActionRequest request, ActionResponse response) {
    try {
      response.setView(
          ActionView.define(I18n.get("Suppliers"))
              .add("html", Beans.get(MapService.class).getMapURI("supplier"))
              .map());
    } catch (Exception e) {
      TraceBackService.trace(e);
    }
  }

  public void checkQuartzScheduler(ActionRequest request, ActionResponse response) {
    if (Beans.get(JobRunner.class).isEnabled()) {
      response.setInfo(I18n.get(BaseExceptionMessage.QUARTZ_SCHEDULER_ENABLED));
    }
  }
}
