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
package com.axelor.apps.docusign.web;

import com.axelor.apps.base.db.Wizard;
import com.axelor.apps.docusign.db.DocuSignEnvelope;
import com.axelor.apps.docusign.db.DocuSignEnvelopeSetting;
import com.axelor.apps.docusign.db.repo.DocuSignEnvelopeRepository;
import com.axelor.apps.docusign.db.repo.DocuSignEnvelopeSettingRepository;
import com.axelor.apps.docusign.service.DocuSignEnvelopeService;
import com.axelor.db.Model;
import com.axelor.db.Query;
import com.axelor.exception.ResponseMessageType;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import java.util.Map;
import org.apache.commons.collections.MapUtils;

public class DocuSignEnvelopeController {

  public void createEnvelope(ActionRequest request, ActionResponse response) {
    Model context = request.getContext().asType(Model.class);
    String model = request.getModel();

    String simpleModel = model.substring(model.lastIndexOf(".") + 1);

    Query<DocuSignEnvelopeSetting> envelopeSettingQuery =
        Beans.get(DocuSignEnvelopeSettingRepository.class)
            .all()
            .filter("self.metaModel.fullName = ?", model);

    try {
      long settingsCount = envelopeSettingQuery.count();

      if (settingsCount == 0) {
        response.setView(
            ActionView.define(I18n.get("Create envelope"))
                .model(DocuSignEnvelope.class.getName())
                .add("form", "docusign-envelope-form")
                .param("forceEdit", "true")
                .context("_templateContextModel", model)
                .context("_objectId", context.getId().toString())
                .map());
      } else if (settingsCount == 1) {
        response.setView(
            Beans.get(DocuSignEnvelopeService.class)
                .generateEnvelope(envelopeSettingQuery.fetchOne(), context.getId()));
      } else if (settingsCount >= 2) {
        response.setView(
            ActionView.define(I18n.get("Select envelope setting"))
                .model(Wizard.class.getName())
                .add("form", "docusign-select-envelope-setting-wizard-form")
                .param("show-confirm", "false")
                .context("_objectId", context.getId().toString())
                .context("_templateContextModel", model)
                .context("_simpleModel", simpleModel)
                .map());
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void createEnvelopeFromWizard(ActionRequest request, ActionResponse response) {
    Context context = request.getContext();

    Map envelopeSettingContext = (Map) context.get("envelopeSetting");
    DocuSignEnvelopeSetting envelopeSetting;
    if (MapUtils.isNotEmpty(envelopeSettingContext)) {
      envelopeSetting =
          Beans.get(DocuSignEnvelopeSettingRepository.class)
              .find(Long.parseLong(envelopeSettingContext.get("id").toString()));

      Long objectId = Long.parseLong(context.get("_objectId").toString());
      String model = (String) context.get("_templateContextModel");
      String simpleModel = (String) context.get("_simpleModel");

      try {
        response.setView(
            Beans.get(DocuSignEnvelopeService.class).generateEnvelope(envelopeSetting, objectId));
      } catch (Exception e) {
        TraceBackService.trace(response, e);
      }
    }
  }

  public void sendEnvelope(ActionRequest request, ActionResponse response) {
    try {
      DocuSignEnvelope envelope = request.getContext().asType(DocuSignEnvelope.class);
      envelope = Beans.get(DocuSignEnvelopeRepository.class).find(envelope.getId());
      Beans.get(DocuSignEnvelopeService.class).sendEnvelope(envelope);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void synchroniseEnvelopeStatus(ActionRequest request, ActionResponse response) {
    try {
      DocuSignEnvelope envelope = request.getContext().asType(DocuSignEnvelope.class);
      envelope = Beans.get(DocuSignEnvelopeRepository.class).find(envelope.getId());
      Beans.get(DocuSignEnvelopeService.class).synchroniseEnvelopeStatus(envelope);
      response.setReload(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
      response.setError(I18n.get(e.getMessage()));
    }
  }
}
