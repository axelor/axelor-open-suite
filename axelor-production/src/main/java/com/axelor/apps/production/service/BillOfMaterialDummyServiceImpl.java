package com.axelor.apps.production.service;

import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.BillOfMaterialLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.google.inject.Inject;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;

public class BillOfMaterialDummyServiceImpl implements BillOfMaterialDummyService {

  protected final SaleOrderLineRepository saleOrderLineRepository;

  @Inject
  public BillOfMaterialDummyServiceImpl(SaleOrderLineRepository saleOrderLineRepository) {
    this.saleOrderLineRepository = saleOrderLineRepository;
  }

  @Override
  public boolean getIsUsedInSaleOrder(BillOfMaterial billOfMaterial) {
    List<BillOfMaterialLine> billOfMaterialLineList = billOfMaterial.getBillOfMaterialLineList();
    if (CollectionUtils.isEmpty(billOfMaterialLineList)) {
      return false;
    }

    for (BillOfMaterialLine billOfMaterialLine : billOfMaterialLineList) {
      if (CollectionUtils.isNotEmpty(
          saleOrderLineRepository
              .all()
              .filter(
                  "self.billOfMaterialLine IS NOT NULL AND self.billOfMaterialLine = :billOfMaterialLine")
              .bind("billOfMaterialLine", billOfMaterialLine)
              .fetch())) {
        return true;
      }
    }
    return false;
  }
}
