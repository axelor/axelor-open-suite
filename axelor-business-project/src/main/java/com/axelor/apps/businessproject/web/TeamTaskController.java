/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
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
package com.axelor.apps.businessproject.web;

import com.axelor.apps.businessproject.service.TeamTaskBusinessService;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.team.db.TeamTask;
import com.axelor.team.db.repo.TeamTaskRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;

public class TeamTaskController {

  @Inject private TeamTaskBusinessService teamTaskBusinessService;

  @Inject private TeamTaskRepository teamTaskRepo;

  public void updateDiscount(ActionRequest request, ActionResponse response) {

    TeamTask teamTask = request.getContext().asType(TeamTask.class);

    if (teamTask.getProduct() == null || teamTask.getProject() == null) {
      return;
    }

    try {
      teamTask = teamTaskBusinessService.updateDiscount(teamTask);

      response.setValue("discountTypeSelect", teamTask.getDiscountTypeSelect());
      response.setValue("discountAmount", teamTask.getDiscountAmount());
      response.setValue("priceDiscounted", teamTask.getPriceDiscounted());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public void compute(ActionRequest request, ActionResponse response) {
    TeamTask teamTask = request.getContext().asType(TeamTask.class);

    try {
      teamTask = teamTaskBusinessService.compute(teamTask);
      response.setValue("priceDiscounted", teamTask.getPriceDiscounted());
      response.setValue("exTaxTotal", teamTask.getExTaxTotal());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  /**
   * Invert value of 'toInvoice' field and save the record
   *
   * @param request
   * @param response
   */
  @Transactional
  public void updateToInvoice(ActionRequest request, ActionResponse response) {
    try {
      TeamTask teamTask = request.getContext().asType(TeamTask.class);
      teamTask = teamTaskRepo.find(teamTask.getId());
      teamTask.setToInvoice(!teamTask.getToInvoice());
      teamTaskRepo.save(teamTask);
      response.setValue("toInvoice", teamTask.getToInvoice());
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
