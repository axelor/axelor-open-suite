/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
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
package com.axelor.apps.supplychain.web;

import com.axelor.apps.base.callable.ControllerCallableTool;
import com.axelor.apps.base.service.exception.TraceBackService;
import com.axelor.apps.supplychain.db.Mrp;
import com.axelor.apps.supplychain.db.repo.MrpRepository;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.apps.supplychain.service.MrpFilterSaleOrderLineService;
import com.axelor.apps.supplychain.service.MrpProposalService;
import com.axelor.apps.supplychain.service.MrpService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class MrpController {

  public void undoManualChanges(ActionRequest request, ActionResponse response) {
    Mrp mrp = request.getContext().asType(Mrp.class);
    MrpService mrpService = Beans.get(MrpService.class);
    MrpRepository mrpRepository = Beans.get(MrpRepository.class);
    try {
      mrpService.undoManualChanges(mrpRepository.find(mrp.getId()));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
      mrpService.reset(mrpRepository.find(mrp.getId()));
    } finally {
      response.setReload(true);
    }
  }

  public void runCalculation(ActionRequest request, ActionResponse response) {

    Mrp mrp = request.getContext().asType(Mrp.class);
    MrpService mrpService = Beans.get(MrpService.class);
    MrpRepository mrpRepository = Beans.get(MrpRepository.class);
    try {
      if (mrpService.isOnGoing(mrpRepository.find(mrp.getId()))) {
        response.setInfo(I18n.get(SupplychainExceptionMessage.MRP_ALREADY_STARTED));
        return;
      }
      mrpService.setMrp(Beans.get(MrpRepository.class).find(mrp.getId()));

      // Tool class that does not need to be injected
      ControllerCallableTool<Mrp> mrpControllerCallableTool = new ControllerCallableTool<>();

      mrpControllerCallableTool.runInSeparateThread(mrpService, response);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
      mrpService.saveErrorInMrp(mrpRepository.find(mrp.getId()), e);
    } finally {
      response.setReload(true);
    }
  }

  public void generateAllProposals(ActionRequest request, ActionResponse response) {

    try {
      Mrp mrp = request.getContext().asType(Mrp.class);
      Boolean isProposalsPerSupplier =
          (Boolean) request.getContext().get("consolidateProposalsPerSupplier");
      Beans.get(MrpProposalService.class)
          .generateAllProposals(
              Beans.get(MrpRepository.class).find(mrp.getId()),
              isProposalsPerSupplier != null && isProposalsPerSupplier);
      response.setInfo(I18n.get("Proposals have been generated successfully."));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    } finally {
      response.setReload(true);
    }
  }

  public void generateSelectedProposals(ActionRequest request, ActionResponse response) {
    try {
      Mrp mrp = request.getContext().asType(Mrp.class);
      Boolean isProposalsPerSupplier =
          (Boolean) request.getContext().get("consolidateProposalsPerSupplier");
      Beans.get(MrpProposalService.class)
          .generateSelectedProposals(
              Beans.get(MrpRepository.class).find(mrp.getId()),
              isProposalsPerSupplier != null && isProposalsPerSupplier);
      response.setInfo(I18n.get("Proposals have been generated successfully."));
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    } finally {
      response.setReload(true);
    }
  }

  public void setSaleOrderLineSetDomain(ActionRequest request, ActionResponse response) {
    Mrp mrp = request.getContext().asType(Mrp.class);

    try {
      List<Long> idList =
          Beans.get(MrpFilterSaleOrderLineService.class)
              .getSaleOrderLinesComplyingToMrpLineTypes(mrp);

      String idListStr = idList.stream().map(Object::toString).collect(Collectors.joining(","));

      response.setAttr("saleOrderLineSet", "domain", "self.id IN (" + idListStr + ")");
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
