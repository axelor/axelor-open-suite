/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2025 Axelor (<http://axelor.com>).
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
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.purchase.db.CallTender;
import com.axelor.apps.purchase.db.CallTenderNeed;
import com.axelor.apps.purchase.db.repo.CallTenderNeedRepository;
import com.axelor.apps.purchase.service.CallTenderGenerateService;
import com.axelor.apps.purchase.service.CallTenderNeedService;
import com.axelor.apps.purchase.service.SupplierCatalogService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MrpCallTenderServiceImpl implements MrpCallTenderService {

  protected final MrpRepository mrpRepository;
  protected final MrpLineRepository mrpLineRepository;
  protected final CallTenderGenerateService callTenderGenerateService;
  protected final CallTenderNeedService callTenderNeedService;
  protected final CallTenderNeedRepository callTenderNeedRepository;
  protected final SupplierCatalogService supplierCatalogService;

  protected final int FETCH_LIMIT = 10;

  @Inject
  public MrpCallTenderServiceImpl(
      MrpRepository mrpRepository,
      MrpLineRepository mrpLineRepository,
      CallTenderGenerateService callTenderGenerateService,
      CallTenderNeedService callTenderNeedService,
      CallTenderNeedRepository callTenderNeedRepository,
      SupplierCatalogService supplierCatalogService) {
    this.mrpRepository = mrpRepository;
    this.mrpLineRepository = mrpLineRepository;
    this.callTenderGenerateService = callTenderGenerateService;
    this.callTenderNeedService = callTenderNeedService;
    this.callTenderNeedRepository = callTenderNeedRepository;
    this.supplierCatalogService = supplierCatalogService;
  }

  @Override
  public CallTender generateCallTenderForSelectedLines(Mrp mrp) throws AxelorException {
    List<MrpLine> mrpLineList;
    var callTenderNeedList = new ArrayList<CallTenderNeed>();
    int offset = 0;
    if (getSelectedMrpLines(mrp).count() <= 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(SupplychainExceptionMessage.MRP_GENERATE_PROPOSAL_NO_CALL_TENDER_NEEDED));
    }

    while (!(mrpLineList = getSelectedMrpLines(mrp).fetch(FETCH_LIMIT, offset)).isEmpty()) {
      mrp = mrpRepository.find(mrp.getId());
      for (MrpLine mrpLine : mrpLineList) {
        callTenderNeedList.add(
            callTenderNeedService.createCallTenderNeed(
                mrpLine.getProduct(),
                mrpLine.getQty(),
                supplierCatalogService.getUnit(
                    mrpLine.getProduct(), null, mrp.getStockLocation().getCompany()),
                mrpLine.getMaturityDate(),
                CallTenderNeedRepository.MRP_TYPE));
      }

      offset += FETCH_LIMIT;
      JPA.clear();
    }
    mrp = mrpRepository.find(mrp.getId());
    return callTenderGenerateService.generateCallTender(
        mrp.getMrpSeq(),
        mrp.getStockLocation().getCompany(),
        callTenderNeedList.stream()
            .map(ctd -> callTenderNeedRepository.find(ctd.getId()))
            .collect(Collectors.toList()));
  }

  @Override
  public CallTender generateCallTenderForAllLines(Mrp mrp) throws AxelorException {

    List<MrpLine> mrpLineList;
    var callTenderNeedList = new ArrayList<CallTenderNeed>();
    int offset = 0;
    if (getAllMrpLines(mrp).count() <= 0) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_INCONSISTENCY,
          I18n.get(SupplychainExceptionMessage.MRP_GENERATE_PROPOSAL_NO_CALL_TENDER_NEEDED));
    }

    while (!(mrpLineList = getAllMrpLines(mrp).fetch(FETCH_LIMIT, offset)).isEmpty()) {
      mrp = mrpRepository.find(mrp.getId());
      for (MrpLine mrpLine : mrpLineList) {
        callTenderNeedList.add(
            callTenderNeedService.createCallTenderNeed(
                mrpLine.getProduct(),
                mrpLine.getQty(),
                supplierCatalogService.getUnit(
                    mrpLine.getProduct(), null, mrp.getStockLocation().getCompany()),
                mrpLine.getMaturityDate(),
                CallTenderNeedRepository.MRP_TYPE));
      }

      offset += FETCH_LIMIT;
      JPA.clear();
    }

    mrp = mrpRepository.find(mrp.getId());
    return callTenderGenerateService.generateCallTender(
        mrp.getMrpSeq(),
        mrp.getStockLocation().getCompany(),
        callTenderNeedList.stream()
            .map(ctd -> callTenderNeedRepository.find(ctd.getId()))
            .collect(Collectors.toList()));
  }

  protected Query<MrpLine> getSelectedMrpLines(Mrp mrp) {
    return mrpLineRepository
        .all()
        .filter(
            "self.mrp.id = :mrpId AND self.mrpLineType.elementSelect = :purchaseProposal  AND self.proposalToProcess = true")
        .bind("mrpId", mrp.getId())
        .bind("purchaseProposal", MrpLineTypeRepository.ELEMENT_PURCHASE_PROPOSAL)
        .order("maturityDate");
  }

  protected Query<MrpLine> getAllMrpLines(Mrp mrp) {
    return mrpLineRepository
        .all()
        .filter("self.mrp.id = :mrpId AND self.mrpLineType.elementSelect = :purchaseProposal")
        .bind("mrpId", mrp.getId())
        .bind("purchaseProposal", MrpLineTypeRepository.ELEMENT_PURCHASE_PROPOSAL)
        .order("maturityDate");
  }
}
