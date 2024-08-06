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

import com.axelor.apps.base.db.IndicatorGenerator;
import com.axelor.apps.base.db.repo.IndicatorGeneratorRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.administration.IndicatorGeneratorService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;

@Singleton
public class IndicatorGeneratorController {

  public void run(ActionRequest request, ActionResponse response) {

    IndicatorGenerator indicatorGenerator = request.getContext().asType(IndicatorGenerator.class);

    try {
      Beans.get(IndicatorGeneratorService.class)
          .run(Beans.get(IndicatorGeneratorRepository.class).find(indicatorGenerator.getId()));
      response.setReload(true);
      response.setInfo(I18n.get(BaseExceptionMessage.INDICATOR_GENERATOR_3));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void exportCsv(ActionRequest request, ActionResponse response) {
    IndicatorGenerator indicatorGenerator = request.getContext().asType(IndicatorGenerator.class);

    try {
      indicatorGenerator =
          Beans.get(IndicatorGeneratorRepository.class).find(indicatorGenerator.getId());

      MetaFile csvFile =
          Beans.get(IndicatorGeneratorService.class).getQueryResultCsvFile(indicatorGenerator);

      if (csvFile != null) {
        response.setView(
            ActionView.define(I18n.get("Export file"))
                .model(IndicatorGenerator.class.getName())
                .add(
                    "html",
                    "ws/rest/com.axelor.meta.db.MetaFile/"
                        + csvFile.getId()
                        + "/content/download?v="
                        + csvFile.getVersion())
                .param("download", "true")
                .map());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
