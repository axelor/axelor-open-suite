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
package com.axelor.apps.supplychain.web;

import com.axelor.apps.base.service.administration.AbstractBatch;
import com.axelor.apps.supplychain.db.Mrp;
import com.axelor.apps.supplychain.db.MrpLine;
import com.axelor.apps.supplychain.db.repo.MrpLineRepository;
import com.axelor.apps.supplychain.db.repo.MrpLineTypeRepository;
import com.axelor.apps.supplychain.db.repo.MrpRepository;
import com.axelor.apps.supplychain.service.MrpLineService;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

@Singleton
public class MrpLineController {

  public void generateProposal(ActionRequest request, ActionResponse response)
      throws AxelorException {
    MrpLine mrpLine = request.getContext().asType(MrpLine.class);
    Beans.get(MrpLineService.class)
        .generateProposal(Beans.get(MrpLineRepository.class).find(mrpLine.getId()));
    response.setFlash(I18n.get("The proposal has been successfully generated."));
    response.setReload(true);
  }

  @Transactional(rollbackOn = {Exception.class})
  public void select(ActionRequest request, ActionResponse response) {
    toggle(request, response, true);
  }

  @Transactional(rollbackOn = {Exception.class})
  public void unselect(ActionRequest request, ActionResponse response) {
    toggle(request, response, false);
  }

  @SuppressWarnings("unchecked")
  @Transactional(rollbackOn = {Exception.class})
  private void toggle(ActionRequest request, ActionResponse response, boolean value) {
    List<Integer> mrpLineIds = (List<Integer>) request.getContext().get("_ids");

    if (CollectionUtils.isNotEmpty(mrpLineIds)) {
      MrpLineRepository mrpLineRepo = Beans.get(MrpLineRepository.class);
      MrpLine mrpLine;

      for (Integer mrpId : mrpLineIds) {
        mrpLine = mrpLineRepo.find(Long.valueOf(mrpId));
        mrpLine.setToProcess(value);
        mrpLineRepo.save(mrpLine);
      }
    }

    response.setAttr("mrpLinePanel", "refresh", true);
  }

  @Transactional(rollbackOn = {Exception.class})
  public void selectAll(ActionRequest request, ActionResponse response) {
    try {
      MrpLineRepository mrpLineRepo = Beans.get(MrpLineRepository.class);
      Mrp mrp = request.getContext().getParent().asType(Mrp.class);
      mrp = Beans.get(MrpRepository.class).find(mrp.getId());

      Query<MrpLine> mrpLineQuery =
          mrpLineRepo
              .all()
              .filter(
                  "(self.mrp.displayProductWithoutProposal = true AND self.mrp.id = ?1)"
                      + " OR (self.mrp.displayProductWithoutProposal = false AND self.mrp.id = ?1 AND self.product.id IN (select m.product from MrpLine as m where m.mrp.id = ?1 AND m.mrpLineType.elementSelect in (?2, ?3, ?4)))",
                  mrp.getId(),
                  MrpLineTypeRepository.ELEMENT_PURCHASE_PROPOSAL,
                  MrpLineTypeRepository.ELEMENT_MANUFACTURING_PROPOSAL,
                  MrpLineTypeRepository.ELEMENT_MANUFACTURING_PROPOSAL_NEED)
              .order("id");

      int offset = 0;
      List<MrpLine> mrpLineList;

      while (!(mrpLineList = mrpLineQuery.fetch(AbstractBatch.FETCH_LIMIT, offset)).isEmpty()) {
        for (MrpLine mrpLine : mrpLineList) {
          offset++;

          mrpLine.setToProcess(true);
          mrpLineRepo.save(mrpLine);
        }

        JPA.clear();
      }

      response.setAttr("mrpLinePanel", "refresh", true);
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
