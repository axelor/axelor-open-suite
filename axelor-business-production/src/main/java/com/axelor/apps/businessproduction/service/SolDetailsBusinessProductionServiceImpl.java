package com.axelor.apps.businessproduction.service;

import com.axelor.apps.production.db.SaleOrderLineDetails;
import com.axelor.apps.production.db.repo.SaleOrderLineDetailsRepository;
import com.axelor.apps.production.service.SolDetailsBomUpdateService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class SolDetailsBusinessProductionServiceImpl
    implements SolDetailsBusinessProductionService {

  protected final SaleOrderLineDetailsRepository saleOrderLineDetailsRepository;
  protected final SolDetailsBomUpdateService solDetailsBomUpdateService;

  @Inject
  public SolDetailsBusinessProductionServiceImpl(
      SaleOrderLineDetailsRepository saleOrderLineDetailsRepository,
      SolDetailsBomUpdateService solDetailsBomUpdateService) {
    this.saleOrderLineDetailsRepository = saleOrderLineDetailsRepository;
    this.solDetailsBomUpdateService = solDetailsBomUpdateService;
  }

  @Transactional
  @Override
  public void copySolDetailsList(SaleOrder saleOrder) {
    List<SaleOrderLine> saleOrderLineList = saleOrder.getSaleOrderLineList();
    if (CollectionUtils.isEmpty(saleOrderLineList)) {
      return;
    }

    for (SaleOrderLine saleOrderLine : saleOrderLineList) {
      copySolDetailsList(saleOrderLine);
    }
  }

  protected void copySolDetailsList(SaleOrderLine saleOrderLine) {
    List<SaleOrderLine> subSaleOrderLineList = saleOrderLine.getSubSaleOrderLineList();
    if (CollectionUtils.isNotEmpty(subSaleOrderLineList)) {
      for (SaleOrderLine subSaleOrderLine : saleOrderLine.getSubSaleOrderLineList()) {
        copySolDetailsList(subSaleOrderLine);
      }
    }

    copyDetailsLine(saleOrderLine);
  }

  protected void copyDetailsLine(SaleOrderLine saleOrderLine) {
    List<SaleOrderLineDetails> saleOrderLineDetailsList =
        saleOrderLine.getSaleOrderLineDetailsList();
    if (CollectionUtils.isNotEmpty(saleOrderLineDetailsList)) {
      for (SaleOrderLineDetails saleOrderLineDetails : saleOrderLineDetailsList) {
        SaleOrderLineDetails copySolDetails =
            saleOrderLineDetailsRepository.copy(saleOrderLineDetails, true);
        copySolDetails.setSaleOrderLine(null);
        copySolDetails.setProjectSaleOrderLine(saleOrderLine);
        copySolDetails.setOriginSaleOrderLineDetails(saleOrderLineDetails);
        saleOrderLine.addProjectSaleOrderLineDetailsListItem(copySolDetails);
      }
    }
  }

  @Transactional
  @Override
  public void deleteSolDetailsList(SaleOrder saleOrder) {
    List<SaleOrderLine> saleOrderLineList = saleOrder.getSaleOrderLineList();
    if (CollectionUtils.isEmpty(saleOrderLineList)) {
      return;
    }

    for (SaleOrderLine saleOrderLine : saleOrderLineList) {
      deleteSolDetailsList(saleOrderLine);
    }
  }

  protected void deleteSolDetailsList(SaleOrderLine saleOrderLine) {
    List<SaleOrderLine> subSaleOrderLineList = saleOrderLine.getSubSaleOrderLineList();
    if (CollectionUtils.isNotEmpty(subSaleOrderLineList)) {
      for (SaleOrderLine subSaleOrderLine : saleOrderLine.getSubSaleOrderLineList()) {
        deleteSolDetailsList(subSaleOrderLine);
      }
    }

    List<SaleOrderLineDetails> saleOrderLineDetailsList =
        saleOrderLine.getSaleOrderLineDetailsList();
    var isSolDetailsUpdated =
        solDetailsBomUpdateService.isSolDetailsUpdated(saleOrderLine, saleOrderLineDetailsList);
    if (!isSolDetailsUpdated) {
      saleOrderLine.clearProjectSaleOrderLineDetailsList();
    }
  }
}
