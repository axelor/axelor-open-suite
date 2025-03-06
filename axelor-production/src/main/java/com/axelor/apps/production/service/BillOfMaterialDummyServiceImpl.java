package com.axelor.apps.production.service;

import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.BillOfMaterialLine;
import com.axelor.apps.production.db.repo.SaleOrderLineDetailsRepository;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.google.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class BillOfMaterialDummyServiceImpl implements BillOfMaterialDummyService {

  protected final SaleOrderLineRepository saleOrderLineRepository;
  protected final SaleOrderLineDetailsRepository saleOrderLineDetailsRepository;

  @Inject
  public BillOfMaterialDummyServiceImpl(
      SaleOrderLineRepository saleOrderLineRepository,
      SaleOrderLineDetailsRepository saleOrderLineDetailsRepository) {
    this.saleOrderLineRepository = saleOrderLineRepository;
    this.saleOrderLineDetailsRepository = saleOrderLineDetailsRepository;
  }

  @Override
  public boolean getIsUsedInSaleOrder(BillOfMaterial billOfMaterial) {
    List<BillOfMaterialLine> billOfMaterialLineList = billOfMaterial.getBillOfMaterialLineList();
    if (CollectionUtils.isEmpty(billOfMaterialLineList)) {
      return false;
    }
    String idList =
        billOfMaterialLineList.stream()
            .map(BillOfMaterialLine::getId)
            .map(Object::toString)
            .collect(Collectors.joining(","));
    String filter =
        "self.billOfMaterialLine IS NOT NULL AND self.billOfMaterialLine IN (" + idList + ")";
    return CollectionUtils.isNotEmpty(saleOrderLineRepository.all().filter(filter).fetch())
        || CollectionUtils.isNotEmpty(saleOrderLineDetailsRepository.all().filter(filter).fetch());
  }
}
