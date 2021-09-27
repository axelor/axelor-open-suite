package com.axelor.apps.production.service.manuforder;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.ProdProduct;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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
            .map(bom -> bom.getProduct().getDefaultBillOfMaterial())
            .collect(Collectors.toList());
    if (!defaultBomList.isEmpty()) {
      billOfMaterialList.addAll(defaultBomList);
    }
    return billOfMaterialList;
  }

  @Override
  public Map<BillOfMaterial, BigDecimal> generateBOMMap(
      ManufOrder mo, List<ProdProduct> prodProductList) {
    Objects.requireNonNull(mo);
    Objects.requireNonNull(prodProductList);
    List<Product> productList =
        prodProductList.stream().map(ProdProduct::getProduct).collect(Collectors.toList());
    HashMap<BillOfMaterial, BigDecimal> billOfMaterialMap = new HashMap<>();

    Set<BillOfMaterial> billOfMaterialSet = mo.getBillOfMaterial().getBillOfMaterialSet();

    for (BillOfMaterial billOfMaterial : billOfMaterialSet) {

      if (billOfMaterial.getDefineSubBillOfMaterial()
          && billOfMaterial.getProdProcess() != null
          && productList.contains(billOfMaterial.getProduct())) {
        Optional<ProdProduct> optProdProduct =
            prodProductList.stream()
                .filter(prodProduct -> prodProduct.getProduct().equals(billOfMaterial.getProduct()))
                .findAny();
        if (optProdProduct.isPresent()) {
          billOfMaterialMap.put(billOfMaterial, optProdProduct.get().getQty());
        } else {
          billOfMaterialMap.put(billOfMaterial, billOfMaterial.getQty());
        }
      } else if (!billOfMaterial.getDefineSubBillOfMaterial()
          && billOfMaterial.getProduct() != null
          && billOfMaterial.getProduct().getDefaultBillOfMaterial() != null
          && billOfMaterial.getProduct().getDefaultBillOfMaterial().getProdProcess() != null
          && productList.contains(billOfMaterial.getProduct())) {
        Optional<ProdProduct> optProdProduct =
            prodProductList.stream()
                .filter(prodProduct -> prodProduct.getProduct().equals(billOfMaterial.getProduct()))
                .findAny();
        BillOfMaterial defaultBillOfMaterial =
            billOfMaterial.getProduct().getDefaultBillOfMaterial();
        if (optProdProduct.isPresent()) {
          billOfMaterialMap.put(defaultBillOfMaterial, optProdProduct.get().getQty());
        } else {
          billOfMaterialMap.put(defaultBillOfMaterial, defaultBillOfMaterial.getQty());
        }
      }
    }

    return billOfMaterialMap;
  }
}
