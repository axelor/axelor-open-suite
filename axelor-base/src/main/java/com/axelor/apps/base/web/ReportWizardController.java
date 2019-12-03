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

import com.axelor.apps.base.db.BirtTemplate;
import com.axelor.apps.base.db.Wizard;
import com.axelor.apps.base.db.repo.BirtTemplateRepository;
import com.axelor.apps.base.exceptions.IExceptionMessage;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.base.service.message.TemplateMessageServiceBaseImpl;
import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.auth.AuthUtils;
import com.axelor.db.JpaRepository;
import com.axelor.db.Model;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.axelor.tool.template.TemplateMaker;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

public class ReportWizardController {

  @SuppressWarnings("unchecked")
  public void printReport(ActionRequest request, ActionResponse response) {

    try {
      Context context = request.getContext();
      Class<? extends Model> klass = (Class<? extends Model>) context.getContextClass();
      Long modelId = request.getContext().asType(klass).getId();
      BirtTemplateRepository repo = new BirtTemplateRepository();
      List<BirtTemplate> birtTemplateList =
          (List<BirtTemplate>)
              repo.all().filter("self.metaModel.fullName = ?", klass.getName()).fetch();

      if (birtTemplateList.size() == 0) {
        response.setFlash(IExceptionMessage.META_MODEL_NOT_FOUND);
      } else if (birtTemplateList.size() == 1) {
        BirtTemplate birtTemplate = birtTemplateList.get(0);
        String fileLink = getTemplateMaker(klass, modelId, birtTemplate);
        response.setView(ActionView.define(birtTemplate.getName()).add("html", fileLink).map());
      } else {
        ActionViewBuilder confirmView =
            ActionView.define("Report Bind")
                .model(Wizard.class.getName())
                .add("form", "report-bind-wizard-form")
                .param("popup", "true")
                .param("show-toolbar", "false")
                .param("show-confirm", "false")
                .param("popup-save", "false")
                .param("forceEdit", "true")
                .context("modelId", modelId)
                .context("modelName", klass.getName());

        response.setView(confirmView.map());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  @SuppressWarnings("unchecked")
  public void printReportFromWizard(ActionRequest request, ActionResponse response) {

    try {
      if (request.getContext().get("report") == null) {
        response.setFlash(IExceptionMessage.REPORT_FIELD_FROM_REPORT_WIZARD);
        return;
      }
      Context context = request.getContext();
      LinkedHashMap<String, Object> br = (LinkedHashMap<String, Object>) context.get("report");
      Long BirtTemplateId = Long.parseLong(br.get("id").toString());
      Long modelId = Long.parseLong(request.getContext().get("modelId").toString());
      BirtTemplate birtTemplate = Beans.get(BirtTemplateRepository.class).find(BirtTemplateId);
      String modelName = birtTemplate.getMetaModel().getFullName();
      Class<? extends Model> klass = (Class<? extends Model>) Class.forName(modelName);

      String fileLink = getTemplateMaker(klass, modelId, birtTemplate);

      response.setCanClose(true);
      response.setView(ActionView.define(birtTemplate.getName()).add("html", fileLink).map());

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  private String getTemplateMaker(
      Class<? extends Model> klass, Long modelId, BirtTemplate birtTemplate)
      throws AxelorException {
    JpaRepository<? extends Model> repo = JpaRepository.of(klass);
    Model model = repo.all().filter("self.id = ?", modelId).fetchOne();

    String fileName =
        klass.getSimpleName()
            + birtTemplate.getName()
            + "-"
            + Beans.get(AppBaseService.class).getTodayDate();

    String language = AuthUtils.getUser().getLanguage();
    TemplateMaker maker = new TemplateMaker(new Locale(language), '$', '$');
    maker.setContext(model, klass.getSimpleName());

    return getTemplateLink(maker, fileName, birtTemplate);
  }

  private String getTemplateLink(TemplateMaker maker, String fileName, BirtTemplate birtTemplate)
      throws AxelorException {

    if (birtTemplate.getEmbeddedReport() == false) {

      if (!ReportSettings.useIntegratedEngine()) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(IExceptionMessage.EMBEDDED_REPORT_ENGINE_NOT_TRUE));
      }

      if (birtTemplate.getBirtFile() == null) {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            I18n.get(IExceptionMessage.INVOICE_FIELD_FROM_BIRT_TEMPLATE));
      }

      return Beans.get(TemplateMessageServiceBaseImpl.class)
          .generateReportTemplateLink(
              maker,
              fileName,
              birtTemplate.getBirtFile().getFileName(),
              birtTemplate.getFormat(),
              birtTemplate.getBirtTemplateParameterList());
    } else {
      return Beans.get(TemplateMessageServiceBaseImpl.class)
          .generateReportTemplateLink(
              maker,
              fileName,
              birtTemplate.getTemplateLink(),
              birtTemplate.getFormat(),
              birtTemplate.getBirtTemplateParameterList());
    }
  }
}
