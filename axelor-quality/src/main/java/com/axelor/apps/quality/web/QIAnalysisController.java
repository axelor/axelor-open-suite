/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.quality.db.QIActionDistribution;
import com.axelor.apps.quality.db.QIAnalysis;
import com.axelor.apps.quality.service.QIAnalysisService;
import com.axelor.db.mapper.Mapper;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.message.db.Template;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.axelor.utils.db.Wizard;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class QIAnalysisController {

  public void setAdvancement(ActionRequest request, ActionResponse response) {
    try {
      QIAnalysis qiAnalysis = request.getContext().asType(QIAnalysis.class);
      int advancement = Beans.get(QIAnalysisService.class).setAdvancement(qiAnalysis);
      response.setValue("advancement", advancement);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void generateQIActionDistribution(ActionRequest request, ActionResponse response) {
    try {
      QIAnalysis qiAnalysis = request.getContext().asType(QIAnalysis.class);
      List<QIActionDistribution> qiActionDistributionList =
          Beans.get(QIAnalysisService.class).generateQIActionDistribution(qiAnalysis);
      response.setReload(true);

      if (CollectionUtils.isNotEmpty(qiActionDistributionList)) {
        response.setView(
            ActionView.define(I18n.get("Send QI Action distributions"))
                .model(Wizard.class.getName())
                .add("form", "qi-action-distribution-wizard-form")
                .context("_qiActionDistributionList", qiActionDistributionList)
                .param("popup", "reload")
                .param("popup-save", "false")
                .param("show-toolbar", "false")
                .param("show-confirm", "false")
                .map());
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  @SuppressWarnings("unchecked")
  public void generateQIActionDistributionForOthers(
      ActionRequest request, ActionResponse response) {
    try {
      Context context = request.getContext();
      QIAnalysis qiAnalysis =
          Mapper.toBean(QIAnalysis.class, (Map<String, Object>) context.get("_qiAnalysis"));
      Integer recepient = (Integer) request.getContext().get("recipient");
      Partner recepientPartner = (Partner) context.get("recipientPartner");
      List<QIActionDistribution> qiActionDistributionList =
          Beans.get(QIAnalysisService.class)
              .generateQIActionDistributionForOthers(qiAnalysis, recepient, recepientPartner);
      if (CollectionUtils.isNotEmpty(qiActionDistributionList)) {
        response.setView(
            ActionView.define(I18n.get("Send QI Action distributions"))
                .model(Wizard.class.getName())
                .add("form", "qi-action-distribution-wizard-form")
                .context("_qiActionDistributionList", qiActionDistributionList)
                .param("popup", "reload")
                .param("popup-save", "false")
                .param("show-toolbar", "false")
                .param("show-confirm", "false")
                .map());
      }

    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  @SuppressWarnings("unchecked")
  public void sendQIActionDistributions(ActionRequest request, ActionResponse response) {
    try {
      Context context = request.getContext();

      List<QIActionDistribution> qiActionDistributionList =
          ((Collection<Map<String, Object>>) context.get("qiActionDistributionList"))
              .stream()
                  .map(map -> Mapper.toBean(QIActionDistribution.class, map))
                  .collect(Collectors.toList());

      Template qiActionDistributionMessageTemplate =
          Mapper.toBean(
              Template.class,
              (Map<String, Object>) context.get("qiActionDistributionMessageTemplate"));

      Beans.get(QIAnalysisService.class)
          .sendQIActionDistributions(qiActionDistributionList, qiActionDistributionMessageTemplate);
      response.setCanClose(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
