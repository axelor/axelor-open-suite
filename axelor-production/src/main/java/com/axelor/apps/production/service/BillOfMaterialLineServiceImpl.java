package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.BillOfMaterialLine;
import com.google.inject.Inject;
import java.math.BigDecimal;

public class BillOfMaterialLineServiceImpl implements BillOfMaterialLineService {

  protected ProductRepository productRepository;
  protected BillOfMaterialService billOfMaterialService;

  @Inject
  public BillOfMaterialLineServiceImpl(
      ProductRepository productRepository, BillOfMaterialService billOfMaterialService) {
    this.productRepository = productRepository;
    this.billOfMaterialService = billOfMaterialService;
  }

  @Override
  public BillOfMaterialLine createFromRawMaterial(
      long productId, int priority, BillOfMaterial billOfMaterial) throws AxelorException {
    Product product = productRepository.find(productId);
    BillOfMaterial bom = null;
    if (product != null
        && product
            .getProductSubTypeSelect()
            .equals(ProductRepository.PRODUCT_SUB_TYPE_SEMI_FINISHED_PRODUCT)) {
      bom = billOfMaterialService.getBOM(product, billOfMaterial.getCompany());
    }

    return createBillOfMaterialLine(
        product, bom, BigDecimal.ONE, billOfMaterial.getUnit(), priority, false);
  }

  @Override
  public BillOfMaterialLine createBillOfMaterialLine(
      Product product,
      BillOfMaterial billOfMaterial,
      BigDecimal qty,
      Unit unit,
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

  @Override
  public BillOfMaterialLine createFromBillOfMaterial(BillOfMaterial billOfMaterial) {

    Product product = billOfMaterial.getProduct();
    BigDecimal qty = billOfMaterial.getQty();
    boolean hasNoManageStock = billOfMaterial.getHasNoManageStock();

    return createBillOfMaterialLine(
        product, billOfMaterial, qty, billOfMaterial.getUnit(), null, hasNoManageStock);
  }

  @Override
  public void fillBom(BillOfMaterialLine billOfMaterialLine, Company company)
      throws AxelorException {

    Product product = billOfMaterialLine.getProduct();
    if (product != null
        && product
            .getProductSubTypeSelect()
            .equals(ProductRepository.PRODUCT_SUB_TYPE_SEMI_FINISHED_PRODUCT)) {
      billOfMaterialLine.setBillOfMaterial(billOfMaterialService.getBOM(product, company));
    }
  }

  @Override
  public void fillHasNoManageStock(BillOfMaterialLine billOfMaterialLine) {
    Product product = billOfMaterialLine.getProduct();

    if (product != null && !product.getStockManaged()) {
      billOfMaterialLine.setHasNoManageStock(true);
    }
  }

  @Override
  public void fillUnit(BillOfMaterialLine billOfMaterialLine) {

    Product product = billOfMaterialLine.getProduct();
    if (product != null && billOfMaterialLine.getUnit() == null) {
      billOfMaterialLine.setUnit(product.getUnit());
    }
  }
}
