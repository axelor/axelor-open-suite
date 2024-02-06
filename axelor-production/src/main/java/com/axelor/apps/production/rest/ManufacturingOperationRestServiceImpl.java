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
import com.axelor.apps.production.db.ManufacturingOperation;
import com.axelor.apps.production.db.repo.ManufacturingOperationRepository;
import com.axelor.apps.production.rest.dto.ManufacturingOperationResponse;
import com.axelor.apps.production.service.manufacturingoperation.ManufacturingOperationWorkflowService;
import com.axelor.apps.production.service.manuforder.ManufOrderWorkflowService;
import com.axelor.apps.production.translation.ITranslation;
import com.axelor.auth.AuthUtils;
import com.axelor.i18n.I18n;
import com.axelor.utils.api.ResponseConstructor;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import javax.ws.rs.core.Response;

public class ManufacturingOperationRestServiceImpl implements ManufacturingOperationRestService {

  protected ManufacturingOperationWorkflowService manufacturingOperationWorkflowService;
  protected ManufOrderWorkflowService manufOrderWorkflowService;

  @Inject
  public ManufacturingOperationRestServiceImpl(
      ManufacturingOperationWorkflowService manufacturingOperationWorkflowService,
      ManufOrderWorkflowService manufOrderWorkflowService) {
    this.manufacturingOperationWorkflowService = manufacturingOperationWorkflowService;
    this.manufOrderWorkflowService = manufOrderWorkflowService;
  }

  public Response updateStatusOfManufacturingOperation(
      ManufacturingOperation manufacturingOperation, Integer targetStatus) throws AxelorException {
    ManufOrder manufOrder = manufacturingOperation.getManufOrder();
    if ((manufacturingOperation.getStatusSelect() == ManufacturingOperationRepository.STATUS_PLANNED
            || manufacturingOperation.getStatusSelect()
                == ManufacturingOperationRepository.STATUS_IN_PROGRESS)
        && targetStatus == ManufacturingOperationRepository.STATUS_IN_PROGRESS) {
      manufacturingOperationWorkflowService.start(manufacturingOperation);
    } else if (manufacturingOperation.getStatusSelect()
            == ManufacturingOperationRepository.STATUS_IN_PROGRESS
        && targetStatus == ManufacturingOperationRepository.STATUS_STANDBY) {
      manufacturingOperationWorkflowService.pause(manufacturingOperation, AuthUtils.getUser());
      // Operation order not paused
      if (manufacturingOperation.getStatusSelect()
          != ManufacturingOperationRepository.STATUS_STANDBY) {
        return ResponseConstructor.build(
            Response.Status.OK,
            I18n.get(ITranslation.MANUFACTURING_OPERATION_DURATION_PAUSED_200),
            new ManufacturingOperationResponse((manufacturingOperation)));
      }
    } else if (manufacturingOperation.getStatusSelect()
            == ManufacturingOperationRepository.STATUS_STANDBY
        && targetStatus == ManufacturingOperationRepository.STATUS_IN_PROGRESS) {
      manufacturingOperationWorkflowService.resume(manufacturingOperation);
    } else if ((manufacturingOperation.getStatusSelect()
                == ManufacturingOperationRepository.STATUS_IN_PROGRESS
            || manufacturingOperation.getStatusSelect()
                == ManufacturingOperationRepository.STATUS_STANDBY)
        && targetStatus == ManufacturingOperationRepository.STATUS_FINISHED) {
      finishProcess(manufacturingOperation, manufOrder);
      manufOrderWorkflowService.sendFinishedMail(manufOrder);

      if (manufacturingOperation.getStatusSelect()
          != ManufacturingOperationRepository.STATUS_FINISHED) {
        return ResponseConstructor.build(
            Response.Status.FORBIDDEN,
            I18n.get(ITranslation.MANUFACTURING_OPERATION_DURATION_PAUSED_403),
            new ManufacturingOperationResponse((manufacturingOperation)));
      }
    } else {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(ITranslation.MANUFACTURING_OPERATION_WORKFLOW_NOT_SUPPORTED));
    }

    return ResponseConstructor.build(
        Response.Status.OK,
        "Operation order status successfully updated.",
        new ManufacturingOperationResponse((manufacturingOperation)));
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void finishProcess(ManufacturingOperation manufacturingOperation, ManufOrder manufOrder)
      throws AxelorException {
    manufacturingOperationWorkflowService.finish(manufacturingOperation, AuthUtils.getUser());
    manufOrderWorkflowService.allOpFinished(manufOrder);
  }
}
