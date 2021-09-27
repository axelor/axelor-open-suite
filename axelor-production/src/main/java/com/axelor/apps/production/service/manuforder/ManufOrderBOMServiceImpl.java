package com.axelor.apps.production.service.manuforder;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.ProdProduct;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class ManufOrderBOMServiceImpl implements ManufOrderBOMService {

  @Override
  public List<BillOfMaterial> generateBOMList(ManufOrder mo, List<ProdProduct> prodProductList) {
    Objects.requireNonNull(mo);
    Objects.requireNonNull(prodProductList);
    List<Product> productList =
        prodProductList.stream().map(ProdProduct::getProduct).collect(Collectors.toList());
    List<BillOfMaterial> billOfMaterialList =
        mo.getBillOfMaterial().getBillOfMaterialSet().stream()
            .filter(
                billOfMaterial ->
                    billOfMaterial.getDefineSubBillOfMaterial()
                        && billOfMaterial.getProdProcess() != null
                        && productList.contains(billOfMaterial.getProduct()))
            .collect(Collectors.toList());
    List<BillOfMaterial> defaultBomList =
        mo.getBillOfMaterial().getBillOfMaterialSet().stream()
            .filter(
                billOfMaterial ->
                    !billOfMaterial.getDefineSubBillOfMaterial()
                        && billOfMaterial.getProduct() != null
                        && billOfMaterial.getProduct().getDefaultBillOfMaterial() != null
                        && billOfMaterial.getProduct().getDefaultBillOfMaterial().getProdProcess()
                            != null
                        && productList.contains(billOfMaterial.getProduct()))
            .map(
                bom -> {
                  Optional<ProdProduct> prodProduct =
                      prodProductList.stream()
                          .filter(pProduct -> pProduct.getProduct().equals(bom.getProduct()))
                          .findAny();
                  BillOfMaterial defaultBOM = bom.getProduct().getDefaultBillOfMaterial();
                  if (prodProduct.isPresent()) {
                    defaultBOM.setQty(prodProduct.get().getQty());
                  }
                  return defaultBOM;
                })
            .collect(Collectors.toList());
    if (!defaultBomList.isEmpty()) {
      billOfMaterialList.addAll(defaultBomList);
    }
    return billOfMaterialList;
  }
}
