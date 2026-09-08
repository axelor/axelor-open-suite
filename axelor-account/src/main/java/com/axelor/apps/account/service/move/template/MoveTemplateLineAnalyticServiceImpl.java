/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2026 Axelor (<http://axelor.com>).
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
package com.axelor.apps.account.service.move.template;

import com.axelor.apps.account.db.MoveTemplate;
import com.axelor.apps.account.db.MoveTemplateLine;
import com.axelor.apps.account.service.analytic.AnalyticAttrsService;
import com.axelor.apps.account.service.analytic.AnalyticGroupService;
import com.axelor.apps.account.service.analytic.AnalyticLineService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import jakarta.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class MoveTemplateLineAnalyticServiceImpl implements MoveTemplateLineAnalyticService {

  protected MoveTemplateLineComputeAnalyticService moveTemplateLineComputeAnalyticService;
  protected AnalyticLineService analyticLineService;
  protected AnalyticGroupService analyticGroupService;
  protected AnalyticAttrsService analyticAttrsService;
  protected AccountConfigService accountConfigService;

  @Inject
  public MoveTemplateLineAnalyticServiceImpl(
      MoveTemplateLineComputeAnalyticService moveTemplateLineComputeAnalyticService,
      AnalyticLineService analyticLineService,
      AnalyticGroupService analyticGroupService,
      AnalyticAttrsService analyticAttrsService,
      AccountConfigService accountConfigService) {
    this.moveTemplateLineComputeAnalyticService = moveTemplateLineComputeAnalyticService;
    this.analyticLineService = analyticLineService;
    this.analyticGroupService = analyticGroupService;
    this.analyticAttrsService = analyticAttrsService;
    this.accountConfigService = accountConfigService;
  }

  @Override
  public Map<String, Object> getAnalyticDistributionTemplateOnChangeValuesMap(
      MoveTemplateLine moveTemplateLine, MoveTemplate moveTemplate) throws AxelorException {
    moveTemplateLineComputeAnalyticService.clearAnalyticAccounting(moveTemplateLine);
    moveTemplateLineComputeAnalyticService.createAnalyticDistributionWithTemplate(moveTemplateLine);
    analyticLineService.setAnalyticAccount(moveTemplateLine, moveTemplate.getCompany());

    if (moveTemplateLine.getAnalyticDistributionTemplate() == null) {
      moveTemplateLineComputeAnalyticService.clearAnalyticAccounting(moveTemplateLine);
    }

    return analyticGroupService.createAnalyticValuesMap(moveTemplateLine);
  }

  @Override
  public Map<String, Map<String, Object>> getAnalyticDistributionTemplateOnChangeAttrsMap(
      MoveTemplateLine moveTemplateLine, MoveTemplate moveTemplate) throws AxelorException {
    Map<String, Map<String, Object>> attrsMap = new HashMap<>();

    Company company = moveTemplate != null ? moveTemplate.getCompany() : null;
    analyticAttrsService.addAnalyticAccountRequired(moveTemplateLine, company, attrsMap);

    return attrsMap;
  }

  @Override
  public Map<String, Map<String, Object>> getAnalyticDistributionTemplateOnSelectAttrsMap(
      MoveTemplateLine moveTemplateLine, MoveTemplate moveTemplate) throws AxelorException {
    Map<String, Map<String, Object>> attrsMap = new HashMap<>();

    if (moveTemplate == null) {
      return attrsMap;
    }

    analyticAttrsService.addAnalyticDistributionTemplateDomain(
        moveTemplateLine,
        moveTemplateLine.getPartner(),
        moveTemplateLine.getProduct(),
        moveTemplate.getCompany(),
        null,
        attrsMap);

    return attrsMap;
  }

  @Override
  public Map<String, Object> getAnalyticAxisOnChangeValuesMap(
      MoveTemplateLine moveTemplateLine, MoveTemplate moveTemplate) throws AxelorException {
    moveTemplateLineComputeAnalyticService.clearAnalyticAccountingIfEmpty(moveTemplateLine);
    moveTemplateLineComputeAnalyticService.analyzeMoveTemplateLine(
        moveTemplateLine, moveTemplate.getCompany());
    moveTemplateLineComputeAnalyticService.clearAnalyticAccountingIfEmpty(moveTemplateLine);

    Map<String, Object> valuesMap = new HashMap<>();

    valuesMap.put("analyticMoveLineList", moveTemplateLine.getAnalyticMoveLineList());

    return valuesMap;
  }

  @Override
  public Map<String, Map<String, Object>> getAnalyticAxisOnChangeAttrsMap(
      MoveTemplateLine moveTemplateLine, MoveTemplate moveTemplate) throws AxelorException {
    Map<String, Map<String, Object>> attrsMap = new HashMap<>();

    Company company = moveTemplate != null ? moveTemplate.getCompany() : null;
    analyticAttrsService.addAnalyticAccountRequired(moveTemplateLine, company, attrsMap);

    return attrsMap;
  }

  @Override
  public Map<String, Object> getAnalyticMoveLineOnChangeValuesMap(
      MoveTemplateLine moveTemplateLine, MoveTemplate moveTemplate) throws AxelorException {
    analyticLineService.setAnalyticAccount(moveTemplateLine, moveTemplate.getCompany());

    return analyticGroupService.createAnalyticValuesMap(moveTemplateLine);
  }

  @Override
  public Map<String, Map<String, Object>> getAnalyticMoveLineOnChangeAttrsMap(
      MoveTemplateLine moveTemplateLine, MoveTemplate moveTemplate) throws AxelorException {
    Map<String, Map<String, Object>> attrsMap = new HashMap<>();

    Company company = moveTemplate != null ? moveTemplate.getCompany() : null;
    analyticAttrsService.addAnalyticAccountRequired(moveTemplateLine, company, attrsMap);

    return attrsMap;
  }

  @Override
  public Map<String, Object> getAccountOnChangeValuesMap(
      MoveTemplateLine moveTemplateLine, MoveTemplate moveTemplate) throws AxelorException {

    Map<String, Object> valuesMap =
        new HashMap<>(
            getAnalyticDistributionTemplateOnChangeValuesMap(moveTemplateLine, moveTemplate));

    valuesMap.put(
        "analyticDistributionTemplate", moveTemplateLine.getAnalyticDistributionTemplate());

    return valuesMap;
  }

  @Override
  public Map<String, Map<String, Object>> getAccountOnChangeAttrsMap(
      MoveTemplateLine moveTemplateLine, MoveTemplate moveTemplate) throws AxelorException {
    Map<String, Map<String, Object>> attrsMap = new HashMap<>();

    Company company = moveTemplate != null ? moveTemplate.getCompany() : null;
    analyticAttrsService.addAnalyticAccountRequired(moveTemplateLine, company, attrsMap);

    if (company != null) {
      analyticAttrsService.addAnalyticAxisAttrs(company, null, attrsMap);
    }

    return attrsMap;
  }

  @Override
  public Map<String, Object> getOnLoadAnalyticDistributionValuesMap(
      MoveTemplateLine moveTemplateLine, MoveTemplate moveTemplate) throws AxelorException {
    Map<String, Object> valuesMap = new HashMap<>();

    valuesMap.put(
        "$analyticDistributionTypeSelect",
        accountConfigService
            .getAccountConfig(moveTemplate.getCompany())
            .getAnalyticDistributionTypeSelect());

    return valuesMap;
  }

  @Override
  public Map<String, Map<String, Object>> getOnLoadAnalyticDistributionAttrsMap(
      MoveTemplateLine moveTemplateLine, MoveTemplate moveTemplate) throws AxelorException {
    Map<String, Map<String, Object>> attrsMap = new HashMap<>();

    if (moveTemplate == null || moveTemplate.getCompany() == null) {
      return attrsMap;
    }

    boolean condition =
        accountConfigService
                .getAccountConfig(moveTemplate.getCompany())
                .getManageAnalyticAccounting()
            && moveTemplateLine.getAccount() != null
            && moveTemplateLine.getAccount().getAnalyticDistributionAuthorized();

    attrsMap.put("analyticDistributionPanel", new HashMap<>());
    attrsMap.get("analyticDistributionPanel").put("hidden", !condition);

    analyticAttrsService.addAnalyticAxisAttrs(moveTemplate.getCompany(), null, attrsMap);

    return attrsMap;
  }
}
