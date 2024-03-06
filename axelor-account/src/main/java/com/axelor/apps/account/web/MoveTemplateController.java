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
package com.axelor.apps.account.web;

import com.axelor.apps.account.db.Move;
import com.axelor.apps.account.db.MoveTemplate;
import com.axelor.apps.account.db.MoveTemplateLine;
import com.axelor.apps.account.db.MoveTemplateType;
import com.axelor.apps.account.db.repo.MoveTemplateRepository;
import com.axelor.apps.account.db.repo.MoveTemplateTypeRepository;
import com.axelor.apps.account.exception.AccountExceptionMessage;
import com.axelor.apps.account.service.move.MoveTemplateService;
import com.axelor.apps.account.service.move.MoveViewHelperService;
import com.axelor.apps.base.ResponseMessageType;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import com.google.common.base.Joiner;
import com.google.inject.Singleton;
import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class MoveTemplateController {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public void checkValidity(ActionRequest request, ActionResponse response) {
    MoveTemplate moveTemplate = request.getContext().asType(MoveTemplate.class);
    moveTemplate = Beans.get(MoveTemplateRepository.class).find(moveTemplate.getId());

    boolean valid = Beans.get(MoveTemplateService.class).checkValidity(moveTemplate);

    if (valid) {
      response.setReload(true);
    } else {
      response.setInfo(I18n.get(AccountExceptionMessage.MOVE_TEMPLATE_1));
    }
  }

  @SuppressWarnings("unchecked")
  public void generateMove(ActionRequest request, ActionResponse response) {

    try {
      Context context = request.getContext();

      HashMap<String, Object> moveTemplateTypeMap =
          (HashMap<String, Object>) context.get("moveTemplateType");
      MoveTemplateType moveTemplateType =
          Beans.get(MoveTemplateTypeRepository.class)
              .find(Long.parseLong(moveTemplateTypeMap.get("id").toString()));

      HashMap<String, Object> moveTemplateMap =
          (HashMap<String, Object>) context.get("moveTemplate");
      MoveTemplate moveTemplate = null;
      if (moveTemplateType.getTypeSelect() == MoveTemplateTypeRepository.TYPE_PERCENTAGE) {
        moveTemplate =
            Beans.get(MoveTemplateRepository.class)
                .find(Long.parseLong(moveTemplateMap.get("id").toString()));
      }

      List<HashMap<String, Object>> dataList =
          (List<HashMap<String, Object>>) context.get("dataInputList");

      List<HashMap<String, Object>> moveTemplateList =
          (List<HashMap<String, Object>>) context.get("moveTemplateSet");

      LocalDate moveDate = null;
      if (moveTemplateType.getTypeSelect() == MoveTemplateTypeRepository.TYPE_AMOUNT) {
        moveDate =
            LocalDate.parse(
                (String) context.get("moveDate"), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
      }

      LOG.debug("MoveTemplate : {}", moveTemplate);
      LOG.debug("Data inputlist : {}", dataList);
      LOG.debug("Data inputlist : {}", moveTemplateList);

      if ((dataList != null && !dataList.isEmpty())
          || (moveTemplateList != null && !moveTemplateList.isEmpty())) {
        MoveTemplateService moveTemplateService = Beans.get(MoveTemplateService.class);
        List<Long> moveList =
            moveTemplateService.generateMove(
                moveTemplateType, moveTemplate, dataList, moveDate, moveTemplateList);
        List<String> exceptionsList = moveTemplateService.getExceptionsList();
        if (!CollectionUtils.isEmpty(exceptionsList)) {
          response.setInfo(Joiner.on("<br>").join(exceptionsList));
        }
        if (!CollectionUtils.isEmpty(moveList)) {
          response.setView(
              ActionView.define(I18n.get(AccountExceptionMessage.MOVE_TEMPLATE_3))
                  .model(Move.class.getName())
                  .add("grid", "move-grid")
                  .add("form", "move-form")
                  .param("search-filters", "move-filters")
                  .domain("self.id in (" + Joiner.on(",").join(moveList) + ")")
                  .map());
        }
      } else {
        response.setInfo(I18n.get(AccountExceptionMessage.MOVE_TEMPLATE_4));
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e, ResponseMessageType.ERROR);
    }
  }

  public void setIsValid(ActionRequest request, ActionResponse response) {
    MoveTemplate moveTemplate = request.getContext().asType(MoveTemplate.class);
    if (moveTemplate.getIsValid()) {
      boolean isValid = true;
      for (MoveTemplateLine line : moveTemplate.getMoveTemplateLineList()) {
        if (!line.getIsValid()) {
          isValid = false;
        }
      }
      if (!isValid) {
        response.setValue("isValid", false);
      }
    }
  }

  public void computeTotals(ActionRequest request, ActionResponse response) {
    try {
      MoveTemplate moveTemplate = request.getContext().asType(MoveTemplate.class);
      Map<String, Object> values = Beans.get(MoveTemplateService.class).computeTotals(moveTemplate);
      response.setValues(values);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void filterPartner(ActionRequest request, ActionResponse response) {
    try {
      MoveTemplate moveTemplate = request.getContext().getParent().asType(MoveTemplate.class);
      if (moveTemplate != null) {
        String domain =
            Beans.get(MoveViewHelperService.class)
                .filterPartner(moveTemplate.getCompany(), moveTemplate.getJournal());
        response.setAttr("partner", "domain", domain);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
