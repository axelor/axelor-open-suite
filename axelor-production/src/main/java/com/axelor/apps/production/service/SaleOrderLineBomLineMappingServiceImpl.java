package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.repo.ProductRepository;
import com.axelor.apps.production.db.BillOfMaterialLine;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineComputeService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineProductService;
import com.google.inject.Inject;
import java.util.Objects;
import java.util.Optional;

public class SaleOrderLineBomLineMappingServiceImpl implements SaleOrderLineBomLineMappingService {

  protected final SaleOrderLineBomService saleOrderLineBomService;
  protected final SaleOrderLineProductService saleOrderLineProductService;
  protected final SaleOrderLineComputeService saleOrderLineComputeService;

  @Inject
  public SaleOrderLineBomLineMappingServiceImpl(
      SaleOrderLineBomService saleOrderLineBomService,
      SaleOrderLineProductService saleOrderLineProductService,
      SaleOrderLineComputeService saleOrderLineComputeService) {
    this.saleOrderLineBomService = saleOrderLineBomService;
    this.saleOrderLineProductService = saleOrderLineProductService;
    this.saleOrderLineComputeService = saleOrderLineComputeService;
  }

  @Override
  public SaleOrderLine mapToSaleOrderLine(
      BillOfMaterialLine billOfMaterialLine, SaleOrder saleOrder) throws AxelorException {
    Objects.requireNonNull(billOfMaterialLine);

    if (billOfMaterialLine.getProduct().getProductSubTypeSelect()
        == ProductRepository.PRODUCT_SUB_TYPE_SEMI_FINISHED_PRODUCT) {
      SaleOrderLine saleOrderLine = new SaleOrderLine();
      saleOrderLine.setProduct(billOfMaterialLine.getProduct());
      saleOrderLine.setQty(billOfMaterialLine.getQty());
      saleOrderLine.setBillOfMaterialLine(billOfMaterialLine);
      saleOrderLineProductService.computeProductInformation(saleOrderLine, saleOrder);
      saleOrderLineComputeService.computeValues(saleOrder, saleOrderLine);

      return saleOrderLine;
    }
    return null;
  }

  @Override
  public boolean equals(BillOfMaterialLine billOfMaterialLine, SaleOrderLine saleOrderLine) {

    return billOfMaterialLine.getQty().equals(saleOrderLine.getQty())
        && billOfMaterialLine.getProduct().equals(saleOrderLine.getProduct())
        && billOfMaterialLine.getUnit().equals(saleOrderLine.getUnit())
        && Optional.ofNullable(saleOrderLine.getBillOfMaterial())
            .map(solBom -> solBom.equals(billOfMaterialLine.getBillOfMaterial()))
            .or(() -> Optional.of(billOfMaterialLine.getBillOfMaterial() == null))
            .get();
  }

  @Override
  public boolean isSyncWithBomLine(SaleOrderLine saleOrderLine) {
    Objects.requireNonNull(saleOrderLine);

    // No BOM => Not synchronize
    if (saleOrderLine.getBillOfMaterialLine() == null) {
      return false;
    }

    return this.equals(saleOrderLine.getBillOfMaterialLine(), saleOrderLine);
  }
}
