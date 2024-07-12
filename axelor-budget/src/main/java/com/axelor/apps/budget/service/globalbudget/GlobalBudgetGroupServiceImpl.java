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
package com.axelor.apps.budget.service.globalbudget;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.AdvancedExport;
import com.axelor.apps.budget.db.GlobalBudget;
import com.axelor.apps.budget.db.repo.AdvancedExportBudgetRepository;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GlobalBudgetGroupServiceImpl implements GlobalBudgetGroupService {

  protected GlobalBudgetService globalBudgetService;
  protected GlobalBudgetWorkflowService globalBudgetWorkflowService;
  protected AdvancedExportBudgetRepository advancedExportBudgetRepository;

  @Inject
  public GlobalBudgetGroupServiceImpl(
      GlobalBudgetService globalBudgetService,
      GlobalBudgetWorkflowService globalBudgetWorkflowService,
      AdvancedExportBudgetRepository advancedExportBudgetRepository) {
    this.globalBudgetService = globalBudgetService;
    this.globalBudgetWorkflowService = globalBudgetWorkflowService;
    this.advancedExportBudgetRepository = advancedExportBudgetRepository;
  }

  @Override
  public void validateStructure(GlobalBudget globalBudget) throws AxelorException {

    globalBudgetWorkflowService.validateStructure(globalBudget);
  }

  @Override
  public Map<String, Object> getOnNewValuesMap() {
    Map<String, Object> valuesMap = new HashMap<>();

    valuesMap.put(
        "$advancedExportGlobalBudget", getUniqueAdvancedExportByMetaModelName("GlobalBudget"));
    valuesMap.put(
        "$advancedExportBudgetLevel", getUniqueAdvancedExportByMetaModelName("BudgetLevel"));
    valuesMap.put("$advancedExportBudget", getUniqueAdvancedExportByMetaModelName("Budget"));
    valuesMap.put(
        "$advancedExportBudgetLine", getUniqueAdvancedExportByMetaModelName("BudgetLine"));

    return valuesMap;
  }

  protected AdvancedExport getUniqueAdvancedExportByMetaModelName(String modelName) {
    List<AdvancedExport> advancedExportList =
        advancedExportBudgetRepository.findByMetaModelName(modelName);

    return advancedExportList.size() == 1 ? advancedExportList.get(0) : null;
  }
}
