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

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.PrintingTemplate;
import com.axelor.apps.base.db.PrintingTemplateWizard;
import com.axelor.apps.base.db.repo.PrintingTemplateRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.exceptions.BaseExceptionMessage;
import com.axelor.apps.base.service.exception.ErrorException;
import com.axelor.apps.base.service.printing.template.PrintingTemplatePrintService;
import com.axelor.apps.base.service.printing.template.PrintingTemplateService;
import com.axelor.apps.base.service.printing.template.model.PrintingGenFactoryContext;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.common.ObjectUtils;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.axelor.utils.db.Wizard;
import com.google.inject.Singleton;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.io.FilenameUtils;

@Singleton
public class PrintingTemplateController {

  private static final String CONTEXT_MODEL_CLASS = "_modelClass";
  private static final String CONTEXT_MODEL_ID = "_modelId";
  private static final String CONTEXT_ID_LIST = "_idList";

  @ErrorException
  public void print(ActionRequest request, ActionResponse response)
      throws AxelorException, ClassNotFoundException {

    PrintingTemplateService printingTemplateService = Beans.get(PrintingTemplateService.class);
    Map<String, Object> map = getModelAndId(request);
    String modelName = map.get(CONTEXT_MODEL_CLASS).toString();
    Long recordId = (Long) map.get(CONTEXT_MODEL_ID);

    List<PrintingTemplate> printingTemplates =
        printingTemplateService.getActivePrintingTemplates(modelName);
    if (printingTemplates.size() > 1) {
      List<Long> templateIdList =
          printingTemplates.stream().map(PrintingTemplate::getId).collect(Collectors.toList());
      response.setView(
          ActionView.define(I18n.get("Select template"))
              .model(Wizard.class.getName())
              .add("form", "printing-template-print-config-wizard")
              .context("_printingTemplateIdList", templateIdList)
              .context(CONTEXT_MODEL_ID, recordId)
              .context(CONTEXT_MODEL_CLASS, modelName)
              .param("popup", "true")
              .param("popup-save", "false")
              .param("show-toolbar", "false")
              .map());
      return;
    }

    PrintingTemplate printingTemplate = printingTemplates.iterator().next();
    printTemplate(response, printingTemplate, Class.forName(modelName), recordId);
  }

  @SuppressWarnings("unchecked")
  @ErrorException
  public void printList(ActionRequest request, ActionResponse response)
      throws AxelorException, IOException {
    List<Integer> idList = (List<Integer>) request.getContext().get("_ids");
    if (ObjectUtils.isEmpty(idList)) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
          I18n.get(BaseExceptionMessage.NO_RECORD_SELECTED_TO_PRINT));
    }
    PrintingTemplateService printingTemplateService = Beans.get(PrintingTemplateService.class);
    String modelName = request.getModel();
    Class<? extends Model> contextClass =
        (Class<? extends Model>) request.getContext().getContextClass();

    List<PrintingTemplate> printingTemplates =
        printingTemplateService.getActivePrintingTemplates(modelName);
    if (printingTemplates.size() > 1) {
      List<Long> templateIdList =
          printingTemplates.stream().map(PrintingTemplate::getId).collect(Collectors.toList());
      response.setView(
          ActionView.define(I18n.get("Select template"))
              .model(Wizard.class.getName())
              .add("form", "printing-template-print-config-wizard")
              .context("_printingTemplateIdList", templateIdList)
              .context(CONTEXT_ID_LIST, idList)
              .context(CONTEXT_MODEL_CLASS, modelName)
              .param("popup", "true")
              .param("popup-save", "false")
              .param("show-toolbar", "false")
              .map());
      return;
    }

    PrintingTemplate printingTemplate = printingTemplates.iterator().next();
    String outputLink =
        Beans.get(PrintingTemplatePrintService.class)
            .getPrintLinkForList(idList, contextClass, printingTemplate);
    print(response, printingTemplate, outputLink);
  }

  @ErrorException
  @SuppressWarnings("unchecked")
  public void printFromWizard(ActionRequest request, ActionResponse response)
      throws ClassNotFoundException, AxelorException, IOException {
    Context context = request.getContext();
    Object printingTemplateObj = context.get("printingTemplate");

    if (printingTemplateObj == null || context.get(CONTEXT_MODEL_CLASS) == null) {
      return;
    }

    Long printingTemplateId =
        Long.valueOf(((LinkedHashMap<String, Object>) printingTemplateObj).get("id").toString());
    PrintingTemplate printingTemplate =
        Beans.get(PrintingTemplateRepository.class).find(printingTemplateId);

    Class<?> klass = Class.forName(context.get(CONTEXT_MODEL_CLASS).toString());
    if (context.get(CONTEXT_MODEL_ID) != null) {
      Long id = Long.valueOf(context.get(CONTEXT_MODEL_ID).toString());
      printTemplate(response, printingTemplate, klass, id);
    } else {
      List<Integer> idList = (List<Integer>) context.get(CONTEXT_ID_LIST);
      if (idList != null && idList.size() == 1) {
        printTemplate(response, printingTemplate, klass, Long.valueOf(idList.get(0)));
      } else {
        String outputLink =
            Beans.get(PrintingTemplatePrintService.class)
                .getPrintLinkForList(idList, (Class<? extends Model>) klass, printingTemplate);

        print(response, printingTemplate, outputLink);
      }
    }
  }

  @SuppressWarnings("unchecked")
  protected <T extends Model> void printTemplate(
      ActionResponse response, PrintingTemplate printingTemplate, Class<?> modelClass, Long modelId)
      throws AxelorException {
    Model model = JPA.find((Class<T>) modelClass, modelId);

    String outputLink =
        Beans.get(PrintingTemplatePrintService.class)
            .getPrintLink(printingTemplate, new PrintingGenFactoryContext(model));
    print(response, printingTemplate, outputLink);
  }

  protected Map<String, Object> getModelAndId(ActionRequest request) {
    String model = request.getModel();
    Context context = request.getContext();
    Long id = Long.valueOf(context.get("id").toString());
    if (context.getContextClass() == PrintingTemplateWizard.class) {
      PrintingTemplateWizard printingTemplateWizard = context.asType(PrintingTemplateWizard.class);
      model = printingTemplateWizard.getMetaModel().getFullName();
      id = Long.valueOf(printingTemplateWizard.getRecordValue());
    }
    return Map.of(CONTEXT_MODEL_CLASS, model, CONTEXT_MODEL_ID, id);
  }

  protected void print(
      ActionResponse response, PrintingTemplate printingTemplate, String outputLink) {
    if (ReportSettings.FORMAT_PDF.equals(FilenameUtils.getExtension(outputLink))) {
      response.setView(
          ActionView.define(I18n.get(printingTemplate.getName())).add("html", outputLink).map());
    } else {
      response.setExportFile(outputLink);
    }
  }
}
