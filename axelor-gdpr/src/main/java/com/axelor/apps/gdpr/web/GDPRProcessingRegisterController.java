package com.axelor.apps.gdpr.web;

import com.axelor.apps.base.callable.ControllerCallableTool;
import com.axelor.apps.base.db.AnonymizerLine;
import com.axelor.apps.gdpr.db.GDPRProcessingRegister;
import com.axelor.apps.gdpr.db.GDPRProcessingRegisterRule;
import com.axelor.apps.gdpr.db.repo.GDPRProcessingRegisterRepository;
import com.axelor.apps.gdpr.db.repo.GDPRProcessingRegisterRuleRepository;
import com.axelor.apps.gdpr.service.GDPRAnonymizeService;
import com.axelor.apps.gdpr.service.GDPRProcessingRegisterService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.*;
import java.util.stream.Collectors;

public class GDPRProcessingRegisterController {

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
              .filter(mf -> !GDPRAnonymizeService.excludeFields.contains(mf.getName()))
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
      GDPRProcessingRegisterService gdprProcessingRegisterService =
          Beans.get(GDPRProcessingRegisterService.class);
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
