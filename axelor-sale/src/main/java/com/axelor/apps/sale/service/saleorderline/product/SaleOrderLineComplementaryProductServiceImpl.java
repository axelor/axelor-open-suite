package com.axelor.apps.sale.service.saleorderline.product;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.sale.db.ComplementaryProduct;
import com.axelor.apps.sale.db.ComplementaryProductSelected;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.ComplementaryProductRepository;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineComputeService;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.collections.CollectionUtils;

public class SaleOrderLineComplementaryProductServiceImpl
    implements SaleOrderLineComplementaryProductService {

  protected SaleOrderLineProductService saleOrderLineProductService;
  protected SaleOrderLineComputeService saleOrderLineComputeService;
  protected SaleOrderLineRepository saleOrderLineRepository;

  @Inject
  public SaleOrderLineComplementaryProductServiceImpl(
      SaleOrderLineProductService saleOrderLineProductService,
      SaleOrderLineComputeService saleOrderLineComputeService,
      SaleOrderLineRepository saleOrderLineRepository) {
    this.saleOrderLineProductService = saleOrderLineProductService;
    this.saleOrderLineComputeService = saleOrderLineComputeService;
    this.saleOrderLineRepository = saleOrderLineRepository;
  }

  @Override
  public List<SaleOrderLine> manageComplementaryProductSaleOrderLine(
      ComplementaryProduct complementaryProduct, SaleOrder saleOrder, SaleOrderLine saleOrderLine)
      throws AxelorException {

    List<SaleOrderLine> newComplementarySOLines = new ArrayList<>();
    if (saleOrderLine.getMainSaleOrderLine() != null) {
      return newComplementarySOLines;
    }

    if (saleOrderLine.getComplementarySaleOrderLineList() == null) {
      saleOrderLine.setComplementarySaleOrderLineList(new ArrayList<>());
    }

    SaleOrderLine complementarySOLine =
        getOrCreateComplementryLine(
            complementaryProduct.getProduct(), saleOrderLine, newComplementarySOLines);

    complementarySOLine.setQty(complementaryProduct.getQty());
    complementarySOLine.setIsComplementaryPartnerProductsHandled(
        complementaryProduct.getGenerationTypeSelect()
            == ComplementaryProductRepository.GENERATION_TYPE_SALE_ORDER);
    saleOrderLineProductService.computeProductInformation(complementarySOLine, saleOrder);
    saleOrderLineComputeService.computeValues(saleOrder, complementarySOLine);
    saleOrderLineRepository.save(complementarySOLine);
    return newComplementarySOLines;
  }

  protected SaleOrderLine getOrCreateComplementryLine(
      Product product, SaleOrderLine saleOrderLine, List<SaleOrderLine> newComplementarySOLines) {
    SaleOrderLine complementarySOLine;
    Optional<SaleOrderLine> complementarySOLineOpt =
        saleOrderLine.getComplementarySaleOrderLineList().stream()
            .filter(
                line -> line.getMainSaleOrderLine() != null && line.getProduct().equals(product))
            .findFirst();
    if (complementarySOLineOpt.isPresent()) {
      complementarySOLine = complementarySOLineOpt.get();
    } else {
      complementarySOLine = new SaleOrderLine();
      complementarySOLine.setSequence(saleOrderLine.getSequence());
      complementarySOLine.setProduct(product);
      complementarySOLine.setMainSaleOrderLine(saleOrderLine);
      newComplementarySOLines.add(complementarySOLine);
    }
    return complementarySOLine;
  }

  @Override
  public Map<String, Object> fillComplementaryProductList(SaleOrderLine saleOrderLine) {
    Map<String, Object> saleOrderLineMap = new HashMap<>();
    if (saleOrderLine.getProduct() != null
        && saleOrderLine.getProduct().getComplementaryProductList() != null) {
      if (saleOrderLine.getSelectedComplementaryProductList() == null) {
        saleOrderLine.setSelectedComplementaryProductList(new ArrayList<>());
      }
      saleOrderLine.clearSelectedComplementaryProductList();
      for (ComplementaryProduct complProduct :
          saleOrderLine.getProduct().getComplementaryProductList()) {
        ComplementaryProductSelected newComplProductLine = new ComplementaryProductSelected();

        newComplProductLine.setProduct(complProduct.getProduct());
        newComplProductLine.setQty(complProduct.getQty());
        newComplProductLine.setOptional(complProduct.getOptional());

        newComplProductLine.setIsSelected(!complProduct.getOptional());
        newComplProductLine.setSaleOrderLine(saleOrderLine);
        saleOrderLine.addSelectedComplementaryProductListItem(newComplProductLine);
      }
      saleOrderLineMap.put(
          "selectedComplementaryProductList", saleOrderLine.getSelectedComplementaryProductList());
    }
    return saleOrderLineMap;
  }

  @Override
  public Map<String, Object> setIsComplementaryProductsUnhandledYet(SaleOrderLine saleOrderLine) {
    Product product = saleOrderLine.getProduct();

    saleOrderLine.setIsComplementaryProductsUnhandledYet(
        product != null && CollectionUtils.isNotEmpty(product.getComplementaryProductList()));
    Map<String, Object> saleOrderLineMap = new HashMap<>();
    saleOrderLineMap.put(
        "isComplementaryProductsUnhandledYet",
        saleOrderLine.getIsComplementaryProductsUnhandledYet());

    return saleOrderLineMap;
  }
}
