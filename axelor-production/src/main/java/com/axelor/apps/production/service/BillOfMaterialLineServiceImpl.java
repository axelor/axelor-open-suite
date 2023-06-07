package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.BillOfMaterialLine;
import com.google.inject.Inject;
import java.math.BigDecimal;

public class BillOfMaterialLineServiceImpl implements BillOfMaterialLineService {

  protected ProductRepository productRepository;
  protected BillOfMaterialService billOfMaterialService;
  

  @Inject
  public BillOfMaterialLineServiceImpl(ProductRepository productRepository,
		  BillOfMaterialService billOfMaterialService) {
    this.productRepository = productRepository;
    this.billOfMaterialService = billOfMaterialService;
  }

  @Override
  public BillOfMaterialLine createFromRawMaterial(long productId, int priority, Company company) throws AxelorException {
    Product product = productRepository.find(productId);
    BillOfMaterial bom = null;
    if (product != null
        && product
            .getProductSubTypeSelect()
            .equals(ProductRepository.PRODUCT_SUB_TYPE_FINISHED_PRODUCT)) {
      bom = billOfMaterialService.getBOM(product, company);
    }

    return newBillOfMaterial(product, bom, BigDecimal.ONE, priority, false);
  }

  @Override
  public BillOfMaterialLine newBillOfMaterial(
      Product product,
      BillOfMaterial billOfMaterial,
      BigDecimal qty,
      Integer priority,
      boolean hasNoManageStock) {

    BillOfMaterialLine billOfMaterialLine = new BillOfMaterialLine();

    billOfMaterialLine.setProduct(product);
    billOfMaterialLine.setBillOfMaterial(billOfMaterial);
    billOfMaterialLine.setQty(qty);
    billOfMaterialLine.setPriority(priority);
    billOfMaterial.setHasNoManageStock(hasNoManageStock);

    return billOfMaterialLine;
  }
}
