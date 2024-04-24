package com.axelor.apps.production.service;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.supplychain.db.MrpLine;
import com.axelor.apps.supplychain.service.MrpLineTool;
import java.util.Optional;

public class BillOfMaterialMrpLineServiceImpl implements BillOfMaterialMrpLineService {

  @Override
  public Optional<BillOfMaterial> getEligibleBillOfMaterialOfProductInMrpLine(
      MrpLine mrpLine, Product product) {

    if (mrpLine == null) {
      return Optional.empty();
    }

    BillOfMaterial billOfMaterial = mrpLine.getBillOfMaterial();
    if (billOfMaterial != null) {
      return Optional.of(billOfMaterial);
    }

    if (product == null) {
      return Optional.empty();
    }

    Optional<SaleOrderLine> saleOrderLineOpt =
        MrpLineTool.getOriginSaleOrderLineInMrpLineOrigin(mrpLine);
    Optional<BillOfMaterial> billOfMaterialOpt =
        saleOrderLineOpt.map(SaleOrderLine::getBillOfMaterial);

    if (billOfMaterialOpt.isPresent()
        && product.equals(billOfMaterialOpt.map(BillOfMaterial::getProduct).orElse(null))) {
      return billOfMaterialOpt;
    }
    return Optional.empty();
  }
}
