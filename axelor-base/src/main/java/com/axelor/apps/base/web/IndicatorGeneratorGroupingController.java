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

import com.axelor.apps.base.db.IndicatorGeneratorGrouping;
import com.axelor.apps.base.db.repo.IndicatorGeneratorGroupingRepository;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.apps.base.service.administration.IndicatorGeneratorGroupingService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;

@Singleton
public class IndicatorGeneratorGroupingController {

  public void run(ActionRequest request, ActionResponse response) {

    IndicatorGeneratorGrouping indicatorGeneratorGrouping =
        request.getContext().asType(IndicatorGeneratorGrouping.class);

    try {
      Beans.get(IndicatorGeneratorGroupingService.class)
          .run(
              Beans.get(IndicatorGeneratorGroupingRepository.class)
                  .find(indicatorGeneratorGrouping.getId()));
      response.setReload(true);
      response.setFlash(I18n.get(IExceptionMessage.INDICATOR_GENERATOR_3));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void export(ActionRequest request, ActionResponse response) {

    IndicatorGeneratorGrouping indicatorGeneratorGrouping =
        request.getContext().asType(IndicatorGeneratorGrouping.class);

    try {
      Beans.get(IndicatorGeneratorGroupingService.class)
          .export(
              Beans.get(IndicatorGeneratorGroupingRepository.class)
                  .find(indicatorGeneratorGrouping.getId()));
      response.setReload(true);
      response.setFlash(I18n.get(IExceptionMessage.INDICATOR_GENERATOR_GROUPING_4));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
