/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.quality.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.service.exception.HandleExceptionResponse;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.quality.db.*;
import com.axelor.apps.quality.db.repo.ControlPointRepository;
import com.axelor.apps.quality.db.repo.QualityControlRepository;
import com.axelor.apps.quality.db.repo.QualityProcessRepository;
import com.axelor.apps.quality.service.ControlEntryPlanLineService;
import com.axelor.apps.quality.service.QIAnalysisService;
import com.axelor.apps.quality.service.QualityControlService;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.inject.Singleton;

import javax.annotation.Nullable;
import java.util.*;

@Singleton
public class ControlEntryPlanLineController {

  public void checkConformity(ActionRequest request, ActionResponse response) throws AxelorException {

      ControlEntryPlanLine controlEntryPlanLine = request.getContext().asType(ControlEntryPlanLine.class);
      Beans.get(ControlEntryPlanLineService.class).conformityEval(controlEntryPlanLine);
      response.setValue("resultSelect", controlEntryPlanLine.getResultSelect());

  }

}
