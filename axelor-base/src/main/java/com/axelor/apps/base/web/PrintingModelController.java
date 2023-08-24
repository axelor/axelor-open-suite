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
package com.axelor.apps.base.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.db.BirtPrintingWizard;
import com.axelor.apps.base.db.BirtTemplate;
import com.axelor.apps.base.db.repo.BirtTemplateRepository;
import com.axelor.apps.base.service.BirtTemplateConfigLineService;
import com.axelor.apps.base.service.PrintFromBirtTemplateService;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.axelor.utils.db.Wizard;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class PrintingModelController {

  private static final String CONTEXT_MODEL_CLASS = "_modelClass";
  private static final String CONTEXT_MODEL_ID = "_modelId";

  public void print(ActionRequest request, ActionResponse response) {

    BirtTemplateConfigLineService birtTemplateConfigLineService =
        Beans.get(BirtTemplateConfigLineService.class);
    try {
      Map<String, Object> map = getModelAndId(request);

      String modelName = map.get(CONTEXT_MODEL_CLASS).toString();
      Long recordId = (Long) map.get(CONTEXT_MODEL_ID);

      Set<BirtTemplate> birtTemplates = birtTemplateConfigLineService.getBirtTemplates(modelName);
      if (birtTemplates.size() > 1) {
        List<Long> templateIdList =
            birtTemplates.stream().map(BirtTemplate::getId).collect(Collectors.toList());
        response.setView(
            ActionView.define(I18n.get("Select template"))
                .model(Wizard.class.getName())
                .add("form", "birt-template-print-config-wizard")
                .context("_birtTemplateIdList", templateIdList)
                .context(CONTEXT_MODEL_ID, recordId)
                .context(CONTEXT_MODEL_CLASS, modelName)
                .param("popup", "true")
                .param("popup-save", "false")
                .param("show-toolbar", "false")
                .map());
        return;
      }

      BirtTemplate birtTemplate = birtTemplates.iterator().next();
      printTemplate(response, birtTemplate, Class.forName(modelName), recordId);
    } catch (AxelorException | ClassNotFoundException e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  @SuppressWarnings("unchecked")
  public void printFromWizard(ActionRequest request, ActionResponse response) {
    Context context = request.getContext();
    Object birtTemplateObj = context.get("birtTemplate");

    if (birtTemplateObj == null
        || context.get(CONTEXT_MODEL_ID) == null
        || context.get(CONTEXT_MODEL_CLASS) == null) {
      return;
    }

    try {
      Long birtTemplateId =
          Long.valueOf(((LinkedHashMap<String, Object>) birtTemplateObj).get("id").toString());
      BirtTemplate birtTemplate = Beans.get(BirtTemplateRepository.class).find(birtTemplateId);

      Class<?> klass = Class.forName(context.get(CONTEXT_MODEL_CLASS).toString());
      Long id = Long.valueOf(context.get(CONTEXT_MODEL_ID).toString());

      printTemplate(response, birtTemplate, klass, id);
    } catch (ClassNotFoundException | AxelorException e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  private Map<String, Object> getModelAndId(ActionRequest request) {
    String model = request.getModel();
    Context context = request.getContext();
    Long id = Long.valueOf(context.get("id").toString());
    if (context.getContextClass() == BirtPrintingWizard.class) {
      BirtPrintingWizard birtPrintingWizard = context.asType(BirtPrintingWizard.class);
      model = birtPrintingWizard.getMetaModel().getFullName();
      id = Long.valueOf(birtPrintingWizard.getRecordValue());
    }
    return Map.of(CONTEXT_MODEL_CLASS, model, CONTEXT_MODEL_ID, id);
  }

  @SuppressWarnings("unchecked")
  private <T extends Model> void printTemplate(
      ActionResponse response, BirtTemplate birtTemplate, Class<?> modelClass, Long modelId)
      throws AxelorException {
    Model model = JPA.find((Class<T>) modelClass, modelId);
    String outputLink = Beans.get(PrintFromBirtTemplateService.class).print(birtTemplate, model);
    if (outputLink != null) {
      response.setView(ActionView.define(birtTemplate.getName()).add("html", outputLink).map());
    }
  }
}
