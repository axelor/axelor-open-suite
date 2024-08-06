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
package com.axelor.apps.gdpr.web;

import com.axelor.apps.base.callable.ControllerCallableTool;
import com.axelor.apps.base.db.AnonymizerLine;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.gdpr.db.GDPRProcessingRegister;
import com.axelor.apps.gdpr.db.GDPRProcessingRegisterRule;
import com.axelor.apps.gdpr.db.repo.GDPRProcessingRegisterRepository;
import com.axelor.apps.gdpr.db.repo.GDPRProcessingRegisterRuleRepository;
import com.axelor.apps.gdpr.service.GdprAnonymizeService;
import com.axelor.apps.gdpr.service.GdprProcessingRegisterService;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class GdprProcessingRegisterController {

  public void addAnonymizer(ActionRequest request, ActionResponse response) {
    Map<String, Object> parent = (Map<String, Object>) request.getContext().get("_parent");

    if (Objects.isNull(parent)) return;

    List<Map> gdprProcessingRegisterRuleList =
        (List<Map>) parent.get("gdprProcessingRegisterRuleList");

    if (Objects.isNull(gdprProcessingRegisterRuleList)) return;

    List<AnonymizerLine> anonymizerLines = new ArrayList<>();

    for (Map map : gdprProcessingRegisterRuleList) {
      MetaModel metaModel = null;
      if (map.get("metaModel") == null) {
        GDPRProcessingRegisterRule gdprProcessingRegisterRule =
            Beans.get(GDPRProcessingRegisterRuleRepository.class)
                .find(Long.decode(map.get("id").toString()));
        metaModel = gdprProcessingRegisterRule.getMetaModel();
      }

      if (map.get("metaModel") != null) {
        Map metaModelMap = (Map) map.get("metaModel");
        metaModel =
            Beans.get(MetaModelRepository.class)
                .find(Long.decode(metaModelMap.get("id").toString()));
      }

      List<MetaField> metaFields =
          metaModel.getMetaFields().stream()
              .filter(mf -> Objects.isNull(mf.getRelationship()))
              .filter(mf -> !GdprAnonymizeService.excludeFields.contains(mf.getName()))
              .collect(Collectors.toList());

      for (MetaField metaField : metaFields) {
        AnonymizerLine anonymizerLine = new AnonymizerLine();
        anonymizerLine.setMetaModel(metaModel);
        anonymizerLine.setMetaField(metaField);
        anonymizerLines.add(anonymizerLine);
      }
    }

    response.setValue("anonymizerLineList", anonymizerLines);
  }

  public void launchProcessingRegister(ActionRequest request, ActionResponse response) {

    List<GDPRProcessingRegister> processingRegisters = new ArrayList<>();

    if (request.getContext().get("id") != null) {
      GDPRProcessingRegister gdprProcessingRegister =
          request.getContext().asType(GDPRProcessingRegister.class);
      gdprProcessingRegister =
          Beans.get(GDPRProcessingRegisterRepository.class).find(gdprProcessingRegister.getId());
      processingRegisters.add(gdprProcessingRegister);
    } else {
      processingRegisters =
          Beans.get(GDPRProcessingRegisterRepository.class)
              .findByStatus(GDPRProcessingRegisterRepository.PROCESSING_REGISTER_STATUS_ACTIVE)
              .fetch();
    }

    try {
      GdprProcessingRegisterService gdprProcessingRegisterService =
          Beans.get(GdprProcessingRegisterService.class);
      gdprProcessingRegisterService.setGdprProcessingRegister(processingRegisters);

      ControllerCallableTool<List<GDPRProcessingRegister>> controllerCallableTool =
          new ControllerCallableTool<>();
      controllerCallableTool.runInSeparateThread(gdprProcessingRegisterService, response);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    } finally {
      response.setReload(true);
    }
  }
}
