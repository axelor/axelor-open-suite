package com.axelor.apps.businessproduction.service;

import com.axelor.apps.production.db.SaleOrderLineDetails;
import com.axelor.apps.production.db.repo.SaleOrderLineDetailsRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class SaleOrderBusinessProductionServiceImpl implements SaleOrderBusinessProductionService {

  protected final SaleOrderLineDetailsRepository saleOrderLineDetailsRepository;

  @Inject
  public SaleOrderBusinessProductionServiceImpl(
      SaleOrderLineDetailsRepository saleOrderLineDetailsRepository) {
    this.saleOrderLineDetailsRepository = saleOrderLineDetailsRepository;
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
        copySolDetails.setConfirmedSaleOrderLine(saleOrderLine);
        copySolDetails.setOriginSaleOrderLineDetails(saleOrderLineDetails);
        saleOrderLine.addProjectSaleOrderLineDetailsListItem(copySolDetails);
      }
    }
  }
}
