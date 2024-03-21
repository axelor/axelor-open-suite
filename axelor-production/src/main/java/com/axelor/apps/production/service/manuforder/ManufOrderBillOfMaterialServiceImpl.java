package com.axelor.apps.production.service.manuforder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.BillOfMaterialLine;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.service.BillOfMaterialService;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

public class ManufOrderBillOfMaterialServiceImpl implements ManufOrderBillOfMaterialService {

  protected final ManufOrderProdProductService manufOrderProdProductService;
  protected final BillOfMaterialService billOfMaterialService;

  @Inject
  public ManufOrderBillOfMaterialServiceImpl(
      ManufOrderProdProductService manufOrderProdProductService,
      BillOfMaterialService billOfMaterialService) {
    this.manufOrderProdProductService = manufOrderProdProductService;
    this.billOfMaterialService = billOfMaterialService;
  }

  @Override
  public List<Pair<BillOfMaterial, BigDecimal>> getToConsumeSubBomList(
      BillOfMaterial billOfMaterial, ManufOrder mo, List<Product> productList)
      throws AxelorException {
    List<Pair<BillOfMaterial, BigDecimal>> bomList = new ArrayList<>();

    for (BillOfMaterialLine boml : billOfMaterial.getBillOfMaterialLineList()) {
      Product product = boml.getProduct();
      if (productList != null && !productList.contains(product)) {
        continue;
      }

      BigDecimal qtyReq =
          manufOrderProdProductService.computeToConsumeProdProductLineQuantity(
              mo.getBillOfMaterial().getQty(), mo.getQty(), boml.getQty());

      BillOfMaterial bom = boml.getBillOfMaterial();
      if (bom != null) {
        if (bom.getProdProcess() != null) {
          bomList.add(Pair.of(bom, qtyReq));
        }
      } else {
        BillOfMaterial defaultBOM = billOfMaterialService.getDefaultBOM(product, null);

        if ((product.getProductSubTypeSelect()
                    == ProductRepository.PRODUCT_SUB_TYPE_FINISHED_PRODUCT
                || product.getProductSubTypeSelect()
                    == ProductRepository.PRODUCT_SUB_TYPE_SEMI_FINISHED_PRODUCT)
            && defaultBOM != null
            && defaultBOM.getProdProcess() != null) {
          bomList.add(Pair.of(defaultBOM, qtyReq));
        }
      }
    }
    return bomList;
  }
}
