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
package com.axelor.apps.production.service;

import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.quality.db.ControlPlan;
import com.axelor.apps.quality.db.repo.ControlEntryRepository;
import com.axelor.apps.quality.db.repo.ControlPlanRepository;
import com.axelor.apps.quality.service.ControlEntrySampleService;
import com.axelor.apps.quality.service.ControlEntryServiceImpl;
import com.axelor.rpc.Context;
import jakarta.inject.Inject;

public class ControlEntryProductionServiceImpl extends ControlEntryServiceImpl {

  @Inject
  public ControlEntryProductionServiceImpl(
      ControlEntrySampleService controlEntrySampleService,
      ControlPlanRepository controlPlanRepository,
      ControlEntryRepository controlEntryRepository) {
    super(controlEntrySampleService, controlPlanRepository, controlEntryRepository);
  }

  @Override
  protected String getControlPlanRelatedToSelect(Class<?> contextClass) {
    if (OperationOrder.class.equals(contextClass)) {
      return ProdProcessLine.class.getName();
    }
    return super.getControlPlanRelatedToSelect(contextClass);
  }

  @Override
  protected Long getControlPlanRelatedToSelectId(
      Class<?> contextClass, Context context, Long relatedToSelectId) {
    if (OperationOrder.class.equals(contextClass)) {
      if (context.get("prodProcessLine") != null) {
        ProdProcessLine prodProcessLine = (ProdProcessLine) context.get("prodProcessLine");
        return prodProcessLine.getId();
      }
    }
    return super.getControlPlanRelatedToSelectId(contextClass, context, relatedToSelectId);
  }

  @Override
  protected String getRelatedToSelectOnControlPlanChange(ControlPlan controlPlan) {
    if (ProdProcessLine.class.getName().equals(controlPlan.getRelatedToSelect())) {
      return OperationOrder.class.getName();
    }
    return super.getRelatedToSelectOnControlPlanChange(controlPlan);
  }

  @Override
  protected Long getRelatedToSelectIdOnControlPlanChange(
      ControlPlan controlPlan, String relatedToSelect) {
    if (ProdProcessLine.class.getName().equals(controlPlan.getRelatedToSelect())
        && !OperationOrder.class.getName().equals(relatedToSelect)) {
      return null;
    }
    return super.getRelatedToSelectIdOnControlPlanChange(controlPlan, relatedToSelect);
  }
}
