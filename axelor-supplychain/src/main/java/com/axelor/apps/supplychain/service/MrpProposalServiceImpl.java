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
package com.axelor.apps.supplychain.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.base.service.administration.AbstractBatch;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.supplychain.db.Mrp;
import com.axelor.apps.supplychain.db.MrpLine;
import com.axelor.apps.supplychain.db.repo.MrpLineRepository;
import com.axelor.apps.supplychain.db.repo.MrpLineTypeRepository;
import com.axelor.apps.supplychain.db.repo.MrpRepository;
import com.axelor.apps.supplychain.exception.SupplychainExceptionMessage;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.tuple.Pair;

public class MrpProposalServiceImpl implements MrpProposalService {

  protected MrpRepository mrpRepository;
  protected MrpLineRepository mrpLineRepository;
  protected MrpLineService mrpLineService;

  @Inject
  public MrpProposalServiceImpl(
      MrpRepository mrpRepository,
      MrpLineRepository mrpLineRepository,
      MrpLineService mrpLineService) {
    this.mrpRepository = mrpRepository;
    this.mrpLineRepository = mrpLineRepository;
    this.mrpLineService = mrpLineService;
  }

  @Override
  public void generateSelectedProposals(Mrp mrp, boolean isProposalPerSupplier)
      throws AxelorException {

    Map<Pair<Partner, LocalDate>, PurchaseOrder> purchaseOrders = new HashMap<>();
    Map<Partner, PurchaseOrder> purchaseOrdersPerSupplier = new HashMap<>();
    List<MrpLine> mrpLineList;

    if (getSelectedMrpLines(mrp).count() <= 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(SupplychainExceptionMessage.MRP_GENERATE_PROPOSAL_NO_LINE_SELECTED));
    }

    while (!(mrpLineList = getSelectedMrpLines(mrp).fetch(1)).isEmpty()) {
      mrp = mrpRepository.find(mrp.getId());
      generateProposals(
          isProposalPerSupplier, purchaseOrders, purchaseOrdersPerSupplier, mrpLineList);
      JPA.clear();
    }
  }

  protected Query<MrpLine> getSelectedMrpLines(Mrp mrp) {
    return mrpLineRepository
        .all()
        .filter(
            "self.mrp.id = ?1 AND self.proposalToProcess = true AND self.proposalGenerated = false",
            mrp.getId())
        .order("maturityDate");
  }

  @Override
  public void generateAllProposals(Mrp mrp, boolean isProposalsPerSupplier) throws AxelorException {
    Map<Pair<Partner, LocalDate>, PurchaseOrder> purchaseOrders = new HashMap<>();
    Map<Partner, PurchaseOrder> purchaseOrdersPerSupplier = new HashMap<>();
    List<MrpLine> mrpLineList;

    if (getAllMrpLines(mrp).count() <= 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(SupplychainExceptionMessage.MRP_GENERATE_PROPOSAL_NO_POSSIBLE_LINE));
    }

    while (!(mrpLineList = getAllMrpLines(mrp).fetch(1)).isEmpty()) {
      mrp = mrpRepository.find(mrp.getId());
      generateProposals(
          isProposalsPerSupplier, purchaseOrders, purchaseOrdersPerSupplier, mrpLineList);
      JPA.clear();
    }
  }

  protected Query<MrpLine> getAllMrpLines(Mrp mrp) {
    return mrpLineRepository
        .all()
        .filter(
            "self.mrp.id = :mrpId AND self.proposalGenerated = false AND self.mrpLineType.elementSelect in (:purchaseProposal, :manufProposal)")
        .bind("mrpId", mrp.getId())
        .bind("purchaseProposal", MrpLineTypeRepository.ELEMENT_PURCHASE_PROPOSAL)
        .bind("manufProposal", MrpLineTypeRepository.ELEMENT_MANUFACTURING_PROPOSAL)
        .order("maturityDate");
  }

  @Transactional(rollbackOn = {Exception.class})
  protected void generateProposals(
      boolean isProposalPerSupplier,
      Map<Pair<Partner, LocalDate>, PurchaseOrder> purchaseOrders,
      Map<Partner, PurchaseOrder> purchaseOrdersPerSupplier,
      List<MrpLine> mrpLineList)
      throws AxelorException {
    for (MrpLine mrpLine : mrpLineList) {
      if (!mrpLine.getProposalGenerated()) {
        generateProposal(isProposalPerSupplier, purchaseOrders, purchaseOrdersPerSupplier, mrpLine);
      }
    }
  }

  protected void generateProposal(
      boolean isProposalPerSupplier,
      Map<Pair<Partner, LocalDate>, PurchaseOrder> purchaseOrders,
      Map<Partner, PurchaseOrder> purchaseOrdersPerSupplier,
      MrpLine mrpLine)
      throws AxelorException {
    mrpLineService.generateProposal(
        mrpLine, purchaseOrders, purchaseOrdersPerSupplier, isProposalPerSupplier);
    mrpLine.setProposalToProcess(false);
    mrpLineRepository.save(mrpLine);
  }

  @Override
  @Transactional(rollbackOn = {Exception.class})
  public void massUpdateProposalToProcess(Mrp mrp, boolean proposalToProcess) {
    Query<MrpLine> mrpLineQuery =
        mrpLineRepository
            .all()
            .filter(
                "self.mrp.id = :mrpId AND self.mrpLineType.elementSelect in (:purchaseProposal, :manufProposal)")
            .bind("mrpId", mrp.getId())
            .bind("purchaseProposal", MrpLineTypeRepository.ELEMENT_PURCHASE_PROPOSAL)
            .bind("manufProposal", MrpLineTypeRepository.ELEMENT_MANUFACTURING_PROPOSAL)
            .order("id");

    int offset = 0;
    List<MrpLine> mrpLineList;

    while (!(mrpLineList = mrpLineQuery.fetch(AbstractBatch.FETCH_LIMIT, offset)).isEmpty()) {
      for (MrpLine mrpLine : mrpLineList) {
        offset++;

        mrpLineService.updateProposalToProcess(mrpLine, true);
      }

      JPA.clear();
    }
  }
}
