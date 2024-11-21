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
package com.axelor.apps.quality.web;

import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.quality.db.QIDecisionDistribution;
import com.axelor.apps.quality.db.QIResolution;
import com.axelor.apps.quality.db.repo.QIResolutionRepository;
import com.axelor.apps.quality.service.QIResolutionService;
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

public class QIResolutionController {

  public void generateQIDecisionDistributions(ActionRequest request, ActionResponse response) {
    try {
      QIResolution qiResolution = request.getContext().asType(QIResolution.class);
      qiResolution = Beans.get(QIResolutionRepository.class).find(qiResolution.getId());

      List<QIDecisionDistribution> qiDecisionDistributionList =
          Beans.get(QIResolutionService.class).generateQIDecisionDistributions(qiResolution);
      response.setReload(true);

      if (CollectionUtils.isNotEmpty(qiDecisionDistributionList)) {
        response.setView(
            ActionView.define(I18n.get("Send QI Decision distributions"))
                .model(Wizard.class.getName())
                .add("form", "qi-decision-distribution-send-form")
                .context("_qiDecisionDistributionList", qiDecisionDistributionList)
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
  public void sendQIDecisionDistributions(ActionRequest request, ActionResponse response) {
    try {
      Context context = request.getContext();
      List<QIDecisionDistribution> qiDecisionDistributionList =
          ((Collection<Map<String, Object>>) context.get("qiDecisionDistributionList"))
              .stream()
                  .map(map -> Mapper.toBean(QIDecisionDistribution.class, map))
                  .collect(Collectors.toList());
      Template qiDecisionDistributionMessageTemplate =
          Mapper.toBean(
              Template.class,
              (Map<String, Object>) context.get("qiDecisionDistributionMessageTemplate"));

      Beans.get(QIResolutionService.class)
          .sendQIDecisionDistributions(
              qiDecisionDistributionList, qiDecisionDistributionMessageTemplate);
      response.setCanClose(true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
