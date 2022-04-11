/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2021 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.hr.web;

import com.axelor.apps.base.db.Batch;
import com.axelor.apps.hr.db.HrBatch;
import com.axelor.apps.hr.db.repo.HrBatchRepository;
import com.axelor.apps.hr.service.batch.HrBatchService;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;

@Singleton
public class HrBatchController {

  /**
   * Launch any type of HR batch
   *
   * @param request
   * @param response
   * @throws AxelorException
   */
  public void launchHrBatch(ActionRequest request, ActionResponse response) throws AxelorException {

    HrBatch hrBatch = request.getContext().asType(HrBatch.class);

    Batch batch =
        Beans.get(HrBatchService.class)
            .run(Beans.get(HrBatchRepository.class).find(hrBatch.getId()));

    if (batch != null) {
      response.setFlash(batch.getComments());
    }
    response.setReload(true);
  }
}
