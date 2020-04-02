/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
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

import com.axelor.apps.crm.db.Target;
import com.axelor.apps.crm.db.repo.TargetRepository;
import com.axelor.apps.crm.service.TargetService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class TargetController {

  @Inject private TargetService targetService;

  @Inject private TargetRepository targetRepo;

  public void update(ActionRequest request, ActionResponse response) {

    Target target = request.getContext().asType(Target.class);

    try {
      targetService.update(targetRepo.find(target.getId()));
      response.setValue("opportunityAmountWon", target.getOpportunityAmountWon());
      response.setValue("opportunityCreatedNumber", target.getOpportunityCreatedNumber());
      response.setValue("opportunityCreatedWon", target.getOpportunityCreatedWon());
      response.setValue("callEmittedNumber", target.getCallEmittedNumber());
      response.setValue("meetingNumber", target.getMeetingNumber());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
