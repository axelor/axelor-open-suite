package com.axelor.apps.production.service;

import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.BillOfMaterialLine;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class SaleOrderLineBomSyncServiceImpl implements SaleOrderLineBomSyncService {
  @Override
  public void syncSaleOrderLineBom(SaleOrder saleOrder) {
    List<SaleOrderLine> saleOrderLineList = saleOrder.getSaleOrderLineList();
    if (CollectionUtils.isNotEmpty(saleOrderLineList)) {
      for (SaleOrderLine saleOrderLine : saleOrderLineList) {
        removeBomLines(saleOrderLine);
      }
    }
  }

  protected void removeBomLines(SaleOrderLine saleOrderLine) {
    List<SaleOrderLine> subSaleOrderLineList = saleOrderLine.getSubSaleOrderLineList();

    if (subSaleOrderLineList != null) {
      for (SaleOrderLine subSaleOrderLine : subSaleOrderLineList) {
        removeBomLines(subSaleOrderLine);
      }
    }

    BillOfMaterial billOfMaterial = saleOrderLine.getBillOfMaterial();
    if (billOfMaterial != null && billOfMaterial.getPersonalized()) {
      List<BillOfMaterialLine> bomLineToRemove =
          getBomLinesToRemove(billOfMaterial, subSaleOrderLineList);
      for (BillOfMaterialLine billOfMaterialLine : bomLineToRemove) {
        billOfMaterial.removeBillOfMaterialLineListItem(billOfMaterialLine);
      }
    }
  }

  protected List<BillOfMaterialLine> getBomLinesToRemove(
      BillOfMaterial billOfMaterial, List<SaleOrderLine> subSaleOrderLineList) {
    Set<BillOfMaterialLine> billOfMaterialLineList =
        billOfMaterial.getBillOfMaterialLineList().stream()
            .filter(
                line ->
                    line.getProduct()
                        .getProductSubTypeSelect()
                        .equals(ProductRepository.PRODUCT_SUB_TYPE_SEMI_FINISHED_PRODUCT))
            .collect(Collectors.toSet());
    Set<BillOfMaterialLine> soBillOfMaterialLineList =
        subSaleOrderLineList.stream()
            .map(SaleOrderLine::getBillOfMaterialLine)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    List<BillOfMaterialLine> bomLineToRemove = new ArrayList<>(billOfMaterialLineList);
    bomLineToRemove.removeAll(soBillOfMaterialLineList);
    return bomLineToRemove;
  }
}
