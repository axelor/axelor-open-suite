/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2022 Axelor (<http://axelor.com>).
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
package com.axelor.apps.crm.web;

import com.axelor.apps.crm.db.repo.OpportunityStatusRepository;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class OpportunityStatusController {

  public void setAttrs(ActionRequest request, ActionResponse response) {
    Boolean isWonPresent = false;
    Boolean isLostPresent = false;

    OpportunityStatusRepository opportunityStatusRepo =
        Beans.get(OpportunityStatusRepository.class);

    if (opportunityStatusRepo.findByTypeSelect(OpportunityStatusRepository.STATUS_TYPE_CLOSED_WON)
        != null) {
      isWonPresent = true;
    }

    if (opportunityStatusRepo.findByTypeSelect(OpportunityStatusRepository.STATUS_TYPE_CLOSED_LOST)
        != null) {
      isLostPresent = true;
    }

    if (isWonPresent && isLostPresent) {
      response.setAttr("typeSelect", "hidden", true);
    } else if (isWonPresent) {
      response.setAttr(
          "typeSelect", "selection-in", OpportunityStatusRepository.STATUS_TYPE_CLOSED_LOST);
    } else if (isLostPresent) {
      response.setAttr(
          "typeSelect", "selection-in", OpportunityStatusRepository.STATUS_TYPE_CLOSED_WON);
    }
  }
}
