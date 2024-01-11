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
package com.axelor.apps.production.rest;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.service.manuforder.ManufOrderWorkflowService;
import com.google.inject.Inject;

public class ManufOrderRestServiceImpl implements ManufOrderRestService {

  protected ManufOrderWorkflowService manufOrderWorkflowService;

  @Inject
  public ManufOrderRestServiceImpl(ManufOrderWorkflowService manufOrderWorkflowService) {
    this.manufOrderWorkflowService = manufOrderWorkflowService;
  }

  public void updateStatusOfManufOrder(ManufOrder manufOrder, int targetStatus)
      throws AxelorException {
    if (manufOrder.getStatusSelect() == ManufOrderRepository.STATUS_DRAFT
        && targetStatus == ManufOrderRepository.STATUS_PLANNED) {
      manufOrderWorkflowService.plan(manufOrder);
    } else if (manufOrder.getStatusSelect() == ManufOrderRepository.STATUS_PLANNED
        && targetStatus == ManufOrderRepository.STATUS_IN_PROGRESS) {
      manufOrderWorkflowService.start(manufOrder);
    } else if (manufOrder.getStatusSelect() == ManufOrderRepository.STATUS_IN_PROGRESS
        && targetStatus == ManufOrderRepository.STATUS_STANDBY) {
      manufOrderWorkflowService.pause(manufOrder);
    } else if (manufOrder.getStatusSelect() == ManufOrderRepository.STATUS_STANDBY
        && targetStatus == ManufOrderRepository.STATUS_IN_PROGRESS) {
      manufOrderWorkflowService.resume(manufOrder);
    } else if (manufOrder.getStatusSelect() == ManufOrderRepository.STATUS_IN_PROGRESS
        && targetStatus == ManufOrderRepository.STATUS_FINISHED) {
      manufOrderWorkflowService.finish(manufOrder);
    } else {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          "This workflow is not supported for manufacturing order status.");
    }
  }
}
